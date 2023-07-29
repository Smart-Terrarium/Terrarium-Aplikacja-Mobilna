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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BackgroundNotificationService extends Service {

    private String token;
    private static final long INTERVAL = 1000 * 60 * 15; // 15 minut
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Sprawdzanie nowych powiadomień
            new CheckNotificationsTask().execute();

            handler.postDelayed(this, INTERVAL);
        }
    };

    // Lista przechowująca identyfikatory wcześniej wyświetlonych powiadomień
    private ArrayList<Integer> displayedNotificationIds = new ArrayList<>();

    private class CheckNotificationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Sprawdzanie nowych powiadomień
            checkNotifications();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Możemy wykonać dodatkowe działania po zakończeniu sprawdzania powiadomień, jeśli to potrzebne
        }
    }

    private void checkNotifications() {
        // Tworzenie zapytania HTTP GET do pobrania powiadomień
        String apiUrl = MainActivity.BaseUrl.BASE_URL + "/devices/alerts?sort_by_priority=true&only_served=false";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);

            // Odczytanie odpowiedzi
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            String response = stringBuilder.toString();

            // Przetworzenie odpowiedzi JSON
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject alertObject = jsonArray.getJSONObject(i);
                boolean served = alertObject.getBoolean("served");
                int notificationId = alertObject.getInt("id"); // Pobranie identyfikatora powiadomienia

                if (!served && !displayedNotificationIds.contains(notificationId)) {
                    // Jeżeli powiadomienie nie zostało obsłużone i nie zostało wcześniej wyświetlone,
                    // pokaż je jako systemowe powiadomienie
                    String description = alertObject.getString("description");
                    showSystemNotification("Nowe powiadomienie", description);
                    displayedNotificationIds.add(notificationId); // Dodanie identyfikatora do listy wyświetlonych powiadomień
                }
            }
        } catch (IOException | JSONException e) {
            Log.e("BackgroundNotification", "Błąd podczas pobierania powiadomień: " + e.getMessage());
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Pobranie tokenu z Intentu
        if (intent != null) {
            token = intent.getStringExtra("auth_token");
        }

        // Rozpoczęcie cyklicznego sprawdzania powiadomień
        handler.postDelayed(runnable, INTERVAL);

        // Start the service as a foreground service with a notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            CharSequence channelName = "Channel Name";
            String channelDescription = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setDescription(channelDescription);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("Foreground Service")
                    .setContentText("Service is running in the background")
                    .setSmallIcon(R.drawable.background) // Replace with your notification icon
                    .build();

            startForeground(1, notification);
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        // Remove the foreground state and stop the service
        stopForeground(true);
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showSystemNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Channel (Required for Android Oreo and above)
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

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.background) // Replace with your notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        int notificationId = 1;
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }



}
