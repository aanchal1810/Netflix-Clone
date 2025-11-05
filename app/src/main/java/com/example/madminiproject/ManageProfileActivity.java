package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madminiproject.viewmodel.ProfileSelectionViewModel;

import java.util.ArrayList;

public class ManageProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private ProfileSelectionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView headerText = findViewById(R.id.header_text);
        headerText.setText("Select the profile you want to edit");

        recyclerView = findViewById(R.id.manage_profiles_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        viewModel = new ViewModelProvider(this).get(ProfileSelectionViewModel.class);

        adapter = new ProfileAdapter(new ArrayList<>(), new ProfileAdapter.OnProfileSelectedListener() {
            @Override
            public void onProfileSelected(Profile profile, View sharedView) {
                Log.d("ManageProfileActivity", "Profile ID: " + profile.getId());
                Intent intent = new Intent(ManageProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("PROFILE_ID", profile.getId());
                intent.putExtra("PROFILE_NAME", profile.getName());
                intent.putExtra("PROFILE_AVATAR_URL", profile.getAvatarUrl());
                intent.putExtra("COL_INX", profile.getColorIndex());
                startActivity(intent);
            }

            @Override
            public void onAddProfileClicked() {
                Toast.makeText(ManageProfileActivity.this, "Profile adding disabled here", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        viewModel.getProfiles().observe(this, adapter::setProfiles);
        viewModel.getErrorMessage().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        viewModel.fetchProfiles();
    }
}
