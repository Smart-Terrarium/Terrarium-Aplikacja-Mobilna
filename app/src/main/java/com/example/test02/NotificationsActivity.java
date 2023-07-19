package com.example.test02;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationsActivity extends UserActivity {

    private static final String BASE_URL = "http://localhost:8000/devices/alerts";
    private static final String token = null;

    private Button getNotificationsButton;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        getNotificationsButton = findViewById(R.id.get_notifications_button);

        getNotificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNotifications();
            }
        });
    }


    private void fetchNotifications() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL + "?sort_by_priority=true&only_served=false")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Błąd podczas pobierania powiadomień", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onFailure(call, new IOException("Unexpected code " + response));
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONArray notificationsArray = new JSONArray(responseData);

                    // Przetwarzamy tablicę z powiadomieniami
                    for (int i = 0; i < notificationsArray.length(); i++) {
                        JSONObject notificationObject = notificationsArray.getJSONObject(i);
                        // Wyciągamy potrzebne dane z powiadomienia
                        int deviceId = notificationObject.getInt("device_id");
                        String description = notificationObject.getString("description");
                        boolean served = notificationObject.getBoolean("served");
                        int priority = notificationObject.getInt("priority");




                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
