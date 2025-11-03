package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Transition;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.madminiproject.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private MainViewModel mainViewModel;
    private final List<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        adapter = new MoviesAdapter(this, movieList);
        recyclerView.setAdapter(adapter);

        View navbarBottom = findViewById(R.id.navbar_bottom);
        ImageView profileIcon = navbarBottom.findViewById(R.id.navbar_profile_icon);
        ImageView mainProfileAvatar = findViewById(R.id.main_profile_avatar);
        boolean shouldAnimate = getIntent().getBooleanExtra("RUN_AVATAR_ANIMATION", false);

        String avatarUrl = getIntent().getStringExtra("PROFILE_AVATAR_URL");
        if (avatarUrl != null) {
            Glide.with(this).load(avatarUrl).into(mainProfileAvatar);
        }

        View navbar = findViewById(R.id.navbar);
        ImageView searchIcon = navbar.findViewById(R.id.search);
        searchIcon.setOnClickListener(v ->
                startActivity(new Intent(this, Search.class))
        );
        if (shouldAnimate) {
            // Animate avatar into navbar when activity starts
            mainProfileAvatar.postDelayed(() -> {
                int[] iconLoc = new int[2];
                profileIcon.getLocationOnScreen(iconLoc);
                int[] avatarLoc = new int[2];
                mainProfileAvatar.getLocationOnScreen(avatarLoc);

                float targetX = iconLoc[0] - avatarLoc[0];
                float targetY = iconLoc[1] - avatarLoc[1];

                mainProfileAvatar.animate()
                        .scaleX(0.25f)
                        .scaleY(0.25f)
                        .translationX(targetX)
                        .translationY(targetY)
                        .setDuration(600)
                        .start();
            }, 400);
        }

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getMovieList().observe(this, movies -> {
            movieList.clear();
            movieList.addAll(movies);
            adapter.notifyDataSetChanged();
        });
        getIntent().removeExtra("RUN_AVATAR_ANIMATION");
    }
}
