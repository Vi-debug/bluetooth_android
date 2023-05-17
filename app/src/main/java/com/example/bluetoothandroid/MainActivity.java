package com.example.bluetoothandroid;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CODE_PERMISSIONS = 5;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private Handler handler;

    // UUID for the Bluetooth service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);

        startConnect();
        setContentView(R.layout.activity_main);
    }

    private void startConnect() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler();
        if (bluetoothAdapter == null) {
            return;
        }
//        device = getBluetoothDevice();
//        if (device != null) {
//            ConnectThread connectThread = new ConnectThread(device);
//            connectThread.start();
//        }
    }

    @SuppressLint("MissingPermission")
    private void scanDevices() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        bluetoothAdapter.startDiscovery();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i("MainActivity", "Found device: " + device.getName() + " - " + device.getAddress());
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);

        // Stop device discovery after 10 seconds
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                bluetoothAdapter.cancelDiscovery();
//                unregisterReceiver(receiver);
//            }
//        },);
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice getBluetoothDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            Log.i(TAG, "getBluetoothDevice: " + device.getName());
            // Check if the device matches your criteria (e.g., device name, MAC address, etc.)
            if (device.getName().equals("Your Device Name")) {
                return device; // Return the BluetoothDevice object if it matches
            }
        }
        return null;
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Could not connect the client socket", e);
            }
            socket = tmp;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            // Connect to the Bluetooth device
            try {
                socket.connect();
                // Connection successful, handle accordingly
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                scanDevices();
            } else {
                // Permissions not granted, handle accordingly
                Log.i(TAG, "onRequestPermissionsResult: Error");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}