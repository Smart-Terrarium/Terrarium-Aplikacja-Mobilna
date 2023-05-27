package com.example.test02;

import android.annotation.SuppressLint;
import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;
import java.net.URISyntaxException;
import tech.gusavila92.websocketclient.WebSocketClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import android.graphics.Color;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.Legend;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;

import android.view.View;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;


public class ChartActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;
    private LineChart lineChart;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<String> formattedTimestamps = new ArrayList<>();
    private String token;
    private TextView mTokenTextView;
    private Spinner sensorSpinner;
    private ArrayAdapter<String> sensorAdapter;
    private List<String> sensorList;
    private String selectedSensorId = ""; // Przechowuje aktualnie wybrany identyfikator sensora

    private boolean isSensorListUpdated = false;
    private int selectedSensorIndex = -1; // Indeks aktualnie wybranego sensora

    class Sensor {
        private final String id;
        private final String timestamp;
        private final double value;
        private String formattedTimestamp;

        public Sensor(String id, String timestamp, double value) {
            this.id = id;
            this.timestamp = timestamp;
            this.value = value;
            this.formattedTimestamp = "";
        }

        public String getId() {
            return id;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        lineChart = findViewById(R.id.lineChart);
        sensorSpinner = findViewById(R.id.sensorSpinner);
        token = getIntent().getStringExtra("auth_token");
        mTokenTextView = findViewById(R.id.tokenTextView);

        token = getIntent().getStringExtra("auth_token");
        mTokenTextView.setText(token);

        createWebSocketClient();


    }

    private void createWebSocketClient() {
        URI uri;
        try {
            uri = new URI("ws://192.168.88.252:8000/data");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            Boolean updated =false;

            @Override
            public void onOpen() {
                Log.i("WebSocket", "Rozpoczęto sesję");
                webSocketClient.send("Hello World!");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Odebrano wiadomość");
                List<Device> devices = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(s);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String id = keys.next();
                        JSONObject deviceObject = json.getJSONObject(id);

                        Device device = new Device(id);
                        Iterator<String> sensorKeys = deviceObject.keys();
                        while (sensorKeys.hasNext()) {
                            String sensorId = sensorKeys.next();
                            JSONObject sensorObject = deviceObject.getJSONObject(sensorId);

                            Sensor sensor = new Sensor(sensorId, sensorObject.getString("timestamp"), sensorObject.getDouble("value"));
                            device.getSensors().add(sensor);
                        }

                        devices.add(device);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateSensorList(devices, updated);
                            updated =true;
                        }
                    });

                } catch (JSONException e) {
                    Log.e("WebSocket", "Błąd podczas parsowania JSON", e);
                }
            }

            private Set<String> availableSensors = new HashSet<>();
            private Set<String> buildSetWithDevices(List<Device> devices)
            {
                Set<String> out = new HashSet<>();
                for (Device device : devices) {
                    List<Sensor> sensors = device.getSensors();
                    for (Sensor sensor : sensors) {
                        String sensorName = "Device: " + device.getId() + ", Sensor: " + sensor.getId();
                        out.add(sensorName); // Dodaj sensor do zbioru dostępnych sensorów
                    }
                }
                return out;
            }
            private void updateSensorList(List<Device> devices, boolean updated) {
                List<String> updatedSensorList = new ArrayList<>(); // Aktualizowana lista sensorów
                for (Device device : devices) {
                    List<Sensor> sensors = device.getSensors();
                    for (Sensor sensor : sensors) {
                        String sensorName = "Device: " + device.getId() + ", Sensor: " + sensor.getId();
                        updatedSensorList.add(sensorName);
                        availableSensors.add(sensorName); // Dodaj sensor do zbioru dostępnych sensorów
                    }
                }

                // Sortuj listę sensorów od najmniejszej do największej wartości
                Collections.sort(updatedSensorList);

                // Aktualizuj adapter tylko z nowymi danymi sensorów
                ArrayAdapter<String> updatedSensorAdapter = new ArrayAdapter<>(ChartActivity.this, android.R.layout.simple_spinner_item, updatedSensorList);
                updatedSensorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sensorSpinner.setAdapter(updatedSensorAdapter);

                // Sprawdź, czy wybrany sensor jest nadal dostępny
                if (selectedSensorIndex != -1 && selectedSensorIndex < updatedSensorList.size()) {
                    sensorSpinner.setSelection(selectedSensorIndex);
                }
                sensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position != selectedSensorIndex) {
                            String selectedItem = updatedSensorList.get(position);
                            String[] parts = selectedItem.split(", ");
                            if (parts.length == 2) {
                                String deviceId = parts[0].replace("Device: ", "");
                                String sensorId = parts[1].replace("Sensor: ", "");
                                selectedSensorId = sensorId; // Aktualizuj wybrany identyfikator sensora
                                displaySensorData(devices, deviceId, sensorId);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {


                    }
                });
            }

            private void displaySensorData(List<Device> devices, String deviceId, String sensorId) {
                for (Device device : devices) {
                    if (device.getId().equals(deviceId)) {
                        List<Sensor> sensors = device.getSensors();
                        for (Sensor sensor : sensors) {
                            if (sensor.getId().equals(sensorId)) {
                                String timestampString = sensor.getTimestamp();
                                try {
                                    Date timestamp = inputFormat.parse(timestampString);
                                    String formattedTimestamp = outputFormat.format(timestamp);
                                    System.out.println("Przetworzony timestamp: " + formattedTimestamp);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addEntryToChart(sensor.getValue(), formattedTimestamp);
                                            updateChartAxis(formattedTimestamp);
                                        }
                                    });
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            private void addEntryToChart(double value, String timestamp) {
                if (lineDataSet == null) {
                    lineDataSet = new LineDataSet(null, "Selected Sensor");
                    lineDataSet.setValueTextSize(20f);
                    YAxis leftAxis = lineChart.getAxisLeft();
                    leftAxis.setTextSize(15f);

                    YAxis rightAxis = lineChart.getAxisRight();
                    rightAxis.setEnabled(false);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setTextSize(15f);

                    lineData = new LineData(lineDataSet);
                    lineChart.setData(lineData);

                    lineChart.setDrawGridBackground(true);
                    lineChart.setGridBackgroundColor(Color.LTGRAY);
                    lineChart.getAxisLeft().setGridColor(Color.WHITE);
                    lineChart.getXAxis().setGridColor(Color.WHITE);

                    lineChart.setBorderColor(Color.LTGRAY);

                    Legend legend = lineChart.getLegend();
                    legend.setEnabled(true);
                    legend.setTextColor(Color.WHITE);
                    legend.setTextSize(15f);

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
                Log.i("WebSocket", "Odebrano ping");
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
                Log.i("WebSocket", "Zamknięto sesję");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }
}
