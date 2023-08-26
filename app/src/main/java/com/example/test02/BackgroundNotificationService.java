package com.example.test02;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class BackgroundNotificationService extends Service {

    private String token;
    private MainActivity.BaseUrl baseUrlManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            token = intent.getStringExtra("auth_token");
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }

        baseUrlManager = new MainActivity.BaseUrl();

        new Thread(new Runnable() {
            public void run() {
                startSSEConnection();
            }
        }).start();

        return START_STICKY;
    }

    private void startSSEConnection() {
        try {
            URL url = new URL("http://" + baseUrlManager.getBaseUrl(this) + ":8000/devices/notifier/alerts");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "text/event-stream");
            System.out.println(token + "TTTTTTTTTTTTTT");


            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String eventData = line.substring(6).trim();
                        processEventData(eventData);
                    }
                }
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processEventData(String eventData) {
        try {
            Log.d("SSE", "Received event data: " + eventData);


            System.out.println("Event Data: " + eventData);


            eventData = eventData.replace("macasdasas", "");

            JSONObject eventJson = new JSONObject(eventData);
            String type = eventJson.optString("type");
            if ("report".equals(type)) {
                JSONObject payload = eventJson.optJSONObject("payload");
                if (payload != null) {
                    JSONObject alert = payload.optJSONObject("alert");
                    if (alert != null) {
                        String macAddress = alert.optString("mac_address");
                        int alertNumber = alert.optInt("alert_number");
                        String description = alert.optString("description");
                        int priority = alert.optInt("priority");


                        showSystemNotification("Alert: " + description, "Priority: " + priority);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SSE", "Error parsing JSON: " + e.getMessage());
        }
    }



    private void showSystemNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            CharSequence channelName = "Channel Name";
            String channelDescription = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setDescription(channelDescription);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        int notificationId = 1;
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
