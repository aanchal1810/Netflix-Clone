package com.example.madminiproject;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.madminiproject.viewmodel.EditProfileViewModel;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText nameInput;
    private Button saveButton, removeImageButton, deleteButton;

    private Uri selectedImageUri = null;
    private String profileId;
    private String originalImageUrl = null;

    private EditProfileViewModel viewModel;
    public int backgroundID;
    private int bgColourIndex;
    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    selectedImageUri = result.getUriContent();
                    profileImage.setImageURI(selectedImageUri);
                } else if (result.getError() != null) {
                    Toast.makeText(this, "Error: " + result.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.edit_profile_avatar);
        nameInput = findViewById(R.id.edit_profile_name);
        saveButton = findViewById(R.id.save_profile_button);
        removeImageButton = findViewById(R.id.remove_profile_image_button);
        deleteButton = findViewById(R.id.delete_profile_button);
//        progressBar = findViewById(R.id.edit_profile_progress);

        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        // Receive data from intent
        profileId = getIntent().getStringExtra("PROFILE_ID");
        Log.d("EditProfileActivity", "Profile ID: " + profileId);
        String profileName = getIntent().getStringExtra("PROFILE_NAME");
        originalImageUrl = getIntent().getStringExtra("PROFILE_AVATAR_URL");
        bgColourIndex = getIntent().getIntExtra("COL_INX", -1);
        backgroundID = -1;
        if (bgColourIndex != -1) {
            int[] profileBackgrounds = {
                    R.drawable.profile_red,
                    R.drawable.profile_blue,
                    R.drawable.profile_green,
                    R.drawable.profile_pink,
                    R.drawable.profile_purple
            };
            backgroundID = profileBackgrounds[bgColourIndex];
            profileImage.setImageResource(backgroundID);
        }
        // Pre-fill data
        nameInput.setText(profileName);
        if (originalImageUrl != null && !originalImageUrl.isEmpty()) {
            Glide.with(this).load(originalImageUrl).into(profileImage);
        } else {
            if (bgColourIndex != -1){
                profileImage.setImageResource(backgroundID);
            }
            else {
            profileImage.setImageResource(R.drawable.profile_pink);
            }
        }

        // Click listeners
        profileImage.setOnClickListener(v -> openImagePicker());

        removeImageButton.setOnClickListener(v -> {
            viewModel.removeProfileImage(profileId); // updates Firestore
            selectedImageUri = null;
            originalImageUrl = null;
            profileImage.setImageResource(R.drawable.profile_pink);
        });


        saveButton.setOnClickListener(v -> {
            if (profileId == null || profileId.isEmpty()) {
                Toast.makeText(this, "Cannot update: profile ID missing", Toast.LENGTH_SHORT).show();
                return;
            }
            String newName = nameInput.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Only 3 parameters
            viewModel.updateProfile(profileId, newName, selectedImageUri);
        });
        Button deleteButton = findViewById(R.id.delete_profile_button);

        deleteButton.setOnClickListener(v -> {
            if (profileId == null || profileId.isEmpty()) return;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this profile?")
                    .setPositiveButton("Yes", (dialog, which) -> viewModel.deleteProfile(profileId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        observeViewModel();

    }

    private void openImagePicker() {
        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.guidelines = CropImageView.Guidelines.ON;
        cropOptions.aspectRatioX = 1;
        cropOptions.aspectRatioY = 1;
        cropOptions.cropShape = CropImageView.CropShape.OVAL;

        CropImageContractOptions options = new CropImageContractOptions(null, cropOptions);
        cropImage.launch(options);
    }
    private void observeViewModel() {
        viewModel.getSuccessMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(EditProfileActivity.this, ProfilePageActivity.class);
                 intent.putExtra("PROFILE_ID", profileId);

                 intent.putExtra("PROFILE_BG_RES_ID1", backgroundID);
                 startActivity(intent);
                 finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
