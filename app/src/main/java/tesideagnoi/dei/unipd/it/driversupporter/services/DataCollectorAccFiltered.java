package tesideagnoi.dei.unipd.it.driversupporter.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tesideagnoi.dei.unipd.it.driversupporter.AccelerometerData;
import tesideagnoi.dei.unipd.it.driversupporter.NoGraphsActivity;
import tesideagnoi.dei.unipd.it.driversupporter.TestUtilities;
//TODO: meglio NON filtrati
public class DataCollectorAccFiltered extends Service implements SensorEventListener {
    // Ramp-speed usata dal filtro
    static float ALPHA = 0.25f;
    static float SPEED_THRESHOLD = 5f;  // circa 20 km/h
    static float ACC_THRESHOLD = 1.38f; // 5 km/h^2
    static float CURVE_ACC_THRESHOLD = 2f;
    static float JUMP_LEAP_THRESHOLD = 2f;
    static float SPEED_LEAP_THRESHOLD = 8f;

    private SensorManager mSm;
    private Sensor mAccelerometer;
    private ArrayList<AccelerometerData> mSamples, mSamplesNF;
    private float mLatitude;
    private float mLongitude;
    private static Timer sTimer;
    private Context mContext;
    private AccelerometerData mMeasuredData;
    private long sampleRate;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private float mSpeed;
    private AccelerometerData mFilteredData;
    private int mLastIndex;
    private Display mDisplay;
    private float mSensorX;
    private float mSensorY;
    private long lastLeapTimestamp;


    public DataCollectorAccFiltered() {
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class DataCollectorAccFilteredBinder extends Binder {
        public DataCollectorAccFiltered getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataCollectorAccFiltered.this;
        }
    }

