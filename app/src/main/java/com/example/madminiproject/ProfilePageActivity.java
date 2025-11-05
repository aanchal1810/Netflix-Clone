package com.example.madminiproject;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.madminiproject.viewmodel.ProfileSelectionViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ProfilePageActivity extends AppCompatActivity {

    private ImageView searchIcon, selectedProfile, hamburger, profileIcon, homeIcon;
    private LinearLayout switchProfile, navbarBottom;
    private ProfileAdapter adapter;
    private RecyclerView recyclerView;
    private ProfileSelectionViewModel viewModel;
    private ImageView dialogAvatarImage;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    selectedImageUri = result.getUriContent();
                    if (dialogAvatarImage != null) {
                        dialogAvatarImage.setImageURI(selectedImageUri);
                    }
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

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_page);

        // Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        searchIcon = findViewById(R.id.search_profile_page);
        selectedProfile = findViewById(R.id.profile_avatar);
        hamburger = findViewById(R.id.hamburger);
        switchProfile = findViewById(R.id.selected_profile);
        navbarBottom = findViewById(R.id.bottomNav);
        profileIcon = navbarBottom.findViewById(R.id.navbar_profile_icon);
        homeIcon = navbarBottom.findViewById(R.id.homeIcon);

        LinearLayout downloadsCard = findViewById(R.id.downloads_card);
        downloadsCard.setOnClickListener(v -> {
                    Intent goToDownloads = new Intent(ProfilePageActivity.this, DownloadsActivity.class);


                    startActivity(goToDownloads);
                });

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileSelectionViewModel.class);

        // Load profile from intent
        Intent getProfileInfo = getIntent();
        String profileUrl = getProfileInfo.getStringExtra("PROFILE_AVATAR_URL1");
        int bgResIdProfile = getProfileInfo.getIntExtra("PROFILE_BG_RES_ID1", -1);

        // Load profile image/background
        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileUrl)
                    .apply(new RequestOptions().dontAnimate())
                    .into(selectedProfile);
            Glide.with(this)
                    .load(profileUrl)
                    .apply(new RequestOptions().dontAnimate())
                    .into(profileIcon);
        } else if (bgResIdProfile != -1) {
            selectedProfile.setBackgroundResource(bgResIdProfile);
            profileIcon.setBackgroundResource(bgResIdProfile);
        } else {
            selectedProfile.setBackgroundResource(R.drawable.profile_pink);
            profileIcon.setBackgroundResource(R.drawable.profile_pink);
        }

        // Search click
        searchIcon.setOnClickListener(v -> startActivity(new Intent(ProfilePageActivity.this, Search.class)));

        // Hamburger click
        hamburger.setOnClickListener(v -> showMenuBottomSheet());

        // Switch profile click
        switchProfile.setOnClickListener(v -> showSwitchProfileBottomSheet());

        //to back to home page
        homeIcon.setOnClickListener(v -> {
            Intent goToHome = new Intent(this, MainActivity.class);
            startActivity(goToHome);
        });
    }

    private void showMenuBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
        sheetView.findViewById(R.id.manageProfiles).setOnClickListener(v -> {
            Intent goToManage = new Intent(this, ManageProfileActivity.class);
            startActivity(goToManage);
        });
        sheetView.findViewById(R.id.signOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, GetStarted.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showSwitchProfileBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.switch_profile_menu, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
        sheetView.findViewById(R.id.manageProfiles1).setOnClickListener(v -> {
            Intent goToManage = new Intent(this, ManageProfileActivity.class);
            startActivity(goToManage);
        });
        recyclerView = sheetView.findViewById(R.id.switch_profile_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new ProfileAdapter(new ArrayList<>(), new ProfileAdapter.OnProfileSelectedListener() {
            @Override
            public void onProfileSelected(Profile profile, View sharedView) {
                if (profile == null) return;

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

                Intent intent = new Intent(ProfilePageActivity.this, ProfileTransitionActivity.class);
                intent.putExtra("PROFILE_ID", profile.getId());
                intent.putExtra("PROFILE_AVATAR_URL", profile.getAvatarUrl());
                intent.putExtra("IS_IMAGE_AVATAR", isImageAvatar);
                intent.putExtra("PROFILE_BG_RES_ID", bgResId);
                intent.putExtra("TRANSITION_NAME", sharedView.getTransitionName());

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                        ProfilePageActivity.this, sharedView, sharedView.getTransitionName()
                );
                startActivity(intent, options.toBundle());
                bottomSheetDialog.dismiss();
            }

            @Override
            public void onAddProfileClicked() {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    showAddProfileDialog();
                } else {
                    Toast.makeText(ProfilePageActivity.this, "You must be logged in to add a profile.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(adapter);

        // Observe profiles
        viewModel.getProfiles().observe(this, profiles -> {
            adapter.setProfiles(profiles);
            // Hide add button after 5 profiles
            boolean limitReached = profiles.size() >= 5;
            adapter.setProfileLimitReached(limitReached);
        });

        viewModel.getErrorMessage().observe(this, error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        );

        viewModel.fetchProfiles();
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
            String profileName = editText.getText().toString().trim();
            if (!profileName.isEmpty()) {
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

    private android.transition.Transition makeArcMotionTransition() {
        android.transition.ArcMotion arcMotion = new android.transition.ArcMotion();
        arcMotion.setMinimumHorizontalAngle(60f);
        arcMotion.setMinimumVerticalAngle(60f);

        android.transition.ChangeBounds changeBounds = new android.transition.ChangeBounds();
        changeBounds.setPathMotion(arcMotion);
        changeBounds.setDuration(700);
        changeBounds.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        return changeBounds;
    }
}
