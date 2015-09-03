package tesideagnoi.dei.unipd.it.driversupporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Si occupa della valutazione della guida dell'utente. Riceve dati con un riferimento alla lista
 * dei dati dell'accelerometro del service, che si occupera di richiedere una valutazione ogni volta
 * che ne raccoglie di nuovi. Quindi la classe aggiornerà il proprio stato che sarà osservabile dalle
 * actvity interessate via Observable pattern.
 * La valutazione avviene in due passi:
 *  - il primo passo consiste nell'aggiornare i dati correnti;
 *  - il secondo passo consiste nella valutazione vera e propria e avviene circa una volta al secondo
 *  (essendo che il listener dell'accelerometro è impostato in modo da rilevare i dati con un periodo di 0,2 secondi
 *  avrò una valutazione ogni 5 rilevazioni).
 * Created by Andrea on 23/07/2015.
 */
public class EvaluationUnit extends Observable{
    private final ArrayList<AccelerometerData> mAccelerometerData;
    // Soglie costanti per la valutazione
    static float SPEED_THRESHOLD = 5.5f;  // circa 20 km/h
    static float ACC_THRESHOLD = 0.76f; // m/s^2
    static float CURVE_ACC_THRESHOLD = 1f; // m/s^2
    static float JUMP_LEAP_THRESHOLD = 1f; // m/s^2
    static float SPEED_LEAP_THRESHOLD = 8f; // circa 30 km/h
    private final Context context;
    // Stato
    private EvaluationData oldEvaluationData;
    private EvaluationData currentEvaluationData;
    private int mAccEvaluation;
    private int mDecEvaluation;
    private int mCurveEvaluation;
    private int mLeapEvaluation;

    private SharedPreferences sharedPref;
    private boolean isDecelerating;


    public EvaluationUnit(ArrayList<AccelerometerData> accelerometerData, Context context) {
        mAccelerometerData = accelerometerData;
        currentEvaluationData = new EvaluationData();
        this.context  = context;
    }

    /**
     * Metodo temporaneo che da un feedback sulla guida.
     * 1) Valutazione sull'accelerazione, semplicemente confrontando la variazione di accelerazione nell'asse parallelo al moto (Z) tra due rilevazioni.
     * Confronta anche con la velocità attuale se il dato è disponibile.
     * 2) Valutazione sull'accelerazione in curva: se l'accelerazione sull'asse Y (o X se il telefono è in verticale)
     * è maggiore di una certa soglia allora il modulo dell'accelerazione su Z e Y viene valutato.
     * 3) Valutazione sui dossi o buche: se rileva un picco di accelerazione in altezza ripetuto due volte in sequenza (attribuibilie ad un dosso preso in velocità)
     */
    public void driverEvaluation(int lastIndex) {
        if(mAccelerometerData.size()<2){
            return;
        }
        currentEvaluationData.updateAccumulatedDataCount();
        sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        // 1^ valutazione: accelerazione/decelerazione progressiva
        if (mAccelerometerData.get(lastIndex).getSpeed() >= sharedPref.getFloat("speedThreshold", SPEED_THRESHOLD)) {
            float accVar = (mAccelerometerData.get(lastIndex).getZ() - mAccelerometerData.get(lastIndex - 1).getZ());
            float currAcc = mAccelerometerData.get(lastIndex).getZ();
            if ( accVar > 0.1 && currAcc > 0) {
                if (accVar < sharedPref.getFloat("accThreshold", ACC_THRESHOLD)) {
                    currentEvaluationData.updatemGoodDecelerationCount();
                } else {
                    currentEvaluationData.updatemBadDecelerationCount();
                }
            } else{
                if(accVar < -0.1 && currAcc < 0) {
                        if (accVar > -sharedPref.getFloat("accThreshold", ACC_THRESHOLD)) {
                            currentEvaluationData.updatemGoodAccelerationCount();
                        } else {
                            currentEvaluationData.updatemBadAccelerationCount();
                        }
                    }

                }



            // 2^ valutazione: accelerazione in curva
        if (Math.abs(mAccelerometerData.get(lastIndex).getX()) > sharedPref.getFloat("curveAccThreshold", CURVE_ACC_THRESHOLD)) {
                if (Math.abs(Math.pow(Math.pow(mAccelerometerData.get(lastIndex).getZ(), 2) + Math.pow(mAccelerometerData.get(lastIndex).getX(), 2), 0.5)
                        - Math.pow(Math.pow(mAccelerometerData.get(lastIndex - 1).getZ(), 2) + Math.pow(mAccelerometerData.get(lastIndex - 1).getX(), 2), 0.5)) < sharedPref.getFloat("accThreshold", ACC_THRESHOLD)) {
                    currentEvaluationData.updatemGoodCurveAccelerationCount();

                } else {
                    currentEvaluationData.updatemBadCurveAccelerationCount();

                }
            }

            if (Math.abs(mAccelerometerData.get(lastIndex).getY()) > sharedPref.getFloat("leapAccThreshold", JUMP_LEAP_THRESHOLD)){
                if(mAccelerometerData.get(lastIndex).getTimestamp()-currentEvaluationData.getLastLeapTimestamp()< 5000000000L){
                    if(mAccelerometerData.get(lastIndex).getSpeed() < sharedPref.getFloat("leapSpeedThreshold", SPEED_LEAP_THRESHOLD)) {
                        currentEvaluationData.updatemGoodLeapAccelerationCount();
                    }
                    else {
                        currentEvaluationData.updatemBadLeapAccelerationCount();

                    }
                }
                currentEvaluationData.setLastLeapTimestamp(mAccelerometerData.get(lastIndex).getTimestamp());
            }
        }

        if( currentEvaluationData.getAccumulatedDataCount()>4) {
            oldEvaluationData = currentEvaluationData.makeCopy();
            currentEvaluationData = new EvaluationData();
            currentEvaluationData.setLastLeapTimestamp(oldEvaluationData.getLastLeapTimestamp());
            sendPeriodicNotify();
        }
}

