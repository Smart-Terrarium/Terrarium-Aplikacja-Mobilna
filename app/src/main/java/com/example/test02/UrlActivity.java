package com.example.test02;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        mSaveButton = findViewById(R.id.saveButton);
        mCurrentUrlTextView = findViewById(R.id.currentUrlTextView);


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String savedBaseUrl = settings.getString(BASE_URL_KEY, "");
        mCurrentUrlTextView.setText("Current address: " + savedBaseUrl);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseUrl = mBaseUrlEditText.getText().toString();
                if (!baseUrl.isEmpty()) {
                    baseUrlManager.setBaseUrl(UrlActivity.this, baseUrl); // Use instance method

                    mCurrentUrlTextView.setText("Aktualny zapisany adres: " + baseUrl);
                }
            }
        });
    }
}