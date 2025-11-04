package com.example.madminiproject;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProfileTransitionActivity extends AppCompatActivity {

    private static final String TAG = "ProfileTransitionActivity";

    private int bgResId;
    private String avatarUrl;
    private boolean isImageAvatar;
    private boolean hasStartedMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.setSharedElementEnterTransition(makeArcMotionTransition());
        window.setSharedElementExitTransition(makeArcMotionTransition());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_transition);

        ImageView avatar = findViewById(R.id.transition_avatar);
        Intent i = getIntent();

        avatarUrl = i.getStringExtra("PROFILE_AVATAR_URL");
        bgResId = i.getIntExtra("PROFILE_BG_RES_ID", -1);
        isImageAvatar = i.getBooleanExtra("IS_IMAGE_AVATAR", false);
        String transitionName = i.getStringExtra("TRANSITION_NAME");

        Log.d(TAG, "Intent extras: avatarUrl=" + (avatarUrl != null ? "[URL]" : "null")
                + " bgResId=" + bgResId + " isImageAvatar=" + isImageAvatar
                + " transitionName=" + transitionName);

        // IMPORTANT: set transition name early
        if (transitionName != null) {
            avatar.setTransitionName(transitionName);
            Log.d(TAG, "Set transitionName on avatar: " + avatar.getTransitionName());
        }

        // Load image/background immediately (use Glide to ensure caching)
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl).into(avatar);
        } else if (bgResId != -1) {
            avatar.setBackgroundResource(bgResId);
        } else {
            avatar.setBackgroundResource(R.drawable.profile_pink);
        }

        // Play a small center animation then navigate to Main using shared element transition
        avatar.postDelayed(() -> {
            Log.d(TAG, "Starting enlarge animation and then goToMainActivity()");
            avatar.animate()
                    .setDuration(500)
                    .withEndAction(this::goToMainActivity)
                    .start();
        }, 200);
    }

    private void goToMainActivity() {
        if (hasStartedMain) {
            Log.d(TAG, "goToMainActivity: already started, returning");
            return;
        }
        hasStartedMain = true;

        ImageView avatar = findViewById(R.id.transition_avatar);
        String transitionName = avatar.getTransitionName();
        Log.d(TAG, "goToMainActivity: avatar transitionName=" + transitionName + " bgResId=" + bgResId);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("PROFILE_AVATAR_URL", avatarUrl);
        intent.putExtra("PROFILE_BG_RES_ID", bgResId);
        intent.putExtra("IS_IMAGE_AVATAR", isImageAvatar);
        intent.putExtra("TRANSITION_NAME", transitionName);
        intent.putExtra("RUN_AVATAR_ANIMATION", true);

        // Make sure exit/shared reenter transitions are set to same arc motion
        getWindow().setExitTransition(makeArcMotionTransition());
        getWindow().setSharedElementReenterTransition(makeArcMotionTransition());

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                avatar,
                transitionName
        );
        Log.d(TAG, "Starting MainActivity with shared element: " + transitionName);
        startActivity(intent, options.toBundle());
        // allow system to finish transition cleanly
        finishAfterTransition();
    }

    private Transition makeArcMotionTransition() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(60f);
        arcMotion.setMinimumVerticalAngle(60f);

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setPathMotion(arcMotion);
        changeBounds.setDuration(700);
        changeBounds.setInterpolator(new AccelerateDecelerateInterpolator());
        return changeBounds;
    }
}
