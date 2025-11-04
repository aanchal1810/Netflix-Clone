package com.example.madminiproject;

import com.example.madminiproject.viewmodel.MovieRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface ApiService {

    @GET("/initialmovies")
    Call<List<String>> getInitialMovies();

    @POST("/recommend")
    Call<List<String>> getRecommendedMovies(@Body MovieRequest movieRequest);

    @GET("/getfinalrecommendation")
    Call<List<String>> getFinalRec();
    @GET("/getgenremovies")
    Call<Map<String,List<String>>> getGenreMovies();
    @POST("/becauseyouwatched")
    Call<List<String>> becauseYouWatched(@Body MovieRequest movieRequest);

}
