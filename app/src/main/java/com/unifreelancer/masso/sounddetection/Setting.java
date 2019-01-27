package com.unifreelancer.masso.sounddetection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Setting extends AppCompatActivity implements View.OnClickListener{
    Button save;
    EditText minDecibel, maxDecibel;
    private PatientResult resultData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        String min = String.valueOf(PatientResult.instance().getMinDb());
        String max = String.valueOf(PatientResult.instance().getMaxDb());

        minDecibel = (EditText)findViewById(R.id.minDb);
        maxDecibel = (EditText)findViewById(R.id.maxDb);

        minDecibel.setText(min);
        maxDecibel.setText(max);

        save = (Button)findViewById(R.id.btnSaveUpdate);
        save.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == save){
            String minDb;
            String maxDb;

            minDb = minDecibel.getText().toString();
            maxDb = maxDecibel.getText().toString();

            PatientResult.instance().setMinDb(Double.parseDouble(minDb));
            PatientResult.instance().setMaxDb(Double.parseDouble(maxDb));

            Intent intent = new Intent(getApplicationContext(), AnalysisSpeechActivity.class);
            startActivity(intent);

        }
    }
}
