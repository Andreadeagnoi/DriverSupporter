package tesideagnoi.dei.unipd.it.driversupporter;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
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

    public DebugSettingsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        mSpeedThresholdValue = (EditText)rootView.findViewById(R.id.speedThreshold);
        mAccThresholdValue = (EditText)rootView.findViewById(R.id.accThreshold);
        mCurveAccThresholdValue = (EditText)rootView.findViewById(R.id.curveAccThreshold);
        mLeapAccThresholdValue = (EditText)rootView.findViewById(R.id.leapAccThreshold);
        mLeapSpeedThresholdValue = (EditText)rootView.findViewById(R.id.leapSpeedThreshold);

        return rootView;
    }
}
