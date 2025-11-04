package com.example.madminiproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madminiproject.Movie;
import com.example.madminiproject.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerViewModel extends ViewModel {

    private final MutableLiveData<Movie> movie = new MutableLiveData<>();
    private final MutableLiveData<Long> playbackPosition = new MutableLiveData<>(0L);
    private final MutableLiveData<Profile> currentProfile = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference profileRef;

    public LiveData<Movie> getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie.setValue(movie);
        // You would also fetch the current profile here and check if the movie is in favorites
    }

    public LiveData<Long> getPlaybackPosition() {
        return playbackPosition;
    }

    public void setPlaybackPosition(long position) {
        this.playbackPosition.setValue(position);
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
                updateFavoriteStatus();
            }
        });
    }

    private void updateFavoriteStatus() {
        Movie currentMovie = movie.getValue();
        Profile profile = currentProfile.getValue();
        if (currentMovie != null && profile != null) {
            currentMovie.setFavorite(profile.getFavoritesAsList().contains(currentMovie.getTitle()));
            movie.setValue(currentMovie);
        }
    }

    public void toggleFavorite() {
        Movie currentMovie = movie.getValue();
        Profile profile = currentProfile.getValue();
        if (currentMovie != null && profile != null && profileRef != null) {
            List<String> favorites = profile.getFavoritesAsList();
            if (favorites.contains(currentMovie.getTitle())) {
                favorites.remove(currentMovie.getTitle());
            } else {
                favorites.add(currentMovie.getTitle());
            }
            profileRef.update("favorites", favorites);
        }
    }

    public void saveWatchHistory(String title, long position) {
        if (profileRef != null) {
            Map<String, Object> watchHistoryData = new HashMap<>();
            Map<String, Long> history = new HashMap<>();
            history.put(title, position);
            watchHistoryData.put("watchHistory", history);
            profileRef.set(watchHistoryData, SetOptions.merge());
        }
    }
}
