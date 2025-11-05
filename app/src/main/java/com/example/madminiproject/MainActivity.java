package com.example.madminiproject;

import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestOptions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView,recyclerViewRec;
    private MoviesAdapter adapter,adapterrec;
    private MainViewModel mainViewModel;
    private LinearLayout mainContainer;
    private List<String> watchedMoviesTitles;
    private final List<Movie> movieList = new ArrayList<>(), recmovielist = new ArrayList<>(),watchedMovies = new ArrayList<>();
    private final Map<String, RecyclerView> genreRecyclerViewMap = new HashMap<>();
    private final Map<String, MoviesAdapter> genreAdapterMap = new HashMap<>();
    private final Map<String, List<Movie>> genreMovieListMap = new HashMap<>();

    private String profileId;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Initialize mainContainer AFTER setContentView
        mainContainer = findViewById(R.id.mainContainer);



        LinearLayout navbarBottom = findViewById(R.id.bottomNav);
        ImageView profileIcon = navbarBottom.findViewById(R.id.navbar_profile_icon);
        View navbar = findViewById(R.id.navbar);
        watchedMoviesTitles = Arrays.asList(
                "Avatar",
                "Stitches",
                "1982"
        );
        // get intent extras
        profileId = getIntent().getStringExtra("PROFILE_ID");


        // get data from naitik
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        adapter = new MoviesAdapter(this, movieList,profileId);
        recyclerView.setAdapter(adapter);

        recyclerViewRec = findViewById(R.id.recyclerViewRec);
        recyclerViewRec.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        adapterrec = new MoviesAdapter(this, recmovielist,profileId);
        recyclerViewRec.setAdapter(adapterrec);

        for (String watchedMoviesTitle : watchedMoviesTitles){
            addCategorySection(watchedMoviesTitle);
        }
        addGenreSection();

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
        initMovieRecRecycler();

        // Search nav (no shared element) - keep simple fade
        ImageView searchIcon = navbar.findViewById(R.id.search);
        searchIcon.setOnClickListener(v -> {
            // explicitly disable shared element transitions for this navigation path
            getWindow().setExitTransition(null);
            getWindow().setEnterTransition(null);
            startActivity(new Intent(this, Search.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        profileIcon.setOnClickListener(v -> {
            Intent goToAccount = new Intent(this, ProfilePageActivity.class);
            goToAccount.putExtra("PROFILE_AVATAR_URL1", avatarUrl);
            goToAccount.putExtra("PROFILE_BG_RES_ID1", bgResId);
            startActivity(goToAccount);
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
        Log.d(TAG, "[MainActivity] Setting up RecyclerView 1 observer");
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getMovieList().observe(this, movies -> {
            if (movies != null) {
                Log.d(TAG, "[MainActivity] RecyclerView 1 received " + movies.size() + " movies from ViewModel");
                movieList.clear();
                movieList.addAll(movies);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "[MainActivity] RecyclerView 1 adapter notified. Current list size: " + movieList.size());
            } else {
                Log.w(TAG, "[MainActivity] RecyclerView 1 received null movies list");
            }
        });
    }
    private void addGenreSection() {
        // Get ViewModel and observe data for this specific section
        if (mainViewModel == null) {
            mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        }
        mainViewModel.getGenreMovie().observe(this, movies -> {
            if (movies != null) {
                for (Map.Entry<String, List<Movie>> entry : movies.entrySet()) {
                    String genre = entry.getKey();
                    List<Movie> movieObject = entry.getValue();

                    // Check if this genre section already exists
                    if (genreRecyclerViewMap.containsKey(genre)) {
                        // Update existing RecyclerView
                        List<Movie> sectionMovieList = genreMovieListMap.get(genre);
                        MoviesAdapter adapter = genreAdapterMap.get(genre);
                        if (sectionMovieList != null && adapter != null) {
                            sectionMovieList.clear();
                            sectionMovieList.addAll(movieObject);
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "[MainActivity] Updated existing genre section: " + genre + " with " + movieObject.size() + " movies");
                        }
                    } else {
                        // Create new genre section
                        TextView title = new TextView(this);
                        title.setText(genre);
                        title.setTextSize(18);
                        title.setTypeface(title.getTypeface(), Typeface.BOLD);
                        title.setPadding(16, 24, 0, 8);

                        // Create the RecyclerView
                        RecyclerView recyclerView = new RecyclerView(this);
                        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
                        recyclerView.setClipToPadding(false);

                        // Each RecyclerView needs its own list and adapter
                        List<Movie> sectionMovieList = new ArrayList<>();
                        MoviesAdapter sectionAdapter = new MoviesAdapter(this, sectionMovieList,profileId);
                        recyclerView.setAdapter(sectionAdapter);

                        // Store references for future updates
                        genreRecyclerViewMap.put(genre, recyclerView);
                        genreAdapterMap.put(genre, sectionAdapter);
                        genreMovieListMap.put(genre, sectionMovieList);

                        sectionMovieList.clear();
                        sectionMovieList.addAll(movieObject);
                        sectionAdapter.notifyDataSetChanged();
                        mainContainer.addView(title);
                        mainContainer.addView(recyclerView);
                        Log.d(TAG, "[MainActivity] Created new genre section: " + genre + " with " + movieObject.size() + " movies");
                    }
                }
            } else {
                Log.w(TAG, "[MainActivity] Genre sections received null movies map");
            }
        });
    }
    private void addCategorySection(String titleText) {
        // Create the title TextView
        TextView title = new TextView(this);
        title.setText("Because You Watched " + titleText);
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setPadding(16, 24, 0, 8);

        // Create the RecyclerView
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setClipToPadding(false);

        // Each RecyclerView needs its own list and adapter
        List<Movie> sectionMovieList = new ArrayList<>();
        MoviesAdapter sectionAdapter = new MoviesAdapter(this, sectionMovieList,profileId);
        recyclerView.setAdapter(sectionAdapter);

        // Get ViewModel and observe data for this specific section
        if (mainViewModel == null) {
            mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        }
        mainViewModel.getWatchedRecMovieList(titleText).observe(this, movies -> {
            if (movies != null) {
                Log.d(TAG, "[MainActivity] RecyclerView section '" + titleText + "' received " + movies.size() + " movies from ViewModel");
                sectionMovieList.clear();
                sectionMovieList.addAll(movies);
                sectionAdapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "[MainActivity] RecyclerView section '" + titleText + "' received null movies list");
            }
        });

        // Add to main container
        mainContainer.addView(title);
        mainContainer.addView(recyclerView);
    }
    private void initMovieRecRecycler() {
        Log.d(TAG, "[MainActivity] Setting up RecyclerView 2 observer");
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getRecMovieList().observe(this, movies -> {
            if (movies != null) {
                Log.d(TAG, "[MainActivity] RecyclerView 2 received " + movies.size() + " movies from ViewModel");
                recmovielist.clear();
                recmovielist.addAll(movies);
                adapterrec.notifyDataSetChanged();
                Log.d(TAG, "[MainActivity] RecyclerView 2 adapter notified. Current list size: " + recmovielist.size());
            } else {
                Log.w(TAG, "[MainActivity] RecyclerView 2 received null movies list");
            }
        });
    }
}
