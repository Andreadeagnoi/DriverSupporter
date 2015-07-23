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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import tesideagnoi.dei.unipd.it.driversupporter.AccelerometerData;
import tesideagnoi.dei.unipd.it.driversupporter.NoGraphsActivity;

public class DataCollectorTest extends Service implements SensorEventListener{
    private SensorManager mSm;
    private Sensor mAccelerometer;
    private LinkedList<AccelerometerData> mSamples;
    private float mLatitude;
    private float mLongitude;
    private static Timer sTimer;
    private Context mContext;
    private AccelerometerData mMeasuredData;
    private long sampleRate;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private float mSpeed;

    public DataCollectorTest() {
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class DataCollectorBinder extends Binder  {
        public DataCollectorTest getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataCollectorTest.this;
        }
    }

    // Binder given to clients
    private final IBinder mBinder = new DataCollectorBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // Imposta i listener dei sensori
        mSm = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSamples = new LinkedList<AccelerometerData>();
        mMeasuredData = new AccelerometerData(0,0,0,0);
        mContext = this;
        sampleRate = 200000000;
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {


            public void onLocationChanged(Location location) {
                // Called when a new location is found by the gps.
                mLatitude = (float) location.getLatitude();
                mLongitude = (float) location.getLongitude();
                mSpeed = location.getSpeed();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

    }

    public void play() {
        mSm.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        runAsForeground();
        setDataCollectTimer();
    }

    public void stop() {
        sTimer.cancel();

        // Scrive su file i dati
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();

        File file = new File(dir, System.currentTimeMillis()+".txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            for(int i = 0; i<mSamples.size();i++) {
                pw.println(mSamples.get(i).getTimestamp()+";"+
                        mSamples.get(i).getX()+";"+
                        mSamples.get(i).getY()+";"+
                        mSamples.get(i).getZ()+";"+
                        mSamples.get(i).getVectorLength()+";"+
                        mSamples.get(i).getLat()+";"+
                        mSamples.get(i).getLon()+";"+
                        mSamples.get(i).getSpeed());
            }
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        stopForeground(true);
        stopSelf();

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Imposta una notifica persistente per evitare chiusure non intenzionali
     * del service.
     */
    private void runAsForeground() {

        Intent notificationIntent = new Intent(this,
                NoGraphsActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                        mIntent);
            }
        }, 0, 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Long timestamp = event.timestamp;
            //controllo che sia rispettato il sample rate impostato
            if (mSamples.size() > 0) {
                if (timestamp - mSamples.getLast().getTimestamp() < sampleRate) {
                    return;
                }
            }
            //gestisco la memorizzazione dei dati su coda
            mMeasuredData = new AccelerometerData(timestamp, event.values[0], event.values[1], event.values[2]);
            mMeasuredData.setLat(mLatitude);
            mMeasuredData.setLon(mLongitude);
            mMeasuredData.setSpeed(mSpeed);
            mSamples.add(mMeasuredData);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
