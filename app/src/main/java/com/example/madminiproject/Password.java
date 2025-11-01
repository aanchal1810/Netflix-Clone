package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Password extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userEmail;

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


        mAuth = FirebaseAuth.getInstance();
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        userEmail = getIntent().getStringExtra("email");

        Button loginBtn1 = findViewById(R.id.loginBtn1);
        EditText passwordField = findViewById(R.id.password);

        loginBtn1.setOnClickListener(v -> {
            String userPass = passwordField.getText().toString().trim();

            System.out.println(userEmail+"\t"+userPass);
            if (userPass.isEmpty()) {
                Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                return;
            }

            checkUserAndProceed(userEmail, userPass);
        });
    }

    private void checkUserAndProceed(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Password.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, signupTask -> {
                                    if (signupTask.isSuccessful()) {
                                        FirebaseUser newUser = mAuth.getCurrentUser();
                                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Password.this, OnboardingSwipe.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Error: " +
                                                signupTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
    }
}
