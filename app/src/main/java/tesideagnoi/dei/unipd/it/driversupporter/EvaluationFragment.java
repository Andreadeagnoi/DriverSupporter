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

import tesideagnoi.dei.unipd.it.driversupporter.services.DataCollectorAccFiltered;


/**
 * A placeholder fragment containing a simple view.
 */
public class EvaluationFragment extends Fragment {

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
        mLeapAccelerationPositiveEvaluationValue.setText(mGoodLeapAccelerationPositive+"");
        mLeapAccelerationNegativeEvaluationValue.setText(mGoodLeapAccelerationNegative+"");
        mProgressBar.setMax(1000);
        mSwitchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                //TODO: orrendo che chiama così tante volte metodi dell'activity
                if (isChecked) {
                    if (((InfoViewerActivity) getActivity()).mBound) {
                        ((InfoViewerActivity) getActivity()).mService.play();
                    } else {

                        ((InfoViewerActivity) getActivity()).mService.play();
                    }
                } else {
                    ((InfoViewerActivity)getActivity()).mService.stop();
                }

            }
        });
        return rootView;
    }

    /**
     * Handler per gli intent ricevuti dall'evento "Evaluation"
     */
    private BroadcastReceiver mEvaluationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Controlla per evitare scritture al layout dopo che è stato
            // terminato il service
            if (isMyServiceRunning(DataCollectorAccFiltered.class, context)) {
                int evaluation = intent.getIntExtra("Good_Acceleration", 0);
                if (evaluation > 0) {
                    mScore ++;
                    mGoodAccelerationPositive++;
                }
                else if (evaluation < 0){

                    mGoodAccelerationNegative++;
                    mScore -= 1000;
                }
                evaluation = intent.getIntExtra("Good_Curve_Acceleration", 0);
                if (evaluation > 0) {
                    mScore ++;
                    mGoodCurveAccelerationPositive++;
                }
                else if (evaluation < 0){
                    mGoodCurveAccelerationNegative++;
                    mScore -= 1000;
                }
                evaluation = intent.getIntExtra("Good_Leap_Acceleration", 0);
                if (evaluation > 0) {
                    mScore ++;
                    mGoodLeapAccelerationPositive++;
                }
                else if (evaluation < 0){
                    mGoodLeapAccelerationNegative++;
                    mScore -= 1000;
                }
                if (mScore < 0){
                    mScore = 0;
                }
                if (mScore > 5000){
                    mScore = 5000;
                }
                mAccelerationPositiveEvaluationValue.setText(mGoodAccelerationPositive+"");
                mAccelerationNegativeEvaluationValue.setText(mGoodAccelerationNegative+"");
                mCurveAccelerationPositiveEvaluationValue.setText(mGoodCurveAccelerationPositive+"");
                mCurveAccelerationNegativeEvaluationValue.setText(mGoodCurveAccelerationNegative+"");
                mLeapAccelerationPositiveEvaluationValue.setText(mGoodLeapAccelerationPositive+"");
                mLeapAccelerationNegativeEvaluationValue.setText(mGoodLeapAccelerationNegative+"");
                mProgressBar.setProgress(mScore % 1000);
                mRatingBar.setRating(mScore / 1000);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this.getActivity())
                .registerReceiver(mEvaluationReceiver,
                        new IntentFilter("Evaluation"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.getActivity())
                .unregisterReceiver(mEvaluationReceiver);

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


}
