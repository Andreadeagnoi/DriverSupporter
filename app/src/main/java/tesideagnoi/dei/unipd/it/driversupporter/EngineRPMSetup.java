package tesideagnoi.dei.unipd.it.driversupporter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.google.android.gms.internal.zzhl.runOnUiThread;
import static tesideagnoi.dei.unipd.it.driversupporter.TestUtilities.writeToExternalStorageSpecter;



public class EngineRPMSetup extends Fragment {

    // Costanti per registrazione audio e fft
    public static final int SAMPLESIZE = 1024;
    public static final int FFTSIZE = 2048;
    public static final int SAMPLERATE = 8000;

    private View rootView;
    private Button calibrate2000RPM;
    private AudioRecord mRecorder;
    private AudioTrack mTrack;
    private short[] mBuffer;
    private Thread mRecordThread;
    private DoubleFFT_1D fftDo;
    private double[] fftTemp;
    private ArrayList<float[]> calibrationDataArray;

    public EngineRPMSetup() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_engine_rpmsetup, container, false);
        calibrate2000RPM = (Button) rootView.findViewById(R.id.calibrate2000);
        calibrate2000RPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              calibrate(2000);
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Memorizza per la data soglia di giri motore i valori di frequenza e magnitudo, minima e massima.
     * Portare il motore già a tali giri frizionando e poi premere il pulsante per la registrazione.
     * Il metodo farà la fft 5 volte e memorizzerà i valori in shared pref.
     * Esempio con soglia di giri al minuto di 2000: meanFrequency2000, minMagnitude2000 e maxMagnitude2000.
     * @param rpmThreshold
     */
    private void calibrate(final int rpmThreshold) {
       calibrationDataArray = new ArrayList<>();
        int bufferSize =  AudioRecord.getMinBufferSize(SAMPLERATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLERATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        int maxJitter = AudioTrack.getMinBufferSize(SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mTrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
        mBuffer = new short[SAMPLESIZE];


        mRecordThread = new Thread()  {
            @Override
            public void run() {
                // Prepara le variabili e il file su cui registrare
                int bufferReadResult = 0;
                fftTemp = new double[FFTSIZE];
                // Write the output audio in byte
                int j = 0; //indice per tener conto di quando arrivo a 2048 campioni nella fft
                int z= 0; //indice per tener conto di quante volte esegue la fft
                mRecorder.startRecording();
                // Elabora i dati registrati dal microfono
                while (z < 6) {
                    bufferReadResult = mRecorder.read(mBuffer, 0, SAMPLESIZE);
                    fftDo = new DoubleFFT_1D(FFTSIZE);
                    // Copia i campioni correnti in un array temporaneo
                    for(int i=0;i<SAMPLESIZE;i++){
                        fftTemp[i+j*1024] = mBuffer[i];
                    }
                    j++;
                    // quando ho nell'array 2048 campioni fa la fft.
                    if (j > 1) {
                        analyzeFFT();
                        j=0;
                        z++;
                    }
                }
                computeCalibrationData(rpmThreshold);
            }
        };

    }

    /**
     * Salva in shared pref i valori necessari per la calibrazione.
     */
    private void computeCalibrationData(int rpmThreshold) {
        float frequency4Threshold = calibrationDataArray.get(0)[0]; //valore di frequenza per i giri al minuti analizzati (media sui dati raccolti dalla calibrazione)
        float minMagnitude4Threshold;  //minimo valore di magnitudo della frequenza per i giri al minuti analizzati
        float maxMagnitude4Threshold;  //massimo valore di magnitudo della frequenza per i giri al minuti analizzati
        int i;
        minMagnitude4Threshold = calibrationDataArray.get(0)[1];
        maxMagnitude4Threshold = calibrationDataArray.get(0)[1];
        for(i = 1; i < calibrationDataArray.size(); i++){
            frequency4Threshold = (frequency4Threshold + calibrationDataArray.get(i)[0]);
            if(calibrationDataArray.get(i)[1] < minMagnitude4Threshold){
                minMagnitude4Threshold = calibrationDataArray.get(i)[1];
            }
            if(calibrationDataArray.get(i)[1] > maxMagnitude4Threshold){
                maxMagnitude4Threshold = calibrationDataArray.get(i)[1];
            }
        }
        frequency4Threshold = frequency4Threshold / i;
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("frequencyFor" + rpmThreshold, frequency4Threshold);
        editor.putFloat("minMagnitudeFor" + rpmThreshold, minMagnitude4Threshold);
        editor.putFloat("maxMagnitudeFor" + rpmThreshold, maxMagnitude4Threshold);
        editor.commit();
    }

    /**
     * Fa una fft sui campioni audio e ne cerca i picchi entra un range di frequenze adatto a trovare
     * il rumore del motore.
     */
    public void analyzeFFT(){
        double[] fft = fftTemp.clone();
        fftDo.realForward(fft);
        String fftToString="";
        double[] magnitude = new double[FFTSIZE/2];
        for(int i=0;i<(FFTSIZE-1)/2-1;i++){
            double re = fft[2*i];
            double im =  fft[2*i+1];
            magnitude[i]= Math.sqrt(re*re+im*im);
            fftToString = fftToString + (magnitude[i]+";"); // SOLO PER TEST
        }
        int peak = 0;
        double peakValue = magnitude[0];
        int[] peaks = new int[2];
        double[] peakValues = new double[2];
        peaks[0] = 0; peaks[1] = 0;
        peakValues[0] = 0; peakValues[1] = 0;
        // Memorizzo due picchi a scopo di debug.
        for(int i=1;i<15*4/2;i++){ // Cerca un picco tra 10hz e 150hz (ipotesi ho 4 cilindri)
            if(magnitude[i] > peakValues[0]) {
                if (magnitude[i - 1] < magnitude[i]  && magnitude[i + 1] < magnitude[i] ) {
                    peaks[1] = peaks[0];
                    peaks[0] = i;
                    peakValues[1] = peakValues[0];
                    peakValues[0] = magnitude[i];
                }
            }
            else if(magnitude[i] > peakValues[1]){
                if (magnitude[i - 1] < magnitude[i]  && magnitude[i + 1] < magnitude[i] ) {
                    peaks[1] = i;
                    peakValues[1] = magnitude[i];
                }
            }
        }
        // Se è predominante una frequenza molto bassa provo a cercare altri picchi
        if(peaks[0] < 4){
            for(int i=2*4/2;i<15*4/2;i++){
                if (magnitude[i - 1] < magnitude[i]  && magnitude[i + 1] < magnitude[i] ) {
                    peaks[1] = peaks[0];
                    peaks[0] = i;
                    peakValues[1] = peakValues[0];
                    peakValues[0] = magnitude[i];
                }
            }
        }
        float[] calibrationData = new float[2];
        calibrationData[0] = peaks[0] * SAMPLERATE / FFTSIZE;
        calibrationData[1]= (float)peakValues[0];
        calibrationDataArray.add(calibrationData);
    }



}
