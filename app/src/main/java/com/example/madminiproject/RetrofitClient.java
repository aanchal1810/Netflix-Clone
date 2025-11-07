package com.example.madminiproject;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static ApiService apiService = null;
    private RetrofitClient(){};

    public static ApiService getApiService(){
        if (apiService == null){
            if (retrofit == null){
                // Add logging interceptor to see request/response details
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build();
                
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
