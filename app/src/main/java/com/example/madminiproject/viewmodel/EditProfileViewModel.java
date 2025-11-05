package com.example.madminiproject.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditProfileViewModel extends ViewModel {

    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void updateProfile(String profileId, String newName, Uri imageUri) {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not authenticated.");
            return;
        }
        String userId = auth.getCurrentUser().getUid();

        if (imageUri != null) {
            uploadNewImage(userId, profileId, newName, imageUri);
        } else {
            updateProfileData(userId, profileId, newName, null);
        }
    }

    private void uploadNewImage(String userId, String profileId, String newName, Uri imageUri) {
        StorageReference storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("avatars/" + userId + "/" + UUID.randomUUID());

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateProfileData(userId, profileId, newName, uri.toString());
                }))
                .addOnFailureListener(e -> errorMessage.setValue("Image upload failed: " + e.getMessage()));
    }

    private void updateProfileData(String userId, String profileId, String newName, String newAvatarUrl) {
        var docRef = db.collection("users").document(userId)
                .collection("profiles").document(profileId);
        var updateData = new java.util.HashMap<String, Object>();
        updateData.put("name", newName);
        if (newAvatarUrl != null) updateData.put("avatarUrl", newAvatarUrl);

        // Use set() with merge to avoid NOT_FOUND
        docRef.set(updateData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> successMessage.setValue("Profile updated successfully!"))
                .addOnFailureListener(e -> errorMessage.setValue("Update failed: " + e.getMessage()));
    }
    // Add in EditProfileViewModel
    public void removeProfileImage(String profileId) {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not authenticated.");
            return;
        }
        String userId = auth.getCurrentUser().getUid();

        var docRef = db.collection("users").document(userId)
                .collection("profiles").document(profileId);

        var updateData = new java.util.HashMap<String, Object>();
        updateData.put("avatarUrl", null);

        docRef.set(updateData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> successMessage.setValue("Image removed successfully!"))
                .addOnFailureListener(e -> errorMessage.setValue("Failed to remove image: " + e.getMessage()));
    }
    public void deleteProfile(String profileId) {
        if (auth.getCurrentUser() == null) {
            errorMessage.setValue("User not authenticated.");
            return;
        }
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("profiles").document(profileId)
                .delete()
                .addOnSuccessListener(aVoid -> successMessage.setValue("Profile deleted successfully!"))
                .addOnFailureListener(e -> errorMessage.setValue("Failed to delete profile: " + e.getMessage()));
    }



}
