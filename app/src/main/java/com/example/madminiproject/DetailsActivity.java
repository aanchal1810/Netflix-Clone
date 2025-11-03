package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.madminiproject.viewmodel.PlayerViewModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class DetailsActivity extends AppCompatActivity {

    private ImageView movieThumbnail;
    private TextView movieOverview;
    private Button playButton;
    private Button downloadButton;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private TextView favoriteButton;
    private PlayerViewModel playerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        movieThumbnail = findViewById(R.id.detail_movie_thumbnail);
        movieOverview = findViewById(R.id.detail_movie_overview);
        playButton = findViewById(R.id.play_button);
        downloadButton = findViewById(R.id.download_button);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        favoriteButton = findViewById(R.id.favorite_button);

        Movie movie = (Movie) getIntent().getSerializableExtra("movie");

        if (movie != null) {
            playerViewModel.setMovie(movie);

            playerViewModel.getMovie().observe(this, observedMovie -> {
                if (observedMovie != null) {
                    collapsingToolbar.setTitle(observedMovie.getTitle());
                    movieOverview.setText(observedMovie.getOverview());

                    Glide.with(this)
                            .load(observedMovie.getPosterPath())
                            .into(movieThumbnail);

                    updateFavoriteButton(observedMovie.isFavorite());
                }
            });

            favoriteButton.setOnClickListener(v -> playerViewModel.toggleFavorite());

            playButton.setOnClickListener(v -> {
                Intent intent = new Intent(DetailsActivity.this, PlayerActivity.class);
                intent.putExtra("movie", playerViewModel.getMovie().getValue());
                startActivity(intent);
            });

            downloadButton.setOnClickListener(v -> {
                // TODO: Implement download functionality
                Toast.makeText(DetailsActivity.this, "Download button clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_red_24dp, 0, 0);
        } else {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_border_red_24dp, 0, 0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
