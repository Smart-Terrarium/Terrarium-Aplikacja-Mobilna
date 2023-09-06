package com.example.SmartTerrariumAplikacjaMobilna;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast; // Importuj Toast

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {
    private String mAuthToken;
    private OkHttpClient mHttpClient;
    private MainActivity.BaseUrl baseUrlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mHttpClient = new OkHttpClient();
        baseUrlManager = new MainActivity.BaseUrl();
        mAuthToken = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", null);

        // Tworzy obiekt Handler, który opóźnia uruchomienie nowej aktywności
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthTokenValidity();
            }
        }, 1200);
    }

    private void redirectToUserActivity() {
        Intent intent = new Intent(SplashActivity.this, UserActivity.class);
        intent.putExtra("auth_token", mAuthToken);
        startActivity(intent);
        finish();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra("auth_token", mAuthToken);
        startActivity(intent);
        finish();
    }

    private void checkAuthTokenValidity() {
        String baseUrl = baseUrlManager.getBaseUrl(this);

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "http://" + baseUrl;
        }

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        Request request = new Request.Builder()
                .url(baseUrl + ":8000/devices")  // Aktualizuj URL na odpowiedni
                .addHeader("Authorization", "Bearer " + mAuthToken)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(() -> redirectToMainActivity());
                        System.out.println("HALOHALOHALO11111");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    System.out.println("HALOHALOHALO11111");
                    runOnUiThread(() -> redirectToUserActivity());
                } else if (response.code() == 401 || response.code() == 403) {
                    System.out.println("HALOHALOHALO2222222");
                    runOnUiThread(() -> redirectToMainActivity());
                } else {
                    System.out.println("HALOHALOHALO333333");
                }
            }
        });
    }
}
