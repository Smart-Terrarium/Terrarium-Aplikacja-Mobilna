package com.example.test02;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationsActivity extends AppCompatActivity {

    private String token;
    private List<String> notificationList;
    private ArrayAdapter<String> notificationAdapter;
    private ListView notificationListView;
    private String responseData;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient mHttpClient;
    private MainActivity.BaseUrl baseUrlManager;
    private String BASE_URL;

    // Filtry
    private boolean sortByPriority = true;
    private boolean onlyServed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mHttpClient = new OkHttpClient();

        baseUrlManager = new MainActivity.BaseUrl();
        BASE_URL = baseUrlManager.getBaseUrl(this) + ":8000/devices/alerts";

        token = getIntent().getStringExtra("auth_token");
        notificationList = new ArrayList<>();
        notificationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        notificationListView = findViewById(R.id.notificationListView);
        notificationListView.setAdapter(notificationAdapter);



        // Wywołanie fetchNotifications() PO trustAllCertificates()
        fetchNotifications();

        // Obsługa kliknięcia na element listy powiadomień
        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showNotificationDetails(position);
            }
        });

        findViewById(R.id.resetListButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetNotificationList();
            }
        });

        findViewById(R.id.onlyServedToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOnlyServedToggle(v);
            }
        });
    }


    private void resetNotificationList() {
        // Wyzeruj filtry i odśwież listę
        sortByPriority = true;
        onlyServed = false;
        applyFilters();
    }

    // Metoda do ustawiania filtrów i odświeżania listy
    private void applyFilters() {
        fetchNotifications();
    }


    // Obsługa kliknięcia przycisku filtrowania pokazującego tylko obsłużone
    public void onOnlyServedToggle(View view) {
        onlyServed = !onlyServed;
        applyFilters();
    }


    // Wyświetlanie szczegółów powiadomienia w oknie dialogowym
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

            // Tworzenie tekstu ze szczegółami powiadomienia
            StringBuilder details = new StringBuilder();
            details.append("Description: ").append(description).append("\n")
                    .append("Device ID: ").append(deviceID).append("\n")
                    .append("Sensor ID: ").append(sensorID).append("\n")
                    .append("Served: ").append(served).append("\n")
                    .append("Notification ID: ").append(notificationID).append("\n")
                    .append("Date: ").append(date).append("\n")
                    .append("Priority: ").append(priority).append("\n");

            // Utworzenie i wyświetlenie okna dialogowego ze szczegółami powiadomienia
            new AlertDialog.Builder(this)
                    .setTitle("Notification Details")
                    .setMessage(details.toString())
                    .setPositiveButton("Change Status", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Wywołanie funkcji do zmiany statusu powiadomienia
                            changeNotificationStatus(notificationID);
                        }
                    })
                    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Wywołanie funkcji do usunięcia powiadomienia
                            deleteNotification(notificationID);
                        }
                    })
                    .setNeutralButton("Cancel", null) // Przycisk "Cancel" bez dodatkowej akcji
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Metoda do usuwania powiadomienia o podanym ID
    private void deleteNotification(String notificationId) {
        String url = "http://" +  BASE_URL + "/" + notificationId;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .delete()
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Error deleting notification", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Notification deleted", Toast.LENGTH_SHORT).show();
                            fetchNotifications(); // Odświeżenie listy po usunięciu
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Error deleting notification", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // Metoda do zmiany statusu powiadomienia
    private void changeNotificationStatus(String notificationID) {
        String url = "http://" +  BASE_URL + "/" +  notificationID;
        System.out.println(url + "  URLLL");
        JSONObject json = new JSONObject();
        try {
            json.put("served", true); // Ustawienie statusu na true w celu zmiany statusu
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .put(requestBody)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Error changing notification status", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Notification status changed", Toast.LENGTH_SHORT).show();
                            fetchNotifications(); // Odświeżenie listy po zmianie statusu
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NotificationsActivity.this, "Error changing notification status", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // Metoda do pobierania powiadomień z serwera
    private void fetchNotifications() {
        String url = "http://" + BASE_URL +
                "?sort_by_priority=" + sortByPriority +
                "&only_served=" + onlyServed;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NotificationsActivity.this, "Error fetching notifications", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    responseData = response.body().string(); // Zapisanie danych odpowiedzi do późniejszego użycia
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
                            Toast.makeText(NotificationsActivity.this, "Error fetching notifications", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }}