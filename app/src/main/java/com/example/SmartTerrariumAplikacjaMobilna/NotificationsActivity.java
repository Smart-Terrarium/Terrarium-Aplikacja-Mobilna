package com.example.SmartTerrariumAplikacjaMobilna;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

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

    private boolean sortByPriority = true;
    private boolean onlyServed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mHttpClient = new OkHttpClient();

        baseUrlManager = new MainActivity.BaseUrl();
        BASE_URL = baseUrlManager.getBaseUrl(this) + ":8000/devices/alerts";

        token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", null);
        notificationList = new ArrayList<>();
        notificationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        notificationListView = findViewById(R.id.notificationListView);
        notificationListView.setAdapter(notificationAdapter);

        fetchNotifications();



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

        Button backToUserButton = findViewById(R.id.backToUserButton);
        backToUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(NotificationsActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

    }

    private void resetNotificationList() {
        onlyServed = false;
        applyFilters();
    }

    private void applyFilters() {
        fetchNotifications();
    }

    public void onOnlyServedToggle(View view) {
        onlyServed = true;
        applyFilters();
    }

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
            details.append("Description: ").append(description).append("\n")
                    .append("Device ID: ").append(deviceID).append("\n")
                    .append("Sensor ID: ").append(sensorID).append("\n")
                    .append("Served: ").append(served).append("\n")
                    .append("Notification ID: ").append(notificationID).append("\n")
                    .append("Date: ").append(date).append("\n")
                    .append("Priority: ").append(priority).append("\n");

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Notification Details")
                    .setMessage(details.toString())
                    .setNeutralButton("Cancel", null);

            if (served.equals("true")) {
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteNotification(notificationID);
                    }
                });

            }

            if (served.equals("false")) {
                builder.setNegativeButton("Change Status", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeNotificationStatus(notificationID);
                    }
                });
            }

            builder.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deleteNotification(String notificationId) {
        String url = "http://" + BASE_URL + "/" + notificationId;

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
                            fetchNotifications();
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

    private void changeNotificationStatus(String notificationID) {
        String url = "http://" + BASE_URL + "/" + notificationID;
        JSONObject json = new JSONObject();
        try {
            json.put("served", true);
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
                            fetchNotifications();
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

    private void fetchNotifications() {
        String url = "http://" + BASE_URL +
                "?sort_by_priority=" + sortByPriority;
        if (onlyServed) {
            url += "&only_served=true";
        } else {
            url += "&only_not_served=true";
            url += "&sort_by_served=false";
        }

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
                    responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                notificationList.clear();
                                JSONArray jsonArray = new JSONArray(responseData);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject notification = jsonArray.getJSONObject(i);
                                    String description = notification.getString("description");
                                    String served = notification.getString("served");

                                    if (served.equals("false")) {
                                        description = "New Alert! Click for check details\n" + description;
                                    }

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
    }
}