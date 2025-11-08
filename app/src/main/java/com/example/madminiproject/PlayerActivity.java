package com.example.madminiproject;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.mediarouter.app.MediaRouteButton;

import com.bumptech.glide.Glide;
import com.example.madminiproject.viewmodel.PlayerViewModel;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;

public class PlayerActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private PlayerView playerView;
    private ExoPlayer player;
    private CastContext castContext;
    private Movie movie;
    private ImageView backdropImage;
    private String profileId;

    private static final String DUMMY_VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerView = findViewById(R.id.player_view);
        backdropImage = findViewById(R.id.backdrop_image);

        profileId=getIntent().getStringExtra("profileId");
        if (profileId != null && !profileId.isEmpty()) {
            playerViewModel.loadProfile(profileId);
        }

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(DemoUtil.getDataSourceFactory(this)))
                .build();
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        playerView.setPlayer(player);

        // 🔹 Get Movie object
        movie = (Movie) getIntent().getSerializableExtra("movie");
        if (movie == null) {
            // fallback dummy movie
            movie = new Movie("Movie", null, null, "No overview available.");
        }

        // 🔹 Use local URI if available, else online
        Uri mediaUri = movie.getLocalUri() != null ? Uri.parse(movie.getLocalUri()) : Uri.parse(DUMMY_VIDEO_URL);
        preparePlayer(mediaUri, movie);

        initializeCustomControls();
    }
    private void preparePlayer(Uri uri, Movie movie) {
        // Title and description
        ((TextView)findViewById(R.id.video_title)).setText(movie.getTitle());
        ((TextView)findViewById(R.id.video_description)).setText(movie.getOverview());

        // Backdrop
        if (movie.getFullBackdropUrl() != null) {
            Glide.with(this).load(movie.getFullBackdropUrl()).into(backdropImage);
        }

        // Play video
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(new MediaMetadata.Builder().setTitle(movie.getTitle()).build())
                .build();

        player.setMediaItem(mediaItem);
        player.prepare();

        Long pos = playerViewModel.getPlaybackPosition().getValue();
        if (pos != null) player.seekTo(pos);
        player.play();
    }

    private void initializeCustomControls() {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        ImageButton exoRew = findViewById(R.id.exo_rew);
        exoRew.setOnClickListener(v -> player.seekTo(player.getCurrentPosition() - 10000));

        ImageButton exoFfwd = findViewById(R.id.exo_ffwd);
        exoFfwd.setOnClickListener(v -> player.seekTo(player.getCurrentPosition() + 10000));

        ImageButton exoPlay = findViewById(R.id.exo_play);
        exoPlay.setOnClickListener(v -> player.play());

        ImageButton exoPause = findViewById(R.id.exo_pause);
        exoPause.setOnClickListener(v -> player.pause());


        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    // Video is playing, show PAUSE, hide PLAY
                    exoPause.setVisibility(View.VISIBLE);
                    exoPlay.setVisibility(View.GONE);
                } else {
                    // Video is paused, show PLAY, hide PAUSE
                    exoPause.setVisibility(View.GONE);
                    exoPlay.setVisibility(View.VISIBLE);
                }
            }
        });

        SeekBar volumeSlider = findViewById(R.id.volume_slider);
        ImageView volumeIcon = findViewById(R.id.volume_icon);

        volumeSlider.setMax(100);
        volumeSlider.setProgress((int) (player.getVolume() * 100));

        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float volume = progress / 100f;
                    player.setVolume(volume);
                    if (volume == 0) {
                        volumeIcon.setImageResource(R.drawable.ic_volume_off_white_24dp);
                    } else {
                        volumeIcon.setImageResource(R.drawable.ic_volume_up_white_24dp);
                    }
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        ImageButton playbackSpeedButton = findViewById(R.id.playback_speed_button);
        playbackSpeedButton.setOnClickListener(v -> showPlaybackSpeedDialog());

        MediaRouteButton mediaRouteButton = findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
    }

    private void showPlaybackSpeedDialog() {
        final String[] speeds = {"0.5x", "1x", "1.5x", "2x"};
        final float[] speedValues = {0.5f, 1f, 1.5f, 2f};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Playback Speed");
        builder.setItems(speeds, (dialog, which) -> {
            player.setPlaybackParameters(new PlaybackParameters(speedValues[which]));
        });
        builder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        playerView.onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            playerViewModel.setPlaybackPosition(player.getCurrentPosition());
            if (movie != null) {
                System.out.println("Pause");
                playerViewModel.saveWatchHistory(movie.getTitle(), player.getCurrentPosition());
            }
            player.pause();
        }
    }

    @Override
protected void onStop() {
        super.onStop();
        playerView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            if (movie != null) {
                System.out.println("Destroy");
                playerViewModel.saveWatchHistory(movie.getTitle(), player.getCurrentPosition());
            }
            player.release();
            player = null;
        }
    }

    private void hideSystemUi() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}