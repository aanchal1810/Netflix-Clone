package com.example.madminiproject;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.madminiproject.viewmodel.PlayerViewModel;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DetailsActivity extends AppCompatActivity {

    private ImageView movieBackdrop;
    private TextView movieOverview;
    private Button playButton;
    private Button downloadButton;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private TextView favoriteButton;
    private PlayerViewModel playerViewModel;
    private CastContext mCastContext;
    private long downloadID;

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                Toast.makeText(DetailsActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCastContext = CastContext.getSharedInstance(this);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        movieBackdrop = findViewById(R.id.detail_movie_backdrop);
        movieOverview = findViewById(R.id.detail_movie_overview);
        playButton = findViewById(R.id.play_button);
        downloadButton = findViewById(R.id.download_button);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        favoriteButton = findViewById(R.id.favorite_button);

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED);

        Movie movie = (Movie) getIntent().getSerializableExtra("movie");

        if (movie != null) {
            playerViewModel.setMovie(movie);

            playerViewModel.getMovie().observe(this, observedMovie -> {
                if (observedMovie != null) {
                    collapsingToolbar.setTitle(observedMovie.getTitle());
                    movieOverview.setText(observedMovie.getOverview());

                    Glide.with(this)
                            .load(observedMovie.getFullBackdropUrl())
                            .into(movieBackdrop);

                    updateFavoriteButton(observedMovie.isFavorite());
                }
            });

            favoriteButton.setOnClickListener(v -> playerViewModel.toggleFavorite());

            playButton.setOnClickListener(v -> {
                Intent intent = new Intent(DetailsActivity.this, PlayerActivity.class);
                intent.putExtra("movie", playerViewModel.getMovie().getValue());
                startActivity(intent);
            });

            downloadButton.setOnClickListener(v -> downloadMovie());
        }
    }

    private void downloadMovie() {
        Movie movie = playerViewModel.getMovie().getValue();
        if (movie == null) return;

        String videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

        // Save movie metadata as a .json file
        try {
            String json = new Gson().toJson(movie);
            String jsonFileName = movie.getTitle() + ".json";
            File jsonFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), jsonFileName);
            try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
                fos.write(json.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save movie metadata", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
        request.setTitle(movie.getTitle());
        request.setDescription("Downloading...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String fileName = movie.getTitle() + ".mp4";
        File destinationFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName);
        request.setDestinationUri(Uri.fromFile(destinationFile));

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = manager.enqueue(request);
        Toast.makeText(this, "Download Started", Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_red_24dp, 0, 0);
        } else {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_border_red_24dp, 0, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }
}
