package com.example.madminiproject;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.madminiproject.viewmodel.ProfileSelectionViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ProfileSelectionActivity extends AppCompatActivity implements ProfileAdapter.OnProfileSelectedListener {

    private ProfileSelectionViewModel viewModel;
    private ProfileAdapter adapter;
    private ProgressBar loadingSpinner;
    private RecyclerView recyclerView;
    private String profileName;
    private ImageView dialogAvatarImage;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            selectedImageUri = result.getUriContent();
            dialogAvatarImage.setImageURI(selectedImageUri);
        } else {
            Exception error = result.getError();
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.setSharedElementEnterTransition(makePathMotion());
        window.setSharedElementExitTransition(makePathMotion());
        setContentView(R.layout.activity_profile_selection);

        viewModel = new ViewModelProvider(this).get(ProfileSelectionViewModel.class);

        recyclerView = findViewById(R.id.profiles_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new ProfileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        loadingSpinner = findViewById(R.id.loading_spinner);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        viewModel.getProfiles().observe(this, profiles -> {
            adapter.setProfiles(profiles);
            recyclerView.startAnimation(fadeIn);
            loadingSpinner.setVisibility(View.GONE);
        });

        viewModel.getNavigateToMain().observe(this, navigate -> {
            if (navigate) {
                startActivity(new Intent(ProfileSelectionActivity.this, ProfileTransitionActivity.class));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            loadingSpinner.setVisibility(View.GONE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });

        viewModel.isProfileLimitReached().observe(this, limitReached -> {
            adapter.setProfileLimitReached(limitReached);
            adapter.setProfileLimitReached(limitReached);
        });


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadingSpinner.setVisibility(View.VISIBLE);
            viewModel.fetchProfiles();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onProfileSelected(Profile profile, View sharedView) {

        viewModel.onProfileSelected(profile);
        Intent intent = new Intent(this, ProfileTransitionActivity.class);
        intent.putExtra("PROFILE_AVATAR_URL", profile.getAvatarUrl());
        intent.putExtra("TRANSITION_NAME", sharedView.getTransitionName());

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                this, sharedView, sharedView.getTransitionName()
        );
        startActivity(intent, options.toBundle());
    }
    @Override
    public void onAddProfileClicked() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            showAddProfileDialog();
        } else {
            Toast.makeText(this, "You must be logged in to add a profile.", Toast.LENGTH_SHORT).show();
        }
    }


    private void showAddProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_profile, null);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();

        dialogAvatarImage = customLayout.findViewById(R.id.dialog_avatar_image);
        EditText editText = customLayout.findViewById(R.id.edit_text_profile_name);
        Button cancelButton = customLayout.findViewById(R.id.dialog_cancel_button);
        Button saveButton = customLayout.findViewById(R.id.dialog_save_button);

        dialogAvatarImage.setOnClickListener(v -> openImagePicker());

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            profileName = editText.getText().toString();
            if (!profileName.isEmpty()) {
                loadingSpinner.setVisibility(View.VISIBLE);
                viewModel.addProfile(profileName, selectedImageUri);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Profile name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void openImagePicker() {
        // Create CropImageOptions with RECTANGLE crop shape and guidelines ON
        CropImageOptions cropOptions = new CropImageOptions(
                true,  // include gallery
                true,  // include camera
                CropImageView.CropShape.RECTANGLE, // crop shape
                CropImageView.CropCornerShape.RECTANGLE, // default
                0, 0, 0, // corner radius, snap radius, touch radius (use default)
                CropImageView.Guidelines.ON, // guidelines
                CropImageView.ScaleType.FIT_CENTER // scale type
                // remaining parameters will take default values
        );

        CropImageContractOptions options =
                new CropImageContractOptions(null, cropOptions);

        cropImage.launch(options);
    }

    private Transition makePathMotion() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setPathMotion(arcMotion);
        changeBounds.setDuration(800);
        changeBounds.setInterpolator(new AccelerateDecelerateInterpolator());

        return changeBounds;
    }
}
