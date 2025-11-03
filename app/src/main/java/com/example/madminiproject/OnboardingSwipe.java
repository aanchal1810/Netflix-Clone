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
import java.util.List;

public class OnboardingSwipe extends AppCompatActivity implements CardStackListener {

    private DrawerLayout drawerLayout;
    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private OnboardingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_swipe);

        drawerLayout = findViewById(R.id.drawer_layout);
        cardStackView = findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(this, this);

        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        final int[] previousSize = {0};
        viewModel.getMovies().observe(this, movies -> {
            if (movies == null || movies.isEmpty()) {
                return;
            }
            // Create adapter once, then update it with new movies
            // This way recommended movies are appended to initial movies in the ViewModel
            if (adapter == null) {
                adapter = new CardStackAdapter(movies);
                cardStackView.setAdapter(adapter);
                previousSize[0] = movies.size();
            } else {
                // If new movies were added (incremental update), use addMovies for efficiency
                // Otherwise, use setMovies for full replacement
                if (movies.size() > previousSize[0]) {
                    // New movies were added, use incremental update
                    List<Movie> newMovies = movies.subList(previousSize[0], movies.size());
                    adapter.addMovies(new ArrayList<>(newMovies));
                    previousSize[0] = movies.size();
                } else {
                    // Full list replacement (shouldn't happen often, but handle it)
                    adapter.setMovies(movies);
                    previousSize[0] = movies.size();
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
