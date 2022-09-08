package com.example.moodboxcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.util.UUID;

public class ControllerActivity extends AppCompatActivity {
    MaterialCardView happyCard, alrightCard, sadCard, madCard, whateverCard, linearPatternCard,
            randomPatternCard, reversePatternCard, fadePatternCard;
    TextView currentMoodView, currentPatternView;
    View gradientOverlay;

    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;
    boolean isConnected = false;
    String macAddress;

    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        initializeViews();

        // Get information from previous activity
        macAddress = getIntent().getStringExtra("MAC_ADDRESS");

        happyCard.setOnClickListener(view -> sendMood("Happy"));

        alrightCard.setOnClickListener(view -> sendMood("Alright"));

        sadCard.setOnClickListener(view -> sendMood("Sad"));

        madCard.setOnClickListener(view -> sendMood("Mad"));

        whateverCard.setOnClickListener(view -> sendMood("Whatever"));

        linearPatternCard.setOnClickListener(view -> sendPattern(1, "Linear"));

        randomPatternCard.setOnClickListener(view -> sendPattern(2, "Random"));

        reversePatternCard.setOnClickListener(view -> sendPattern(3, "Reverse"));

        fadePatternCard.setOnClickListener(view -> sendPattern(4, "Fade"));

        // Run async task and connect to arduino
        ConnectToArduino connectToArduino = new ConnectToArduino();
        connectToArduino.execute();

    }

    private void initializeViews() {
        happyCard = findViewById(R.id.happy_card);
        alrightCard = findViewById(R.id.alright_card);
        sadCard = findViewById(R.id.sad_card);
        madCard = findViewById(R.id.mad_card);
        whateverCard = findViewById(R.id.whatever_card);
        linearPatternCard = findViewById(R.id.linear_pattern_card);
        randomPatternCard = findViewById(R.id.random_pattern_card);
        reversePatternCard = findViewById(R.id.reverse_pattern_card);
        fadePatternCard = findViewById(R.id.fade_pattern_card);
        currentMoodView = findViewById(R.id.current_mood);
        currentPatternView = findViewById(R.id.current_pattern);
        gradientOverlay = findViewById(R.id.gradient_overlay_controller);
    }

    private void sendMood(String mood) {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.getOutputStream().write(mood.toLowerCase().getBytes());

                makeToast("Mood: " + mood);
                currentMoodView.setText("Mood: " + mood);
            } catch (IOException e) {
                makeToast("Error");
                Log.e("sendMood", e.getMessage());
            }
        } else {
            makeToast("Bluetooth socket is null");
        }
    }

    private void sendPattern(int patternCode, String pattern) {
        if (bluetoothSocket != null) {
            try {
                String command = patternCode + ":";
                bluetoothSocket.getOutputStream().write(command.getBytes());

                makeToast("Pattern: " + pattern);
                currentPatternView.setText("Pattern: " + pattern);
            } catch (IOException e) {
                makeToast("Error");
                Log.e("sendPattern", e.getMessage());
            }
        } else {
            makeToast("Bluetooth socket is null");
        }
    }

    private void disconnect() {
        // If the btSocket is busy
        if (bluetoothSocket!=null) {
            try {
                // Close connection
                bluetoothSocket.close();
                makeToast("Disconnected");
            } catch (IOException e) {
                makeToast("Error");
                Log.e("disconnect", e.getMessage());
            }
        } else {
            makeToast("Bluetooth socket is null");
        }
    }

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    private class ConnectToArduino extends AsyncTask<Void, Void, Void> {

        private boolean connectSuccess = true; //if it's here, it's almost connected
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ControllerActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);//connects to the device's address and checks if it's available
                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();//start connection
                }
            } catch (IOException e) {
                connectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!connectSuccess) {
                makeToast("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                makeToast("Connected.");
                isConnected = true;
            }
            progressDialog.dismiss();
        }
    }
}