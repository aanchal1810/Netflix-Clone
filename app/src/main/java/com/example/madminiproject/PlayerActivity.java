package com.example.madminiproject;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
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

    private static final String DUMMY_VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        playerView = findViewById(R.id.player_view);
        backdropImage = findViewById(R.id.backdrop_image);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                ImageButton exoPlay = findViewById(R.id.exo_play);
                ImageButton exoPause = findViewById(R.id.exo_pause);
                View videoSurface = playerView.getVideoSurfaceView();

                if (isPlaying) {
                    exoPlay.setVisibility(View.GONE);
                    exoPause.setVisibility(View.VISIBLE);
                    backdropImage.animate().alpha(0f).setDuration(500);
                    if (videoSurface != null) {
                        videoSurface.setVisibility(View.VISIBLE);
                    }
                } else {
                    exoPlay.setVisibility(View.VISIBLE);
                    exoPause.setVisibility(View.GONE);
                    backdropImage.animate().alpha(1f).setDuration(500);
                    if (videoSurface != null) {
                        videoSurface.setVisibility(View.GONE);
                    }
                }
            }
        });

        castContext = CastContext.getSharedInstance(this);

        String movieUrl = getIntent().getStringExtra("movie_url");
        if (movieUrl != null) {
            String movieTitle = getIntent().getStringExtra("movie_title");
            preparePlayer(movieUrl, movieTitle);
        } else {
            movie = (Movie) getIntent().getSerializableExtra("movie");
            playerViewModel.setMovie(movie);
            playerViewModel.getMovie().observe(this, this::preparePlayer);
        }

        initializeCustomControls();
    }

    private void preparePlayer(Movie movie) {
        if (movie == null) return;

        preparePlayer(DUMMY_VIDEO_URL, movie.getTitle());

        TextView descriptionView = findViewById(R.id.video_description);
        descriptionView.setText(movie.getOverview());

        Glide.with(this)
                .load(movie.getFullBackdropUrl())
                .into(backdropImage);
    }

    private void preparePlayer(String videoUrl, String title) {
        TextView titleView = findViewById(R.id.video_title);
        titleView.setText(title);

        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle(title)
                .build();

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaMetadata(mediaMetadata)
                .build();

        player.setMediaItem(mediaItem);
        player.prepare();

        Long playbackPosition = playerViewModel.getPlaybackPosition().getValue();
        if (playbackPosition != null) {
            player.seekTo(playbackPosition);
        }

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
