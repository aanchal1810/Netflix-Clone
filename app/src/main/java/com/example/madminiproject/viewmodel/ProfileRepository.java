package com.example.madminiproject.viewmodel;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.madminiproject.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;
import java.util.function.Consumer;

public class ProfileRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public void updateProfile(String profileId, String newName, Uri newImageUri,
                              Consumer<Profile> onSuccess,
                              Consumer<String> onError) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            onError.accept("User not authenticated.");
            return;
        }

        String userId = user.getUid();

        if (newImageUri != null) {
            uploadAvatar(userId, newImageUri, avatarUrl -> {
                updateProfileInFirestore(userId, profileId, newName, avatarUrl, onSuccess, onError);
            }, onError);
        } else {
            updateProfileInFirestore(userId, profileId, newName, null, onSuccess, onError);
        }
    }

    private void uploadAvatar(String userId, Uri imageUri,
                              Consumer<String> onUploaded,
                              Consumer<String> onError) {
        StorageReference storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("avatars/" + userId + "/" + UUID.randomUUID());

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> onUploaded.accept(uri.toString()))
                        .addOnFailureListener(e -> onError.accept("Failed to get image URL: " + e.getMessage())))
                .addOnFailureListener(e -> onError.accept("Failed to upload image: " + e.getMessage()));
    }

    private void updateProfileInFirestore(String userId, String profileId, String newName, String avatarUrl,
                                          Consumer<Profile> onSuccess,
                                          Consumer<String> onError) {

        if (profileId == null) {
            onError.accept("Profile ID missing.");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("profiles")
                .document(profileId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Profile profile = snapshot.toObject(Profile.class);
                        if (profile == null) {

                            onError.accept("Profile not found.");
                            return;
                        }
                        if (profile != null){
                            profile.setId(snapshot.getId());
                        }
                        if (newName != null && !newName.isEmpty()) {
                            profile.setName(newName);
                        }
                        if (avatarUrl != null) {
                            profile.setAvatarUrl(avatarUrl);
                        }

                        db.collection("users").document(userId)
                                .collection("profiles").document(profileId)
                                .set(profile)
                                .addOnSuccessListener(aVoid -> onSuccess.accept(profile))
                                .addOnFailureListener(e -> onError.accept("Failed to update: " + e.getMessage()));
                    } else {
                        onError.accept("Profile not found.");
                    }
                })
                .addOnFailureListener(e -> onError.accept("Failed to fetch profile: " + e.getMessage()));
    }
}
