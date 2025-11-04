package com.example.madminiproject.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.madminiproject.BuildConfig;
import com.example.madminiproject.Movie;
import com.example.madminiproject.MovieResponse;
import com.example.madminiproject.TmdbApi;
import com.example.madminiproject.TmdbClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "MainViewModel";


    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final Pattern CSV_SPLIT = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private final MainRepository repository = new MainRepository();
    private final MutableLiveData<List<Movie>> movieList = new MutableLiveData<>();
    private final MutableLiveData<List<String>> moveTitleFinalRec = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> recMovieList = new MutableLiveData<>();

    private final TmdbApi tmdbApi;

    public MainViewModel(@NonNull Application application) {
        super(application);

        tmdbApi = TmdbClient.getInstance();


        loadMovies();
    }

    public LiveData<List<Movie>> getMovieList() {
        return movieList;
    }

    private void loadMovies() {
        List<String> titles = loadMovieTitlesFromAssets();
        fetchMoviesFromApi(titles);
    }

    private List<String> loadMovieTitlesFromAssets() {
        List<String> titles = new ArrayList<>();
        try {
            InputStream is = getApplication().getAssets().open("tmdb_5000_movies.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String headerLine = reader.readLine();
            if (headerLine == null) return titles;

            String[] headers = CSV_SPLIT.split(headerLine, -1);
            int titleIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].replaceAll("^\"|\"$", "").trim().equalsIgnoreCase("title")) {
                    titleIndex = i;
                    break;
                }
            }

            if (titleIndex == -1) {
                Log.e(TAG, "title column not found in CSV");
                return titles;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = CSV_SPLIT.split(line, -1);
                if (parts.length > titleIndex) {
                    String rawTitle = parts[titleIndex].replaceAll("^\"|\"$", "").trim();
                    if (!rawTitle.isEmpty()) titles.add(rawTitle);
                }
                if (titles.size() >= 20) break; // safety limit
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read CSV: " + e.getMessage());
        }
        return titles;
    }

    private void fetchMoviesFromApi(List<String> titles) {
        if (titles == null || titles.isEmpty()) {
            Log.e(TAG, "[RecyclerView 1] No titles to fetch");
            return;
        }

        Log.d(TAG, "[RecyclerView 1] Starting to fetch " + titles.size() + " movies from API");
        List<Movie> newMovies = new ArrayList<>();
        int[] callCount = {0};
        for (String title : titles) {
            Log.d(TAG, "[RecyclerView 1] Making API call for movie: " + title);
            Call<MovieResponse> call = tmdbApi.searchMovies(BuildConfig.TMDB_API_KEY, title);
            call.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    callCount[0]++;
                    if (response.isSuccessful() && response.body() != null) {
                        List<Movie> results = response.body().getResults();
                        Log.v(TAG, "[RecyclerView 1] API Response for " + title + ": " + response.body());
                        if (results != null && !results.isEmpty()) {
                            Movie movie = results.get(0);
                            Log.d(TAG, "[RecyclerView 1] Successfully fetched: " + movie.getTitle() + " (Poster: " + movie.getFullPosterUrl() + ")");
                            newMovies.add(movie);
                            movieList.setValue(new ArrayList<>(newMovies));
                            Log.d(TAG, "[RecyclerView 1] Updated movieList with " + newMovies.size() + " movies. Total calls completed: " + callCount[0] + "/" + titles.size());
                        } else {
                            Log.w(TAG, "[RecyclerView 1] No results found for: " + title);
                        }
                    } else {
                        Log.e(TAG, "[RecyclerView 1] API call failed for " + title + " - Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    callCount[0]++;
                    Log.e(TAG, "[RecyclerView 1] API call failed for " + title + ": " + t.getMessage(), t);
                    Log.d(TAG, "[RecyclerView 1] Total calls completed: " + callCount[0] + "/" + titles.size());
                }
            });
        }
    }
    public void getRecMovies(){
        Log.d(TAG, "[RecyclerView 2] getRecMovies() called - Starting API call to fetch initial movie titles");
        Call<List<String>> call = repository.getFinalRec();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> titles = response.body();
                    moveTitleFinalRec.postValue(titles);
                    Log.d(TAG, "[RecyclerView 2] Successfully received " + titles.size() + " initial movie titles: " + titles);

                    // Get a list of API calls for each title
                    Log.d(TAG, "[RecyclerView 2] Starting to fetch movie posters for " + titles.size() + " titles");
                    List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(Objects.requireNonNull(titles));
                    Log.d(TAG, "[RecyclerView 2] Created " + movieTitleCalls.size() + " API calls for movie posters");

                    // Batch initial movies similar to recommended movies
                    fetchFinalRecMoviePosters(movieTitleCalls);

                } else {
                    moveTitleFinalRec.postValue(null);
                    Log.e(TAG, "[RecyclerView 2] Failed to fetch initial movie titles. Response code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "[RecyclerView 2] Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "[RecyclerView 2] Failed to read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                moveTitleFinalRec.postValue(null);
                Log.e(TAG, "[RecyclerView 2] Failed to fetch initial movies: " + t.getMessage(), t);
            }
        });
    }
    private void fetchFinalRecMoviePosters(List<Call<MovieResponse>> movieTitleCalls) {
        if (movieTitleCalls == null || movieTitleCalls.isEmpty()) {
            Log.w(TAG, "[RecyclerView 2] No movie poster calls to fetch");
            return;
        }

        final int totalCalls = movieTitleCalls.size();
        final List<Movie> finalRecMovies = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};
        final int[] successCount = {0};
        final int[] failureCount = {0};

        Log.d(TAG, "[RecyclerView 2] Starting " + totalCalls + " parallel API calls for movie posters");
        int callIndex = 0;
        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            final int currentIndex = callIndex++;
            Log.d(TAG, "[RecyclerView 2] Initiating API call #" + (currentIndex + 1) + "/" + totalCalls);
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                String moviename = body.getResults().get(0).getTitle();
                                String movieposterpath = body.getResults().get(0).getFullPosterUrl();

                                Log.d(TAG, "[RecyclerView 2] Successfully fetched movie #" + (currentIndex + 1) + ": " + moviename);
                                Log.v(TAG, "[RecyclerView 2] Poster URL: " + movieposterpath);

                                finalRecMovies.add(new Movie(moviename, movieposterpath));
                                synchronized (successCount) {
                                    successCount[0]++;
                                }
                            } else {
                                Log.w(TAG, "[RecyclerView 2] API call #" + (currentIndex + 1) + " returned empty results");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[RecyclerView 2] Error parsing TMDB response for call #" + (currentIndex + 1), e);
                        }
                    } else {
                        Log.e(TAG, "[RecyclerView 2] API call #" + (currentIndex + 1) + " failed - Response code: " + response.code());
                    }

                    // Check if all calls are completed
                    synchronized (completedCount) {
                        completedCount[0]++;
                        int currentFailures = completedCount[0] - successCount[0];
                        Log.d(TAG, "[RecyclerView 2] Progress: " + completedCount[0] + "/" + totalCalls + " calls completed (Success: " + successCount[0] + ", Failed: " + currentFailures + ")");
                        
                        if (completedCount[0] == totalCalls) {
                            // All calls completed, remove duplicates and set all initial movies at once
                            Log.d(TAG, "[RecyclerView 2] All API calls completed. Processing " + finalRecMovies.size() + " movies (removing duplicates)");
                            List<Movie> uniqueMovies = new ArrayList<>();
                            for (Movie movie : finalRecMovies) {
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
                            recMovieList.postValue(uniqueMovies);
                            Log.d(TAG, "[RecyclerView 2] ✅ Updated RecyclerView with " + uniqueMovies.size() + " unique movies (filtered from " + finalRecMovies.size() + " total)");
                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "[RecyclerView 2] API call #" + (currentIndex + 1) + " failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        failureCount[0]++;
                        Log.d(TAG, "[RecyclerView 2] Progress: " + completedCount[0] + "/" + totalCalls + " calls completed (Success: " + successCount[0] + ", Failed: " + failureCount[0] + ")");
                        
                        if (completedCount[0] == totalCalls) {
                            // Even if some failed, post what we have (removing duplicates)
                            if (!finalRecMovies.isEmpty()) {
                                Log.d(TAG, "[RecyclerView 2] All calls completed (some failed). Processing " + finalRecMovies.size() + " successful movies");
                                List<Movie> uniqueMovies = new ArrayList<>();
                                for (Movie movie : finalRecMovies) {
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
                                recMovieList.postValue(uniqueMovies);
                                Log.d(TAG, "[RecyclerView 2] ✅ Updated RecyclerView with " + uniqueMovies.size() + " unique movies (some API calls failed)");
                            } else {
                                Log.w(TAG, "[RecyclerView 2] ⚠️ No movies were successfully fetched. RecyclerView will remain empty.");
                            }
                        }
                    }
                }
            });
        }
    }

    public LiveData<List<Movie>> getRecMovieList() {
        getRecMovies();
        return recMovieList;
    }
}
