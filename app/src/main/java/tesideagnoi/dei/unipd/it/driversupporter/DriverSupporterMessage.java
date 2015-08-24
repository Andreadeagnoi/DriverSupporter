package tesideagnoi.dei.unipd.it.driversupporter;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.nio.ByteBuffer;

/**
 * Contenitore per i dati da mandare al server relativi alla guida.
 * Utilizzo: creare l'oggetto usando il builder e dopo chiamare il metodo getMessage.
 * Created by Andrea on 04/08/2015.
 */
public class DriverSupporterMessage {
    private byte[] timestamp = new byte[8];
    private byte[] sessionTimestamp = new byte[8];
    private byte[] xAcceleration = new byte[4];
    private byte[] yAcceleration = new byte[4];
    private byte[] zAcceleration = new byte[4];
    private byte[] latitude = new byte[4];
    private byte[] longitude = new byte[4];
    private byte[] speed = new byte[4];
    private byte[] engineRPM = new byte[2];
    private byte[] clientID = new byte[6];

    private DriverSupporterMessage(DriverSupporterMessageBuilder builder){
        this.timestamp = builder.dataTimestamp;
        this.sessionTimestamp = builder.sessionTimestamp;
        this.xAcceleration = builder.xAcceleration;
        this.yAcceleration = builder.yAcceleration;
        this.zAcceleration = builder.zAcceleration;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.speed = builder.speed;
        this.engineRPM = builder.engineRPM;
        this.clientID = builder.clientID;
    }

    /**
     * Ritorna un array di byte contenente i dati.
     */
    public byte[] getBytes() {
        byte[] message = new byte[48];
        short lastByte = 0;
        System.arraycopy(clientID, 0, message, lastByte, clientID.length);
        lastByte += clientID.length;
        System.arraycopy(engineRPM, 0, message,  lastByte, engineRPM.length);
        lastByte += engineRPM.length;
        System.arraycopy(sessionTimestamp, 0, message,  lastByte, sessionTimestamp.length);
        lastByte += sessionTimestamp.length;
        System.arraycopy(timestamp, 0, message, lastByte, timestamp.length);
        lastByte += timestamp.length;
        System.arraycopy(xAcceleration, 0, message,  lastByte, xAcceleration.length);
        lastByte += xAcceleration.length;
        System.arraycopy(yAcceleration, 0, message, lastByte, yAcceleration.length);
        lastByte += yAcceleration.length;
        System.arraycopy(zAcceleration, 0, message, lastByte, zAcceleration.length);
        lastByte += zAcceleration.length;
        System.arraycopy(latitude, 0, message,lastByte, latitude.length);
        lastByte += latitude.length;
        System.arraycopy(longitude, 0, message, lastByte, longitude.length);
        lastByte += longitude.length;
        System.arraycopy(speed, 0, message, lastByte, speed.length);
        return message;
    }

    public static class DriverSupporterMessageBuilder {
        private byte[] dataTimestamp = new byte[8];
        private byte[] sessionTimestamp = new byte[8];
        private byte[] xAcceleration = new byte[4];
        private byte[] yAcceleration = new byte[4];
        private byte[] zAcceleration = new byte[4];
        private byte[] latitude = new byte[4];
        private byte[] longitude = new byte[4];
        private byte[] speed = new byte[4];
        private byte[] engineRPM = new byte[2];
        private byte[] clientID = new byte[6];

        public DriverSupporterMessageBuilder(String macAddress, long sessionTimestamp) {

            clientID = MACParse(macAddress);
            this.sessionTimestamp = ByteBuffer.allocate(8).putLong(sessionTimestamp).array();
        }

        public DriverSupporterMessageBuilder engineRPM(short engineRPM){
            this.engineRPM[0] = (byte) engineRPM;
            this.engineRPM[1] = (byte) (engineRPM >> 8);
            return this;
        }

        public DriverSupporterMessageBuilder accData(AccelerometerData accData){
            this.dataTimestamp = ByteBuffer.allocate(8).putLong(accData.getTimestamp()).array();
            this.xAcceleration = ByteBuffer.allocate(4).putFloat(accData.getX()).array();
            this.yAcceleration = ByteBuffer.allocate(4).putFloat(accData.getY()).array();
            this.zAcceleration = ByteBuffer.allocate(4).putFloat(accData.getZ()).array();
            this.latitude = ByteBuffer.allocate(4).putFloat(accData.getLat()).array();
            this.longitude = ByteBuffer.allocate(4).putFloat(accData.getLon()).array();
            this.speed = ByteBuffer.allocate(4).putFloat(accData.getSpeed()).array();
            return this;
        }

        public DriverSupporterMessage build() {
            return new DriverSupporterMessage(this);
        }
        /**
         * Effettua il parsing di una stringa contenente un mac e restituisce il mac come array di byte.
         * @param address
         * @return
         */
        private byte[] MACParse(String address) {
            byte[] mac = new byte[6];
                String[] addressSplit = address.split(":");
                for(int i = 0; i < addressSplit.length; i++) {
                    mac[i] = Integer.decode("0x" + addressSplit[i]).byteValue();
                }
                return mac;
        }
    }
}
