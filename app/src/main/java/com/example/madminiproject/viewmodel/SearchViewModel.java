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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends AndroidViewModel {
    private static final String TAG = "SearchViewModel";
    private final MutableLiveData<List<Movie>> searchResults = new MutableLiveData<>();
    private final TmdbApi tmdbApi;
    private final ExecutorService executorService;

    private final MutableLiveData<Profile> currentProfile = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference profileRef;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        tmdbApi = TmdbClient.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Movie>> getSearchResults() {
        return searchResults;
    }

    public void loadProfile(String profileId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profileRef = db.collection("users").document(userId).collection("profiles").document(profileId);
        profileRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Profile profile = snapshot.toObject(Profile.class);
                currentProfile.setValue(profile);
                updateWatchlistStatus();
            }
        });
    }

    public void searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            return;
        }

        // Execute search logic on background thread
        executorService.execute(() -> {
            Log.d(TAG, "Searching TMDB for: " + query);
            Call<MovieResponse> call = tmdbApi.searchMovies(BuildConfig.TMDB_API_KEY, query);
            call.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Movie> results = response.body().getResults();
                        if (results != null) {
                            searchResults.postValue(results);
                            updateWatchlistStatus(results);
                        } else {
                            searchResults.postValue(new ArrayList<>());
                        }
                    } else {
                        Log.e(TAG, "API response failed: " + response.message());
                        searchResults.postValue(new ArrayList<>());
                    }
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.e(TAG, "API call failed: " + t.getMessage());
                    searchResults.postValue(new ArrayList<>());
                }
            });
        });
    }

    private void updateWatchlistStatus() {
        List<Movie> movies = searchResults.getValue();
        if (movies != null && !movies.isEmpty()) {
            updateWatchlistStatus(movies);
        }
    }

    private void updateWatchlistStatus(List<Movie> movies) {
        Profile profile = currentProfile.getValue();
        if (profile != null) {
            List<String> watchList = profile.getWatchListAsList();
            for (Movie movie : movies) {
                movie.setWatchlisted(watchList.contains(movie.getTitle()));
            }
            searchResults.postValue(new ArrayList<>(movies)); // postValue to ensure UI updates
        }
    }

    public void toggleWatchList(String movieTitle) {
        Profile profile = currentProfile.getValue();
        if (profile != null && profileRef != null) {
            List<String> watchList = profile.getWatchListAsList();
            if (watchList.contains(movieTitle)) {
                watchList.remove(movieTitle);
            } else {
                watchList.add(movieTitle);
            }
            profileRef.update("watchList", watchList);
        }
    }
    public LiveData<Profile> getCurrentProfile() {
        return currentProfile;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
