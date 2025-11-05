package com.example.madminiproject;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

    private final ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    selectedImageUri = result.getUriContent();
                    dialogAvatarImage.setImageURI(selectedImageUri);
                } else if (result.getError() != null) {
                    Toast.makeText(this, "Error: " + result.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        window.setSharedElementEnterTransition(makeArcMotionTransition());
        window.setSharedElementExitTransition(makeArcMotionTransition());

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

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            loadingSpinner.setVisibility(View.GONE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });

        viewModel.isProfileLimitReached().observe(this, adapter::setProfileLimitReached);

        viewModel.getProfileAddedForOnboarding().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                Intent intent = new Intent(ProfileSelectionActivity.this, OnboardingSwipe.class);
                startActivity(intent);
                viewModel.onOnboardingNavigated(); // Reset the trigger
            }
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
        int[] profileBackgrounds = {
                R.drawable.profile_red,
                R.drawable.profile_blue,
                R.drawable.profile_green,
                R.drawable.profile_pink,
                R.drawable.profile_purple
        };

        int colorIndex = Math.max(0, Math.min(profile.getColorIndex(), profileBackgrounds.length - 1));
        int bgResId = profileBackgrounds[colorIndex];
        boolean isImageAvatar = profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty();

        viewModel.onProfileSelected(profile);

        Intent intent = new Intent(this, ProfileTransitionActivity.class);
        intent.putExtra("PROFILE_ID", profile.getId());
        intent.putExtra("PROFILE_AVATAR_URL", profile.getAvatarUrl());
        intent.putExtra("IS_IMAGE_AVATAR", isImageAvatar);
        intent.putExtra("PROFILE_BG_RES_ID", bgResId);
        intent.putExtra("TRANSITION_NAME", sharedView.getTransitionName());

        Log.d("ProfileSelectionDebug", "Starting transition with bgResId=" + bgResId);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                this, sharedView, sharedView.getTransitionName()
        );
        startActivity(intent, options.toBundle());
        finishAfterTransition();
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
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_profile, null);
        builder.setView(customLayout);

        AlertDialog dialog = builder.create();

        dialogAvatarImage = customLayout.findViewById(R.id.dialog_avatar_image);
        EditText editText = customLayout.findViewById(R.id.edit_text_profile_name);
        Button cancelButton = customLayout.findViewById(R.id.dialog_cancel_button);
        Button saveButton = customLayout.findViewById(R.id.dialog_save_button);

        dialogAvatarImage.setOnClickListener(v -> openImagePicker());
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            profileName = editText.getText().toString().trim();
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
        CropImageOptions cropOptions = new CropImageOptions(
                true,
                true,
                CropImageView.CropShape.RECTANGLE,
                CropImageView.CropCornerShape.RECTANGLE,
                0, 0, 0,
                CropImageView.Guidelines.ON,
                CropImageView.ScaleType.FIT_CENTER
        );

        CropImageContractOptions options = new CropImageContractOptions(null, cropOptions);
        cropImage.launch(options);
    }

    private Transition makeArcMotionTransition() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(60f);
        arcMotion.setMinimumVerticalAngle(60f);

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setPathMotion(arcMotion);
        changeBounds.setDuration(700);
        changeBounds.setInterpolator(new AccelerateDecelerateInterpolator());
        return changeBounds;
    }
}