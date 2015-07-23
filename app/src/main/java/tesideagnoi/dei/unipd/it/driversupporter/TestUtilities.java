package tesideagnoi.dei.unipd.it.driversupporter;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by Andrea on 15/07/2015.
 */
public class TestUtilities {
    /**
     * Scrive sulla memoria esterna del telefono i dati dell'accelerometro rilevati.
     * Usato per testing.
     */
    public static void writeToExternalStorage(List<AccelerometerData> mSamples) {
        File root = Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();

        File file = new File(dir, System.currentTimeMillis()+".txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println("Timestamp;X_Acceleration;Y_Acceleration;Z_Acceleration;Acceleration_Absolute_Value;Latitude;Longitude;Speed;" +
                    "X_AccelerationNF;Y_AccelerationNF;Z_AccelerationNF;Acceleration_Absolute_ValueNF");
            for(int i = 0; i<mSamples.size();i++) {
                pw.println(mSamples.get(i).getTimestamp()+";"+
                        mSamples.get(i).getX()+";"+
                        mSamples.get(i).getY()+";"+
                        mSamples.get(i).getZ()+";"+
                        mSamples.get(i).getVectorLength()+";"+
                        mSamples.get(i).getLat()+";"+
                        mSamples.get(i).getLon()+";"+
                        mSamples.get(i).getSpeed() + ";"
                      );
            }
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
