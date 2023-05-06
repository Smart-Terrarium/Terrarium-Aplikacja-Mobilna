package com.example.test02;
import org.json.*;

import android.os.Bundle;
import org.json.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Iterator;
import androidx.appcompat.app.AppCompatActivity;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;
import tech.gusavila92.websocketclient.WebSocketClient;

public class ChartActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
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

                try {
                    //JSONParser parser = new JSONParser();
                    //JSONObject json = (JSONObject) parser.parse(s);
                    //s ="\"{\\\"2\\\": {\\\"6\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:51.372609\\\", \\\"value\\\": 28.86612323908645}, \\\"8\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:50.625622\\\", \\\"value\\\": 27.430420626059316}, \\\"1\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:49.721906\\\", \\\"value\\\": 23.582264565233757}, \\\"4\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:49.471529\\\", \\\"value\\\": 35.54956294645577}, \\\"2\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:44.497297\\\", \\\"value\\\": 31.366992588593433}, \\\"3\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:41.965832\\\", \\\"value\\\": 25.258133385518228}, \\\"7\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:39.090196\\\", \\\"value\\\": 33.893140790081596}, \\\"5\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:37.400957\\\", \\\"value\\\": 18.52029738334786}}, \\\"1\\\": {\\\"8\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:50.985998\\\", \\\"value\\\": 23.88428747172619}, \\\"5\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:50.182918\\\", \\\"value\\\": 33.1512214258401}, \\\"4\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:49.608302\\\", \\\"value\\\": 21.611447327119954}, \\\"2\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:49.250255\\\", \\\"value\\\": 35.65451501501501}, \\\"1\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:48.806911\\\", \\\"value\\\": 35.23710028458882}, \\\"6\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:47.485838\\\", \\\"value\\\": 18.19029173489022}, \\\"7\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:46.313356\\\", \\\"value\\\": 23.983209424753603}, \\\"3\\\": {\\\"timestamp\\\": \\\"2023-05-06T13:07:37.003867\\\", \\\"value\\\": 24.09385948068696}}}\"";
                    JSONObject jsonObject = new JSONObject(convertStandardJSONString(s));




                    JSONObject object2 = jsonObject.getJSONObject("2");
                    double value2_6 = object2.getJSONObject("6").getDouble("value");
                    String timestamp2_6 = object2.getJSONObject("6").getString("timestamp");
                    System.out.println("VVVVVVVVVV" + value2_6);
                    System.out.println("XXXXXXX" + timestamp2_6);
                    ;
                   // Log.d("content: ",s);
                    StringBuilder sb = new StringBuilder();
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject innerJsonObject = jsonObject.getJSONObject(key);
                        Iterator<String> innerKeys = innerJsonObject.keys();
                        while (innerKeys.hasNext()) {
                            String innerKey = innerKeys.next();
                            JSONObject innermostJsonObject = innerJsonObject.getJSONObject(innerKey);
                            String timestamp = innermostJsonObject.getString("timestamp");
                            double value = innermostJsonObject.getDouble("value");
                            sb.append("key1: ").append(key)
                                    .append(", key2: ").append(innerKey)
                                    .append(", timestamp: ").append(timestamp)
                                    .append(", value: ").append(value)
                                    .append("\n");
                          }
                    }
                    TextView textView = findViewById(R.id.chart);
                      textView.setText(sb.toString());
                      }
                catch (JSONException e) {
                    System.out.println(e.toString());
                    e.printStackTrace();
                }


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
                }}
            class Device {
                private String id;
                private List<Sensor> sensors;

                public Device(String id) {
                    this.id = id;
                    sensors = new ArrayList<>();
                }

                public void addSensor(Sensor sensor) {
                    sensors.add(sensor);
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