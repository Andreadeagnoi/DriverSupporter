package tesideagnoi.dei.unipd.it.driversupporter;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import tesideagnoi.dei.unipd.it.driversupporter.services.DataCollector;


public class InfoViewerActivity extends ActionBarActivity
                                    implements OnMapReadyCallback {


    private static GoogleMap mMap;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_viewer);

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = mapFragment.getMap();
        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mAccelerationReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isMyServiceRunning(DataCollector.class, this)) {
            Intent intent = new Intent(this, DataCollector.class);
            // Bind to WatcherService
            startService(intent);
            bindService(intent, mConnection, 0);
        }
        else {
            if(!mBound) {
                Intent intent = new Intent(getApplicationContext(), DataCollector.class);
                // Bind to WatcherService
                bindService(intent, mConnection, 0);
            }
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mAccelerationReceiver,
                        new IntentFilter("AccData"));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_debug_settings){
            Intent showDebugSettings = new Intent(this, DebugSettingsActivity.class);
            startActivity(showDebugSettings);
        }
        return super.onOptionsItemSelected(item);
    }

    protected static boolean mBound;
    protected static DataCollector mService;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    protected static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DataCollector.DataCollectorAccFilteredBinder binder = (DataCollector.DataCollectorAccFilteredBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            mBound = false;
        }
    };

    /**
     * Handler per gli intent ricevuti dall'evento "AccData"
     */
    private BroadcastReceiver mAccelerationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Controlla per evitare scritture al layout dopo che è stato
            // terminato il service
            if (isMyServiceRunning(DataCollector.class, context)) {
                double lat = intent.getFloatExtra("Lat", 0f);
                double lon = intent.getFloatExtra("Lon", 0f);
                LatLng latLon = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions()
                        .position(latLon)
                        .title("Marker"))
                        .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_point));

                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLon).zoom(14.0f).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.moveCamera(cameraUpdate);
            }
        }
    };

    /**
     * Controlla se il service è già stato avviato.
     *
     * @param serviceClass
     *            il nome della classe contenente il service
     * @return vero se è stato avviato, falso altrimenti
     */
    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
