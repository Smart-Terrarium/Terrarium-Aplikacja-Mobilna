package com.example.test02;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationsActivity extends AppCompatActivity {
    private String token;
    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Retrieve the OAuth 2.0 token.
        token = getIntent().getStringExtra("auth_token");

        // Initialize the RecyclerView and its adapter
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        // Call the method to display push notifications.
        displayPushNotifications();
    }


    // Method to display push notifications.
    private void displayPushNotifications() {
        notificationList.clear();
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8000/devices/alerts?sort_by_priority=true&only_served=false";

        // Tworzenie żądania HTTP z dodanym nagłówkiem Authorization z tokenem OAuth 2.0.
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .build();

        // Wykonanie żądania asynchronicznie.
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Obsługa błędów (np. brak połączenia internetowego).
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Błąd: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Odczytanie odpowiedzi (JSON) z serwera.
                    String responseData = response.body().string();

                    try {
                        // Parsowanie otrzymanego JSON-a.
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject notificationObject = jsonArray.getJSONObject(i);

                            // Pobranie potrzebnych danych z obiektu powiadomienia.
                            String description = notificationObject.getString("description");
                            boolean served = notificationObject.getBoolean("served");
                            int priority = notificationObject.getInt("priority");

                            // Dodanie powiadomienia do listy, która zostanie wyświetlona w RecyclerView.
                            NotificationItem notificationItem = new NotificationItem(description, served, priority);
                            notificationList.add(notificationItem);
                        }

                        // Aktualizacja interfejsu użytkownika na wątku głównym.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notificationAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Obsługa niepowodzenia odpowiedzi HTTP (np. błąd serwera).
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Błąd serwera: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // Model class for NotificationItem
    private static class NotificationItem {
        private String description;
        private boolean served;
        private int priority;

        public NotificationItem(String description, boolean served, int priority) {
            this.description = description;
            this.served = served;
            this.priority = priority;
        }

        public String getDescription() {
            return description;
        }

        public boolean isServed() {
            return served;
        }

        public int getPriority() {
            return priority;
        }
    }

    // RecyclerView Adapter
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
        private List<NotificationItem> notificationList;

        public NotificationAdapter(List<NotificationItem> notificationList) {
            this.notificationList = notificationList;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            NotificationItem notificationItem = notificationList.get(position);
            holder.notificationDescriptionTextView.setText(notificationItem.getDescription());
            String details = "Served: " + notificationItem.isServed() + ", Priority: " + notificationItem.getPriority();
            holder.notificationDetailsTextView.setText(details);
        }

        @Override
        public int getItemCount() {
            return notificationList.size();
        }

        public class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView notificationDescriptionTextView;
            TextView notificationDetailsTextView;

            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                notificationDescriptionTextView = itemView.findViewById(R.id.notificationDescriptionTextView);
                notificationDetailsTextView = itemView.findViewById(R.id.notificationDetailsTextView);
            }
        }
    }
}