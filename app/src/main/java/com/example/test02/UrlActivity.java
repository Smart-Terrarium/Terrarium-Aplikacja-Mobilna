package com.example.test02;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.test02.MainActivity;

public class UrlActivity extends AppCompatActivity {

    private EditText mBaseUrlEditText;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_setting);

        mBaseUrlEditText = findViewById(R.id.baseUrlEditText);
        mSaveButton = findViewById(R.id.saveButton);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseUrl = mBaseUrlEditText.getText().toString();
                if (!baseUrl.isEmpty()) {
                    MainActivity.setBaseUrl(baseUrl);
                    finish();
                }
            }
        });
    }
}
