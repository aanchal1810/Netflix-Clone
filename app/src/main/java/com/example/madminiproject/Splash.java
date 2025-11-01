package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            LottieAnimationView animationView = findViewById(R.id.splash);
            animationView.playAnimation();

            int duration = (int) animationView.getDuration();
            if (duration <= 0) duration = 3000;

            new Handler().postDelayed(() -> {
                startActivity(new Intent(Splash.this, GetStarted.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }, duration);
        }
    }
}
