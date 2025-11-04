package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.madminiproject.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestOptions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private MainViewModel mainViewModel;
    private final List<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Tell system to wait for us
        supportPostponeEnterTransition();

        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.setSharedElementEnterTransition(makeArcMotionTransition());
        window.setSharedElementExitTransition(makeArcMotionTransition());

        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        LinearLayout navbarBottom = findViewById(R.id.bottomNav);
        ImageView profileIcon = navbarBottom.findViewById(R.id.navbar_profile_icon);
        View navbar = findViewById(R.id.navbar);

        // basic recycler setup (adapter but don't fill data yet)
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        adapter = new MoviesAdapter(this, movieList);
        recyclerView.setAdapter(adapter);

        // get intent extras
        boolean shouldAnimate = getIntent().getBooleanExtra("RUN_AVATAR_ANIMATION", false);
        String avatarUrl = getIntent().getStringExtra("PROFILE_AVATAR_URL");
        int bgResId = getIntent().getIntExtra("PROFILE_BG_RES_ID", -1);
        String transitionName = getIntent().getStringExtra("TRANSITION_NAME");

        Log.d(TAG, "onCreate: shouldAnimate=" + shouldAnimate + " transitionName=" + transitionName + " bgResId=" + bgResId);

        if (shouldAnimate && transitionName != null) {
            profileIcon.setTransitionName(transitionName);
            Log.d(TAG, "Set profileIcon transitionName: " + profileIcon.getTransitionName());
        }

        // Load avatar - important to notify when ready
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Log.d(TAG, "Loading avatarUrl via Glide");
            Glide.with(this)
                    .load(avatarUrl)
                    .apply(new RequestOptions().dontAnimate())
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            Log.w(TAG, "Glide load failed - starting postponed enter transition", e);
                            supportStartPostponedEnterTransition();
                            return false; // allow Glide to handle placeholder
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "Glide resource ready - starting postponed enter transition");
                            supportStartPostponedEnterTransition();
                            return false;
                        }
                    })
                    .into(profileIcon);
        } else {
            if (bgResId != -1) profileIcon.setBackgroundResource(bgResId);
            else profileIcon.setBackgroundResource(R.drawable.profile_pink);
            // view is ready — start the postponed transition
            profileIcon.post(() -> {
                Log.d(TAG, "No avatarUrl - starting postponed enter transition after post()");
                supportStartPostponedEnterTransition();
            });
        }

        // initialize data (ViewModel) - safe to do while transition plays
        initRecyclerAndData();

        // Search nav (no shared element) - keep simple fade
        ImageView searchIcon = navbar.findViewById(R.id.search);
        searchIcon.setOnClickListener(v -> {
            // explicitly disable shared element transitions for this navigation path
            getWindow().setExitTransition(null);
            getWindow().setEnterTransition(null);
            startActivity(new Intent(this, Search.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Prevent re-run flag
        getIntent().removeExtra("RUN_AVATAR_ANIMATION");
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

    private void initRecyclerAndData() {
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getMovieList().observe(this, movies -> {
            movieList.clear();
            movieList.addAll(movies);
            adapter.notifyDataSetChanged();
        });
    }
}
