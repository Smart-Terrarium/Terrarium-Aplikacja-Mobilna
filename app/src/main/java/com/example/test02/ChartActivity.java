package com.example.test02;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.Iterator;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.Response;

import tech.gusavila92.websocketclient.WebSocketClient;


public class ChartActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;
    private LineChart lineChart;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<String> formattedTimestamps = new ArrayList<>();
    private String mAuthToken;
    private String selectedDeviceId;
    private String selectedSensorId;
    private Spinner deviceSpinner;
    private Spinner sensorSpinner;
    private List<String> deviceNames = new ArrayList<>();
    private List<String> sensorNames = new ArrayList<>();
    private String selectedDevice;

    class Sensor {
        private final String pin_number;
        private final String timestamp;
        private final double value;
        private String formattedTimestamp;

        public Sensor(String pin_number, String timestamp, double value) {
            this.pin_number = pin_number;
            this.timestamp = timestamp;
            this.value = value;
            this.formattedTimestamp = "";
        }
        public String getPin_number(){
            return pin_number;
        }



        public String getTimestamp() {
            return timestamp;
        }

        public double getValue() {
            return value;
        }
    }

    class Device {
        private final String id;
        private final List<Sensor> sensors;

        public Device(String id) {
            this.id = id;
            this.sensors = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public List<Sensor> getSensors() {
            return sensors;
        }
    }

    private List<String> deviceIds = new ArrayList<>();

    private List<String> getListDeviceIds(String token) {
        Request request = new Request.Builder().url(MainActivity.BaseUrl.BASE_URL + "/devices").header("Authorization", "Bearer " + token).build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        String responseBody = response.body().string();
                        JSONArray responseJson = null;
                        try {
                            responseJson = new JSONArray(responseBody);
                            for (int i = 0; i < responseJson.length(); i++) {
                                JSONObject deviceJson = responseJson.getJSONObject(i);
                                int deviceId = deviceJson.getInt("id");
                                String deviceName = deviceJson.getString("mac_address");
                                deviceNames.add(deviceName);
                                deviceIds.add(String.valueOf(deviceId));
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(ChartActivity.this, android.R.layout.simple_spinner_item, deviceNames);
                                    deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    deviceSpinner.setAdapter(deviceAdapter);

                                    // Zaznacz wartość w sensorSpinner, jeśli jest dostępna
                                    if (deviceNames.contains(selectedDeviceId)) {
                                        int selectedDeviceIndex = deviceNames.indexOf(selectedDeviceId);
                                        sensorSpinner.setSelection(selectedDeviceIndex);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // System.out.println(responseJson.toString());
                    } else {
                        System.out.println("problem");
                    }
                }
            }
        });

        return deviceIds;
    }

    private void getListSensorsIds(String token, String selectedDevice) {
        String url = MainActivity.BaseUrl.BASE_URL + "/device/" + selectedDevice;
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token).build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println(selectedDevice);
                    if (response.code() == 200) {
                        String responseBody = response.body().string();
                        JSONArray responseJson = null;
                        try {
                            responseJson = new JSONObject(responseBody).getJSONArray("sensors");
                            sensorNames.clear();
                            for (int i = 0; i < responseJson.length(); i++) {
                                JSONObject sensorJson = responseJson.getJSONObject(i);
                                String sensorName = sensorJson.getString("pin_number");
                                sensorNames.add(sensorName);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<String> sensorAdapter = new ArrayAdapter<>(ChartActivity.this, android.R.layout.simple_spinner_item, sensorNames);
                                    sensorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    sensorSpinner.setAdapter(sensorAdapter);

                                    // Zaznacz wartość w sensorSpinner, jeśli jest dostępna
                                    if (sensorNames.contains(selectedSensorId)) {
                                        int selectedSensorIndex = sensorNames.indexOf(selectedSensorId);
                                        sensorSpinner.setSelection(selectedSensorIndex);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println(responseJson.toString());
                    } else {
                        System.out.println("problem");
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        lineChart = findViewById(R.id.lineChart);
        deviceSpinner = findViewById(R.id.deviceSpinner);
        sensorSpinner = findViewById(R.id.sensorSpinner);
        mAuthToken  = getIntent().getStringExtra("auth_token");
        System.out.println("Token: " + mAuthToken);

        getListDeviceIds(mAuthToken);
        createWebSocketClient();

        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position >= 0 && position < deviceIds.size()) {
                    selectedDevice = deviceIds.get(position);
                    getListSensorsIds(mAuthToken, selectedDevice);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                if (position <sensorNames.size()) {
                    selectedSensorId = sensorNames.get(position);
                    if(deviceNames.size()!=0)
                    selectedDeviceId = deviceNames.get(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void createWebSocketClient() {
        URI uri;
        try {
            uri = new URI(MainActivity.BaseWebsocketUrl.BASE_WEBSOCKET_URL + "/device/1/sensor/data");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Rozpoczęto sesję");
                JSONObject json = new JSONObject();
                try {
                    json.put("token", mAuthToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocketClient.send(json.toString());  // Wysłanie wiadomości w formacie JSON

            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Odebrano wiadomość");
                System.out.println(s);
                List<Device> devices = new ArrayList<>();
                System.out.println("SENSORRRRR " + selectedSensorId);
                System.out.println("deviceeeee " + selectedDeviceId);
                try {
                    // Parsowanie otrzymanego ciągu JSON
                    JSONObject json = new JSONObject(s);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String id = keys.next();
                        JSONObject deviceObject = json.getJSONObject(id);
                        System.out.println("SENSORRRRR " + selectedSensorId);
                        System.out.println("deviceeeee " + selectedDeviceId);
                        // Tworzenie obiektu urządzenia
                        Device device = new Device(id);
                        // Iteracja po czujnikach w danym urządzeniu
                        Iterator<String> sensorKeys = deviceObject.keys();
                        while (sensorKeys.hasNext()) {
                            String sensorId = sensorKeys.next();
                            JSONObject sensorObject = deviceObject.getJSONObject(sensorId);

                            // Tworzenie obiektu czujnika i dodawanie go do listy czujników urządzenia
                            Sensor sensor = new Sensor(sensorId, sensorObject.getString("timestamp"), sensorObject.getDouble("value"));
                            device.getSensors().add(sensor);
                        }

                        // Dodawanie urządzenia do listy urządzeń
                        devices.add(device);
                    }

                    // Przetwarzanie danych czujników
                    for (Device device : devices) {
                        //  System.out.println("ID urządzenia: " + device.getId());
                        List<Sensor> sensors = device.getSensors();
                        for (Sensor sensor : sensors) {
                                System.out.println("ID czujnika: " + sensor.getPin_number());
                            //    System.out.println("Timestamp: " + sensor.getTimestamp());
                            //   System.out.println("Wartość: " + sensor.getValue());
                            String timestampString = sensor.getTimestamp();
                            try {
                                Date timestamp = inputFormat.parse(timestampString);
                                String formattedTimestamp = outputFormat.format(timestamp);
                                //      System.out.println("Przetworzony timestamp: " + formattedTimestamp);

                                if (device.getId().equals(selectedDeviceId) && sensor.getPin_number().equals(selectedSensorId)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addEntryToChart(sensor.getValue(), formattedTimestamp);
                                            updateChartAxis(formattedTimestamp);
                                        }
                                    });
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (JSONException e) {
                    Log.e("WebSocket", "Błąd podczas parsowania JSON", e);
                }
            }

            // Metoda do dodawania wpisu do wykresu
            private void addEntryToChart(double value, String timestamp) {
                if (lineDataSet == null) {
                    lineDataSet = new LineDataSet(null, "Czujnik " + selectedSensorId);
                    lineDataSet.setValueTextSize(20f);
                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                    lineDataSet.setColor(Color.parseColor("#2196F3"));  // Ustawienie koloru linii
                    lineDataSet.setCircleColor(Color.parseColor("#2196F3"));  // Ustawienie koloru punktów
                    lineDataSet.setLineWidth(3f);
                    lineDataSet.setCircleRadius(4f);
                    lineDataSet.setDrawFilled(true);  // Włączenie wypełnienia obszaru pod linią
                    lineDataSet.setFillAlpha(65);
                    lineDataSet.setFillColor(Color.parseColor("#2196F3"));  // Ustawienie koloru wypełnienia
                    lineDataSet.setHighLightColor(Color.rgb(244, 117, 117));
                    lineDataSet.setValueTextColor(Color.BLUE);
                    lineDataSet.setValueTextSize(20f);

                    YAxis leftAxis = lineChart.getAxisLeft();
                    leftAxis.setTextSize(15f);  // Zmiana rozmiaru czcionki dla etykiet osi Y

                    YAxis rightAxis = lineChart.getAxisRight();
                    rightAxis.setEnabled(false);  // Wyłączenie prawej osi

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setTextSize(15f);  // Zmiana rozmiaru czcionki dla etykiet osi X

                    lineData = new LineData(lineDataSet);
                    lineChart.setData(lineData);

                    lineChart.setDrawGridBackground(true);  // Włączenie tła siatki
                    lineChart.setGridBackgroundColor(Color.LTGRAY);  // Ustawienie koloru tła siatki
                    lineChart.getAxisLeft().setGridColor(Color.WHITE);  // Ustawienie koloru linii siatki dla osi Y
                    lineChart.getXAxis().setGridColor(Color.WHITE);  // Ustawienie koloru linii siatki dla osi X

                    lineChart.setBorderColor(Color.LTGRAY);  // Ustawienie koloru ramki

                    Legend legend = lineChart.getLegend();
                    legend.setEnabled(true);
                    legend.setTextColor(Color.WHITE);
                    legend.setTextSize(15f);

                    lineChart.getDescription().setEnabled(false);  // Wyłączenie opisu wykresu
                    lineChart.setDrawBorders(true);
                    lineChart.setBorderColor(Color.BLACK);
                    lineChart.setBorderWidth(5f);

                    lineChart.setBackgroundColor(Color.DKGRAY);

                    lineChart.getXAxis().setTextColor(Color.WHITE);
                    lineChart.getAxisLeft().setTextColor(Color.WHITE);
                    lineChart.getAxisRight().setTextColor(Color.WHITE);

                    lineChart.getXAxis().setAxisLineWidth(2f);
                    lineChart.getAxisLeft().setAxisLineWidth(2f);
                    lineChart.getAxisRight().setAxisLineWidth(2f);

                    lineChart.getXAxis().setGridLineWidth(2f);
                    lineChart.getAxisLeft().setGridLineWidth(2f);
                    lineChart.getAxisRight().setGridLineWidth(2f);

                    lineChart.moveViewToX(lineData.getEntryCount());
                    lineChart.getXAxis().setLabelRotationAngle(-45f);
                }

                lineData.addEntry(new Entry(lineDataSet.getEntryCount(), (float) value), 0);
                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.setVisibleXRangeMaximum(10);
                lineChart.moveViewToX(lineData.getEntryCount());
            }


            private void updateChartAxis(String formattedTimestamp) {
                formattedTimestamps.add(formattedTimestamp);

                lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        int index = (int) value;
                        if (index >= 0 && index < formattedTimestamps.size()) {
                            return formattedTimestamps.get(index);
                        } else {
                            return "";
                        }
                    }
                });

                lineChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
            }

            @Override
            public void onBinaryReceived(byte[] data) {
                Log.i("WebSocket", "Odebrano dane binarne");
            }

            @Override
            public void onPingReceived(byte[] data) {
               // Log.i("WebSocket", "Odebrano ping");
            }

            @Override
            public void onPongReceived(byte[] data) {
                Log.i("WebSocket", "Odebrano pong");
            }

            @Override
            public void onException(Exception e) {
                Log.e("WebSocket", "Wystąpił wyjątek", e);
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Zakończono sesję");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }  @Override
    protected void onPause() {
        super.onPause();
        closeWebSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeWebSocket();
    }

    private void closeWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }
}
