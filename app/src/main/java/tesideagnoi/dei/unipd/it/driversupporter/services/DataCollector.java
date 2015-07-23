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
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import tesideagnoi.dei.unipd.it.driversupporter.AccelerometerData;
import tesideagnoi.dei.unipd.it.driversupporter.EvaluationUnit;
import tesideagnoi.dei.unipd.it.driversupporter.NoGraphsActivity;
import tesideagnoi.dei.unipd.it.driversupporter.TestUtilities;
//TODO: meglio NON filtrati
public class DataCollector extends Service implements SensorEventListener {

    private SensorManager mSm;
    private Sensor mAccelerometer;
    private ArrayList<AccelerometerData> mSamples;
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
    private EvaluationUnit mEU;
    private boolean mPlaying;


    public DataCollector() {
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class DataCollectorAccFilteredBinder extends Binder {
        public DataCollector getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataCollector.this;
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
        sampleRate = 200000000; // registro 5 variazioni al secondo
        mDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        mPlaying = false;
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

    public EvaluationUnit play() {
        if(!mPlaying) {
            mPlaying = true;
            mSm.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            runAsForeground();
            mSamples = new ArrayList<AccelerometerData>();
            mEU = new EvaluationUnit(mSamples);
            setDataCollectTimer();
            return mEU;
        }
        return mEU;
    }

    public EvaluationUnit stop() {
        mPlaying = false;
        mSm.unregisterListener(this);
        locationManager.removeUpdates(locationListener);
        sTimer.cancel();
        // Scrive su file i dati
        TestUtilities.writeToExternalStorage(mSamples);
        stopForeground(true);
        return mEU;

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

        final Notification persistentNotification = new NotificationCompat.Builder(
                this)
                .setContentTitle("Assistant-san")
                .setContentText(
                        "Registrando i dati dell'accelerometro.")
                .setContentIntent(pendingIntent)
                .build();
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
            // Registro l'accelerazione e i dati del gps
            mMeasuredData = new AccelerometerData(timestamp,
                    mSensorX,
                    mSensorY,
                    event.values[2]);
//            mFilteredData = lowPass(mMeasuredData, mFilteredData, mLastIndex);
            mMeasuredData.setLat(mLatitude);
            mMeasuredData.setLon(mLongitude);
            mMeasuredData.setSpeed(mSpeed);
            // Memorizzo il dato sulla coda
            mSamples.add(mMeasuredData);
            mEU.driverEvaluation(mLastIndex + 1);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
