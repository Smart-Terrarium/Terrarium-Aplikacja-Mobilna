package com.example.test02;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity implements View.OnClickListener  {

    private TextView mEmailTextView;
    private TextView mTokenTextView;
    private Button button_Chart;
    private Button button_notifications;
    private Button FTPActivity;
    private String token; // Deklaracja zmiennej token

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mEmailTextView = findViewById(R.id.emailTextView);
        mTokenTextView = findViewById(R.id.tokenTextView);

        // odczytaj token z Intentu i wyświetl go w TextView
        token = getIntent().getStringExtra("auth_token"); // Przypisanie wartości do zmiennej token
        mTokenTextView.setText(token);

        // odczytaj email z SharedPreferences i wyświetl go w TextView
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        mEmailTextView.setText("Witaj " + email);

        button_Chart = findViewById(R.id.button_chart);
        button_Chart.setOnClickListener(this);

        button_notifications = findViewById(R.id.button_notifications);
        button_notifications.setOnClickListener(this);

        FTPActivity = findViewById(R.id.button_FTPActivity);
        FTPActivity.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_chart:
                Intent deviceIntent = new Intent(this, ChartActivity.class);
                deviceIntent.putExtra("auth_token", token);
                startActivity(deviceIntent);
                break;
            case R.id.button_notifications:
                Intent deviceIntentDevice = new Intent(this, NotificationsActivity.class);
                deviceIntentDevice.putExtra("auth_token", token);
                startActivity(deviceIntentDevice);
                break;
            case R.id.button_FTPActivity:
                Intent deviceIntentFTPActivity = new Intent(this, ConnectFTPActivity.class);
                deviceIntentFTPActivity.putExtra("auth_token", token);
                startActivity(deviceIntentFTPActivity);
                break;
        }
    }
}