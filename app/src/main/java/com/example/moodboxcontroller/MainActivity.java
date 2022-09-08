package com.example.moodboxcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    View gradientOverlay;
    FloatingActionButton refreshFab;
    ListView devicesListView;
    TextView noBluetoothMessage;

    BluetoothAdapter bluetoothAdapter = null;
    List<BluetoothDevice> pairedDevices = new ArrayList<>();
    ArrayList<String> bluetoothIDs;

    boolean isActivitySetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariables();

        // Fade in the views
        animateOverlay();

        // When the refreshFab is clicked, check again for paired devices.
        refreshFab.setOnClickListener(view -> {
            if (isActivitySetup) findDevices();
        });
    }

    private void initializeVariables() {
        gradientOverlay = findViewById(R.id.gradient_overlay_main);
        refreshFab = findViewById(R.id.refresh_fab);
        devicesListView = findViewById(R.id.bluetooth_devices_list);
        noBluetoothMessage = findViewById(R.id.no_bluetooth_message);
    }

    private void animateOverlay() {
        gradientOverlay.setAlpha(1f);

        // When the animation is over, start loading things.
        gradientOverlay.animate().alpha(0f).setDuration(2000).withEndAction(this::startWork);
    }

    private void startWork() {
        // Checks the permissions required to use bluetooth and requests them if not granted.
        checkPermissions();

        // Setups bluetooth, if available
        setupBluetooth();

        // Search for bluetooth devices
        findDevices();

        isActivitySetup = true;
    }

    private void checkPermissions() {
        ArrayList<String> neededPermissions = new ArrayList<>();

        // BLUETOOTH_CONNECT and BLUETOOTH_SCAN are the permissions in Android 12+ that replace the
        // old BLUETOOTH_ADMIN and BLUETOOTH permissions.
        if (Build.VERSION.SDK_INT >= 31) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
            }

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH);
            }
        }

        if (!neededPermissions.isEmpty()) {
            String[] requestedPermissions = new String[neededPermissions.size()];

            // Convert the ArrayList into an array.
            for (int i = 0; i < neededPermissions.size(); i++) {
                requestedPermissions[i] = neededPermissions.get(i);
            }

            ActivityCompat.requestPermissions(MainActivity.this, requestedPermissions, 2);
        }
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // The device doesn't have a bluetooth adapter
            refreshFab.setVisibility(View.GONE);
            devicesListView.setVisibility(View.GONE);
            noBluetoothMessage.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Bluetooth Not Available", Toast.LENGTH_LONG).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                // Ask the user to turn on the bluetooth
                Intent turnOnBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnBluetoothIntent, 1);
            }
        }
    }

    private void findDevices() {
        try {
            showPairedDevicesList();
        } catch (SecurityException e) {
            // This code will run when the user has not given the app the permissions needed.
            Log.e("ERROR", e.getMessage());
            Toast.makeText(MainActivity.this, "Insufficient permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPairedDevicesList() {
        // Clears the old data.
        pairedDevices.clear();

        // Convert the set into a List so that the get() method can be used elsewhere
        pairedDevices.addAll(bluetoothAdapter.getBondedDevices());

        // The identifiers for the paired bluetooth devices, used to populate the list view.
        bluetoothIDs = new ArrayList<>();

        // If some pairedDevices were found...
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice bluetoothDevice : pairedDevices) {
                bluetoothIDs.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
            }
        } else {
            Toast.makeText(this, "No paired bluetooth devices found.", Toast.LENGTH_LONG).show();
        }

        PairedDevicesAdapter adapter = new PairedDevicesAdapter();
        devicesListView.setAdapter(adapter);
    }

    private class PairedDevicesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return bluetoothIDs.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View listItemLayout = getLayoutInflater().inflate(R.layout.list_item_bluetooth_devices, null);

            LinearLayout rootLayout = listItemLayout.findViewById(R.id.layout_bt_info);
            TextView deviceName = listItemLayout.findViewById(R.id.bt_device_name);
            TextView macAddress = listItemLayout.findViewById(R.id.bt_device_mac_address);

            String[] bluetoothInfo = bluetoothIDs.get(i).split("\n");
            String btDeviceName = bluetoothInfo[0];
            String btMACAddress = bluetoothInfo[1];

            deviceName.setText(btDeviceName);
            macAddress.setText(btMACAddress);

            rootLayout.setOnClickListener(view1 ->
                    startActivity(new Intent(MainActivity.this, ControllerActivity.class)
            .putExtra("MAC_ADDRESS", btMACAddress)));

            return listItemLayout;
        }
    }
}