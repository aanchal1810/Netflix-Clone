package com.example.madminiproject;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.madminiproject.viewmodel.MainViewModel;
import com.example.madminiproject.viewmodel.MainRepository;
import com.example.madminiproject.Profile;
import com.google.firebase.auth.FirebaseAuth;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.madminiproject.MovieResponse;
import java.util.Collections;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    
    // Maps for watchHistory, watchList, and favorites sections
    private RecyclerView watchHistoryRecyclerView;
    private MoviesAdapter watchHistoryAdapter;
    private List<Movie> watchHistoryMovieList;
    
    private RecyclerView watchListRecyclerView;
    private MoviesAdapter watchListAdapter;
    private List<Movie> watchListMovieList;
    
    private RecyclerView favoritesRecyclerView;
    private MoviesAdapter favoritesAdapter;
    private List<Movie> favoritesMovieList;

    private String profileId;
    private FirebaseFirestore db;
    private DocumentReference profileRef;
    private ListenerRegistration profileListener;
    private boolean genreSectionAdded = false;
    private boolean recommendedSectionRepositioned = false;
    private MainRepository repository;
    private ExecutorService executorService;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize mainContainer AFTER setContentView
        mainContainer = findViewById(R.id.mainContainer);



        LinearLayout navbarBottom = findViewById(R.id.bottomNav);
        ImageView profileIcon = navbarBottom.findViewById(R.id.navbar_profile_icon);
        View navbar = findViewById(R.id.navbar);
        
        // Initialize watchedMoviesTitles as empty list - will be populated from Firebase
        watchedMoviesTitles = new ArrayList<>();
        
        // get intent extras
        profileId = getIntent().getStringExtra("PROFILE_ID");
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        repository = new MainRepository();
        
        // Initialize ExecutorService for parallel section loading
        // Using a thread pool with 8 threads for parallel execution
        executorService = Executors.newFixedThreadPool(8);
        
        loadProfileAndWatchHistory();


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

        // Genre section will be added after category sections are loaded from Firebase

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

        // Initialize data sections - observers must be set up on main thread
        initRecyclerAndData();
        initMovieRecRecycler();
        
        // Always show Recommended For You and Genres sections (don't wait for Firebase)
        // Initialize Recommended For You - it will be repositioned after Watch History loads
        addRecommendedForYouSection();
        
        // Always show Genre sections
        if (!genreSectionAdded) {
            executorService.execute(() -> {
                runOnUiThread(() -> {
                    addGenreSection();
                    genreSectionAdded = true;
                });
            });
        }

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

    private void loadProfileAndWatchHistory() {
        if (profileId == null || profileId.isEmpty()) {
            Log.w(TAG, "[MainActivity] profileId is null or empty, cannot load watch history");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.w(TAG, "[MainActivity] User not authenticated, cannot load watch history");
            return;
        }

        profileRef = db.collection("users").document(userId).collection("profiles").document(profileId);
        profileListener = profileRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "[MainActivity] Error loading profile: " + e.getMessage());
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Profile profile = snapshot.toObject(Profile.class);
                if (profile != null) {
                    // Extract all lists from profile
                    List<String> watchHistoryTitles = profile.getWatchHistoryAsList();
                    List<String> watchListTitles = profile.getWatchListAsList();
                    List<String> favoritesTitles = profile.getFavoritesAsList();
                    
                    Log.d(TAG, "[MainActivity] Loaded - Watch History: " + watchHistoryTitles.size() + 
                          ", My List: " + watchListTitles.size() + 
                          ", Favorites: " + favoritesTitles.size() + " movies");
                    
                    // Update watchedMoviesTitles for "Because You Watched" sections
                    List<String> previousWatchedMovies = new ArrayList<>(watchedMoviesTitles);
                    watchedMoviesTitles.clear();
                    watchedMoviesTitles.addAll(watchHistoryTitles);
                    
                    // Update sections reactively - they appear immediately when data changes
                    // 1. Watch History - always update/create if there's data
                    if (!watchHistoryTitles.isEmpty()) {
                        executorService.execute(() -> {
                            runOnUiThread(() -> addWatchHistorySection(watchHistoryTitles));
                        });
                    } else {
                        // Hide section if empty
                        runOnUiThread(() -> removeSectionIfExists("Watch History"));
                    }
                    
                    // 2. Recommended For You (reposition after Watch History if it was just added)
                    executorService.execute(() -> {
                        runOnUiThread(() -> {
                            if (watchHistoryRecyclerView != null) {
                                repositionRecommendedForYouAfterWatchHistory();
                            }
                        });
                    });
                    
                    // 3. My List - create/update immediately when data exists
                    if (!watchListTitles.isEmpty()) {
                        executorService.execute(() -> {
                            runOnUiThread(() -> addWatchListSection(watchListTitles));
                        });
                    } else {
                        // Hide section if empty
                        runOnUiThread(() -> removeSectionIfExists("My List"));
                    }
                    
                    // 4. Favorites - create/update immediately when data exists
                    if (!favoritesTitles.isEmpty()) {
                        executorService.execute(() -> {
                            runOnUiThread(() -> addFavoritesSection(favoritesTitles));
                        });
                    } else {
                        // Hide section if empty
                        runOnUiThread(() -> removeSectionIfExists("Favorites"));
                    }
                    
                    // 5. Because You Watched sections - create immediately for new watched movies
                    // Find newly watched movies
                    Set<String> previousWatchedSet = new HashSet<>(previousWatchedMovies);
                    for (String watchedMovieTitle : watchedMoviesTitles) {
                        final String movieTitle = watchedMovieTitle;
                        // Only create section if it's a new movie or doesn't exist yet
                        if (!previousWatchedSet.contains(movieTitle)) {
                            executorService.execute(() -> {
                                runOnUiThread(() -> {
                                    // Check if section already exists to avoid duplicates
                                    boolean sectionExists = false;
                                    for (int i = 0; i < mainContainer.getChildCount(); i++) {
                                        View child = mainContainer.getChildAt(i);
                                        if (child instanceof TextView) {
                                            TextView titleView = (TextView) child;
                                            if (titleView.getText().toString().contains("Because You Watched " + movieTitle)) {
                                                sectionExists = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!sectionExists) {
                                        addCategorySection(movieTitle);
                                    }
                                });
                            });
                        }
                    }
                    
                    // Remove "Because You Watched" sections for movies that are no longer in watch history
                    for (String previousMovie : previousWatchedMovies) {
                        if (!watchedMoviesTitles.contains(previousMovie)) {
                            final String movieToRemove = previousMovie;
                            runOnUiThread(() -> removeCategorySection("Because You Watched " + movieToRemove));
                        }
                    }
                    
                    // Genre sections are already added in onCreate, no need to add again
                } else {
                    Log.w(TAG, "[MainActivity] Profile object is null");
                }
            } else {
                Log.w(TAG, "[MainActivity] Profile snapshot does not exist - showing default sections");
            }
        });
    }
    
    private void addWatchHistorySection(List<String> movieTitles) {
        if (watchHistoryRecyclerView != null) {
            // Section already exists, just update the movies immediately
            executorService.execute(() -> {
                fetchMoviesForSection(movieTitles, watchHistoryMovieList, watchHistoryAdapter, "Watch History");
            });
            return;
        }
        
        // Create the title TextView
        TextView title = new TextView(this);
        title.setText("Watch History");
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setPadding(16, 24, 0, 8);

        // Create the RecyclerView
        watchHistoryRecyclerView = new RecyclerView(this);
        watchHistoryRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        watchHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        watchHistoryRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        watchHistoryRecyclerView.setClipToPadding(false);

        // Create list and adapter
        watchHistoryMovieList = new ArrayList<>();
        watchHistoryAdapter = new MoviesAdapter(this, watchHistoryMovieList, profileId);
        watchHistoryRecyclerView.setAdapter(watchHistoryAdapter);

        // Add to main container first (UI work)
        mainContainer.addView(title);
        mainContainer.addView(watchHistoryRecyclerView);
        Log.d(TAG, "[MainActivity] Created Watch History section with " + movieTitles.size() + " movies");
        
        // Fetch movies on background thread
        executorService.execute(() -> {
            fetchMoviesForSection(movieTitles, watchHistoryMovieList, watchHistoryAdapter, "Watch History");
        });
    }
    
    private void addRecommendedForYouSection() {
        // Find the existing TextView and RecyclerView from XML
        TextView recommendedTitle = findViewById(R.id.recommendedForYou);
        RecyclerView recommendedRecyclerView = findViewById(R.id.recyclerViewRec);
        
        if (recommendedTitle == null || recommendedRecyclerView == null) {
            Log.w(TAG, "[MainActivity] Recommended For You views not found in XML");
            return;
        }
        
        // Check if they're already in mainContainer
        ViewGroup parent = (ViewGroup) recommendedTitle.getParent();
        if (parent != null && parent.equals(mainContainer)) {
            // Already positioned, no need to do anything
            Log.d(TAG, "[MainActivity] Recommended For You section already in mainContainer");
            return;
        }
        
        // Remove them from their current parent if they have one
        if (parent != null) {
            parent.removeView(recommendedTitle);
            parent.removeView(recommendedRecyclerView);
        }
        
        // If Watch History doesn't exist yet, add at the beginning
        // Otherwise, it will be repositioned when Watch History loads
        int insertIndex = 0;
        
        // Add the views at the beginning (will be repositioned later if Watch History exists)
        mainContainer.addView(recommendedTitle, insertIndex);
        mainContainer.addView(recommendedRecyclerView, insertIndex + 1);
        
        Log.d(TAG, "[MainActivity] Added Recommended For You section at index " + insertIndex);
    }
    
    private void repositionRecommendedForYouAfterWatchHistory() {
        if (recommendedSectionRepositioned) {
            return; // Already repositioned
        }
        
        // Find the existing TextView and RecyclerView
        TextView recommendedTitle = findViewById(R.id.recommendedForYou);
        RecyclerView recommendedRecyclerView = findViewById(R.id.recyclerViewRec);
        
        if (recommendedTitle == null || recommendedRecyclerView == null || watchHistoryRecyclerView == null) {
            return;
        }
        
        // Check if Recommended For You is already after Watch History
        int watchHistoryIndex = -1;
        int recommendedIndex = -1;
        
        for (int i = 0; i < mainContainer.getChildCount(); i++) {
            View child = mainContainer.getChildAt(i);
            if (child.equals(watchHistoryRecyclerView)) {
                watchHistoryIndex = i;
            }
            if (child.equals(recommendedTitle)) {
                recommendedIndex = i;
            }
        }
        
        // If Recommended For You is already right after Watch History, no need to move
        if (watchHistoryIndex >= 0 && recommendedIndex == watchHistoryIndex + 1) {
            recommendedSectionRepositioned = true;
            return;
        }
        
        // Remove from current position
        ViewGroup parent = (ViewGroup) recommendedTitle.getParent();
        if (parent != null && parent.equals(mainContainer)) {
            parent.removeView(recommendedTitle);
            parent.removeView(recommendedRecyclerView);
        }
        
        // Find new position after Watch History
        int insertIndex = watchHistoryIndex + 1;
        
        // Add the views at the correct position
        mainContainer.addView(recommendedTitle, insertIndex);
        mainContainer.addView(recommendedRecyclerView, insertIndex + 1);
        
        recommendedSectionRepositioned = true;
        Log.d(TAG, "[MainActivity] Repositioned Recommended For You section after Watch History at index " + insertIndex);
    }
    
    private void addWatchListSection(List<String> movieTitles) {
        if (watchListRecyclerView != null) {
            // Section already exists, just update the movies immediately
            executorService.execute(() -> {
                fetchMoviesForSection(movieTitles, watchListMovieList, watchListAdapter, "My List");
            });
            return;
        }
        
        // Create the title TextView
        TextView title = new TextView(this);
        title.setText("My List");
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setPadding(16, 24, 0, 8);

        // Create the RecyclerView
        watchListRecyclerView = new RecyclerView(this);
        watchListRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        watchListRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        watchListRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        watchListRecyclerView.setClipToPadding(false);

        // Create list and adapter
        watchListMovieList = new ArrayList<>();
        watchListAdapter = new MoviesAdapter(this, watchListMovieList, profileId);
        watchListRecyclerView.setAdapter(watchListAdapter);

        // Add to main container first (UI work)
        mainContainer.addView(title);
        mainContainer.addView(watchListRecyclerView);
        Log.d(TAG, "[MainActivity] Created My List section with " + movieTitles.size() + " movies");
        
        // Fetch movies on background thread
        executorService.execute(() -> {
            fetchMoviesForSection(movieTitles, watchListMovieList, watchListAdapter, "My List");
        });
    }
    
    private void addFavoritesSection(List<String> movieTitles) {
        if (favoritesRecyclerView != null) {
            // Section already exists, just update the movies immediately
            executorService.execute(() -> {
                fetchMoviesForSection(movieTitles, favoritesMovieList, favoritesAdapter, "Favorites");
            });
            return;
        }
        
        // Create the title TextView
        TextView title = new TextView(this);
        title.setText("Favorites");
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setPadding(16, 24, 0, 8);

        // Create the RecyclerView
        favoritesRecyclerView = new RecyclerView(this);
        favoritesRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        favoritesRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        favoritesRecyclerView.setClipToPadding(false);

        // Create list and adapter
        favoritesMovieList = new ArrayList<>();
        favoritesAdapter = new MoviesAdapter(this, favoritesMovieList, profileId);
        favoritesRecyclerView.setAdapter(favoritesAdapter);

        // Add to main container first (UI work)
        mainContainer.addView(title);
        mainContainer.addView(favoritesRecyclerView);
        Log.d(TAG, "[MainActivity] Created Favorites section with " + movieTitles.size() + " movies");
        
        // Fetch movies on background thread
        executorService.execute(() -> {
            fetchMoviesForSection(movieTitles, favoritesMovieList, favoritesAdapter, "Favorites");
        });
    }
    
    private void fetchMoviesForSection(List<String> movieTitles, List<Movie> movieList, MoviesAdapter adapter, String sectionName) {
        if (movieTitles == null || movieTitles.isEmpty()) {
            Log.w(TAG, "[MainActivity] " + sectionName + ": No movie titles to fetch");
            return;
        }

        List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(movieTitles);
        final int totalCalls = movieTitleCalls.size();
        final List<Movie> fetchedMovies = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};

        Log.d(TAG, "[MainActivity] " + sectionName + ": Starting " + totalCalls + " API calls for movie posters");

        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                String moviename = body.getResults().get(0).getTitle();
                                String movieposterpath = body.getResults().get(0).getFullPosterUrl();
                                fetchedMovies.add(new Movie(moviename, movieposterpath));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[MainActivity] " + sectionName + ": Error parsing TMDB response", e);
                        }
                    }

                    // Check if all calls are completed
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // All calls completed, remove duplicates using HashSet for O(n) performance
                            Set<String> seenTitles = new HashSet<>();
                            List<Movie> uniqueMovies = new ArrayList<>();
                            for (Movie movie : fetchedMovies) {
                                if (seenTitles.add(movie.getTitle())) {
                                    uniqueMovies.add(movie);
                                }
                            }

                            // Update the list and adapter on UI thread
                            runOnUiThread(() -> {
                                movieList.clear();
                                movieList.addAll(uniqueMovies);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "[MainActivity] " + sectionName + ": ✅ Added " + uniqueMovies.size() + " unique movies (from " + movieTitles.size() + " titles)");
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "[MainActivity] " + sectionName + ": TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // Even if some failed, post what we have (avoiding duplicates)
                            if (!fetchedMovies.isEmpty()) {
                                // Remove duplicates using HashSet for O(n) performance
                                Set<String> seenTitles = new HashSet<>();
                                List<Movie> uniqueMovies = new ArrayList<>();
                                for (Movie movie : fetchedMovies) {
                                    if (seenTitles.add(movie.getTitle())) {
                                        uniqueMovies.add(movie);
                                    }
                                }

                                runOnUiThread(() -> {
                                    movieList.clear();
                                    movieList.addAll(uniqueMovies);
                                    adapter.notifyDataSetChanged();
                                    Log.d(TAG, "[MainActivity] " + sectionName + ": ✅ Added " + uniqueMovies.size() + " unique movies (some API calls failed)");
                                });
                            } else {
                                runOnUiThread(() -> {
                                    movieList.clear();
                                    adapter.notifyDataSetChanged();
                                    Log.w(TAG, "[MainActivity] " + sectionName + ": ⚠️ No movies were successfully fetched");
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    private void removeSectionIfExists(String sectionTitle) {
        // Find and remove section by title
        for (int i = 0; i < mainContainer.getChildCount(); i++) {
            View child = mainContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView titleView = (TextView) child;
                if (titleView.getText().toString().equals(sectionTitle)) {
                    // Remove title and corresponding RecyclerView
                    mainContainer.removeView(titleView);
                    if (i < mainContainer.getChildCount()) {
                        View nextChild = mainContainer.getChildAt(i);
                        if (nextChild instanceof RecyclerView) {
                            mainContainer.removeView(nextChild);
                        }
                    }
                    // Reset references
                    if (sectionTitle.equals("Watch History")) {
                        watchHistoryRecyclerView = null;
                        watchHistoryAdapter = null;
                        watchHistoryMovieList = null;
                    } else if (sectionTitle.equals("My List")) {
                        watchListRecyclerView = null;
                        watchListAdapter = null;
                        watchListMovieList = null;
                    } else if (sectionTitle.equals("Favorites")) {
                        favoritesRecyclerView = null;
                        favoritesAdapter = null;
                        favoritesMovieList = null;
                    }
                    Log.d(TAG, "[MainActivity] Removed section: " + sectionTitle);
                    break;
                }
            }
        }
    }
    
    private void removeCategorySection(String sectionTitleText) {
        // Find and remove "Because You Watched" section by title
        for (int i = 0; i < mainContainer.getChildCount(); i++) {
            View child = mainContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView titleView = (TextView) child;
                if (titleView.getText().toString().equals(sectionTitleText)) {
                    // Remove title and corresponding RecyclerView
                    mainContainer.removeView(titleView);
                    if (i < mainContainer.getChildCount()) {
                        View nextChild = mainContainer.getChildAt(i);
                        if (nextChild instanceof RecyclerView) {
                            mainContainer.removeView(nextChild);
                            // Also remove from genre maps if it exists there
                            RecyclerView recyclerView = (RecyclerView) nextChild;
                            String genreKey = null;
                            for (Map.Entry<String, RecyclerView> entry : genreRecyclerViewMap.entrySet()) {
                                if (entry.getValue().equals(recyclerView)) {
                                    genreKey = entry.getKey();
                                    break;
                                }
                            }
                            if (genreKey != null) {
                                genreRecyclerViewMap.remove(genreKey);
                                genreAdapterMap.remove(genreKey);
                                genreMovieListMap.remove(genreKey);
                            }
                        }
                    }
                    Log.d(TAG, "[MainActivity] Removed category section: " + sectionTitleText);
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) {
            profileListener.remove();
        }
        // Shutdown ExecutorService to prevent memory leaks
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
