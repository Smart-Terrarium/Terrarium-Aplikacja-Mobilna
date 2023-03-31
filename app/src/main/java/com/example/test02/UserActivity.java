package com.example.test02;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Context.MODE_PRIVATE;

public class UserActivity extends AppCompatActivity {

    private TextView mEmailTextView;
    private TextView mTokenTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mEmailTextView = findViewById(R.id.emailTextView);
        mTokenTextView = findViewById(R.id.tokenTextView);

        // odczytaj token z Intentu i wyświetl go w TextView
        String token = getIntent().getStringExtra("auth_token");
        mTokenTextView.setText(token);

        // odczytaj email z SharedPreferences i wyświetl go w TextView
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        mEmailTextView.setText(email);
    }
}
