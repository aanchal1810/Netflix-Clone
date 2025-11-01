package com.example.madminiproject;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private final List<Movie> movieList = new ArrayList<>();
    private TmdbApi tmdbApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MoviesAdapter(this, movieList);
        recyclerView.setAdapter(adapter);

        // Retrofit + OkHttp client with logging & timeout
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tmdbApi = retrofit.create(TmdbApi.class);

        // Load CSV titles and fetch movies
        List<String> titles = loadMovieTitlesFromAssets();
        fetchMoviesFromApi(titles);


    }

    // CSV split regex that handles quoted commas
    private static final Pattern CSV_SPLIT = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private List<String> loadMovieTitlesFromAssets() {
        List<String> titles = new ArrayList<>();
        try {
            InputStream is = getAssets().open("tmdb_5000_movies.csv");
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
                if (titles.size() >= 200) break; // safety limit
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

        int limit = Math.min(titles.size(), 100); // avoid spamming API
        for (int i = 0; i < limit; i++) {
            String title = titles.get(i);
            Call<MovieResponse> call = tmdbApi.searchMovies(BuildConfig.TMDB_API_KEY, title);
            call.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Movie> results = response.body().getResults();
                        if (results != null && !results.isEmpty()) {
                            Movie first = results.get(0);
                            runOnUiThread(() -> {
                                movieList.add(first);
                                adapter.notifyItemInserted(movieList.size() - 1);
                            });
                        } else {
                            Log.w(TAG, "No results for: " + title);
                        }
                    } else {
                        Log.w(TAG, "API response failed for: " + title);
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
