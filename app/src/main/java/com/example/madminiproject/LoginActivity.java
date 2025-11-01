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
import androidx.lifecycle.ViewModelProvider;

import com.example.madminiproject.viewmodel.EmailViewModel;

public class LoginActivity extends AppCompatActivity {

    private EmailViewModel emailViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailViewModel = new ViewModelProvider(this).get(EmailViewModel.class);

        Button loginBtn = findViewById(R.id.loginBtn);
        EditText email = findViewById(R.id.email);

        loginBtn.setOnClickListener(v -> {
            if (!emailViewModel.onNextClicked(email.getText().toString())) {
                Toast.makeText(this, "Please enter your email to continue.", Toast.LENGTH_SHORT).show();
            }
        });

        emailViewModel.getNavigateToPassword().observe(this, userEmail -> {
            startActivity(new Intent(LoginActivity.this, Password.class).putExtra("email", userEmail));
            finish();
        });
    }
}
