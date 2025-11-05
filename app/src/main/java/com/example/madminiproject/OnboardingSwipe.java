package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.example.madminiproject.viewmodel.MovieRequest;
import com.example.madminiproject.viewmodel.OnboardingViewModel;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnboardingSwipe extends AppCompatActivity implements CardStackListener {

    private DrawerLayout drawerLayout;
    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private OnboardingViewModel viewModel;
    private int swipecounter = 0;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_swipe);

        drawerLayout = findViewById(R.id.drawer_layout);
        cardStackView = findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(this, this);

        // Initialize ExecutorService for background processing
        executorService = Executors.newFixedThreadPool(4);

        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        viewModel.getMovies().observe(this, movies -> {
            if (movies == null || movies.isEmpty()) {
                // If list is empty and adapter doesn't exist, just return
                // If adapter exists and list is empty, don't update (might be clearing in progress)
                if (adapter == null) {
                    return;
                }
                // If adapter exists but list is empty, don't do anything to avoid clearing
                return;
            }
            
            // Get adapter state on main thread before processing on background thread
            CardStackAdapter currentAdapter = adapter;
            int adapterSize = currentAdapter != null ? currentAdapter.getItemCount() : 0;
            List<Movie> existingMovies = currentAdapter != null ? new ArrayList<>(currentAdapter.getSpots()) : new ArrayList<>();
            int viewModelSize = movies.size();
            
            // Process movie list updates on background thread
            executorService.execute(() -> {
                // Create adapter once, then update it with new movies
                // This way recommended movies are appended to initial movies in the ViewModel
                if (currentAdapter == null) {
                    // Create adapter on background thread, but set it on UI thread
                    List<Movie> processedMovies = new ArrayList<>(movies);
                    runOnUiThread(() -> {
                        adapter = new CardStackAdapter(processedMovies);
                        cardStackView.setAdapter(adapter);
                        Log.d("CardStackView", "Initial adapter created with " + processedMovies.size() + " movies");
                    });
                } else {
                    if (viewModelSize > adapterSize) {
                        // New movies were added, process on background thread
                        List<Movie> newMovies = new ArrayList<>(movies.subList(adapterSize, viewModelSize));
                        // Filter duplicates on background thread
                        List<Movie> uniqueNewMovies = filterDuplicates(newMovies, existingMovies);
                        
                        if (!uniqueNewMovies.isEmpty()) {
                            runOnUiThread(() -> {
                                adapter.addMovies(uniqueNewMovies);
                                Log.d("CardStackView", "Added " + uniqueNewMovies.size() + " new movies. Adapter size: " + adapter.getItemCount());
                            });
                        }
                    } else if (viewModelSize < adapterSize) {
                        // List was reduced - this shouldn't happen normally
                        // Only update if we're sure it's a replacement (e.g., adapter has way more items)
                        // Otherwise, ignore to prevent loops
                        if (adapterSize - viewModelSize > 5) {
                            // Significant reduction, likely a reset
                            List<Movie> processedMovies = new ArrayList<>(movies);
                            runOnUiThread(() -> {
                                adapter.setMovies(processedMovies);
                                Log.d("CardStackView", "List significantly reduced, resetting adapter");
                            });
                        }
                        // Otherwise, ignore to prevent loops
                    } else {
                        // Same size - likely a duplicate update, ignore it
                        // Only update if adapter is empty (shouldn't happen, but safety check)
                        if (adapterSize == 0) {
                            List<Movie> processedMovies = new ArrayList<>(movies);
                            runOnUiThread(() -> {
                                adapter.setMovies(processedMovies);
                            });
                        }
                        // Otherwise, ignore same-size updates to prevent loops
                    }
                }
            });
        });


        setupCardStackView();
        setupButton();


        Button continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingSwipe.this, ProfileSelectionActivity.class));
            finish();
        });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onCardDragging(Direction direction, float ratio) {
        Log.d("CardStackView", "onCardDragging: d = " + direction.name() + ", r = " + ratio);
    }

    @Override
    public void onCardSwiped(Direction direction) {
        Log.d("CardStackView", "onCardSwiped: p = " + manager.getTopPosition() + ", d = " + direction);
        
        // Print the movie list
        if (adapter != null && adapter.getSpots() != null) {
            Log.d("CardStackView", "Movie List Size: " + adapter.getSpots().size());
            for (int i = 0; i < adapter.getSpots().size(); i++) {
                Movie movie = adapter.getSpots().get(i);
                Log.d("CardStackView", "Movie[" + i + "]: " + movie.getTitle());
            }
        }
        
        if (direction == Direction.Right){
            Log.d("CardStack", "Swiped Right!");
            int swippedMovieIndex = manager.getTopPosition() - 1;
            if (swippedMovieIndex >= 0 && swippedMovieIndex < adapter.getItemCount()){
                Movie swippedMovie = adapter.getSpots().get(swippedMovieIndex);
                Log.d("CardStack", "Swiped Right on: " + swippedMovie.getTitle());
                viewModel.fetchRecommendedMovies(swippedMovie.getTitle());
            }
        }
        swipecounter++;
        if (swipecounter == 15 || manager.getTopPosition() == adapter.getItemCount()){
            Intent intent = new Intent(OnboardingSwipe.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCardRewound() {
        Log.d("CardStackView", "onCardRewound: " + manager.getTopPosition());
    }

    @Override
    public void onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: " + manager.getTopPosition());
    }

    @Override
    public void onCardAppeared(@NonNull View view, int position) {
        TextView textView = view.findViewById(R.id.item_name);
        Log.d("CardStackView", "onCardAppeared: (" + position + ") " + textView.getText());
    }

    @Override
    public void onCardDisappeared(@NonNull View view, int position) {
        TextView textView = view.findViewById(R.id.item_name);
        Log.d("CardStackView", "onCardDisappeared: (" + position + ") " + textView.getText());
    }

    private void setupCardStackView() {
        manager.setStackFrom(StackFrom.Bottom);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(12.0f);
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(true);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        manager.setOverlayInterpolator(new LinearInterpolator());

        cardStackView.setLayoutManager(manager);
        if (cardStackView.getItemAnimator() instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) cardStackView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
    }

    private void setupButton() {
        View skip = findViewById(R.id.skip_button);
        skip.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            manager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });


        View like = findViewById(R.id.like_button);
        like.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            manager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });
    }
    
    /**
     * Filters duplicate movies from newMovies list by comparing with existing movies.
     * This runs on background thread for better performance.
     */
    private List<Movie> filterDuplicates(List<Movie> newMovies, List<Movie> existingMovies) {
        if (newMovies == null || newMovies.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<String> existingTitles = new HashSet<>();
        for (Movie movie : existingMovies) {
            existingTitles.add(movie.getTitle());
        }
        
        Set<String> seenTitles = new HashSet<>();
        List<Movie> uniqueMovies = new ArrayList<>();
        
        for (Movie newMovie : newMovies) {
            String title = newMovie.getTitle();
            // Only add if not in existing movies and not already seen in newMovies
            if (!existingTitles.contains(title) && seenTitles.add(title)) {
                uniqueMovies.add(newMovie);
            }
        }
        
        return uniqueMovies;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown ExecutorService to prevent memory leaks
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
