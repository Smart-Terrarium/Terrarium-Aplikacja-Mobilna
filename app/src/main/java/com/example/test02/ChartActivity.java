package com.example.test02;
import org.json.*;

import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;
import tech.gusavila92.websocketclient.WebSocketClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.components.YAxis;




public class ChartActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;
    private LineChart lineChart;
    private LineDataSet lineDataSet;
    private LineData lineData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        lineChart = findViewById(R.id.lineChart);

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
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                webSocketClient.send("Hello World!");

            }
            public String convertStandardJSONString(String data_json) {
                data_json = data_json.replaceAll("\\\\r\\\\n", "");
                data_json = data_json.replace("\\\"", "\"");

                data_json = data_json.replace("\"{", "{");
                data_json = data_json.replace("}\",", "},");
                data_json = data_json.replace("}\"", "}");
                return data_json;
            }



            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                List<Device> devices = new ArrayList<>();
                try {
                    JSONObject json = new JSONObject(convertStandardJSONString(s));
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
                    for (Device device : devices) {
                        System.out.println("Device ID: " + device.getId());
                        List<Sensor> sensors = device.getSensors();
                        for (Sensor sensor : sensors) {
                            System.out.println("Sensor ID: " + sensor.getId());
                            System.out.println("Timestamp: " + sensor.getTimestamp());
                            System.out.println("Value: " + sensor.getValue());
                            if (device.getId().equals("1") && sensor.getId().equals("8")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addEntryToChart(sensor.getValue());
                                    }
                                });}}
                    }

                } catch (JSONException e) {
                    Log.e("WebSocket", "Error parsing JSON", e);
                }
            }
            private void addEntryToChart(double value) {
                if (lineDataSet == null) {
                    lineDataSet = new LineDataSet(null, "Sensor 8");
                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                    lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    lineDataSet.setCircleColors(ColorTemplate.MATERIAL_COLORS);
                    lineDataSet.setLineWidth(2f);
                    lineDataSet.setCircleRadius(4f);
                    lineDataSet.setFillAlpha(65);
                    lineDataSet.setFillColor(ColorTemplate.MATERIAL_COLORS[0]);
                    lineDataSet.setHighLightColor(Color.rgb(244, 117, 117));
                    lineDataSet.setValueTextColor(Color.BLACK);
                    lineDataSet.setValueTextSize(9f);

                    lineData = new LineData(lineDataSet);
                    lineChart.setData(lineData);
                }

                lineData.addEntry(new Entry(lineDataSet.getEntryCount(), (float) value), 0);
                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.setVisibleXRangeMaximum(10);
                lineChart.moveViewToX(lineData.getEntryCount());
            }
            class Sensor {
                private String id;
                private String timestamp;
                private double value;

                public Sensor(String id, String timestamp, double value) {
                    this.id = id;
                    this.timestamp = timestamp;
                    this.value = value;
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
                private String id;
                private List<Sensor> sensors;

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
            public void onBinaryReceived(byte[] data) {}

            @Override
            public void onPingReceived(byte[] data) {}

            @Override
            public void onPongReceived(byte[] data) {}

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }
}