package com.example.test02;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.appcompat.app.AlertDialog; // Import dla AlertDialog

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationsActivity extends AppCompatActivity {

    private String token;
    private List<String> notificationList;
    private ArrayAdapter<String> notificationAdapter;
    private ListView notificationListView;
    private String responseData;
    private static final String BASE_URL = MainActivity.BaseUrl.BASE_URL + "/devices/alerts";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        token = getIntent().getStringExtra("auth_token");
        client = new OkHttpClient();
        notificationList = new ArrayList<>();
        notificationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        notificationListView = findViewById(R.id.notificationListView);
        notificationListView.setAdapter(notificationAdapter);

        // Pobieranie powiadomień i wyświetlanie ich na liście
        fetchNotifications();

        // Obsługa kliknięcia na przycisk do dodawania powiadomienia
        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Wywołaj funkcję do wyświetlenia szczegółów powiadomienia w oknie dialogowym
                showNotificationDetails(position);
            }
        });
    }

    private void showSystemNotification(String title, String content) {
        // Tworzenie identyfikatora kanału powiadomień (potrzebne tylko dla Androida 8.0 i nowszych)
        String channelId = "my_channel_id";
        String channelName = "My Channel";

        // Utworzenie menedżera powiadomień
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Sprawdzenie, czy Android 8.0 lub nowszy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Tworzenie kanału powiadomień (potrzebne tylko dla Androida 8.0 i nowszych)
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Tworzenie powiadomienia
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.background) // Ikona powiadomienia (może być zastąpiona przez odpowiednią ikonę)
                .setContentTitle(title) // Tytuł powiadomienia
                .setContentText(content) // Treść powiadomienia
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priorytet powiadomienia (domyślny)
                .setAutoCancel(true); // Powiadomienie automatycznie zniknie po kliknięciu

        // Wyświetlenie powiadomienia
        notificationManager.notify(0, builder.build());
    }

    // Metoda do wyświetlania szczegółów powiadomienia w oknie dialogowym
    private void showNotificationDetails(int position) {
        try {
            JSONObject notification = new JSONArray(responseData).getJSONObject(position);
            String description = notification.getString("description");
            String deviceID = notification.getString("device_id");
            String sensorID = notification.getString("sensor_id");
            String served = notification.getString("served");
            String notificationID = notification.getString("id");
            String date = notification.getString("date");
            String priority = notification.getString("priority");

            StringBuilder details = new StringBuilder();
            details.append("Description: ").append(description).append("\n").append("Device ID: ").append(deviceID).append("\n").append("Sensor ID: ").append(sensorID).append("\n").append("Served: ").append(served).append("\n").append("Notification ID: ").append(notificationID).append("\n").append("Date: ").append(date).append("\n").append("Priority: ").append(priority).append("\n");

            // Utwórz i wyświetl okno dialogowe z detalami powiadomienia
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Notification Details").setMessage(details.toString()).setPositiveButton("Change Status", (dialog, which) -> {
                        // Wywołaj funkcję do zmiany statusu powiadomienia
                        changeNotificationStatus(notificationID);
                    }).setNegativeButton("Delete", (dialog, which) -> {
                        // Wywołaj funkcję do usunięcia powiadomienia
                        deleteNotification(notificationID);
                    }).setNeutralButton("Cancel", null) // Przycisk Anuluj bez dodatkowej akcji
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }// Metoda do usuwania powiadomienia o podanym ID

    private void deleteNotification(String notificationId) {
        String url = BASE_URL + "/" + notificationId;
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Błąd usuwania powiadomienia", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Powiadomienie usunięte", Toast.LENGTH_SHORT).show();
                            fetchNotifications(); // Odświeżenie listy po usunięciu
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Błąd usuwania powiadomienia", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    // Metoda do zmiany statusu powiadomienia
    private void changeNotificationStatus(String notificationID) {
        String url = BASE_URL + "/" + notificationID;

        // Przykładowy kod dla żądania PUT:
        JSONObject json = new JSONObject();
        try {
            json.put("served", true); // Set the served status to true for changing the status
            // Add other fields if needed
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token).put(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Błąd zmiany statusu powiadomienia", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Status powiadomienia zmieniony", Toast.LENGTH_SHORT).show();
                            fetchNotifications(); // Odświeżenie listy po zmianie statusu
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Błąd zmiany statusu powiadomienia", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    // Metoda do pobierania powiadomień z serwera
    private void fetchNotifications() {
        String url = BASE_URL + "?sort_by_priority=true&only_served=false";
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Błąd pobierania powiadomień", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    responseData = response.body().string(); // Store the response data for later use
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Parsowanie odpowiedzi JSON i aktualizacja listy powiadomień
                                notificationList.clear();
                                JSONArray jsonArray = new JSONArray(responseData);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject notification = jsonArray.getJSONObject(i);
                                    String description = notification.getString("description");
                                    notificationList.add(description);

                                }
                                notificationAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Błąd pobierania powiadomień", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            // Metoda do dodawania nowego powiadomienia
            private void addNotification() {
                // Tutaj możesz zaimplementować logikę dodawania nowego powiadomienia do serwera.
                // Wysłanie żądania POST z danymi nowego powiadomienia i odświeżenie listy powiadomień po zakończeniu.

                // Przykładowy kod dla żądania POST:

                JSONObject json = new JSONObject();
                try {
                    json.put("description", "Nowe powiadomienie");
                    json.put("priority", 1);
                    // Dodaj inne pola związane z powiadomieniem
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody requestBody = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder().url(BASE_URL).header("Authorization", "Bearer " + token).post(requestBody).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(NotificationsActivity.this, "Błąd dodawania powiadomienia", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NotificationsActivity.this, "Dodano nowe powiadomienie", Toast.LENGTH_SHORT).show();
                                    fetchNotifications(); // Odświeżenie listy po dodaniu
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NotificationsActivity.this, "Błąd dodawania powiadomienia", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

            }

            // Metoda do usuwania powiadomienia o podanym ID
            private void deleteNotification(int notificationId) {
                String url = BASE_URL + "/" + notificationId;
                Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token).delete().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(NotificationsActivity.this, "Błąd usuwania powiadomienia", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NotificationsActivity.this, "Powiadomienie usunięte", Toast.LENGTH_SHORT).show();
                                    fetchNotifications(); // Odświeżenie listy po usunięciu
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NotificationsActivity.this, "Błąd usuwania powiadomienia", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}