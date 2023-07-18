package com.example.test02;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.NotificationCompat;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

public class ConnectFTPActivity extends Activity implements OnClickListener {

    private Button uploadButton;
    private Button downloadButton;
    private EditText hostnameEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText ssidEditText;
    private EditText passwordssidEditText;

    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transfer);

        uploadButton = findViewById(R.id.uploadButton);
        downloadButton = findViewById(R.id.downloadButton);
        hostnameEditText = findViewById(R.id.hostnameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        ssidEditText = findViewById(R.id.ssidEditText);
        passwordssidEditText = findViewById(R.id.passwordssidEditText);

        uploadButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
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
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String ssid = ssidEditText.getText().toString();
            String passwordssid = passwordssidEditText.getText().toString();

            JSONObject hotspotData = new JSONObject();
            try {
                hotspotData.put("ssiddddd", ssid);
                hotspotData.put("passworddddd", passwordssid);

                String jsonData = hotspotData.toString();

                // Zapisz dane do pliku
                String filePath = "/storage/emulated/0/Download/example3.txt";
                File xxx = new File(filePath);
                if(xxx.exists())
                {
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
                    ftp.login(username, password);
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    File file = new File(filePath);
                    FileInputStream inputStream = new FileInputStream(file);
                    ftp.storeFile("/apk/example.txt", inputStream);
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

    private class DownloadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String hostname = hostnameEditText.getText().toString();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            FTPClient ftp = new FTPClient();
            try {
                ftp.connect(hostname);
                ftp.enterLocalPassiveMode();
                ftp.login(username, password);
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                File outputFile = new File("/storage/emulated/0/Download/example3.txt");

                FileOutputStream outputStream = new FileOutputStream(outputFile);

                ftp.retrieveFile("/apk/example.txt", outputStream);

                System.out.println("TEST222");

                // ftp.deleteFile("/apk/example.txt"); // Usuwanie pliku
                System.out.println(ftp.getReplyString());
                ftp.logout();
                ftp.disconnect();
                outputStream.close();
            } catch (SocketException e) {
                System.out.println("Błąd SocketException");
                // Obsługa błędu połączenia SocketException
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Błąd IOException");
                // Obsługa błędów wejścia/wyjścia IOException
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Inny błąd");
                // Obsługa ogólnych błędów
                e.printStackTrace();
            }
            return null;
        }
    }
}
