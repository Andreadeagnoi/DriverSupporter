package tesideagnoi.dei.unipd.it.driversupporter;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Test per la classe driverSupporteMessage
 * Created by Andrea on 21/08/2015.
 */
public class DriverSupporterMessageTest extends TestCase {

    public void testGetBytes() throws Exception {
        long timestamp = System.nanoTime();
        AccelerometerData testAccData = new AccelerometerData(timestamp,1.3f,2.3f,3.3f);
        testAccData.setLat(12.3f);
        testAccData.setLon(40.1f);
        testAccData.setSpeed(5.5f);
        long sessionTimestamp = System.currentTimeMillis();
        String macAddress = "7d:2:3:4:5:6";
        //definisco un nuovo oggetto DriverSupporterMessage con dati di prova
        DriverSupporterMessage testMessage = new DriverSupporterMessage.DriverSupporterMessageBuilder(macAddress,sessionTimestamp)
                .accData(testAccData)
                .build();
        byte[] testMessageBytes = testMessage.getBytes();
        assertEquals("testMessageBytes[0] must be 125", 125, testMessageBytes[0]);
        assertEquals("testMessageBytes[1] must be 2", 2, testMessageBytes[1]);
        assertEquals("testMessageBytes[2] must be 3", 3, testMessageBytes[2]);
        assertEquals("testMessageBytes[3] must be 4", 4, testMessageBytes[3]);
    }
}