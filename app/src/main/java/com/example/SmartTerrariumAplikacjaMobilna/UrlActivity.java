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


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

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