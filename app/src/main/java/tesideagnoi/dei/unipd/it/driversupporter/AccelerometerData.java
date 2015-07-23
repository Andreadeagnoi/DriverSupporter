package tesideagnoi.dei.unipd.it.driversupporter;

import android.hardware.SensorManager;

import static java.lang.Math.sqrt;

/**
 * Classe contenitore per i dati dell'accelerometro.
 */
public class AccelerometerData {
    float x;
    float y;
    float z;
    long timestamp;
    float lat;
    float lon;
    float speed;

    public float getLat() {
        return lat;
    }

    public float getSpeed() {
        return speed;
    }

    public float getLon() {
        return lon;
    }

    public void setLat(float lat) {

        this.lat = lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public AccelerometerData(long timestamp, float x, float y, float z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getZ() {
        return z;
    }
    public void setZ(float z) {
        this.z = z;
    }

    public double getVectorLength(){
        return sqrt(x*x+y*y+z*z);
    }

}
