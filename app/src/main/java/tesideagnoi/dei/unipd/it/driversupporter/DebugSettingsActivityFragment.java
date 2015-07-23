package tesideagnoi.dei.unipd.it.driversupporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * A placeholder fragment containing a simple view.
 */
public class DebugSettingsActivityFragment extends Fragment {

    private EditText mSpeedThresholdValue;
    private EditText mAccThresholdValue;
    private EditText mCurveAccThresholdValue;
    private EditText mLeapAccThresholdValue;
    private EditText mLeapSpeedThresholdValue;
    private SharedPreferences sharedPref;

    public DebugSettingsActivityFragment() {
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_reset){
            SharedPreferences sharedPref = getActivity().getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat("speedThreshold",EvaluationUnit.SPEED_THRESHOLD);
            editor.putFloat("accThreshold",EvaluationUnit.ACC_THRESHOLD);
            editor.putFloat("curveAccThreshold",EvaluationUnit.CURVE_ACC_THRESHOLD);
            editor.putFloat("leapAccThreshold",EvaluationUnit.JUMP_LEAP_THRESHOLD);
            editor.putFloat("leapSpeedThreshold",EvaluationUnit.SPEED_LEAP_THRESHOLD);
            editor.commit();
            mSpeedThresholdValue.setText(sharedPref.getFloat("speedThreshold", EvaluationUnit.SPEED_THRESHOLD) + "");
            mAccThresholdValue.setText(sharedPref.getFloat("accThreshold", EvaluationUnit.ACC_THRESHOLD) + "");
            mCurveAccThresholdValue.setText(sharedPref.getFloat("curveAccThreshold", EvaluationUnit.CURVE_ACC_THRESHOLD) + "");
            mLeapAccThresholdValue.setText(sharedPref.getFloat("leapAccThreshold", EvaluationUnit.JUMP_LEAP_THRESHOLD) + "");
            mLeapSpeedThresholdValue.setText(sharedPref.getFloat("leapSpeedThreshold", EvaluationUnit.SPEED_LEAP_THRESHOLD) + "");
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        mSpeedThresholdValue = (EditText)rootView.findViewById(R.id.speedThreshold);
        mAccThresholdValue = (EditText)rootView.findViewById(R.id.accThreshold);
        mCurveAccThresholdValue = (EditText)rootView.findViewById(R.id.curveAccThreshold);
        mLeapAccThresholdValue = (EditText)rootView.findViewById(R.id.leapAccThreshold);
        mLeapSpeedThresholdValue = (EditText)rootView.findViewById(R.id.leapSpeedThreshold);
        Context context = getActivity();
        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mSpeedThresholdValue.setText(sharedPref.getFloat("speedThreshold", EvaluationUnit.SPEED_THRESHOLD)+"");
        mAccThresholdValue.setText(sharedPref.getFloat("accThreshold", EvaluationUnit.ACC_THRESHOLD)+"");
        mCurveAccThresholdValue.setText(sharedPref.getFloat("curveAccThreshold", EvaluationUnit.CURVE_ACC_THRESHOLD)+"");
        mLeapAccThresholdValue.setText(sharedPref.getFloat("leapAccThreshold",EvaluationUnit.JUMP_LEAP_THRESHOLD)+"");
        mLeapSpeedThresholdValue.setText(sharedPref.getFloat("leapSpeedThreshold", EvaluationUnit.SPEED_LEAP_THRESHOLD) + "");
        return rootView;
    }

    @Override
    public void onPause() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("speedThreshold",Float.valueOf(String.valueOf(mSpeedThresholdValue.getText())));
        editor.putFloat("accThreshold",Float.valueOf(String.valueOf(mAccThresholdValue.getText())));
        editor.putFloat("curveAccThreshold",Float.valueOf(String.valueOf(mCurveAccThresholdValue.getText())));
        editor.putFloat("leapAccThreshold",Float.valueOf(String.valueOf(mLeapAccThresholdValue.getText())));
        editor.putFloat("leapSpeedThreshold",Float.valueOf(String.valueOf(mLeapSpeedThresholdValue.getText())));
        editor.commit();
        super.onPause();
    }
}
