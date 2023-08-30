package com.example.SmartTerrariumAplikacjaMobilna;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Switch;

public class UrlActivity extends AppCompatActivity {

    private EditText mBaseUrlEditText;
    private Button mSaveButton;
    private TextView mCurrentUrlTextView;
    private Switch mDarkModeSwitch;
    private MainActivity.BaseUrl baseUrlManager;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String BASE_URL_KEY = "baseUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_setting);

        baseUrlManager = new MainActivity.BaseUrl();

        mBaseUrlEditText = findViewById(R.id.baseUrlEditText);
        mSaveButton = findViewById(R.id.saveUrlButton);
        mCurrentUrlTextView = findViewById(R.id.currentUrlTextView);
        mDarkModeSwitch = findViewById(R.id.darkModeSwitch);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean isDarkModeEnabled = settings.getBoolean("darkMode", false);
        mDarkModeSwitch.setChecked(isDarkModeEnabled);

        // Set the dark mode switch listener
        mDarkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save dark mode setting to SharedPreferences
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("darkMode", isChecked);
                editor.apply();

                // Apply dark mode immediately
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
        String savedBaseUrl = settings.getString(BASE_URL_KEY, "");
        mCurrentUrlTextView.setText("Current address: " + savedBaseUrl);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseUrl = mBaseUrlEditText.getText().toString();
                if (!baseUrl.isEmpty()) {
                    baseUrlManager.setBaseUrl(UrlActivity.this, baseUrl);

                    mCurrentUrlTextView.setText("Current address: " + baseUrl);
                }
            }
        });
    }
}