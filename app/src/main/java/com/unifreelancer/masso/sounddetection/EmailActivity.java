package com.unifreelancer.masso.sounddetection;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class EmailActivity extends AppCompatActivity {

    EditText email, addRec, subject, message;
    String result;

    final Calendar c = Calendar.getInstance();
    private int mYear = c.get(Calendar.YEAR);
    private int mMonth = c.get(Calendar.MONTH);
    private int mDay = c.get(Calendar.DAY_OF_MONTH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        email = (EditText)findViewById(R.id.inEmail);
        addRec = (EditText)findViewById(R.id.inCC);
        subject = (EditText)findViewById(R.id.inSubject);
        message  = (EditText)findViewById(R.id.inMessage);


        String today = "" + String.valueOf(mMonth + 1) + "-" + String.valueOf(mDay) + "-" + String.valueOf(mYear) +" ";

        String oldDate = today;
        String oldScore = String.valueOf(PatientResult.instance().getScore());
        String oldAvrSpeech = String.valueOf(PatientResult.instance().getAvrSpeech());
        String oldDuration = String.valueOf(PatientResult.instance().getDuration());




        result = "\n Date : "+ oldDate +"\n Score : " + oldScore +
                "\n Average Speech : " + oldAvrSpeech +"\n Duration Speech : " + oldDuration + "seconds";


        message.setText(result);

        Button startBtn = (Button) findViewById(R.id.sendEmail);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {



                sendEmail();
            }
        });
    }

    protected void sendEmail() {
        Log.i("Send email", "");
        String em = email.getText().toString();
        String cc = addRec.getText().toString();
        String sub = subject.getText().toString();
        String mess = message.getText().toString();


        String[] TO = {em};
        String[] CC = {cc};
        String SUB = sub;
        String MES = mess;



        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, SUB);
        emailIntent.putExtra(Intent.EXTRA_TEXT, MES);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(EmailActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
