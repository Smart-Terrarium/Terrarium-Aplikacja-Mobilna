package com.example.test02;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PasswordActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private Button mResetPasswordButton;
    private OkHttpClient mHttpClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        mEmailEditText = findViewById(R.id.emailEditText);
        mResetPasswordButton = findViewById(R.id.resetPasswordButton);

        mHttpClient = new OkHttpClient();

        mResetPasswordButton.setOnClickListener(v -> {
            String email = mEmailEditText.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(PasswordActivity.this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new ResetPasswordTask().execute(json);
        });
    }

    private class ResetPasswordTask extends AsyncTask<JSONObject, Void, Boolean> {
        @Override
        protected Boolean doInBackground(JSONObject... params) {
            if (params[0] == null) {
                return false;
            }
            JSONObject json = params[0];
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

            String baseUrl = "http://10.0.2.2:8000";
            Request request = new Request.Builder()
                    .url(baseUrl + "/login/forgot-password")
                    .post(formBody)
                    .build();

            try {
                Response response = mHttpClient.newCall(request).execute();
                return response.isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(PasswordActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PasswordActivity.this, "Password reset failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
