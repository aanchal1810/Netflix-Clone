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
import com.example.madminiproject.Profile;
import com.example.madminiproject.TmdbApi;
import com.example.madminiproject.TmdbClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private String profileID;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final Pattern CSV_SPLIT = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private final MainRepository repository = new MainRepository();
    private final MutableLiveData<List<Movie>> movieList = new MutableLiveData<>();
    private final MutableLiveData<List<String>> moveTitleFinalRec = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> recMovieList = new MutableLiveData<>();
    // Map to store separate LiveData for each category title
    private final Map<String, MutableLiveData<List<Movie>>> watchedMovieRecListMap = new HashMap<>();
    private final MutableLiveData<List<String>> watchedMovieTitles = new MutableLiveData<>();
    private final MutableLiveData<Map<String,List<Movie>>> genreMovies = new MutableLiveData<>();
    private final MutableLiveData<List<String>> watchedMoviesFromFirebase = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> myListMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> watchListMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> continueWatchingMovies = new MutableLiveData<>();

    private final TmdbApi tmdbApi;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference profileRef;

    public MainViewModel(@NonNull Application application) {
        super(application);
        // Read profile ID from SharedPreferences
        profileID = application.getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                .getString("PROFILE_ID", null);
        tmdbApi = TmdbClient.getInstance();
        Log.d(TAG, "Profile ID loaded from SharedPreferences: " + profileID);
        loadMovies();
        loadProfileFromFirebase();
    }
    
    private void loadProfileFromFirebase() {
        if (profileID == null) {
            Log.w(TAG, "Profile ID is null, cannot load profile from Firebase");
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
            ? FirebaseAuth.getInstance().getCurrentUser().getUid() 
            : null;
        
        if (userId == null) {
            Log.w(TAG, "User not authenticated, cannot load profile from Firebase");
            return;
        }
        
        profileRef = db.collection("users").document(userId).collection("profiles").document(profileID);
        profileRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "Error loading profile from Firebase", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Profile profile = snapshot.toObject(Profile.class);
                if (profile != null) {
                    // Get watched movies from watchHistory
                    List<String> watchedTitles = profile.getWatchHistoryAsList();
                    watchedMoviesFromFirebase.postValue(watchedTitles);
                    Log.d(TAG, "Loaded " + watchedTitles.size() + " watched movies from Firebase");
                    
                    // Load favorites (My List)
                    loadMyListMovies(profile.getFavoritesAsList());
                    
                    // Load watchList
                    loadWatchListMovies(profile.getWatchListAsList());
                    
                    // Load Continue Watching from watchHistory
                    loadContinueWatchingMovies(profile.getWatchHistoryAsList());
                }
            } else {
                Log.w(TAG, "Profile snapshot does not exist");
            }
        });
    }
    
    public LiveData<List<String>> getWatchedMoviesFromFirebase() {
        return watchedMoviesFromFirebase;
    }
    
    private void loadMyListMovies(List<String> favoriteTitles) {
        if (favoriteTitles == null || favoriteTitles.isEmpty()) {
            myListMovies.postValue(new ArrayList<>());
            return;
        }
        
        List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(favoriteTitles);
        fetchMyListMoviePosters(movieTitleCalls);
    }
    
    private void fetchMyListMoviePosters(List<Call<MovieResponse>> movieTitleCalls) {
        if (movieTitleCalls == null || movieTitleCalls.isEmpty()) {
            myListMovies.postValue(new ArrayList<>());
            return;
        }
        
        final int totalCalls = movieTitleCalls.size();
        final List<Movie> myListMovieList = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};
        
        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                Movie completeMovie = body.getResults().get(0);
                                myListMovieList.add(completeMovie);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[My List] Error parsing TMDB response", e);
                        }
                    }
                    
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            List<Movie> uniqueMovies = new ArrayList<>();
                            for (Movie movie : myListMovieList) {
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
                            myListMovies.postValue(uniqueMovies);
                            Log.d(TAG, "[My List] Added " + uniqueMovies.size() + " movies");
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "[My List] TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            if (!myListMovieList.isEmpty()) {
                                List<Movie> uniqueMovies = new ArrayList<>();
                                for (Movie movie : myListMovieList) {
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
                                myListMovies.postValue(uniqueMovies);
                            } else {
                                myListMovies.postValue(new ArrayList<>());
                            }
                        }
                    }
                }
            });
        }
    }
    
    private void loadWatchListMovies(List<String> watchListTitles) {
        if (watchListTitles == null || watchListTitles.isEmpty()) {
            watchListMovies.postValue(new ArrayList<>());
            return;
        }
        
        List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(watchListTitles);
        fetchWatchListMoviePosters(movieTitleCalls);
    }
    
    private void fetchWatchListMoviePosters(List<Call<MovieResponse>> movieTitleCalls) {
        if (movieTitleCalls == null || movieTitleCalls.isEmpty()) {
            watchListMovies.postValue(new ArrayList<>());
            return;
        }
        
        final int totalCalls = movieTitleCalls.size();
        final List<Movie> watchListMovieList = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};
        
        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                Movie completeMovie = body.getResults().get(0);
                                watchListMovieList.add(completeMovie);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[Watch List] Error parsing TMDB response", e);
                        }
                    }
                    
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            List<Movie> uniqueMovies = new ArrayList<>();
                            for (Movie movie : watchListMovieList) {
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
                            watchListMovies.postValue(uniqueMovies);
                            Log.d(TAG, "[Watch List] Added " + uniqueMovies.size() + " movies");
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "[Watch List] TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            if (!watchListMovieList.isEmpty()) {
                                List<Movie> uniqueMovies = new ArrayList<>();
                                for (Movie movie : watchListMovieList) {
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
                                watchListMovies.postValue(uniqueMovies);
                            } else {
                                watchListMovies.postValue(new ArrayList<>());
                            }
                        }
                    }
                }
            });
        }
    }
    
    public LiveData<List<Movie>> getMyListMovies() {
        return myListMovies;
    }
    
    public LiveData<List<Movie>> getWatchListMovies() {
        return watchListMovies;
    }
    
    private void loadContinueWatchingMovies(List<String> watchHistoryTitles) {
        if (watchHistoryTitles == null || watchHistoryTitles.isEmpty()) {
            continueWatchingMovies.postValue(new ArrayList<>());
            return;
        }
        
        List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(watchHistoryTitles);
        fetchContinueWatchingMoviePosters(movieTitleCalls);
    }
    
    private void fetchContinueWatchingMoviePosters(List<Call<MovieResponse>> movieTitleCalls) {
        if (movieTitleCalls == null || movieTitleCalls.isEmpty()) {
            continueWatchingMovies.postValue(new ArrayList<>());
            return;
        }
        
        final int totalCalls = movieTitleCalls.size();
        final List<Movie> continueWatchingMovieList = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};
        
        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                Movie completeMovie = body.getResults().get(0);
                                continueWatchingMovieList.add(completeMovie);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[Continue Watching] Error parsing TMDB response", e);
                        }
                    }
                    
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            List<Movie> uniqueMovies = new ArrayList<>();
                            for (Movie movie : continueWatchingMovieList) {
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
                            continueWatchingMovies.postValue(uniqueMovies);
                            Log.d(TAG, "[Continue Watching] Added " + uniqueMovies.size() + " movies");
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "[Continue Watching] TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            if (!continueWatchingMovieList.isEmpty()) {
                                List<Movie> uniqueMovies = new ArrayList<>();
                                for (Movie movie : continueWatchingMovieList) {
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
                                continueWatchingMovies.postValue(uniqueMovies);
                            } else {
                                continueWatchingMovies.postValue(new ArrayList<>());
                            }
                        }
                    }
                }
            });
        }
    }
    
    public LiveData<List<Movie>> getContinueWatchingMovies() {
        return continueWatchingMovies;
    }
    
    /**
     * Manually refresh My List from the current profile in Firestore
     */
    public void refreshMyList() {
        if (profileRef != null) {
            profileRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Profile profile = documentSnapshot.toObject(Profile.class);
                    if (profile != null) {
                        loadMyListMovies(profile.getFavoritesAsList());
                        Log.d(TAG, "[My List] Manually refreshed with " + profile.getFavoritesAsList().size() + " favorites");
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "[My List] Failed to refresh", e);
            });
        }
    }
    
    /**
     * Manually refresh Watch List from the current profile in Firestore
     */
    public void refreshWatchList() {
        if (profileRef != null) {
            profileRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Profile profile = documentSnapshot.toObject(Profile.class);
                    if (profile != null) {
                        loadWatchListMovies(profile.getWatchListAsList());
                        Log.d(TAG, "[Watch List] Manually refreshed with " + profile.getWatchListAsList().size() + " watchlist items");
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "[Watch List] Failed to refresh", e);
            });
        }
    }
    
    /**
     * Manually refresh Continue Watching from the current profile in Firestore
     */
    public void refreshContinueWatching() {
        if (profileRef != null) {
            profileRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Profile profile = documentSnapshot.toObject(Profile.class);
                    if (profile != null) {
                        loadContinueWatchingMovies(profile.getWatchHistoryAsList());
                        Log.d(TAG, "[Continue Watching] Manually refreshed with " + profile.getWatchHistoryAsList().size() + " watch history items");
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "[Continue Watching] Failed to refresh", e);
            });
        }
    }

    public LiveData<List<Movie>> getMovieList() {
        return movieList;
    }
    public LiveData<Map<String,List<Movie>>> getGenreMovie(){
        getGenreMovieList();
        return genreMovies;
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
        // Create MovieRequest with profileID to send to backend
        MovieRequest movieRequest = new MovieRequest("", profileID);
        Call<List<String>> call = repository.getFinalRec(movieRequest);
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
                                Movie completeMovie = body.getResults().get(0);
                                String moviename = completeMovie.getTitle();
                                String movieposterpath = completeMovie.getPosterPath();

                                Log.d(TAG, "[RecyclerView 2] Successfully fetched movie #" + (currentIndex + 1) + ": " + moviename);
                                Log.v(TAG, "[RecyclerView 2] Poster URL: " + movieposterpath);

                                finalRecMovies.add(completeMovie);
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

    public LiveData<List<Movie>> getWatchedRecMovieList(String title) {
        // Create or get the LiveData for this specific category
        if (!watchedMovieRecListMap.containsKey(title)) {
            watchedMovieRecListMap.put(title, new MutableLiveData<>());
        }
        getWatchRecMovies(title);
        return watchedMovieRecListMap.get(title);
    }

    private void getWatchRecMovies(String title) {
        // Create MovieRequest with profileID to send to backend
        MovieRequest movieRequest = new MovieRequest(title, profileID);
        Call<List<String>> call = repository.becauseYouWatched(movieRequest);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> titles = response.body();
                    watchedMovieTitles.postValue(titles);
                    Log.v(TAG, "[Category: " + title + "] Recommended movie titles: " + titles);

                    // Get a list of API calls for each title
                    List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(Objects.requireNonNull(titles));

                    // Collect recommended movies and post them in a batch for this specific category
                    fetchWatchedMoviePosters(movieTitleCalls, title);

                } else {
                    watchedMovieTitles.postValue(null);
                    Log.e(TAG, "[Category: " + title + "] Failed to fetch recommended movie titles. Code: " + response.code());
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
                watchedMovieTitles.postValue(null);
                Log.e(TAG, "[Category: " + title + "] Failed to fetch recommended movies: " + t.getMessage(), t);
            }
        });
    }
    private void fetchWatchedMoviePosters(List<Call<MovieResponse>> movieTitleCalls, String categoryTitle) {
        if (movieTitleCalls == null || movieTitleCalls.isEmpty()) {
            return;
        }

        // Get the specific LiveData for this category
        MutableLiveData<List<Movie>> categoryLiveData = watchedMovieRecListMap.get(categoryTitle);
        if (categoryLiveData == null) {
            categoryLiveData = new MutableLiveData<>();
            watchedMovieRecListMap.put(categoryTitle, categoryLiveData);
        }

        final int totalCalls = movieTitleCalls.size();
        final List<Movie> recommendedMovies = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};

        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            MutableLiveData<List<Movie>> finalCategoryLiveData = categoryLiveData;
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                Movie completeMovie = body.getResults().get(0);
                                String moviename = completeMovie.getTitle();
                                String movieposterpath = completeMovie.getFullPosterUrl();

                                Log.v(TAG, "[Category: " + categoryTitle + "] Recommended Movie Name: " + moviename);
                                Log.v(TAG, "[Category: " + categoryTitle + "] Recommended Poster URL: " + movieposterpath);

                                recommendedMovies.add(completeMovie);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[Category: " + categoryTitle + "] Error parsing TMDB response", e);
                        }
                    }

                    // Check if all calls are completed
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // All calls completed, create a new list for this category (avoiding duplicates)
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

                            // Post the movies for this specific category
                            finalCategoryLiveData.postValue(uniqueRecommendedMovies);
                            Log.v(TAG, "[Category: " + categoryTitle + "] Added " + uniqueRecommendedMovies.size() + " recommended movies (duplicates filtered)");
                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "[Category: " + categoryTitle + "] TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // Even if some failed, post what we have (avoiding duplicates)
                            if (!recommendedMovies.isEmpty()) {
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

                                finalCategoryLiveData.postValue(uniqueRecommendedMovies);
                                Log.v(TAG, "[Category: " + categoryTitle + "] Added " + uniqueRecommendedMovies.size() + " recommended movies (some may have failed, duplicates filtered)");
                            } else {
                                finalCategoryLiveData.postValue(new ArrayList<>());
                                Log.v(TAG, "[Category: " + categoryTitle + "] No movies were successfully fetched");
                            }
                        }
                    }
                }
            });
        }
    }
    public void getGenreMovieList(){
        Call<Map<String,List<String>>> call = repository.getGenreMovies();
        Map<String, List<Movie>> currentMap = genreMovies.getValue();
        if (currentMap == null) {
            currentMap = new HashMap<>();
        }
        final Map<String, List<Movie>> finalCurrentMap = currentMap;
        call.enqueue(new Callback<Map<String, List<String>>>() {
            @Override
            public void onResponse(Call<Map<String, List<String>>> call, Response<Map<String, List<String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String,List<String>> genreMovieResponse = response.body();
                    for (Map.Entry<String, List<String>> entry : genreMovieResponse.entrySet()) {
                        String genre = entry.getKey();
                        List<String> movieTitles = entry.getValue();
                        fetchGenreMoviePosters(genre, movieTitles, finalCurrentMap);
                    }
                } else {
                    Log.e(TAG, "[Genre Movies] Failed to fetch genre movies. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<String>>> call, Throwable t) {
                Log.e(TAG, "[Genre Movies] Failed to fetch genre movies: " + t.getMessage(), t);
            }
        });
    }

    private void fetchGenreMoviePosters(String genre, List<String> movieTitles, Map<String, List<Movie>> currentMap) {
        if (movieTitles == null || movieTitles.isEmpty()) {
            Log.w(TAG, "[Genre: " + genre + "] No movie titles to fetch");
            return;
        }

        List<Call<MovieResponse>> movieTitleCalls = repository.getMoviePosters(Objects.requireNonNull(movieTitles));
        final int totalCalls = movieTitleCalls.size();
        final List<Movie> genreMovieList = Collections.synchronizedList(new ArrayList<>());
        final int[] completedCount = {0};

        Log.d(TAG, "[Genre: " + genre + "] Starting " + totalCalls + " API calls for movie posters");

        for (Call<MovieResponse> moviePosterCall : movieTitleCalls) {
            moviePosterCall.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            MovieResponse body = response.body();
                            if (body.getResults() != null && !body.getResults().isEmpty()) {
                                Movie completeMovie = body.getResults().get(0);
                                genreMovieList.add(completeMovie);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "[Genre: " + genre + "] Error parsing TMDB response", e);
                        }
                    }

                    // Check if all calls are completed
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // All calls completed, remove duplicates and update the map
                            List<Movie> uniqueMovies = new ArrayList<>();
                            for (Movie movie : genreMovieList) {
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

                            // Update the map and post the updated value immediately
                            // This makes LiveData reactive - UI updates as soon as each genre is ready
                            synchronized (currentMap) {
                                currentMap.put(genre, uniqueMovies);
                                // Post a new copy to trigger observers
                                genreMovies.postValue(new HashMap<>(currentMap));
                            }
                            Log.d(TAG, "[Genre: " + genre + "] ✅ Added " + uniqueMovies.size() + " unique movies (from " + movieTitles.size() + " titles)");
                        }
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "[Genre: " + genre + "] TMDB API Call failed: " + t.getMessage(), t);
                    synchronized (completedCount) {
                        completedCount[0]++;
                        if (completedCount[0] == totalCalls) {
                            // Even if some failed, post what we have (avoiding duplicates)
                            if (!genreMovieList.isEmpty()) {
                                List<Movie> uniqueMovies = new ArrayList<>();
                                for (Movie movie : genreMovieList) {
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

                                synchronized (currentMap) {
                                    currentMap.put(genre, uniqueMovies);
                                    // Post immediately even if some calls failed - UI gets partial data reactively
                                    genreMovies.postValue(new HashMap<>(currentMap));
                                }
                                Log.d(TAG, "[Genre: " + genre + "] ✅ Added " + uniqueMovies.size() + " unique movies (some API calls failed)");
                            } else {
                                synchronized (currentMap) {
                                    currentMap.put(genre, new ArrayList<>());
                                    // Still post to notify observers (even if empty)
                                    genreMovies.postValue(new HashMap<>(currentMap));
                                }
                                Log.w(TAG, "[Genre: " + genre + "] ⚠️ No movies were successfully fetched");
                            }
                        }
                    }
                }
            });
        }
    }
}
