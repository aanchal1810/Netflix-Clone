package com.example.madminiproject;

import com.example.madminiproject.viewmodel.MovieRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("/initialmovies")
    Call<List<String>> getInitialMovies();

    @POST("/recommend")
    Call<List<String>> getRecommendedMovies(MovieRequest movieRequest);

}
