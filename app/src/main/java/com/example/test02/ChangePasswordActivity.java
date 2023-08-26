package com.example.test02;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private String token;
    private EditText newPasswordEditText;
    private Button changePasswordButton;
    private MainActivity.BaseUrl baseUrlManager;
    private String BASE_URL;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        token = getIntent().getStringExtra("auth_token");
        newPasswordEditText = findViewById(R.id.new_password_edittext);
        changePasswordButton = findViewById(R.id.change_password_button);
        baseUrlManager = new MainActivity.BaseUrl();
        BASE_URL = baseUrlManager.getBaseUrl(this);
        System.out.println("token ssss sss " + token);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                final String newPassword = newPasswordEditText.getText().toString();

                if (!newPassword.isEmpty()) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @SuppressLint("StaticFieldLeak")
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            try {

                                JSONObject jsonBody = new JSONObject();
                                jsonBody.put("password", newPassword);

                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                RequestBody requestBody = RequestBody.create(JSON, jsonBody.toString());

                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url("http://" + BASE_URL + ":8000/account/user/change-password")
                                        .header("Authorization", "Bearer " + token)
                                        .post(requestBody)
                                        .build();

                                Response response = client.newCall(request).execute();
                                return response.isSuccessful();
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        }

                        @Override
                        protected void onPostExecute(Boolean success) {
                            if (success) {
                                Log.d("ChangePasswordActivity", "Password changed successfully");

                            } else {
                                Log.e("ChangePasswordActivity", "Failed to change password");
                            }
                        }
                    }.execute();
                } else {
                    newPasswordEditText.setError("Please enter a new password");
                }
            }
        });}}
