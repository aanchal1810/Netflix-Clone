package com.example.madminiproject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TmdbClient {
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static volatile TmdbApi instance = null; // volatile ensures visibility across threads

    // private constructor to prevent manual instantiation
    private TmdbClient() {}

    public static TmdbApi getInstance() {
        if (instance == null) {
            synchronized (TmdbClient.class) { // lock only during creation
                if (instance == null) { // double-check locking
                    // setup logging interceptor for debugging API calls
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

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

                    instance = retrofit.create(TmdbApi.class);
                }
            }
        }
        return instance;
    }
}

