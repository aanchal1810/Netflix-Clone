package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.madminiproject.viewmodel.LoginViewModel;

public class Password extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private String userEmail;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        userEmail = getIntent().getStringExtra("email");

        Button loginBtn1 = findViewById(R.id.loginBtn1);
        EditText passwordField = findViewById(R.id.password);
        loadingSpinner = findViewById(R.id.loading_spinner);

        loginBtn1.setOnClickListener(v -> {
            String userPass = passwordField.getText().toString().trim();

            if (userPass.isEmpty()) {
                Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.authenticateUser(userEmail, userPass);
        });

        loginViewModel.getAuthResult().observe(this, authResult -> {
            if (authResult instanceof LoginViewModel.AuthResult.Loading) {
                loadingSpinner.setVisibility(View.VISIBLE);
            } else if (authResult instanceof LoginViewModel.AuthResult.Success) {
                loadingSpinner.setVisibility(View.GONE);
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Password.this, ProfileSelectionActivity.class));
                finish();
            } else if (authResult instanceof LoginViewModel.AuthResult.NewUser) {
                loadingSpinner.setVisibility(View.GONE);
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Password.this, OnboardingSwipe.class));
                finish();
            } else if (authResult instanceof LoginViewModel.AuthResult.Error) {
                loadingSpinner.setVisibility(View.GONE);
                Exception exception = ((LoginViewModel.AuthResult.Error) authResult).getException();
                Toast.makeText(this, "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
