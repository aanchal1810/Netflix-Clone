package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView animationView = findViewById(R.id.splash);
        animationView.playAnimation();

        int duration = (int) animationView.getDuration(); // get length of Lottie anim
        if (duration <= 0) duration = 3000; // fallback if Lottie doesn't report duration

        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, GetStarted.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, duration);
    }
}
