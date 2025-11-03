package com.example.madminiproject.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madminiproject.Movie;
import com.example.madminiproject.MovieResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    public void fetchRecommendedMovies(String moviename){
        MovieRequest movieRequest = new MovieRequest(moviename);
        Call<List<String>> call = repository.getRecommendedMovie(movieRequest);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> titles = response.body();
                    movietitles.postValue(titles);
                    Log.v(TAG, "Recommended movie titles: " + titles);

                    // Get a list of API calls for each title
                    List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(Objects.requireNonNull(titles));
                    
                    // Collect recommended movies and post them in a batch
                    fetchRecommendedMoviePosters(movieTitleCalls);

                } else {
                    movietitles.postValue(null);
                    Log.e(TAG, "Failed to fetch recommended movie titles. Code: " + response.code());
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
                Log.e(TAG, "Failed to fetch recommended movies: " + t.getMessage(), t);
            }
        });
    }
    
    /**
     * Fetches recommended movie posters and batches them before updating the list.
     * This prevents multiple refreshes by posting all recommended movies at once.
     */
    private void fetchRecommendedMoviePosters(List<Call<MovieResponse>> movieTitleCalls) {
        if (movieTitleCalls == null || movieTitleCalls.isEmpty()) {
            return;
        }
        
        final int totalCalls = movieTitleCalls.size();
        final List<Movie> recommendedMovies = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};
        
        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                String moviename = body.getResults().get(0).getTitle();
                                String movieposterpath = body.getResults().get(0).getFullPosterUrl();
                                
                                Log.v(TAG, "Recommended Movie Name: " + moviename);
                                Log.v(TAG, "Recommended Poster URL: " + movieposterpath);
                                
                                recommendedMovies.add(new Movie(moviename, movieposterpath));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing TMDB response", e);
                        }
                    }
                    
                    // Check if all calls are completed
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // All calls completed, append all recommended movies at once (avoiding duplicates)
                            List<Movie> currentList = movies.getValue();
                            if (currentList == null) currentList = new ArrayList<>();
                            
                            // Filter out duplicates by movie title
                            for (Movie newMovie : recommendedMovies) {
                                boolean isDuplicate = false;
                                for (Movie existingMovie : currentList) {
                                    if (existingMovie.getTitle().equals(newMovie.getTitle())) {
                                        isDuplicate = true;
                                        break;
                                    }
                                }
                                if (!isDuplicate) {
                                    currentList.add(newMovie);
                                }
                            }
                            
                            movies.postValue(currentList);
                            Log.v(TAG, "Added recommended movies (duplicates filtered) to the list");
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // Even if some failed, post what we have (avoiding duplicates)
                            if (!recommendedMovies.isEmpty()) {
                                List<Movie> currentList = movies.getValue();
                                if (currentList == null) currentList = new ArrayList<>();
                                
                                // Filter out duplicates by movie title
                                for (Movie newMovie : recommendedMovies) {
                                    boolean isDuplicate = false;
                                    for (Movie existingMovie : currentList) {
                                        if (existingMovie.getTitle().equals(newMovie.getTitle())) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }
                                    if (!isDuplicate) {
                                        currentList.add(newMovie);
                                    }
                                }
                                
                                movies.postValue(currentList);
                                Log.v(TAG, "Added recommended movies (some may have failed, duplicates filtered)");
                            }
                        }
                    }
                }
            });
        }
    }
    private void fetchInitialMovies() {
        Log.v(TAG, "Fetching initial movies...");
        
        // Clear the movies list first to prevent duplicates
        movies.postValue(new ArrayList<>());

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
                    
                    // Batch initial movies similar to recommended movies
                    fetchInitialMoviePosters(movieTitleCalls);

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
     * Fetches initial movie posters and batches them before updating the list.
     * This prevents duplicates and multiple refreshes by posting all initial movies at once.
     */
    private void fetchInitialMoviePosters(List<Call<MovieResponse>> movieTitleCalls) {
        if (movieTitleCalls == null || movieTitleCalls.isEmpty()) {
            return;
        }
        
        final int totalCalls = movieTitleCalls.size();
        final List<Movie> initialMovies = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};
        
        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                String moviename = body.getResults().get(0).getTitle();
                                String movieposterpath = body.getResults().get(0).getFullPosterUrl();
                                
                                Log.v(TAG, "Initial Movie Name: " + moviename);
                                Log.v(TAG, "Initial Poster URL: " + movieposterpath);
                                
                                initialMovies.add(new Movie(moviename, movieposterpath));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing TMDB response", e);
                        }
                    }
                    
                    // Check if all calls are completed
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // All calls completed, remove duplicates and set all initial movies at once
                            List<Movie> uniqueMovies = new ArrayList<>();
                            for (Movie movie : initialMovies) {
                                boolean isDuplicate = false;
                                for (Movie existing : uniqueMovies) {
                                    if (existing.getTitle().equals(movie.getTitle())) {
                                        isDuplicate = true;
                                        break;
                                    }
                                }
                                if (!isDuplicate) {
                                    uniqueMovies.add(movie);
                                }
                            }
                            movies.postValue(uniqueMovies);
                            Log.v(TAG, "Added " + uniqueMovies.size() + " initial movies to the list (duplicates filtered)");
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // Even if some failed, post what we have (removing duplicates)
                            if (!initialMovies.isEmpty()) {
                                List<Movie> uniqueMovies = new ArrayList<>();
                                for (Movie movie : initialMovies) {
                                    boolean isDuplicate = false;
                                    for (Movie existing : uniqueMovies) {
                                        if (existing.getTitle().equals(movie.getTitle())) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }
                                    if (!isDuplicate) {
                                        uniqueMovies.add(movie);
                                    }
                                }
                                movies.postValue(uniqueMovies);
                                Log.v(TAG, "Added " + uniqueMovies.size() + " initial movies (some may have failed, duplicates filtered)");
                            }
                        }
                    }
                }
            });
        }
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
