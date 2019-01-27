package com.unifreelancer.masso.sounddetection;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class AnalysisSpeechActivity extends AppCompatActivity implements View.OnClickListener{


    private Handler mHandler = new Handler();
    private long startTime;
    private long elapsedTime;
    private final int REFRESH_RATE = 100;
    private String hours,minutes,seconds,milliseconds;
    private long secs,mins,hrs,msecs;
    private boolean stopped = false;

    private boolean isAnalyze = false;
    private boolean isRecord = false;

    private static double minDecibel;
    private static double maxDecibel;

    //private PatientResult resultData; // buat data untuk save dengan runner
    private static int count = 0;

    MediaRecorder recorder;
    File audiofile = null;
    static final String TAG = "MediaRecording";

    Button setting, save, analyze, reset;

    private static final int sampleRate = 11025;
    private static final int bufferSizeFactor = 10;

    private AudioRecord audio;
    private int bufferSize;

    private ProgressBar level;


    private TextView outDb;
    TextView score;
    TextView avrSpeech;
    TextView avrDuration;
    TextView date;

    private int valueInSecond;

    private Handler handler = new Handler();

    private short lastLevel = 0;

    final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_speech);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        save = (Button)findViewById(R.id.btnSave);
        save.setOnClickListener(this);

        analyze = (Button)findViewById(R.id.btnAnalyze);
        analyze.setOnClickListener(this);


        reset = (Button)findViewById(R.id.btnReset);
        reset.setOnClickListener(this);

        setting = (Button)findViewById(R.id.btnSetting);
        setting.setOnClickListener(this);

        score = (TextView)findViewById(R.id.outScore);
        avrSpeech = (TextView)findViewById(R.id.outAvSpeech);
        avrDuration = (TextView)findViewById(R.id.outDuration);
        date = (TextView)findViewById(R.id.outDate);

        outDb = (TextView)findViewById(R.id.outDb);

        level = (ProgressBar) findViewById(R.id.progressdB);
        level.setMax(32676);

        updateDisplay();


        ToggleButton record = (ToggleButton) findViewById(R.id.btnRecord);

        record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub

                if (isChecked) {

                    //Set Record to true
                    isRecord = true;


                    minDecibel = PatientResult.instance().getMinDb();
                    maxDecibel = PatientResult.instance().getMaxDb();


                    if(stopped){
                        startTime = System.currentTimeMillis() - elapsedTime;
                    }
                    else{
                        startTime = System.currentTimeMillis();
                    }
                    mHandler.removeCallbacks(startTimer);
                    mHandler.postDelayed(startTimer, 0);
                    //stopwatch

                    bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT) * bufferSizeFactor;

                    audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                    audio.startRecording();

                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            startRecording();
                            readAudioBuffer();
                        }
                    });

                    thread.setPriority(Thread.currentThread().getThreadGroup().getMaxPriority());

                    thread.start();

                    handler.removeCallbacks(update);
                    handler.postDelayed(update, 25);

                } else if (audio != null) {

                    mHandler.removeCallbacks(startTimer);
                    stopped = true;

                    PatientResult.instance().setDuration(valueInSecond);
                    //stopwatch


                    stopRecording();
                    audio.stop();
                    audio.release();
                    audio = null;
                    handler.removeCallbacks(update);
                }

            }
        });


    }

    @Override
    public void onClick(View v) {

         if (v == save ) {

             if (!isAnalyze) {
                 Toast.makeText(this, "Please Analyze Before Save ", Toast.LENGTH_LONG).show();
                } else {
                 save();
             }

         }
        if (v == analyze) {

            if (!isRecord) {
                Toast.makeText(this, "Please Start Record Before Analyze ", Toast.LENGTH_LONG).show();
            } else {
                analyze();
            }
        }

        if (v == reset){

             reset();

        }
        if ( v == setting){

             setting();
        }
    }

    public void setting() {

        Intent intent = new Intent(getApplicationContext(), Setting.class);
        startActivity(intent);
    }


    private void save(){

        Intent intent = new Intent(getApplicationContext(), EmailActivity.class);
        startActivity(intent);
    }


    public void analyze(){


            int avrSpeechResult = count/valueInSecond;

            PatientResult.instance().setAvrSpeech(avrSpeechResult);

            isAnalyze = true;
            AnalysisSpeechActivity.this.avrSpeech.setText(String.valueOf(avrSpeechResult));
    }

    public void reset(){

        isAnalyze = false;
        isRecord = false;

        count = 0;

        avrDuration.setText("00:00:00");

        PatientResult.instance().setScore(0);
        PatientResult.instance().setAvrSpeech(0);
        PatientResult.instance().setDuration(0);

        String newScore = String.valueOf(PatientResult.instance().getScore());
        String newAvrSpeech = String.valueOf(PatientResult.instance().getAvrSpeech());
        String newDuration = String.valueOf(PatientResult.instance().getDuration());

        score.setText(newScore);
        avrSpeech.setText(newAvrSpeech);
        avrDuration.setText(newDuration);

        AnalysisSpeechActivity.this.level.setProgress(0);
        AnalysisSpeechActivity.this.outDb.setText("0 dB");


    }

    private Runnable update = new Runnable() {

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        public void run() {

            double decibel = getAmplitudeEMA();


            Log.i("Min Decibel", String.valueOf(minDecibel));
            Log.i("Max Decibel", String.valueOf(decibel));
            Log.i("Max Decibel", String.valueOf(maxDecibel));
            //AnalysisSpeechActivity.this.outDb.setText(String.valueOf(lastLevel));

            if (decibel >= minDecibel && decibel <= maxDecibel){
                count +=1;

                PatientResult.instance().setScore(count);
            }

            AnalysisSpeechActivity.this.level.setProgress(lastLevel);
            AnalysisSpeechActivity.this.outDb.setText(String.format("%.2f", decibel) + " dB");

            int newscore = PatientResult.instance().getScore();
            AnalysisSpeechActivity.this.score.setText(String.valueOf(newscore));

            lastLevel *= .5;

            handler.postAtTime(this, SystemClock.uptimeMillis() + 500);

        }

    };

    private Runnable startTimer = new Runnable() {
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTime;
            updateTimer(elapsedTime);
            mHandler.postDelayed(this,REFRESH_RATE);
        }
    };

    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];

            int bufferReadResult;

            do {

                bufferReadResult = audio.read(buffer, 0, bufferSize);

                for (int i = 0; i < bufferReadResult; i++) {

                    if (buffer[i] > lastLevel) {
                        lastLevel = buffer[i];
                    }

                }


            }
            while (bufferReadResult > 0 && audio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING);

            if (audio != null) {
                audio.release();
                audio = null;
                handler.removeCallbacks(update);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void startRecording()  {

        //Creating file
        File dir = Environment.getExternalStorageDirectory();
        try {
            audiofile = File.createTempFile("sound", ".mp3", dir);
        } catch (IOException e) {
            Log.e(TAG, "external storage access error");
            return;
        }
        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }

    public void stopRecording() {

        //stopping recorder
        recorder.stop();
        recorder.release();
        //after stopping the recorder, create the sound file and add it to media library.
        addRecordingToMediaLibrary();
    }

    protected void addRecordingToMediaLibrary() {
        //creating content values of size 4
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());

        //creating content resolver and storing it in the external content uri
        ContentResolver contentResolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        //sending broadcast message to scan the media file so that it can be available
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
    }


    //Decibal


    public double getAmplitude() {
        if (recorder != null)
            return  (recorder.getMaxAmplitude());
        else
            return 0;

    }
    public double getAmplitudeEMA() {
        double amp =  getAmplitude();
        float db =(float) ((20 * Math.log10(amp/700.0))+ 17);
        //mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return db;
    }

    //Timer

    private void updateTimer (float time){
        secs = (long)(time/1000);
        mins = (long)((time/1000)/60);
        hrs = (long)(((time/1000)/60)/60);

		/* Convert the seconds to String
		 * and format to ensure it has
		 * a leading zero when required
		 */
        secs = secs % 60;
        seconds=String.valueOf(secs);
        if(secs == 0){
            seconds = "00";
        }
        if(secs <10 && secs > 0){
            seconds = "0"+seconds;
        }

		/* Convert the minutes to String and format the String */

        mins = mins % 60;
        minutes=String.valueOf(mins);
        if(mins == 0){
            minutes = "00";
        }
        if(mins <10 && mins > 0){
            minutes = "0"+minutes;
        }

    	/* Convert the hours to String and format the String */

        hours=String.valueOf(hrs);
        if(hrs == 0){
            hours = "00";
        }
        if(hrs <10 && hrs > 0){
            hours = "0"+hours;
        }

    	/* Although we are not using milliseconds on the timer in this example
    	 * I included the code in the event that you wanted to include it on your own
    	 */
        milliseconds = String.valueOf((long)time);
        if(milliseconds.length()==2){
            milliseconds = "0"+milliseconds;
        }
        if(milliseconds.length()<=1){
            milliseconds = "00";
        }
        milliseconds = milliseconds.substring(milliseconds.length()-3, milliseconds.length()-2);

		/* Setting the timer text to the elapsed time */
        avrDuration.setText(hours + ":" + minutes + ":" + seconds +":"+milliseconds);

        valueInSecond = Integer.parseInt((Integer.parseInt(hours)/60 )+ (Integer.parseInt(minutes)/60) + seconds);
        //((TextView)findViewById(R.id.timerMs)).setText("." + milliseconds);
    }


    //Date

    private void updateDisplay() {
        date.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mMonth + 1).append("-")
                        .append(mDay).append("-")
                        .append(mYear).append(" "));

        PatientResult.instance().setDate("" + String.valueOf(mMonth + 1) + "-" + String.valueOf(mDay) + "-" + String.valueOf(mYear) +" ");
    }

}
