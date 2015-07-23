package tesideagnoi.dei.unipd.it.driversupporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Si occupa della valutazione della guida dell'utente. Riceve dati con un riferimento alla lista
 * dei dati dell'accelerometro del service, che si occupera di richiedere una valutazione ogni volta
 * che ne raccoglie di nuovi. Quindi la classe aggiornerà il proprio stato che sarà osservabile dalle
 * actvity interessate via Observable pattern.
 * Created by Andrea on 23/07/2015.
 */
public class EvaluationUnit extends Observable{
    private final ArrayList<AccelerometerData> mAccelerometerData;
    // Soglie costanti per la valutazione
    static float SPEED_THRESHOLD = 5.5f;  // circa 20 km/h
    static float ACC_THRESHOLD = 1.38f; // 5 km/h^2
    static float CURVE_ACC_THRESHOLD = 2f;
    static float JUMP_LEAP_THRESHOLD = 2f;
    static float SPEED_LEAP_THRESHOLD = 8f;
    private final Context context;
    // Stato
    private int mGoodAccelerationCount;
    private int mBadAccelerationCount;
    private int mGoodCurveAccelerationCount;
    private int mBadCurveAccelerationCount;
    private int mGoodLeapAccelerationCount;
    private int mBadLeapAccelerationCount;
    private long lastLeapTimestamp;
    private int mScore;
    private SharedPreferences sharedPref;

    public EvaluationUnit(ArrayList<AccelerometerData> accelerometerData, Context context) {
        mAccelerometerData = accelerometerData;
        mGoodAccelerationCount = 0;
        mBadAccelerationCount = 0;
        mGoodCurveAccelerationCount = 0;
        mBadCurveAccelerationCount = 0;
        mGoodLeapAccelerationCount = 0;
        mBadLeapAccelerationCount = 0;
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
        sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        // 1^ valutazione: accelerazione/decelerazione progressiva
        if (mAccelerometerData.get(lastIndex).getSpeed() >= sharedPref.getFloat("speedThreshold", SPEED_THRESHOLD)) {
            if (Math.abs(mAccelerometerData.get(lastIndex).getZ() - mAccelerometerData.get(lastIndex - 1).getZ()) < sharedPref.getFloat("accThreshold", ACC_THRESHOLD)) {
                mGoodAccelerationCount++;
                mScore++;
            } else {
                mBadAccelerationCount++;
                mScore-=10;
            }
            // 2^ valutazione: accelerazione in curva
            if (Math.abs(mAccelerometerData.get(lastIndex).getX()) > sharedPref.getFloat("curveAccThreshold", CURVE_ACC_THRESHOLD)) {
                if (Math.abs(Math.pow(Math.pow(mAccelerometerData.get(lastIndex).getZ(), 2) + Math.pow(mAccelerometerData.get(lastIndex).getX(), 2), 0.5)
                        - Math.pow(Math.pow(mAccelerometerData.get(lastIndex - 1).getZ(), 2) + Math.pow(mAccelerometerData.get(lastIndex - 1).getX(), 2), 0.5)) < sharedPref.getFloat("accThreshold", ACC_THRESHOLD)) {
                    mGoodCurveAccelerationCount++;
                    mScore++;
                } else {
                    mBadCurveAccelerationCount++;
                    mScore-=10;
                }
            }
            //TODO: trovare l'accelerazione limite per dare una valutazione positiva sul dosso
            if (Math.abs(mAccelerometerData.get(lastIndex).getY()) > sharedPref.getFloat("leapAccThreshold",JUMP_LEAP_THRESHOLD)){
                if(mAccelerometerData.get(lastIndex).getTimestamp()-lastLeapTimestamp<1000000000){
                    if(mAccelerometerData.get(lastIndex).getSpeed() < sharedPref.getFloat("leapSpeedThreshold", SPEED_LEAP_THRESHOLD)) {
                        mGoodLeapAccelerationCount++;
                        mScore++;
                    }
                    else {
                        mBadLeapAccelerationCount++;
                        mScore-=10;
                    }
                }
                lastLeapTimestamp = mAccelerometerData.get(lastIndex).getTimestamp();
            }
        }
        if (mScore < 0){
            mScore = 0;
        }
        if (mScore > 500){
            mScore = 500;
        }
        Bundle bundledData = new Bundle();
        bundledData.putInt("GoodAcceleration", mGoodAccelerationCount);
        bundledData.putInt("BadAcceleration", mBadAccelerationCount);
        bundledData.putInt("GoodCurveAcceleration", mGoodCurveAccelerationCount);
        bundledData.putInt("BadCurveAcceleration", mBadCurveAccelerationCount);
        bundledData.putInt("GoodLeapAcceleration", mGoodLeapAccelerationCount);
        bundledData.putInt("BadLeapAcceleration", mBadLeapAccelerationCount);
        bundledData.putInt("TotalScore", mScore);
        setChanged();
        notifyObservers(bundledData);
    }


}
