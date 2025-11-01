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


public class LoginActivity extends AppCompatActivity {

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
        Button loginBtn = findViewById(R.id.loginBtn);
        EditText email = findViewById(R.id.email);


        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString();

            if (userEmail.isEmpty()){
                Toast.makeText(this, "Please enter your email to continue.", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(LoginActivity.this, Password.class).putExtra("email", userEmail));
            finish();
        });
    }
}