package com.unifreelancer.masso.sounddetection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    Button setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setting = (Button)findViewById(R.id.btnSetting);
        setting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == setting ) {

            Intent intent = new Intent(getApplicationContext(), Setting.class);
            startActivity(intent);
        }
    }
}
