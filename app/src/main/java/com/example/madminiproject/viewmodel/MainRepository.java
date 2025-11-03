package com.example.madminiproject.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.madminiproject.ApiService;
import com.example.madminiproject.BuildConfig;
import com.example.madminiproject.MovieResponse;
import com.example.madminiproject.RetrofitClient;
import com.example.madminiproject.TmdbApi;
import com.example.madminiproject.TmdbClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainRepository {
    private final TmdbApi tmdbApi = TmdbClient.getInstance();
    private final ApiService movieapiService = RetrofitClient.getApiService();
    public Call<List<String>> getInitialMovie(){
        return movieapiService.getInitialMovies();
    }
    public Call<List<String>> getRecommendedMovie(MovieRequest movieRequest){
        return movieapiService.getRecommendedMovies(movieRequest);
    }
    public List<Call<MovieResponse>> getMoviePosters(List<String> movieTitle){
        List<Call<MovieResponse>> call = new ArrayList<>();
        String apiKey = BuildConfig.TMDB_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            Log.e("MainRepository", "TMDB_API_KEY is null or empty! Please check gradle.properties");
        } else {
            Log.v("MainRepository", "Using TMDB API Key: " + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
        }
        for (String title: movieTitle){
            Log.v("MainRepository","Searching for movie title: " + title);
            call.add(tmdbApi.searchMovies(apiKey, title));
        }
        return call;
    }


}
