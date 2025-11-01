package com.example.madminiproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;

import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_swipe);
        EdgeToEdge.enable(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        cardStackView = findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(this, this);
        adapter = new CardStackAdapter(createSpots());

        setupCardStackView();
        manager.setStackFrom(StackFrom.Bottom);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(12.0f);
        setupButton();
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
        if (manager.getTopPosition() == adapter.getItemCount() - 5) {
            paginate();
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
        initialize();
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

    private void initialize() {
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
        cardStackView.setAdapter(adapter);

        if (cardStackView.getItemAnimator() instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) cardStackView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
    }

    private void paginate() {
        List<Movie> old = adapter.getSpots();
        List<Movie> newList = new ArrayList<>(old);
        newList.addAll(createSpots());
        MovieDiffCallBack callback = new MovieDiffCallBack(old, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setMovies(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void reload() {
        List<Movie> old = adapter.getSpots();
        List<Movie> newList = createSpots();
        MovieDiffCallBack callback = new MovieDiffCallBack(old, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setMovies(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void addFirst(int size) {
        List<Movie> old = adapter.getSpots();
        List<Movie> newList = new ArrayList<>(old);
        for (int i = 0; i < size; i++) {
            newList.add(manager.getTopPosition(), createSpot());
        }
        MovieDiffCallBack callback = new MovieDiffCallBack(old, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setMovies(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void addLast(int size) {
        List<Movie> old = adapter.getSpots();
        List<Movie> newList = new ArrayList<>(old);
        for (int i = 0; i < size; i++) {
            newList.add(createSpot());
        }
        MovieDiffCallBack callback = new MovieDiffCallBack(old, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setMovies(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeFirst(int size) {
        List<Movie> old = adapter.getSpots();
        if (old.isEmpty()) return;
        List<Movie> newList = new ArrayList<>(old);
        for (int i = 0; i < size && manager.getTopPosition() < newList.size(); i++) {
            newList.remove(manager.getTopPosition());
        }
        MovieDiffCallBack callback = new MovieDiffCallBack(old, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setMovies(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeLast(int size) {
        List<Movie> old = adapter.getSpots();
        if (old.isEmpty()) return;
        List<Movie> newList = new ArrayList<>(old);
        for (int i = 0; i < size && !newList.isEmpty(); i++) {
            newList.remove(newList.size() - 1);
        }
        MovieDiffCallBack callback = new MovieDiffCallBack(old, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setMovies(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void replace() {
        List<Movie> old = adapter.getSpots();
        List<Movie> newList = new ArrayList<>(old);
        if (!newList.isEmpty()) {
            newList.set(manager.getTopPosition(), createSpot());
        }
        adapter.setMovies(newList);
        adapter.notifyItemChanged(manager.getTopPosition());
    }

    private void swap() {
        List<Movie> old = adapter.getSpots();
        List<Movie> newList = new ArrayList<>(old);
        if (newList.size() > 1) {
            Movie first = newList.remove(manager.getTopPosition());
            Movie last = newList.remove(newList.size() - 1);
            newList.add(manager.getTopPosition(), last);
            newList.add(first);
        }
        MovieDiffCallBack callback = new MovieDiffCallBack(old, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setMovies(newList);
        result.dispatchUpdatesTo(adapter);
    }

    //dummy data, arhaan add your part of the code here
    private Movie createSpot() {
        return new Movie("Yasaka Shrine", "https://source.unsplash.com/Xq1ntWruZQI/600x800");
    }

    //dummy data, arhaan add your part of the code here
    private List<Movie> createSpots() {
        List<Movie> spots = new ArrayList<>();
        spots.add(new Movie("Moana", "https://i.pinimg.com/1200x/4a/d6/c0/4ad6c0738cb7b23bc9dac91e1d37d770.jpg"));
        spots.add(new Movie("Frozen", "https://i.pinimg.com/736x/c5/7a/a1/c57aa1543487d2bcb69c0217bead64a8.jpg"));
        spots.add(new Movie("Tangled", "https://i.pinimg.com/736x/3c/69/31/3c69316b0386a0548947b68408446472.jpg"));
        spots.add(new Movie("Confessions of a Shopaholic", "https://i.pinimg.com/736x/79/d1/15/79d115e00256c8b89446f39a9a618dd7.jpg"));
        spots.add(new Movie("The devil wears the Prada", "https://i.pinimg.com/1200x/50/6a/e1/506ae1e81ceaafa792c8642fde804298.jpg"));
        spots.add(new Movie("Mama Mia!", "https://i.pinimg.com/1200x/ea/a7/13/eaa7133c088208a12df356c0f9d4120f.jpg"));
        spots.add(new Movie("Monte Carlo", "https://i.pinimg.com/1200x/f4/ab/67/f4ab6785a255b37e8431d3aa12875f82.jpg"));
        spots.add(new Movie("27 Dresses", "https://i.pinimg.com/736x/71/65/57/7165578b8b040424fe6e847ed5c8cca3.jpg"));
        spots.add(new Movie("Bride Wars", "https://i.pinimg.com/736x/9b/f7/44/9bf744c77dcf1192c721ba229e0d3c26.jpg"));
        spots.add(new Movie("13 Going on 30", "https://i.pinimg.com/736x/fa/26/46/fa2646d5fccc0d1a4f95efc667fc2b14.jpg"));
        return spots;
    }
}