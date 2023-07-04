package com.example.test02;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewDeviceActivity extends MainActivity {

    private EditText nameEditText;
    private EditText macAddressEditText;
    private Button addButton;
    private Button clearButton;
    private String mAuthToken;
    private OkHttpClient client;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newdevice);

        nameEditText = findViewById(R.id.nameEditText);
        macAddressEditText = findViewById(R.id.macAddressEditText);
        addButton = findViewById(R.id.addButton);
        clearButton = findViewById(R.id.clearButton);

        client = new OkHttpClient();
        mAuthToken = getIntent().getStringExtra("auth_token");
        System.out.println(mAuthToken);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String macAddress = macAddressEditText.getText().toString();
                addNewDevice(name, macAddress);
            }
        });
    }

    private void addNewDevice(String name, String macAddress) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("mac_address", macAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8000/device")
                .post(requestBody)
                .header("Authorization", "Bearer " + mAuthToken) // Dodaj autoryzację
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast("Request failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast("Device with MAC address " + macAddress + " added");
                } else {
                    showToast("Request unsuccessful");
                }
            }
        });
    }

    private void clearFields() {
        nameEditText.setText("");
        macAddressEditText.setText("");
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NewDeviceActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
