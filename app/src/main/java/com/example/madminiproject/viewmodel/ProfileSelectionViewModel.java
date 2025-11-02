package com.example.madminiproject.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.madminiproject.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileSelectionViewModel extends ViewModel {

    private static final int MAX_PROFILES = 5;

    private final MutableLiveData<List<Profile>> profiles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileLimitReached = new MutableLiveData<>(false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public LiveData<List<Profile>> getProfiles() {
        return profiles;
    }

    public LiveData<Boolean> getNavigateToMain() {
        return navigateToMain;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isProfileLimitReached() {
        return profileLimitReached;
    }

    public void fetchProfiles() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("profiles")
                    .get()
                    .addOnSuccessListener(this::handleFetchSuccess)
                    .addOnFailureListener(e -> {
                        errorMessage.setValue("Failed to fetch profiles: " + e.getMessage());
                    });
        } else {
            errorMessage.setValue("User not authenticated.");
        }
    }

    private void handleFetchSuccess(QuerySnapshot queryDocumentSnapshots) {
        if (queryDocumentSnapshots != null) {
            List<Profile> profileList = queryDocumentSnapshots.toObjects(Profile.class);
            profiles.setValue(profileList);
            profileLimitReached.setValue(profileList.size() >= MAX_PROFILES);
        }
    }

    public void onProfileSelected(Profile profile) {
        // Here you would save the selected profile to SharedPreferences
        navigateToMain.setValue(true);
    }

    public void addProfile(String profileName, Uri imageUri) {
        if (profiles.getValue() != null && profiles.getValue().size() >= MAX_PROFILES) {
            errorMessage.setValue("You can only have up to " + MAX_PROFILES + " profiles.");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (imageUri != null) {
                uploadAvatar(user.getUid(), imageUri, profileName);
            } else {
                // Use a default avatar if no image is selected
                addProfileToFirestore(user.getUid(), profileName, null);
            }
        } else {
            errorMessage.setValue("User not authenticated.");
        }
    }

    private void uploadAvatar(String userId, Uri imageUri, String profileName) {
        StorageReference storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("avatars/" + userId + "/" + UUID.randomUUID().toString());

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    addProfileToFirestore(userId, profileName, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to upload image: " + e.getMessage());
                });
    }

    private void addProfileToFirestore(String userId, String profileName, String avatarUrl) {
        Profile newProfile = new Profile(profileName, avatarUrl);
        db.collection("users").document(userId).collection("profiles").add(newProfile)
                .addOnSuccessListener(documentReference -> fetchProfiles())
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to add profile: " + e.getMessage());
                });
    }
}
