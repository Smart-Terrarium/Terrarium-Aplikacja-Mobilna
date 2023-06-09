package com.example.test02;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import android.content.Intent;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        mHttpClient = new OkHttpClient();

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
    }

    private class LoginTask extends AsyncTask<JSONObject, Void, Boolean> {
        @Override
        protected Boolean doInBackground(JSONObject... params) {
            if (params[0] == null) {
                return false;
            }
            JSONObject json = params[0];
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8000/login")
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

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8000/register")
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
