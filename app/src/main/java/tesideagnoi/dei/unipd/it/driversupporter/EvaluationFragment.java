package tesideagnoi.dei.unipd.it.driversupporter;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private int mAcceleration  = 0;
    private int mCurveAcceleration  = 0;
    private int mLeapAcceleration = 0;
    private int mDeceleration = 0;
    // Fragment Widget
    private RatingBar mRatingBar;
    private Button mStopButton;
    private ProgressBar mAccelerationBar;
    private ProgressBar mDecelerationBar;
    private ProgressBar mCurveBar;
    private ProgressBar mLeapBar;

    public EvaluationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_evaluation, container, false);

        mRatingBar = (RatingBar) rootView.findViewById(R.id.ratingBarEvaluation);
        mStopButton = (Button) rootView.findViewById(R.id.stopButton);
        mAccelerationBar = (ProgressBar) rootView.findViewById(R.id.accelerationBar);
        mAccelerationBar.setMax(150);
        mAccelerationBar.setProgress(50);
        mDecelerationBar = (ProgressBar) rootView.findViewById(R.id.decelerationBar);
        mDecelerationBar.setMax(150);
        mDecelerationBar.setProgress(50);
        mCurveBar = (ProgressBar) rootView.findViewById(R.id.curveBar);
        mCurveBar.setMax(150);
        mCurveBar.setProgress(50);
        mLeapBar = (ProgressBar) rootView.findViewById(R.id.leapBar);
        mLeapBar.setMax(150);
        mLeapBar.setProgress(50);

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InfoViewerActivity) getActivity()).mService.stop().deleteObservers();
            }
        });
        /*Thread waitForBoundService = new Thread() {
            public void run() {
                while(!((InfoViewerActivity) getActivity()).mBound){

                }

            }

        };
        waitForBoundService.start();*/
        return rootView;
    }

    public Context myContext(){
        return this.getActivity();
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
        mAcceleration = bundledData.getInt("accEvaluation");
        mDeceleration = bundledData.getInt("decEvaluation");
        mCurveAcceleration = bundledData.getInt("curveEvaluation");
        mLeapAcceleration = bundledData.getInt("leapEvaluation");

        mAccelerationBar.setProgress(mAcceleration+50);
        mDecelerationBar.setProgress(mDeceleration+50);
        mCurveBar.setProgress(mCurveAcceleration+50);
        mLeapBar.setProgress(mLeapAcceleration+50);
        int score = 1;
        if(mAcceleration > 0) {
            score++;
        }
        if(mDeceleration > 0) {
            score++;
        }
        if(mCurveAcceleration > 0) {
            score++;
        }
        if(mLeapAcceleration > 0) {
            score++;
        }
        mRatingBar.setRating((float)score);
    }

   /* public static void playService(){
        ((InfoViewerActivity) getActivity()).mService.play().addObserver((Observer)this);
    }
*/
}
