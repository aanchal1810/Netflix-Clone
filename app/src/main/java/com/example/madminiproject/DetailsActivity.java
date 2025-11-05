package com.example.madminiproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadCursor;
import androidx.media3.exoplayer.offline.DownloadHelper;
import androidx.media3.exoplayer.offline.DownloadIndex;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadRequest;
import androidx.media3.exoplayer.offline.DownloadService;

import com.bumptech.glide.Glide;
import com.example.madminiproject.viewmodel.PlayerViewModel;
import com.example.madminiproject.viewmodel.SearchViewModel;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@OptIn(markerClass = UnstableApi.class)
public class DetailsActivity extends AppCompatActivity implements DownloadTracker.Listener {

    private ImageView movieBackdrop;
    private TextView movieOverview;
    private Button playButton;
    private Button downloadButton;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private TextView favoriteButton;
    private TextView watchListButton;
    private PlayerViewModel playerViewModel;
    private SearchViewModel searchViewModel;
    private CastContext mCastContext;
    private DownloadTracker downloadTracker;
    private MediaItem mediaItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        downloadTracker = DemoUtil.getDownloadTracker(this);
        downloadTracker.addListener(this);


        mCastContext = CastContext.getSharedInstance(this);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        movieBackdrop = findViewById(R.id.detail_movie_backdrop);
        movieOverview = findViewById(R.id.detail_movie_overview);
        playButton = findViewById(R.id.play_button);
        downloadButton = findViewById(R.id.download_button);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        favoriteButton = findViewById(R.id.favorite_button);
        watchListButton = findViewById(R.id.watchlist_button);



        searchViewModel.getCurrentProfile().observe(this, profile -> {
            if (profile != null && playerViewModel.getMovie().getValue() != null) {
                Movie movie = playerViewModel.getMovie().getValue();
                boolean isWatchlisted = profile.getWatchListAsList().contains(movie.getTitle());
                movie.setWatchlisted(isWatchlisted);
                updateWatchlistButton(isWatchlisted);
            }
        });


        Movie movie = (Movie) getIntent().getSerializableExtra("movie");
        String profileId = getIntent().getStringExtra("profileId");

