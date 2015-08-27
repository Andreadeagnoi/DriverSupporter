package tesideagnoi.dei.unipd.it.driversupporter;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Andrea on 27/08/2015.
 */
public class EvaluationData {
    private int mGoodAccelerationCount;
    private int mBadAccelerationCount;
    private int mGoodCurveAccelerationCount;
    private int mBadCurveAccelerationCount;
    private int mGoodLeapAccelerationCount;
    private int mBadLeapAccelerationCount;
    private int mGoodDecelerationCount;
    private int mBadDecelerationCount;
    private long lastLeapTimestamp;
    private int mScore;

   

    private int accumulatedDataCount;

    public int getAccumulatedDataCount() {
        return accumulatedDataCount;
    }

    public void updateAccumulatedDataCount() {
        this.accumulatedDataCount++;
    }
    
    public EvaluationData() {
    }
    
    public int getmGoodAccelerationCount() {
        return mGoodAccelerationCount;
    }

    public void updatemGoodAccelerationCount() {
        this.mGoodAccelerationCount++;
    }

    public int getmBadAccelerationCount() {
        return mBadAccelerationCount;
    }

    public void updatemBadAccelerationCount() {
        this.mBadAccelerationCount++;
    }

    public int getmGoodCurveAccelerationCount() {
        return mGoodCurveAccelerationCount;
    }

    public void updatemGoodCurveAccelerationCount() {
        this.mGoodCurveAccelerationCount++;
    }

    public int getmBadCurveAccelerationCount() {
        return mBadCurveAccelerationCount;
    }

    public void updatemBadCurveAccelerationCount() {
        this.mBadCurveAccelerationCount++;
    }

    public int getmGoodLeapAccelerationCount() {
        return mGoodLeapAccelerationCount;
    }

    public void updatemGoodLeapAccelerationCount() {
        this.mGoodLeapAccelerationCount++;
    }

    public int getmBadLeapAccelerationCount() {
        return mBadLeapAccelerationCount;
    }

    public void updatemBadLeapAccelerationCount() {
        this.mBadLeapAccelerationCount++;
    }

    public int getmGoodDecelerationCount() {
        return mGoodDecelerationCount;
    }

    public void updatemGoodDecelerationCount() {
        this.mGoodDecelerationCount++;
    }

    public int getmBadDecelerationCount() {
        return mBadDecelerationCount;
    }

    public void updatemBadDecelerationCount() {
        this.mBadDecelerationCount++;
    }

    public long getLastLeapTimestamp() {
        return lastLeapTimestamp;
    }

    public void setLastLeapTimestamp(long lastLeapTimestamp) {
        this.lastLeapTimestamp = lastLeapTimestamp;
    }

    public int getmScore() {
        return mScore;
    }

    public void updatemScore(int mScore) {
        this.mScore += mScore;
    }

    public void setmGoodAccelerationCount(int mGoodAccelerationCount) {
        this.mGoodAccelerationCount = mGoodAccelerationCount;
    }

    public void setmBadAccelerationCount(int mBadAccelerationCount) {
        this.mBadAccelerationCount = mBadAccelerationCount;
    }

    public void setmGoodCurveAccelerationCount(int mGoodCurveAccelerationCount) {
        this.mGoodCurveAccelerationCount = mGoodCurveAccelerationCount;
    }

    public void setmBadCurveAccelerationCount(int mBadCurveAccelerationCount) {
        this.mBadCurveAccelerationCount = mBadCurveAccelerationCount;
    }

    public void setmGoodLeapAccelerationCount(int mGoodLeapAccelerationCount) {
        this.mGoodLeapAccelerationCount = mGoodLeapAccelerationCount;
    }

    public void setmBadLeapAccelerationCount(int mBadLeapAccelerationCount) {
        this.mBadLeapAccelerationCount = mBadLeapAccelerationCount;
    }

    public void setmGoodDecelerationCount(int mGoodDecelerationCount) {
        this.mGoodDecelerationCount = mGoodDecelerationCount;
    }

    public void setmBadDecelerationCount(int mBadDecelerationCount) {
        this.mBadDecelerationCount = mBadDecelerationCount;
    }

    public void setmScore(int mScore) {
        this.mScore = mScore;
    }

    public void setAccumulatedDataCount(int accumulatedDataCount) {
        this.accumulatedDataCount = accumulatedDataCount;
    }

    public EvaluationData makeCopy(){
        EvaluationData copy = new EvaluationData();
        copy.setmGoodAccelerationCount(this.mGoodAccelerationCount);
        copy.setmBadAccelerationCount(this.mBadAccelerationCount);
        copy.setmGoodDecelerationCount(this.mGoodDecelerationCount);
        copy.setmBadDecelerationCount(this.mBadDecelerationCount);
        copy.setmGoodCurveAccelerationCount(this.mGoodCurveAccelerationCount);
        copy.setmBadCurveAccelerationCount(this.mBadCurveAccelerationCount);
        copy.setmGoodLeapAccelerationCount(this.mGoodLeapAccelerationCount);
        copy.setmBadLeapAccelerationCount(this.mBadLeapAccelerationCount);
        copy.setLastLeapTimestamp(this.lastLeapTimestamp);
        return copy;
    }
}
