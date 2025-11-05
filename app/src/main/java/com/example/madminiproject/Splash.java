package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.madminiproject.viewmodel.SplashViewModel;

public class Splash extends AppCompatActivity {

    private SplashViewModel splashViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        LottieAnimationView animationView = findViewById(R.id.splash);
        animationView.playAnimation();

        int duration = (int) animationView.getDuration();
        if (duration <= 0) duration = 3000;

        new Handler().postDelayed(() -> {
            splashViewModel.checkUserLoggedIn();
        }, duration);

        splashViewModel.getIsUserLoggedIn().observe(this, isUserLoggedIn -> {
            if (isUserLoggedIn) {
                startActivity(new Intent(Splash.this, ProfileSelectionActivity.class));
            } else {
                startActivity(new Intent(Splash.this, GetStarted.class));
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }
}
