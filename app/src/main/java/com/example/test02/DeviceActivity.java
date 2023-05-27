package com.example.test02;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

public class DeviceActivity extends AppCompatActivity {

    private ListView mDeviceListView;

    public class Device {
        private String name;
        private String userId;

        public Device(String name, String userId) {
            this.name = name;
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public String getUserId() {
            return userId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        mDeviceListView = findViewById(R.id.deviceListView);

        // Odczytaj token z Intentu
        String token = getIntent().getStringExtra("auth_token");

        // Wykonaj zapytanie do serwera, aby pobrać listę urządzeń i czujników dla danego użytkownika
        new GetDevicesTask().execute(token);
    }

    private void displayDevices(List<String> userDeviceNames) {
        // Wyświetl listę urządzeń użytkownika w widoku ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userDeviceNames);
        mDeviceListView.setAdapter(adapter);
    }

    private class GetDevicesTask extends AsyncTask<String, Void, List<Device>> {

        @Override
        protected List<Device> doInBackground(String... tokens) {
            String token = tokens[0];

            List<Device> userDevices = new ArrayList<>();
            String userId = "123"; // Identyfikator zalogowanego użytkownika

            try {
                // Tworzenie URL na podstawie endpointu
                URL url = new URL("http://10.0.2.2:8000/device");

                // Nawiązanie połączenia HTTP
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                // Odczytanie odpowiedzi serwera
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufferedReader.close();

                    // Przetworzenie odpowiedzi z serwera
                    JSONArray devicesArray = new JSONArray(response.toString());
                    for (int i = 0; i < devicesArray.length(); i++) {
                        JSONObject deviceObject = devicesArray.getJSONObject(i);
                        String deviceName = deviceObject.getString("name");
                        String deviceUserId = deviceObject.getString("user_id");

                        if (deviceUserId.equals(userId)) {
                            // Jeśli urządzenie należy do zalogowanego użytkownika, dodaj je do listy
                            Device device = new Device(deviceName, deviceUserId);
                            userDevices.add(device);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return userDevices;
        }

        @Override
        protected void onPostExecute(List<Device> userDevices) {
            // Wywołane po zakończeniu zadania w tle

            // Wybierz tylko urządzenia i czujniki należące do zalogowanego użytkownika
            List<String> userDeviceNames = filterUserDevices(userDevices);

            // Wyświetl listę urządzeń użytkownika w widoku ListView
            displayDevices(userDeviceNames);
        }
    }

    private List<String> filterUserDevices(List<Device> userDevices) {
        // Zaimplementuj logikę filtrowania urządzeń i czujników, aby wybrać tylko te należące do zalogowanego użytkownika
        // Możesz wykorzystać identyfikator użytkownika lub inne kryteria do filtracji

        List<String> userDeviceNames = new ArrayList<>();
        String userId = "123"; // Identyfikator zalogowanego użytkownika

        for (Device device : userDevices) {
            if (device.getUserId().equals(userId)) {
                // Jeśli urządzenie należy do zalogowanego użytkownika, dodaj jego nazwę do listy
                userDeviceNames.add(device.getName());
            }
        }

        return userDeviceNames;
    }
}