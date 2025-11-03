package com.example.madminiproject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("/initialmovies")
    Call<List<String>> getInitialMovies();

}
