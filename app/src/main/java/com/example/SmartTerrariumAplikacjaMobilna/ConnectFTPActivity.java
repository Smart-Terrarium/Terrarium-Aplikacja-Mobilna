package com.example.SmartTerrariumAplikacjaMobilna;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;

import org.json.JSONException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ConnectFTPActivity extends AppCompatActivity implements View.OnClickListener {

    private Button uploadButton;
    private Button downloadButton;
    private EditText hostnameEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText ssidEditText;
    private EditText passwordssidEditText;
    private String token;
    private EditText nameEditText;
    private EditText macAddressEditText;
    private Button addButton;
    private Button clearButton;
    private String mAuthToken;
    private OkHttpClient client;
    private MainActivity.BaseUrl baseUrlManager;


    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transfer);
   
        hostnameEditText = findViewById(R.id.hostnameEditText);
        //usernameEditText = findViewById(R.id.usernameEditText);
        // passwordEditText = findViewById(R.id.passwordEditText);

        uploadButton = findViewById(R.id.uploadButton);
        downloadButton = findViewById(R.id.downloadButton);
        ssidEditText = findViewById(R.id.ssidEditText);
        passwordssidEditText = findViewById(R.id.passwordssidEditText);

        nameEditText = findViewById(R.id.nameEditText);
        macAddressEditText = findViewById(R.id.macAddressEditText);
        addButton = findViewById(R.id.addButton);

        baseUrlManager = new MainActivity.BaseUrl();

        macAddressEditText.setFocusable(false);

        client = new OkHttpClient();
        mAuthToken = getIntent().getStringExtra("auth_token");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String macAddress = macAddressEditText.getText().toString();
                addNewDevice(name, macAddress);
            }
        });
        Button showDetailsButton = findViewById(R.id.showDetailsButton);
        showDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailsDialog();
            }
        });

        uploadButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
    }
    private void showDetailsDialog() {
        StringBuilder details = new StringBuilder();

        details.append("").append("\n")
                .append("1. Enter the device name. (The mac address fills in automatically after download)").append("\n")
                .append("2. Enter the address of the FTP server from the microcontroller").append("\n")
                .append("3. Enter the network name and password").append("\n")
                .append("4. Change network from the microcontroller").append("\n")
                .append("5. Click the 'UPLOAD CONFIGURATION' and 'DOWNLOAD MAC ADDRESS' buttons").append("\n")
                .append("6. Change network to your default").append("\n")
                .append("7. Click the 'ADD NEW DEVICE' ").append("\n");


        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("To add a new device: ")
                .setMessage(details.toString())
                .setNeutralButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addNewDevice(String name, String macAddress) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("mac_address", macAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder().url("http://" + baseUrlManager.getBaseUrl(this) +
                ":8000/device").post(requestBody).header("Authorization", "Bearer " + mAuthToken).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast("Request failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast("Device with MAC address " + macAddress + " added");
                } else {
                    showToast("Request unsuccessful");
                }
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConnectFTPActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadButton:
                new UploadTask().execute();
                break;
            case R.id.downloadButton:
                new DownloadTask().execute();
                break;
        }
    }

    private class UploadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            String hostname = hostnameEditText.getText().toString();
            String username = "";
            String password = "";

            String ssid = ssidEditText.getText().toString();
            String passwordssid = passwordssidEditText.getText().toString();


            JSONObject hotspotData = new JSONObject();
            try {
                hotspotData.put("TCP_SERVER_IP", baseUrlManager.getBaseUrl(ConnectFTPActivity.this));
                hotspotData.put("TCP_PORT", "5570");
                hotspotData.put("MQTT_SERVER_IP", baseUrlManager.getBaseUrl(ConnectFTPActivity.this));
                hotspotData.put("MQTT_PORT", "1883");
                hotspotData.put("WIFI_SSID", ssid);
                hotspotData.put("WIFI_PASSWORD", passwordssid);

                String jsonData = hotspotData.toString();

                // Zapisz dane do pliku
                @SuppressLint("SdCardPath") String filePath = "/sdcard/Download/example.txt";
                File xxx = new File(filePath);
                if (xxx.exists()) {
                    xxx.delete();
                    System.out.println(jsonData);
                }
                FileWriter fileWriter = new FileWriter(filePath);
                fileWriter.write(jsonData);

                fileWriter.flush();
                fileWriter.close();

                // Wysyłanie pliku za pomocą FTP
                FTPClient ftp = new FTPClient();
                try {
                    ftp.connect(hostname);
                    ftp.login(username, password);
                    ftp.enterLocalPassiveMode();

                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    File file = new File(filePath);
                    FileInputStream inputStream = new FileInputStream(file);
                    ftp.storeFile("/ftp_config_file.json", inputStream);
                    inputStream.close();

                    System.out.println(file);
                    ftp.logout();
                    ftp.disconnect();
                    return true;

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean uploadSuccess) {
            super.onPostExecute(uploadSuccess);
            if (uploadSuccess) {
                showToast("File uploaded successfully");
            } else {
                showToast("File upload failed");
            }
        }
    }

    private class DownloadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String hostname = hostnameEditText.getText().toString();
            String username = "";
            String password = "";

            FTPClient ftp = new FTPClient();
            String macAddress = null;
            try {
                ftp.connect(hostname);
                ftp.login(username, password);
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                @SuppressLint("SdCardPath") File outputFile = new File("/sdcard/Download/example.txt");

                FileOutputStream outputStream = new FileOutputStream(outputFile);

                ftp.retrieveFile("/esp_mac.json", outputStream);

                outputStream.close();

                FileInputStream inputStream = new FileInputStream(outputFile);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();

                if (buffer.length > 0) {
                    String jsonData = new String(buffer, "UTF-8");
                    JSONObject jsonObject = new JSONObject(jsonData);
                    macAddress = jsonObject.getString("mac_address");
                    System.out.println("Extracted MAC Address: " + macAddress);

                    outputFile.delete();
                } else {
                    System.out.println("Downloaded JSON file is empty");
                }

                ftp.logout();
                ftp.disconnect();
            } catch (SocketException e) {
                System.out.println("Błąd SocketException");

                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Błąd IOException");

                e.printStackTrace();
            } catch (JSONException e) {
                System.out.println("Błąd JSONException");

                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Inny błąd");

                e.printStackTrace();
            }
            return macAddress;
        }

        @Override
        protected void onPostExecute(String macAddress) {
            super.onPostExecute(macAddress);
            if (macAddress != null) {
                macAddressEditText.setText(macAddress);
                showToast("MAC Address retrieved successfully");
            } else {
                showToast("Error retrieving MAC Address");
            }
        }
    }
}