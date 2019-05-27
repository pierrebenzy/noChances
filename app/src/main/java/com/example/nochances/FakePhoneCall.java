package com.example.nochances;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.nochances.Model.Intents;
import com.example.nochances.utils.ImageTouchSlider;

public class FakePhoneCall extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fake_phone_call);

        ImageTouchSlider slider = findViewById(R.id.phone_call_answer_slider);
        slider.setOnImageSliderChangedListener(new ImageTouchSlider.OnImageSliderChangedListener() {

            @Override
            public void onChanged() {
                Intent intent = Intents.FakePhoneCallToMapsActivity(FakePhoneCall.this);
                startActivity(intent);
            }

        });
    }
}
