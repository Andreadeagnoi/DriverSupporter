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
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;

import tesideagnoi.dei.unipd.it.driversupporter.services.DataCollector;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsMonitorFragment extends Fragment {

    private TextView mSpeedText;
    private TextView mAccText;
    private TextView mLatText;
    private TextView mLonText;
    private TextView mAltText;
    private TextView mXAccText;
    private TextView mYAccText;
    private TextView mZAccText;
    private TextView mXGText;
    private TextView mYGText;
    private TextView mZGText;
    private Switch mSwitchService;
    // Variabile per il punteggio di feedback
    private int mScore;
    private RatingBar ratingBar;

    public DetailsMonitorFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details_monitor, container, false);
        mSpeedText =  (TextView) rootView.findViewById(R.id.speedValue);
        mAccText =  (TextView) rootView.findViewById(R.id.accValue);
        mLatText =  (TextView) rootView.findViewById(R.id.latValue);
        mLonText =  (TextView) rootView.findViewById(R.id.lonValue);
        mAltText =  (TextView) rootView.findViewById(R.id.altValue);
        mXAccText =  (TextView) rootView.findViewById(R.id.xAccValue);
        mYAccText =  (TextView) rootView.findViewById(R.id.yAccValue);
        mZAccText =  (TextView) rootView.findViewById(R.id.zAccValue);
        mXGText =  (TextView) rootView.findViewById(R.id.xG);
        mYGText =  (TextView) rootView.findViewById(R.id.yG);
        mZGText =  (TextView) rootView.findViewById(R.id.zG);
        mSwitchService = (Switch) rootView.findViewById(R.id.controlService);
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        mSwitchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                //TODO: orrendo che chiama così tante volte metodi dell'activity
                if (isChecked) {
                    if( ((NoGraphsActivity)getActivity()).mBound) {
                        ((NoGraphsActivity)getActivity()).mService.play();
                    }
                    else {

                        ((NoGraphsActivity)getActivity()).mService.play();
                    }
                } else {
                    ((NoGraphsActivity)getActivity()).mService.stop();
                }

            }
        });
        return rootView;
    }

    /**
     * Handler per gli intent ricevuti dall'evento "AccData"
     */
    private BroadcastReceiver mAccelerationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Controlla per evitare scritture al layout dopo che è stato
            // terminato il service
            if (isMyServiceRunning(DataCollector.class, context)) {
                double x = intent.getFloatExtra("xAcc", 0f);
                double y = intent.getFloatExtra("yAcc", 0f);
                double z = intent.getFloatExtra("zAcc", 0f);
                double acc = intent.getDoubleExtra("Acc", 0);
                mXAccText.setText(String.valueOf(x));
                mYAccText.setText(String.valueOf(y));
                mZAccText.setText(String.valueOf(z));
                mAccText.setText(String.valueOf(acc));
            }
        }
    };

    /**
     * Handler per gli intent ricevuti dall'evento "Evaluation"
     */
    private BroadcastReceiver mEvaluationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Controlla per evitare scritture al layout dopo che è stato
            // terminato il service
            if (isMyServiceRunning(DataCollector.class, context)) {
                boolean evaluation = intent.getBooleanExtra("Good_Acceleration", true);
                if (evaluation) {
                    mScore ++;
                }
                else {
                    mScore -= 1000;
                }
                evaluation = intent.getBooleanExtra("Good_Curve_Acceleration", true);
                if (evaluation) {
                    mScore ++;
                }
                else {
                    mScore -= 1000;
                }
                evaluation = intent.getBooleanExtra("Good_Leap_Acceleration", true);
                if (evaluation) {
                    mScore ++;
                }
                else {
                    mScore -= 1000;
                }
                if (mScore < 0){
                    mScore = 0;
                }
                if (mScore > 5000){
                    mScore = 5000;
                }
                ratingBar.setRating(mScore/1000);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this.getActivity())
                .registerReceiver(mAccelerationReceiver,
                        new IntentFilter("AccData"));
        LocalBroadcastManager.getInstance(this.getActivity())
                .registerReceiver(mEvaluationReceiver,
                        new IntentFilter("Evaluation"));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this.getActivity())
                .unregisterReceiver(mAccelerationReceiver);
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
