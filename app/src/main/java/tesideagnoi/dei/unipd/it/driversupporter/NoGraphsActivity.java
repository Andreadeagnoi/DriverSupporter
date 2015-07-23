package tesideagnoi.dei.unipd.it.driversupporter;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import tesideagnoi.dei.unipd.it.driversupporter.services.DataCollectorAccFiltered;


public class NoGraphsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_graphs);

    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DataCollectorAccFiltered.class);
        PendingIntent.getBroadcast(this.getBaseContext(),
                PendingIntent.FLAG_UPDATE_CURRENT, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Bind to WatcherService
        startService(intent);
        bindService(intent, mConnection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, DataCollectorAccFiltered.class);
        // Bind to WatcherService
        bindService(intent, mConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_no_graphs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_play) {
            if(mBound) {
                mService.play();
            }
            else {
                Intent intent = new Intent(this, DataCollectorAccFiltered.class);
                PendingIntent.getBroadcast(this.getBaseContext(),
                        PendingIntent.FLAG_UPDATE_CURRENT, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                // Bind to WatcherService
                startService(intent);
                bindService(intent, mConnection, 0);
                mService.play();
            }
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_stop) {
            if(mBound) {
                mService.stop();
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    protected static boolean mBound;
    protected static DataCollectorAccFiltered mService;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    protected static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DataCollectorAccFiltered.DataCollectorAccFilteredBinder binder = (DataCollectorAccFiltered.DataCollectorAccFilteredBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}
