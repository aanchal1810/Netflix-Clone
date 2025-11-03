package com.example.madminiproject.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madminiproject.Movie;
import com.example.madminiproject.MovieResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingViewModel extends ViewModel {

    private static final String TAG = "OnboardingViewModel";

    private final MutableLiveData<List<Movie>> movies = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> movietitles = new MutableLiveData<>();
    private final MainRepository repository = new MainRepository();

    public OnboardingViewModel() {
        fetchInitialMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public LiveData<List<String>> getMovieTitles() {
        return movietitles;
    }

    /**
     * Fetches a list of initial movie titles from your repository, then for each title,
     * makes a TMDB API call to fetch its poster and details.
     */
    private void fetchInitialMovies() {
        Log.v(TAG, "Fetching initial movies...");

        Call<List<String>> call = repository.getInitialMovie();

        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> titles = response.body();
                    movietitles.postValue(titles);
                    Log.v(TAG, "Initial movie titles: " + titles);

                    // Get a list of API calls for each title
                    List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(Objects.requireNonNull(titles));

                    for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
                        fetchMoviePoster(moviePosterCall);
                    }

                } else {
                    movietitles.postValue(null);
                    Log.e(TAG, "Failed to fetch initial movie titles. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                movietitles.postValue(null);
                Log.e(TAG, "Failed to fetch initial movies: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Fetches a movie’s details/poster from TMDB and updates LiveData.
     */
    private void fetchMoviePoster(Call<MovieResponse> moviePosterCall) {
        moviePosterCall.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        MovieResponse body = response.body();
                        if (body.getResults() != null && !body.getResults().isEmpty()) {
                            String moviename = body.getResults().get(0).getTitle();
                            String movieposterpath = body.getResults().get(0).getFullPosterUrl();

                            Log.v(TAG, "Movie Name: " + moviename);
                            Log.v(TAG, "Poster URL: " + movieposterpath);

                            // Add to current movie list and post the updated list
                            List<Movie> currentList = movies.getValue();
                            if (currentList == null) currentList = new ArrayList<>();
                            currentList.add(new Movie(moviename, movieposterpath));
                            movies.postValue(currentList);
                        } else {
                            Log.w(TAG, "No results found in TMDB response.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing TMDB response", e);
                    }
                } else {
                    Log.e(TAG, "TMDB API Response failed. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e(TAG, "TMDB API Call failed: " + t.getMessage(), t);
                if (t instanceof java.net.UnknownHostException) {
                    Log.e(TAG, "Network error: Cannot reach TMDB API. Check internet connection.");
                } else if (t instanceof javax.net.ssl.SSLException) {
                    Log.e(TAG, "SSL error: Problem with HTTPS connection to TMDB.");
                }
            }
        });
    }
}
