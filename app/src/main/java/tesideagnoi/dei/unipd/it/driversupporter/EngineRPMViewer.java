package tesideagnoi.dei.unipd.it.driversupporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

public class EngineRPMViewer extends AppCompatActivity {
    // Costanti per registrazione audio e fft
    public static final int SAMPLESIZE = 1024;
    public static final int FFTSIZE = 2048;
    public static final int SAMPLERATE = 8000;
    private AudioRecord mRecorder;
    private AudioTrack mTrack;
    private short[] mBuffer;
    private Thread mRecordThread;
    private DoubleFFT_1D fftDo;
    private double[] fftTemp;
    private float frequency2000;
    private float minMag2000;
    private float maxMag2000;
    private float minMag3000;
    private float frequency3000;
    private float maxMag3000;
    private float frequency4000;
    private TextView rpmText;
    private TextView hertzText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engine_rpm_viewer);
        rpmText = (TextView) findViewById(R.id.rpm);
        hertzText = (TextView) findViewById(R.id.hertz);
        // Recupera dalla memoria interna i dati della calibrazione per i 2000 e 3000 giri
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        frequency2000 = sharedPref.getFloat("frequencyFor2000",0);
        minMag2000 = sharedPref.getFloat("minMagnitudeFor2000",0);
        maxMag2000 = sharedPref.getFloat("maxMagnitudeFor2000",0);
        frequency3000 = sharedPref.getFloat("frequencyFor3000",0);
        minMag3000 = sharedPref.getFloat("minMagnitudeFor3000",0);
        maxMag3000 = sharedPref.getFloat("maxMagnitudeFor3000",0);
        // Stima della frequenza per i 4000 giri
        frequency4000 = frequency3000+frequency3000-frequency2000;
        // Prepara le variabili necessarie per ascoltare dal microfono
        int bufferSize =  AudioRecord.getMinBufferSize(SAMPLERATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLERATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        int maxJitter = AudioTrack.getMinBufferSize(SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mTrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
        mBuffer = new short[SAMPLESIZE];

        // Definizione del Thread che eseguirà l'ascolto, la trasformata di fourier e l'analisi in
        // frequenza
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
                while (true) {
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
            }
        };
        mRecordThread.start();

    }

    /**
     * Fa una fft sui campioni audio e ne cerca i picchi entra un range di frequenze adatto a trovare
     * il rumore del motore, considerando le soglie date dalla calibrazione.
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
        for(int i=1;i<frequency4000 / SAMPLERATE * FFTSIZE;i++){
        // Cerca due picchi tra 0 e 4000 giri (stimati conoscendo le frequenze dei 2000 e 3000)
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
        // converto in frequenza l'indice
        peaks[0] = peaks[0] * SAMPLERATE / FFTSIZE;
        peaks[1] = peaks[1] * SAMPLERATE / FFTSIZE;
        // check sul picco, se è maggiore del massimo dei 2000 giri ma risulta sotto i 2000
        // lo scarto e scelgo il secondo picco; ripeto se è compreso tra i 2000 e 3000 controllando
        //rispetto al massimo dei 3000 giri
        if(peaks[0]<frequency2000){
            if(peakValues[0]>maxMag2000){
                peaks[0] = peaks[1];
                peakValues[0] = peakValues[1];
            }
        }
        else {
            if(peaks[0]<frequency3000){
                if(peakValues[0]>maxMag3000){
                    peaks[0] = peaks[1];
                    peakValues[0] = peakValues[1];
                }
            }
        }
        // Controllo per il rumore: se ho un picco sopra i 3000 giri controllo se il secondo picco
        // rilevato è accettabile; se è accettabile scarto il primo picco.
        if(peaks[0]>frequency3000){
            if(peaks[1]<frequency2000){
                if(peakValues[1]<maxMag2000){
                    peaks[0] = peaks[1];
                    peakValues[0] = peakValues[1];
                }
            }
            else {
                if(peaks[1]<frequency3000){
                    if(peakValues[1]<maxMag3000){
                        peaks[0] = peaks[1];
                        peakValues[0] = peakValues[1];
                    }
                }
            }
        }
        final float[] finalRPM = new float[2];
        finalRPM[0] = peaks[0];
        finalRPM[1]= (float)peakValues[0];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRPMText(finalRPM);
            }
        });
    }

    private void updateRPMText(float[] rpmValues) {
        if(rpmValues[0]<frequency2000) {
            rpmText.setText("sotto i 2000 giri");
        }
        else if(rpmValues[0]<frequency3000) {
            rpmText.setText("tra 2000 e 3000 giri");
        }
        else if(rpmValues[0]>frequency3000) {
            rpmText.setText("sopra i 3000 giri");
        }

        hertzText.setText(""+rpmValues[0]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_engine_rpm_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