    private void sendPeriodicNotify() {
        Bundle bundledData = new Bundle();
        if(oldEvaluationData.getmBadAccelerationCount()>0){
            mAccEvaluation -=30;
        }
        else {
            if(oldEvaluationData.getmGoodAccelerationCount()>0){
                mAccEvaluation+=5;
            }
        }
        if(mAccEvaluation<-50) {
            mAccEvaluation = -50;
        }
        if(mAccEvaluation > 100) {
            mAccEvaluation = 100;
        }
        if(oldEvaluationData.getmBadDecelerationCount()>0){
            mDecEvaluation -= 30;
        }
        else {
            if(oldEvaluationData.getmGoodDecelerationCount()>0){
                mDecEvaluation += 5;
            }
        }
        if(mDecEvaluation<-50) {
            mDecEvaluation = -50;
        }
        if(mDecEvaluation > 100) {
            mDecEvaluation = 100;
        }
        if(oldEvaluationData.getmBadAccelerationCount()>0){
            mCurveEvaluation -= 30;
        }
        else {
            if (oldEvaluationData.getmGoodCurveAccelerationCount()>0) {
                mCurveEvaluation += 5;
            }
        }
        if(mCurveEvaluation<-50) {
            mCurveEvaluation = -50;
        }
        if(mCurveEvaluation > 100) {
            mCurveEvaluation = 100;
        }
        if(oldEvaluationData.getmBadLeapAccelerationCount()>0){
            mLeapEvaluation -=30;
        }
        else {
            if(oldEvaluationData.getmGoodLeapAccelerationCount()>0) {
                mLeapEvaluation += 5;
            }
        }
        if(mLeapEvaluation<-50) {
            mLeapEvaluation = -50;
        }
        if(mLeapEvaluation > 100) {
            mLeapEvaluation = 100;
        }
        bundledData.putInt("accEvaluation", mAccEvaluation);
        bundledData.putInt("decEvaluation",mDecEvaluation);
        bundledData.putInt("curveEvaluation",mCurveEvaluation);
        bundledData.putInt("leapEvaluation",mLeapEvaluation);
        setChanged();
        notifyObservers(bundledData);
    }


}