    // Binder given to clients
    private final IBinder mBinder = new DataCollectorAccFilteredBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // Imposta i listener dei sensori
        mSm = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mMeasuredData = new AccelerometerData(0, 0, 0, 0);
        mContext = this;

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                    // Called when a new location is found by the gps.
                    mLatitude = (float) location.getLatitude();
                    mLongitude = (float) location.getLongitude();
                    mSpeed = location.getSpeed();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

    }

    public void play() {
        mSm.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sampleRate = 200000000; // registro 5 variazioni al secondo
        mDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        runAsForeground();
        mSamples = new ArrayList<AccelerometerData>();
        // Dati raw non filtrati
        mSamplesNF = new ArrayList<AccelerometerData>();
        lastLeapTimestamp = 0;
        setDataCollectTimer();
    }

    public void stop() {
        mSm.unregisterListener(this);
        locationManager.removeUpdates(locationListener);
        sTimer.cancel();
        // Scrive su file i dati
        TestUtilities.writeToExternalStorage(mSamples, mSamplesNF);
        stopForeground(true);
        stopSelf();

    }


    /**
     * Imposta una notifica persistente per evitare chiusure non intenzionali
     * del service.
     */
    private void runAsForeground() {

        Intent notificationIntent = new Intent(this,
                NoGraphsActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification persistentNotification = new NotificationCompat.Builder(
                this)
                .setContentTitle("Assistant-san")
                .setContentText(
                        "Registrando i dati dell'accelerometro.")
                .setContentIntent(pendingIntent).build();

        startForeground(1,
                persistentNotification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    public void setDataCollectTimer() {
        sTimer = new Timer();
        sTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Comunico i dati alla activity chiamante
                Intent mIntent = new Intent("AccData");
                mIntent.putExtra("xAcc", mMeasuredData.getX());
                mIntent.putExtra("yAcc", mMeasuredData.getY());
                mIntent.putExtra("zAcc", mMeasuredData.getZ());
                mIntent.putExtra("Acc", mMeasuredData.getVectorLength());
                mIntent.putExtra("Lat", mFilteredData.getLat());
                mIntent.putExtra("Lon", mFilteredData.getLon());
                mIntent.putExtra("Speed", mFilteredData.getSpeed());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                        mIntent);
            }
        }, 0, 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            Long timestamp = event.timestamp;
            mLastIndex = mSamples.size() - 1;
            if (mLastIndex >= 0) {
                //controllo che sia rispettato il sample rate impostato
                if (timestamp - mSamples.get(mLastIndex).getTimestamp() < sampleRate) {
                    return;
                }
            }
            mFilteredData = new AccelerometerData(timestamp, 0, 0, 0);
            // Controlla l'orientazione dello schermo prima di dare dei valori agli assi
            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_0:
                    mSensorX = event.values[0];
                    mSensorY = event.values[1];
                    break;
                case Surface.ROTATION_90:
                    mSensorX = -event.values[1];
                    mSensorY = event.values[0];
                    break;
                case Surface.ROTATION_180:
                    mSensorX = -event.values[0];
                    mSensorY = -event.values[1];
                    break;
                case Surface.ROTATION_270:
                    mSensorX = event.values[1];
                    mSensorY = -event.values[0];
                    break;
            }
            // Utilizza un semplice FIR per filtrare i dati dalla gravità
            // TODO: forse è inutile, anzi dannoso l'applicazione di un passa basso poichè elimina picchi attribuibili a frenate brusche.
            mMeasuredData = new AccelerometerData(timestamp,
                    mSensorX,
                    mSensorY,
                    event.values[2]);
            mFilteredData = lowPass(mMeasuredData, mFilteredData, mLastIndex);
            mFilteredData.setLat(mLatitude);
            mFilteredData.setLon(mLongitude);
            mFilteredData.setSpeed(mSpeed);
            // Memorizzo il dato sulla coda
            mSamples.add(mFilteredData);
            mSamplesNF.add(mMeasuredData);
            driverEvaluation(mLastIndex + 1);
        }
    }

    /**
     * Simple low pass filter to filter noise in accelerometer data
     *
     * @param input     the raw data
     * @param output    the filtered data
     * @param lastIndex index of the last element in the accelerometer data list
     * @return the filtered data
     */
    protected AccelerometerData lowPass(AccelerometerData input, AccelerometerData output, int lastIndex) {
        if (mSamples.size() == 0) {
            return input;
        }
        output.setX(mSamples.get(lastIndex).getX() + ALPHA * (input.getX() - mSamples.get(lastIndex).getX()));
        output.setY(mSamples.get(lastIndex).getY() + ALPHA * (input.getY() - mSamples.get(lastIndex).getY()));
        output.setZ(mSamples.get(lastIndex).getZ() + ALPHA * (input.getZ() - mSamples.get(lastIndex).getZ()));
        return output;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Metodo temporaneo che da un feedback sulla guida.
     * 1) Valutazione sull'accelerazione, semplicemente confrontando la variazione di accelerazione nell'asse parallelo al moto (Z) tra due rilevazioni.
     * Confronta anche con la velocità attuale se il dato è disponibile.
     * 2) Valutazione sull'accelerazione in curva: se l'accelerazione sull'asse Y (o X se il telefono è in verticale)
     * è maggiore di una certa soglia allora il modulo dell'accelerazione su Z e Y viene valutato.
     * 3) Valutazione sui dossi o buche: se rileva un picco di accelerazione in altezza ripetuto due volte in sequenza (attribuibilie ad un dosso preso in velocità)
     */
    public void driverEvaluation(int lastIndex) {
        Intent mIntent = new Intent("Evaluation");
        // 1^ valutazione: accelerazione/decelerazione progressiva
        if (mSamples.get(lastIndex).getSpeed() > SPEED_THRESHOLD) {
            if (Math.abs(mSamplesNF.get(lastIndex).getZ() - mSamplesNF.get(lastIndex - 1).getZ()) < ACC_THRESHOLD) {
                mIntent.putExtra("Good_Acceleration", 1);
            } else {
                mIntent.putExtra("Good_Acceleration", -1);
            }
            // 2^ valutazione: accelerazione in curva
            if (Math.abs(mSamplesNF.get(lastIndex).getX()) > CURVE_ACC_THRESHOLD) {
                if (Math.abs(Math.pow(Math.pow(mSamplesNF.get(lastIndex).getZ(), 2) + Math.pow(mSamplesNF.get(lastIndex).getX(), 2), 0.5)
                        - Math.pow(Math.pow(mSamplesNF.get(lastIndex - 1).getZ(), 2) + Math.pow(mSamplesNF.get(lastIndex - 1).getX(), 2), 0.5)) < ACC_THRESHOLD) {
                    mIntent.putExtra("Good_Curve_Acceleration", 1);
                } else {
                    mIntent.putExtra("Good_Curve_Acceleration", -1);
                }
            }
            //TODO: trovare l'accelerazione limite per dare una valutazione positiva sul dosso
            if (Math.abs(mSamples.get(lastIndex).getY()) > JUMP_LEAP_THRESHOLD){
                if(mSamplesNF.get(lastIndex).getTimestamp()-lastLeapTimestamp<1000000000){
                    if(mSamples.get(lastIndex).getSpeed() < SPEED_LEAP_THRESHOLD) {
                        mIntent.putExtra("Good_Leap_Acceleration", 1);
                    }
                    else {
                        mIntent.putExtra("Good_Leap_Acceleration", -1);
                    }
                }
                lastLeapTimestamp = mSamplesNF.get(lastIndex).getTimestamp();
            }
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                mIntent);
    }
}
