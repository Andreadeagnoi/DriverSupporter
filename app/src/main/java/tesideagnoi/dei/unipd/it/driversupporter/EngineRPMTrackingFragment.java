package tesideagnoi.dei.unipd.it.driversupporter;

import android.content.Context;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import static com.google.android.gms.internal.zzhl.runOnUiThread;
import static tesideagnoi.dei.unipd.it.driversupporter.TestUtilities.writeToExternalStorageLocation;
import static tesideagnoi.dei.unipd.it.driversupporter.TestUtilities.writeToExternalStorageSpecter;


/**
 * Questo fragment conterrà una barra che verra aggiornata in tempo reale con i giri motore e pure la
 * velocità in real time.
 * Utilizzato per il debug del controllo sui giri motore.
 */
public class EngineRPMTrackingFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener  {

    public static final int SAMPLESIZE = 1024;
    public static final int FFTSIZE = 2048;
    public static final int SAMPLERATE = 22050;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private BreakIterator mLatitudeText;
    private BreakIterator mLongitudeText;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    private Location mCurrentLocation;
    private float mCurrentSpeed;
    private ProgressBar mRPMBar;
    private TextView mSpeedValue;
    private Button mRecordButton;
    private AudioRecord mRecorder;
    private boolean isRecording;
    private short[] mBuffer;
    private AudioTrack mTrack;
    private Thread mRecordThread;
    private ArrayList<Float[]> locationData;
    private ArrayList<String> specter;
    private Context context;
    private TextView mRPMValue;
    private ArrayList<Integer> rpmList;
    private DoubleFFT_1D fftDo;
    private double[] fft;
    private LinkedList<Double> fftLinked;
    private double[] fftTemp;
    private int j;

    public EngineRPMTrackingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_engine_rpmtracking, container, false);
        buildGoogleApiClient();
        mSpeedValue = (TextView) rootView.findViewById(R.id.speedRPMValue);
        mRPMValue = (TextView) rootView.findViewById(R.id.RPMValue);
        mRecordButton = (Button) rootView.findViewById(R.id.recordButton);
        isRecording = false;
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording){
                    isRecording = true;
                    mRecordThread.start();

                } else {
                    isRecording = false;
                    mRecordThread.interrupt();
                    mRecorder.stop();
                    mRecorder.release();
                    writeToExternalStorageSpecter(rpmList);
                }

            }
        });
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
                record();
            }
        };
        mRequestingLocationUpdates = false;
        specter = new ArrayList<String>();
        rpmList = new ArrayList<Integer>();
        return rootView;
    }

    private void record() {
        // Prepara le variabili e il file su cui registrare
        int bufferReadResult = 0;
        fftTemp = new double[FFTSIZE];
        // Write the output audio in byte
        File root = Environment.getExternalStorageDirectory();
        String filePath = root.getAbsolutePath() + "/download/8k16bitMono.pcm";


        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        j = 0; //indice per tener conto di quando arrivo a 4096 campioni nella fft
        mRecorder.startRecording();
        // Elabora i dati registrati dal microfono
        while (true) {
            if(mRecordThread.interrupted()){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            };
            bufferReadResult = mRecorder.read(mBuffer, 0, SAMPLESIZE);
            fftDo = new DoubleFFT_1D(FFTSIZE);

            for(int i=0;i<SAMPLESIZE;i++){
                fftTemp[i+j*1024] = mBuffer[i];
            }
            j++;
            try {
                // writes the data to file from buffer stores the voice buffer
                byte bData[] = short2byte(mBuffer);
                os.write(bData, 0, SAMPLESIZE * 2);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (j > 1) {

                analyzeFFT();
                j=0;
            }



        }
    }

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
        for(int i=1;i<13;i++){ // Cerca un picco tra 10hz e 120hz (ipotesi ho 4 cilindri)
            if(magnitude[i] > peakValue) {
                if (magnitude[i - 1] < magnitude[i]  && magnitude[i + 1] < magnitude[i] ) {
                    peak = i;
                    peakValue = magnitude[i];
                }
            }
        }
        final int finalPeak = peak;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRPM(finalPeak);
            }
        });
    }

    public void updateRPM(int peak){
        rpmList.add(peak * SAMPLERATE / FFTSIZE);
        if(rpmList.size()>2) {
            if (Math.abs(rpmList.get(rpmList.size() - 2) - rpmList.get(rpmList.size()-1)) < 30) {
                mRPMValue.setText((peak * SAMPLERATE / FFTSIZE) * 60 / 2 + "");
            }
        }
    }
    //Conversion of short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

        }
        startLocationUpdates();
        mRequestingLocationUpdates = true;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) this);

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mCurrentSpeed = location.getSpeed();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        locationData.add(new Float[]{(float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getElapsedRealtimeNanos(),
                location.getSpeed()});
        updateUI();
    }

    private void updateUI() {
        mSpeedValue.setText(toKMhString(mCurrentSpeed));
    }

    /**
     * Converte da metri al secondo a kilometri orari
     * @param msSpeed velocità in metri al secondo da convertire
     * @return velocità in kilometri orari, formattata come "%velocità% km/h"
     */
    private String toKMhString(float msSpeed){
        float KMh = msSpeed * 3.6f;
        KMh = Math.round(KMh);
        return KMh + " km/h";
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();

        mRequestingLocationUpdates = false;
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        writeToExternalStorageLocation(locationData);
    }

    @Override
    public void onResume() {
        super.onResume();
        locationData = new ArrayList<Float[]>();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
            mRequestingLocationUpdates = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
}
