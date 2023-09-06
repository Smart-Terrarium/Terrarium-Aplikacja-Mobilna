package com.example.SmartTerrariumAplikacjaMobilna;

import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import androidx.core.app.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class BackgroundNotificationService extends Service {

    private static final int NOTIFICATION_ID = 100;
    public static final String CLOSE_NOTIFICATION_ACTION = "com.example.SmartTerrariumAplikacjaMobilna.CLOSE_NOTIFICATION";

    private String token;
    private MainActivity.BaseUrl baseUrlManager;

    private List<String> deviceNames = new ArrayList<>();
    private List<String> deviceIds = new ArrayList<>();
    private Map<String, List<String>> deviceToSensorsMap = new HashMap<>();

    private List<Notification> notificationList = new ArrayList<>();

    private CloseNotificationReceiver closeNotificationReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            token = intent.getStringExtra("auth_token");
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }

        baseUrlManager = new MainActivity.BaseUrl();

        createNotificationChannel();
        Notification notification = buildForegroundServiceNotification("Foreground Service", "Service notifications are running");
        startForeground(NOTIFICATION_ID, notification);

        closeNotificationReceiver = new CloseNotificationReceiver();
        IntentFilter closeNotificationFilter = new IntentFilter(CLOSE_NOTIFICATION_ACTION);
        registerReceiver(closeNotificationReceiver, closeNotificationFilter);

        new Thread(new Runnable() {
            public void run() {
                getListDeviceIdsAndSensors(token);
                startSSEConnection();
            }
        }).start();

        return START_STICKY;
    }

    private void createNotificationChannel() {
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
        }
    }

    private Notification buildForegroundServiceNotification(String title, String message) {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);


        return builder.build();
    }


    private Notification buildNotification(String title, String message) {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Intent closeNotificationIntent = new Intent(BackgroundNotificationService.CLOSE_NOTIFICATION_ACTION);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                closeNotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .addAction(R.drawable.logo, "Clear", closePendingIntent);

        return builder.build();
    }

    private void startSSEConnection() {
        try {
            URL url = new URL("http://" + baseUrlManager.getBaseUrl(this) + ":8000/devices/notifier/alerts");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "text/event-stream");

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
            int jsonStartIndex = eventData.indexOf('{');
            if (jsonStartIndex >= 0) {
                String jsonPart = eventData.substring(jsonStartIndex);
                JSONObject eventJson = new JSONObject(jsonPart);

                String type = eventJson.optString("type");
                if ("report".equals(type)) {
                    JSONObject payload = eventJson.optJSONObject("payload");
                    if (payload != null) {
                        JSONObject alert = payload.optJSONObject("alert");
                        if (alert != null) {
                            String macAddress = alert.optString("mac_address");
                            String description = alert.optString("description");
                            int priority = alert.optInt("priority");

                            String deviceName = getDeviceNameByMac(macAddress);
                            List<String> sensorNames = deviceToSensorsMap.get(macAddress);

                            Notification notification = buildNotification("Alert: " + description,
                                    "Device: " + deviceName + ", Sensors: " + sensorNames + ", Priority: " + priority);
                            notificationList.add(notification);
                            updateSystemNotifications();
                        }
                    }
                }
            } else {
                Log.e("SSE", "Invalid event data format: " + eventData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SSE", "Error parsing JSON: " + e.getMessage());
        }
    }

    private String getDeviceNameByMac(String macAddress) {
        int index = deviceIds.indexOf(macAddress);
        if (index != -1 && index < deviceNames.size()) {
            return deviceNames.get(index);
        }
        return "Unknown Device";
    }

    private void getListDeviceIdsAndSensors(String token) {
        getListDeviceIds(token);
        for (String deviceId : deviceIds) {
            getListSensorIds(token, deviceId);
        }
    }

    private void getListDeviceIds(String token) {
        String baseUrl = baseUrlManager.getBaseUrl(this);
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "http://" + baseUrl;
        }

        String url = baseUrl + ":8000/devices";
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray devicesArray = new JSONArray(response.toString());
                for (int i = 0; i < devicesArray.length(); i++) {
                    JSONObject deviceJson = devicesArray.getJSONObject(i);
                    int deviceId = deviceJson.getInt("id");
                    String deviceName = deviceJson.getString("mac_address");
                    deviceNames.add(deviceName);
                    deviceIds.add(String.valueOf(deviceId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void getListSensorIds(String token, String selectedDevice) {
        String baseUrl = baseUrlManager.getBaseUrl(this);
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "http://" + baseUrl;
        }

        String url = baseUrl + ":8000/device/" + selectedDevice;
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray sensorsArray = new JSONObject(response.toString()).getJSONArray("sensors");
                List<String> sensorNames = new ArrayList<>();
                for (int i = 0; i < sensorsArray.length(); i++) {
                    JSONObject sensorJson = sensorsArray.getJSONObject(i);
                    String sensorName = sensorJson.getString("pin_number");
                    sensorNames.add(sensorName);
                }
                deviceToSensorsMap.put(selectedDevice, sensorNames);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void updateSystemNotifications() {
        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        for (int i = 0; i < notificationList.size(); i++) {
            notificationManager.notify(i + 1, notificationList.get(i));
        }
    }

    public class CloseNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CLOSE_NOTIFICATION_ACTION.equals(intent.getAction())) {
                clearNotifications();
                Log.d("NotificationService", "Close Notification Action Received");
            }
        }
    }

    public void clearNotifications() {
        notificationList.clear();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.d("NotificationService", "Notifications cleared");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (closeNotificationReceiver != null) {
            unregisterReceiver(closeNotificationReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
