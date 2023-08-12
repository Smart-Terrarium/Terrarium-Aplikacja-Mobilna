package com.example.test02;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
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

public class ConnectFTPActivity extends UserActivity implements OnClickListener {

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
        passwordEditText = findViewById(R.id.passwordEditText);

        uploadButton = findViewById(R.id.uploadButton);
        downloadButton = findViewById(R.id.downloadButton);
        hostnameEditText = findViewById(R.id.hostnameEditText);
       // usernameEditText = findViewById(R.id.usernameEditText);
       // passwordEditText = findViewById(R.id.passwordEditText);
        ssidEditText = findViewById(R.id.ssidEditText);
        passwordssidEditText = findViewById(R.id.passwordssidEditText);

        hostnameEditText = findViewById(R.id.hostnameEditText);
        //usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        uploadButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);

        nameEditText = findViewById(R.id.nameEditText);
        macAddressEditText = findViewById(R.id.macAddressEditText);
        addButton = findViewById(R.id.addButton);

        baseUrlManager = new MainActivity.BaseUrl();

        EditText macAddressEditText = findViewById(R.id.macAddressEditText);


        macAddressEditText.setFocusable(false);

        client = new OkHttpClient();
        mAuthToken = getIntent().getStringExtra("auth_token");
        System.out.println(mAuthToken);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String macAddress = macAddressEditText.getText().toString();
                addNewDevice(name, macAddress);
            }
        });

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

        Request request = new Request.Builder().url("http:/" + baseUrlManager.getBaseUrl(this) + "/device").post(requestBody).header("Authorization", "Bearer " + mAuthToken) // Dodaj autoryzację
                .build();

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

    private void clearFields() {
        nameEditText.setText("");
        macAddressEditText.setText("");
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

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String hostname = hostnameEditText.getText().toString();
            // String username = usernameEditText.getText().toString(); // Commented out
          //  String password = passwordEditText.getText().toString();

            String ssid = ssidEditText.getText().toString();
            String passwordssid = passwordssidEditText.getText().toString();

            JSONObject hotspotData = new JSONObject();
            try {
                hotspotData.put("ssid", ssid);
                hotspotData.put("password", passwordssid);

                String jsonData = hotspotData.toString();

                // Zapisz dane do pliku
                String filePath = "/Download/example.txt";
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
                    ftp.enterLocalPassiveMode();

                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    File file = new File(filePath);
                    FileInputStream inputStream = new FileInputStream(file);
                    ftp.storeFile("/ftp_config_file.json", inputStream);
                    inputStream.close();

                    System.out.println(file);
                    ftp.logout();
                    ftp.disconnect();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class DownloadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String hostname = hostnameEditText.getText().toString();
           // String username = usernameEditText.getText().toString();
            //String password = passwordEditText.getText().toString();

            FTPClient ftp = new FTPClient();
            String macAddress = null;
            try {
                ftp.connect(hostname);
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                File outputFile = new File("/Download/example.txt");

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

            if (macAddress != null) {
                macAddressEditText.setText(macAddress);
                showToast("MAC Address retrieved successfully");
            } else {
                showToast("Error retrieving MAC Address");
            }
        }
    }
}