package tesideagnoi.dei.unipd.it.driversupporter;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import tesideagnoi.dei.unipd.it.driversupporter.services.DataCollector;


/**
 * A placeholder fragment containing a simple view.
 */
public class EvaluationFragment extends Fragment implements Observer{

    // Activity State
    private int mScore;
    private int mGoodAccelerationPositive  = 0;
    private int mGoodAccelerationNegative  = 0;
    private int mGoodCurveAccelerationPositive  = 0;
    private int mGoodCurveAccelerationNegative  = 0;
    private int mGoodLeapAccelerationPositive  = 0;
    private int mGoodLeapAccelerationNegative  = 0;
    // Fragment Widget
    private ProgressBar mProgressBar;
    private RatingBar mRatingBar;
    private TextView mAccelerationPositiveEvaluationValue;
    private TextView mAccelerationNegativeEvaluationValue;
    private TextView mDecelerationPositiveEvaluationValue;
    private TextView mDecelerationNegativeEvaluationValue;
    private TextView mCurveAccelerationPositiveEvaluationValue;
    private TextView mCurveAccelerationNegativeEvaluationValue;
    private TextView mLeapAccelerationPositiveEvaluationValue;
    private TextView mLeapAccelerationNegativeEvaluationValue;
    private Switch mSwitchService;

    public EvaluationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_evaluation, container, false);

        mAccelerationPositiveEvaluationValue = (TextView) rootView.findViewById(R.id.accelerationPositiveEvaluation);
        mAccelerationNegativeEvaluationValue = (TextView) rootView.findViewById(R.id.accelerationNegativeEvaluation);
        mDecelerationPositiveEvaluationValue = (TextView) rootView.findViewById(R.id.decelerationPositiveEvaluation);
        mDecelerationNegativeEvaluationValue = (TextView) rootView.findViewById(R.id.decelerationNegativeEvaluation);
        mCurveAccelerationPositiveEvaluationValue = (TextView) rootView.findViewById(R.id.curveAccelerationPositiveEvaluation);
        mCurveAccelerationNegativeEvaluationValue = (TextView) rootView.findViewById(R.id.curveAccelerationNegativeEvaluation);
        mLeapAccelerationPositiveEvaluationValue = (TextView) rootView.findViewById(R.id.leapAccelerationPositiveEvaluation);
        mLeapAccelerationNegativeEvaluationValue = (TextView) rootView.findViewById(R.id.leapAccelerationNegativeEvaluation);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBarEvaluation);
        mRatingBar = (RatingBar) rootView.findViewById(R.id.ratingBarEvaluation);
        mSwitchService = (Switch) rootView.findViewById(R.id.switchService);
        mAccelerationPositiveEvaluationValue.setText(mGoodAccelerationPositive+"");
        mAccelerationNegativeEvaluationValue.setText(mGoodAccelerationNegative+"");
        mCurveAccelerationPositiveEvaluationValue.setText(mGoodCurveAccelerationPositive+"");
        mCurveAccelerationNegativeEvaluationValue.setText(mGoodCurveAccelerationNegative+"");
        mLeapAccelerationPositiveEvaluationValue.setText(mGoodLeapAccelerationPositive + "");
        mLeapAccelerationNegativeEvaluationValue.setText(mGoodLeapAccelerationNegative + "");
        mProgressBar.setMax(100);
        final Observer observer = this;
        mSwitchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                //TODO: orrendo che chiama così tante volte metodi dell'activity
                if (isChecked) {
                    ((InfoViewerActivity) getActivity()).mService.play().addObserver(observer);
                } else {
                    ((InfoViewerActivity)getActivity()).mService.stop().deleteObservers();
                }

            }
        });
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

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


    @Override
    public void update(Observable observable, Object data) {
        Bundle bundledData = (Bundle) data;
        mAccelerationPositiveEvaluationValue.setText(bundledData.getInt("GoodAcceleration")+"");
        mAccelerationNegativeEvaluationValue.setText(bundledData.getInt("BadAcceleration")+"");
        mDecelerationPositiveEvaluationValue.setText(bundledData.getInt("GoodDeceleration")+"");
        mDecelerationNegativeEvaluationValue.setText(bundledData.getInt("BadDeceleration")+"");
        mCurveAccelerationPositiveEvaluationValue.setText(bundledData.getInt("GoodCurveAcceleration")+"");
        mCurveAccelerationNegativeEvaluationValue.setText(bundledData.getInt("BadCurveAcceleration")+"");
        mLeapAccelerationPositiveEvaluationValue.setText(bundledData.getInt("GoodLeapAcceleration")+"");
        mLeapAccelerationNegativeEvaluationValue.setText(bundledData.getInt("BadLeapAcceleration")+"");
        mProgressBar.setProgress(bundledData.getInt("TotalScore") % 100);
        mRatingBar.setRating(bundledData.getInt("TotalScore")  / 100);
    }

}
