package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import java.util.List;

public class OnboardingSwipe extends AppCompatActivity implements CardStackListener {

    private DrawerLayout drawerLayout;
    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private OnboardingViewModel viewModel;
    private int swipecounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding_swipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        cardStackView = findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(this, this);

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
            // Create adapter once, then update it with new movies
            // This way recommended movies are appended to initial movies in the ViewModel
            if (adapter == null) {
                adapter = new CardStackAdapter(new ArrayList<>(movies));
                cardStackView.setAdapter(adapter);
            } else {
                // Compare adapter's current size with ViewModel's size
                int adapterSize = adapter.getItemCount();
                int viewModelSize = movies.size();
                
                if (viewModelSize > adapterSize) {
                    // New movies were added, use incremental update
                    List<Movie> newMovies = movies.subList(adapterSize, viewModelSize);
                    adapter.addMovies(new ArrayList<>(newMovies));
                    Log.d("CardStackView", "Added " + newMovies.size() + " new movies. Adapter size: " + adapter.getItemCount());
                } else if (viewModelSize < adapterSize) {
                    // List was reduced - this shouldn't happen normally
                    // Only update if we're sure it's a replacement (e.g., adapter has way more items)
                    // Otherwise, ignore to prevent loops
                    if (adapterSize - viewModelSize > 5) {
                        // Significant reduction, likely a reset
                        adapter.setMovies(new ArrayList<>(movies));
                        Log.d("CardStackView", "List significantly reduced, resetting adapter");
                    }
                    // Otherwise, ignore to prevent loops
                } else {
                    // Same size - likely a duplicate update, ignore it
                    // Only update if adapter is empty (shouldn't happen, but safety check)
                    if (adapterSize == 0) {
                        adapter.setMovies(new ArrayList<>(movies));
                    }
                    // Otherwise, ignore same-size updates to prevent loops
                }
            }
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
}
