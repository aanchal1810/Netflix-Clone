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
import java.util.List;
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

    private final MutableLiveData<List<Movie>> movieList = new MutableLiveData<>();
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
            Log.e(TAG, "No titles to fetch");
            return;
        }

        List<Movie> newMovies = new ArrayList<>();
        for (String title : titles) {
            Call<MovieResponse> call = tmdbApi.searchMovies(BuildConfig.TMDB_API_KEY, title);
            call.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Movie> results = response.body().getResults();
                        if (results != null && !results.isEmpty()) {
                            newMovies.add(results.get(0));
                            movieList.setValue(new ArrayList<>(newMovies));
                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "API call failed for " + title + ": " + t.getMessage());
                }
            });
        }
    }
}
