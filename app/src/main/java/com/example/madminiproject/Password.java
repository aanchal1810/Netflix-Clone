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

public class Password extends AppCompatActivity {

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
        Button loginBtn1 = findViewById(R.id.loginBtn1);
        EditText password = findViewById(R.id.password);
        String userPass = password.getText().toString();

        loginBtn1.setOnClickListener(v -> {
//            if (userPass.isEmpty()){
//                Toast.makeText(this, "Please enter the password to continue.", Toast.LENGTH_SHORT).show();
//                return;
//            }
            startActivity(new Intent(Password.this, OnboardingSwipe.class));
            finish();
        });
    }
}