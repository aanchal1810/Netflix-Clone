package com.example.madminiproject.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

public class OnboardingViewModel extends AndroidViewModel {

    private static final String TAG = "OnboardingViewModel";
    private String profileID;

    private final MutableLiveData<List<Movie>> movies = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> movietitles = new MutableLiveData<>();
    private final MainRepository repository = new MainRepository();
    private boolean isFetchingRecommended = false;

    public OnboardingViewModel(@NonNull Application application) {
        super(application);
        // Read profile ID from SharedPreferences
        profileID = application.getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                .getString("PROFILE_ID", null);
        Log.d(TAG, "Profile ID loaded from SharedPreferences: " + profileID);
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
        // Prevent concurrent fetches
        if (isFetchingRecommended) {
            Log.v(TAG, "Already fetching recommended movies, ignoring duplicate request");
            return;
        }
        
        // Reload profileID from SharedPreferences to ensure we have the latest value
        profileID = getApplication().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                .getString("PROFILE_ID", null);
        
        if (profileID == null) {
            Log.w(TAG, "Profile ID is null. Cannot fetch recommended movies without profile ID.");
            isFetchingRecommended = false;
            return;
        }
        
        Log.d(TAG, "Using Profile ID for recommendations: " + profileID);
        isFetchingRecommended = true;
        // Create MovieRequest with profileID to send to backend
        MovieRequest movieRequest = new MovieRequest(moviename, profileID);
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
                    isFetchingRecommended = false; // Reset flag on failure
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
                isFetchingRecommended = false; // Reset flag on failure
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
                            
                            // First, remove duplicates within recommendedMovies itself
                            List<Movie> uniqueRecommendedMovies = new ArrayList<>();
                            for (Movie movie : recommendedMovies) {
                                boolean isDuplicate = false;
                                for (Movie existing : uniqueRecommendedMovies) {
                                    if (existing.getTitle().equals(movie.getTitle())) {
                                        isDuplicate = true;
                                        break;
                                    }
                                }
                                if (!isDuplicate) {
                                    uniqueRecommendedMovies.add(movie);
                                }
                            }
                            
                            // Then, filter out duplicates against current list
                            for (Movie newMovie : uniqueRecommendedMovies) {
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
                            
                            int previousSize = movies.getValue() != null ? movies.getValue().size() : 0;
                            movies.postValue(currentList);
                            Log.v(TAG, "Added " + (currentList.size() - previousSize) + " recommended movies (duplicates filtered)");
                            isFetchingRecommended = false; // Reset flag after completion
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
                                
                                // First, remove duplicates within recommendedMovies itself
                                List<Movie> uniqueRecommendedMovies = new ArrayList<>();
                                for (Movie movie : recommendedMovies) {
                                    boolean isDuplicate = false;
                                    for (Movie existing : uniqueRecommendedMovies) {
                                        if (existing.getTitle().equals(movie.getTitle())) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }
                                    if (!isDuplicate) {
                                        uniqueRecommendedMovies.add(movie);
                                    }
                                }
                                
                                // Then, filter out duplicates against current list
                                for (Movie newMovie : uniqueRecommendedMovies) {
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
                            isFetchingRecommended = false; // Reset flag after completion
                        }
                    }
                }
            });
        }
    }
    private void fetchInitialMovies() {
        Log.v(TAG, "Fetching initial movies...");
        
        // Don't clear the list here - let it be set directly when initial movies arrive
        // This prevents unnecessary observer triggers that could cause loops

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
