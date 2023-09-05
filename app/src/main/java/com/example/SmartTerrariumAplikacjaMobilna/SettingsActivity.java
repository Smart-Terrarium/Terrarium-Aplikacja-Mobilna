package com.example.SmartTerrariumAplikacjaMobilna;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private Button ChangePasswordButton;
    private MainActivity.BaseUrl baseUrlManager;
    private String token;
    private Button ChangeLanguageButton;
    
    @SuppressLint("MissingInflatedId")
    @Override
   protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        baseUrlManager = new MainActivity.BaseUrl();

        ChangePasswordButton = findViewById(R.id.button_change_password);
        ChangePasswordButton.setOnClickListener(this);

        token = getIntent().getStringExtra("auth_token");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_change_password:
                Intent changePasswordIntent = new Intent(this, ChangePasswordActivity.class);
                changePasswordIntent.putExtra("auth_token", token);
                startActivity(changePasswordIntent);
                break;

        }
    }}
