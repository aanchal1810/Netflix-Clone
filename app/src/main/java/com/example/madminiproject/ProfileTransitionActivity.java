package com.example.madminiproject;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class ProfileTransitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_profile_transition);

        ImageView avatar = findViewById(R.id.transition_avatar);

        // Pull values
        String avatarUrl = getIntent().getStringExtra("PROFILE_AVATAR_URL");
        String transitionName = getIntent().getStringExtra("TRANSITION_NAME");

        avatar.setTransitionName(transitionName); // must match before transition starts

        if (avatarUrl != null) {
            Glide.with(this).load(avatarUrl).into(avatar);
        }

        avatar.postDelayed(() -> {
            avatar.animate()
                    .setDuration(600)
                    .withEndAction(() -> goToMainActivity(avatarUrl))
                    .start();
        }, 300);
    }
    private void goToMainActivity(String avatarUrl) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("PROFILE_AVATAR_URL", avatarUrl);
        intent.putExtra("RUN_AVATAR_ANIMATION", true);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(
                this, android.R.anim.fade_in, android.R.anim.fade_out
        );
        startActivity(intent, options.toBundle());
        finish();
    }
}