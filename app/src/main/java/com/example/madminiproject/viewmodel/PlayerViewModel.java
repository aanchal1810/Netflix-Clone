package com.example.madminiproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madminiproject.Movie;

public class PlayerViewModel extends ViewModel {

    private final MutableLiveData<Movie> movie = new MutableLiveData<>();

    public LiveData<Movie> getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie.setValue(movie);
    }
}
