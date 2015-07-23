package tesideagnoi.dei.unipd.it.driversupporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class DebugSettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_reset){
            SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat("speedThreshold",EvaluationUnit.SPEED_THRESHOLD);
            editor.putFloat("accThreshold",EvaluationUnit.ACC_THRESHOLD);
            editor.putFloat("curveAccThreshold",EvaluationUnit.CURVE_ACC_THRESHOLD);
            editor.putFloat("leapAccThreshold",EvaluationUnit.JUMP_LEAP_THRESHOLD);
            editor.putFloat("leapSpeedThreshold",EvaluationUnit.SPEED_LEAP_THRESHOLD);
            editor.commit();
        }

        return super.onOptionsItemSelected(item);
    }*/
}
