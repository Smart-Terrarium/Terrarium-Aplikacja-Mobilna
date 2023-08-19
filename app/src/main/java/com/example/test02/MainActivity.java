package com.example.test02;



import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import org.json.JSONException;
import android.content.Intent;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mRegisterButton;
    private String mAuthToken;
    private OkHttpClient mHttpClient;
    private TextView mTokenTextView;
    private TextView mEmailTextView;
    private EditText mBaseUrlEditText;
    private BaseUrl baseUrlManager;


    public static class BaseUrl {
        private String baseUrl = "https://10.0.2.2:8000";

        public String getBaseUrl(Context context) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
            return sharedPreferences.getString("baseUrl", baseUrl);
        }

        public void setBaseUrl(Context context, String newBaseUrl) {
            baseUrl = newBaseUrl;

            SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE).edit();
            editor.putString("baseUrl", newBaseUrl);
            editor.apply();
        }
    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmailEditText = findViewById(R.id.emailEditText);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mLoginButton = findViewById(R.id.loginButton);
        mRegisterButton = findViewById(R.id.registerButton);
        mTokenTextView = findViewById(R.id.tokenTextView);
        mEmailTextView = findViewById(R.id.emailTextView);

        mAuthToken = getIntent().getStringExtra("auth_token");
        mTokenTextView.setText(mAuthToken);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        mEmailTextView.setText(email);
        mBaseUrlEditText = findViewById(R.id.baseUrlEditText);

        mHttpClient = new OkHttpClient();

        try {
            trustAllCertificates();
        } catch (Exception e) {
            e.printStackTrace();
        }
        @SuppressLint("WrongViewCast")

        ImageButton UrlActivityButton = findViewById(R.id.settingsButton);
        UrlActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UrlActivity.class);
            startActivity(intent);
        });

        mLoginButton.setOnClickListener(v -> {
            String userEmail = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            if (userEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email or password is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject json = new JSONObject();
            try {
                json.put("email", userEmail);
                json.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new LoginTask().execute(json);
        });

        mRegisterButton.setOnClickListener(v -> {
            String userEmail = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            if (userEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email or password is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject json = new JSONObject();
            try {
                json.put("email", userEmail);
                json.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new RegisterTask().execute(json);
        });

        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> {
            // Otwarcie nowej aktywności PasswordActivity
            Intent intent = new Intent(MainActivity.this, PasswordActivity.class);
            startActivity(intent);
        });

        baseUrlManager = new BaseUrl();
    }
    private void trustAllCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {}
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        mHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager)trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }
    private class LoginTask extends AsyncTask<JSONObject, Void, Boolean> {
        @Override
        protected Boolean doInBackground(JSONObject... params) {
            if (params[0] == null) {
                return false;
            }
            JSONObject json = params[0];
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

            String baseUrl = baseUrlManager.getBaseUrl(MainActivity.this);
            if (baseUrl.isEmpty()) {
                baseUrl = baseUrlManager.getBaseUrl(MainActivity.this);
            }


            if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                baseUrl = "https://" + baseUrl;
            }

            Request request = new Request.Builder()
                    .url(baseUrl + "/login")
                    .post(formBody)
                    .build();


            try {
                Response response = mHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        String responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);
                        System.out.println(responseJson.toString());
                        mAuthToken = responseJson.getString("access_token");
                    } else {
                        System.out.println("problem");
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                mTokenTextView.setText(mAuthToken);

                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                editor.putString("email", mEmailEditText.getText().toString());
                editor.apply();

                // otwórz nową aktywność po zalogowaniu
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("auth_token", mAuthToken);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RegisterTask extends AsyncTask<JSONObject, Void, Boolean> {
        @Override
        protected Boolean doInBackground(JSONObject... params) {
            if (params[0] == null) {
                return false;
            }
            JSONObject json = params[0];
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

            String baseUrl = baseUrlManager.getBaseUrl(MainActivity.this);
            if (baseUrl.isEmpty()) {
                baseUrl = baseUrlManager.getBaseUrl(MainActivity.this);
            }

            if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                baseUrl = "https://" + baseUrl;
            }
            System.out.println(baseUrl);
            Request request = new Request.Builder()
                    .url(baseUrl + "/register")
                    .post(formBody)
                    .build();

            try {
                Response response = mHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    if (response.code() == 201) {
                        System.out.println("udalo sie ");
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Zarejestrowano! Zaloguj się!", Toast.LENGTH_SHORT).show();
                mTokenTextView.setText(mAuthToken);

                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                editor.putString("email", mEmailEditText.getText().toString());
                editor.apply();


            } else {
                Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
