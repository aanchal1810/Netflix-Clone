package com.example.madminiproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madminiproject.Movie;

public class PlayerViewModel extends ViewModel {

    private final MutableLiveData<Movie> movie = new MutableLiveData<>();
    private final MutableLiveData<Long> playbackPosition = new MutableLiveData<>(0L);

    public LiveData<Movie> getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie.setValue(movie);
    }

    public LiveData<Long> getPlaybackPosition() {
        return playbackPosition;
    }

    public void setPlaybackPosition(long position) {
        this.playbackPosition.setValue(position);
    }

    public void toggleFavorite() {
        Movie currentMovie = movie.getValue();
        if (currentMovie != null) {
            currentMovie.setFavorite(!currentMovie.isFavorite());
            movie.setValue(currentMovie);
        }
    }
}