        // Hardcoded for demo
        String videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));

        if (movie != null) {
            playerViewModel.setMovie(movie);
            if (profileId != null) {
                playerViewModel.loadProfile(profileId);
                searchViewModel.loadProfile(profileId);
            }

            playerViewModel.getMovie().observe(this, observedMovie -> {
                if (observedMovie != null) {
                    collapsingToolbar.setTitle(observedMovie.getTitle());
                    movieOverview.setText(observedMovie.getOverview());

                    Glide.with(this)
                            .load(observedMovie.getFullBackdropUrl())
                            .into(movieBackdrop);

                    updateFavoriteButton(observedMovie.isFavorite());
                    updateWatchlistButton(observedMovie.isWatchlisted());
                }
            });

            favoriteButton.setOnClickListener(v -> playerViewModel.toggleFavorite());
            watchListButton.setOnClickListener(v -> {
                Movie movieVal = playerViewModel.getMovie().getValue();
                if (movieVal == null) return;

                boolean currentlyWatchlisted = movieVal.isWatchlisted();
                boolean newState = !currentlyWatchlisted;

                movieVal.setWatchlisted(newState);
                updateWatchlistButton(newState);

                searchViewModel.toggleWatchList(movieVal.getTitle());
            });


            playButton.setOnClickListener(v -> {
                Movie movieObj = playerViewModel.getMovie().getValue();
                if (movieObj == null) return;

                Uri onlineUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
                Uri finalUri = onlineUri;

                // 🔹 Check if already downloaded
                DownloadManager downloadManager = DemoUtil.getDownloadManager(this);
                DownloadIndex downloadIndex = downloadManager.getDownloadIndex();

                try (DownloadCursor cursor = downloadIndex.getDownloads()) {
                    while (cursor.moveToNext()) {
                        Download download = cursor.getDownload();
                        if (download.request.uri.equals(onlineUri) && download.state == Download.STATE_COMPLETED) {
                            // This is a downloaded movie
                            DownloadRequest request = download.request;
                            Uri downloadedUri = download.request.uri; // ExoPlayer handles local cache URIs automatically
                            finalUri = downloadedUri;
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(DetailsActivity.this, PlayerActivity.class);
                intent.putExtra("movie", movieObj);
                intent.putExtra("profileId", profileId);
                intent.putExtra("mediaItemUri", finalUri.toString());
                startActivity(intent);
            });

            downloadButton.setOnClickListener(v -> downloadMovie());
        }
        updateDownloadButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadTracker.removeListener(this);
    }

    @Override
    public void onDownloadsChanged() {
        updateDownloadButtonState();
    }


    private void updateDownloadButtonState() {
        new Thread(() -> {
            Download downloadFound = null;
            DownloadManager downloadManager = DemoUtil.getDownloadManager(this);
            DownloadIndex downloadIndex = downloadManager.getDownloadIndex();

            try (DownloadCursor cursor = downloadIndex.getDownloads()) {
                while (cursor.moveToNext()) {
                    Download download = cursor.getDownload();
                    if (download.request.uri.equals(mediaItem.localConfiguration.uri)) {
                        downloadFound = download;
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Download finalDownloadFound = downloadFound;
            runOnUiThread(() -> updateButtonWithDownload(finalDownloadFound));
        }).start();
    }


    private void updateButtonWithDownload(@Nullable Download download) {
        if (download != null) {
            switch (download.state) {
                case Download.STATE_DOWNLOADING:
                    float percent = download.getPercentDownloaded();
                    String progress = "Downloading...";
                    if (percent != -1) {
                        progress = String.format(Locale.getDefault(), "Downloading: %.1f%%", percent);
                    }
                    downloadButton.setText(progress);
                    downloadButton.setEnabled(true); // Allow pausing
                    break;
                case Download.STATE_COMPLETED:
                    downloadButton.setText("Delete Download");
                    downloadButton.setEnabled(true);
                    break;
                case Download.STATE_QUEUED:
                    downloadButton.setText("Download Queued");
                    downloadButton.setEnabled(false);
                    break;
                case Download.STATE_STOPPED:
                    downloadButton.setText("Download Paused");
                    downloadButton.setEnabled(true);
                    break;
                case Download.STATE_FAILED:
                    downloadButton.setText("Download Failed. Retry?");
                    downloadButton.setEnabled(true);
                    break;
                case Download.STATE_REMOVING:
                    downloadButton.setText("Deleting...");
                    downloadButton.setEnabled(false);
                    break;
                default: // Includes STATE_RESTARTED
                    downloadButton.setText("Download");
                    downloadButton.setEnabled(true);
                    break;
            }
        } else {
            downloadButton.setText("Download");
            downloadButton.setEnabled(true);
        }
    }



    private void downloadMovie() {
        Movie movie = playerViewModel.getMovie().getValue();
        if (movie == null) return;

        RenderersFactory renderersFactory = DemoUtil.buildRenderersFactory(this, false);

        try {
            // 🔹 Create metadata JSON
            JSONObject data = new JSONObject();
            data.put("title", movie.getTitle());
            data.put("overview", movie.getOverview());
            data.put("thumbnail", movie.getFullBackdropUrl());

            // 🔹 Build MediaItem with URI
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
                    .setMediaId(movie.getTitle())
                    .build();

            // 🔹 Prepare DownloadHelper
            DownloadHelper downloadHelper = DownloadHelper.forMediaItem(
                    this,
                    mediaItem,
                    renderersFactory,
                    DemoUtil.getDataSourceFactory(this)
            );

            downloadHelper.prepare(new DownloadHelper.Callback() {
                @Override
                public void onPrepared(DownloadHelper helper, boolean tracksInfoAvailable) {
                    // Add metadata to request
                    DownloadRequest request = helper.getDownloadRequest(
                            data.toString().getBytes(StandardCharsets.UTF_8)
                    );

                    DownloadService.sendAddDownload(
                            DetailsActivity.this,
                            DemoDownloadService.class,
                            request,
                            true
                    );
                    helper.release();
                }

                @Override
                public void onPrepareError(DownloadHelper helper, IOException e) {
                    helper.release();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setText("Added");
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_red_24dp, 0, 0);
        } else {
            favoriteButton.setText("My List");
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_border_red_24dp, 0, 0);
        }
    }

    private void updateWatchlistButton(boolean isWatchlisted) {
        if (isWatchlisted) {
            watchListButton.setText("Added");
            watchListButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_watchlist_added_24dp, 0, 0);
        } else {
            watchListButton.setText("Add to Watch Later");
            watchListButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_watchlist_add_24dp, 0, 0);
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

}