package com.sarmale.arduinobtexample_v3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.sarmale.arduinobtexample_v3.ConnectedThread;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    // Global variables we will use in the
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    //We will use a Handler to get the BT Connection stats
    public static Handler handler;
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update
    BluetoothDevice arduinoBTModule = null;
    TextToSpeech textToSpeech;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable

    private long lastReceivedTime = 0;
    private static final long INTERVAL = 5000; // 5 seconds in milliseconds
    private String lastResponse = "";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Instances of BT Manager and BT Adapter needed to work with BT in Android.
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        // Instances of the Android UI elements that we will use during the execution of the APP
        TextView btReadings = findViewById(R.id.btReadings);
        TextView btDevices = findViewById(R.id.btDevices);
        Button connectToDevice = (Button) findViewById(R.id.connectToDevice);
        Button seachDevices = (Button) findViewById(R.id.seachDevices);
        Button customize = (Button) findViewById(R.id.refresh);
        Log.d(TAG, "Begin Execution");
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                // if No error is found then only it will run
                if (i != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        // Using a handler to update the interface in case of an error connecting to the BT device
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ERROR_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        if (arduinoMsg != null) {
                            Log.e(TAG, "Reading from Arduino: " + arduinoMsg);
                        }

                        // Retrieve the custom response if available
                        String customResponse = getSharedPreferences("CustomResponses", MODE_PRIVATE)
                                .getString(arduinoMsg, null);

                        // Log the custom response being retrieved
                        if (customResponse != null) {
                            Log.d(TAG, "Custom response retrieved for '" + arduinoMsg + "': " + customResponse);
                        } else {
                            Log.d(TAG, "No custom response found for '" + arduinoMsg + "'. Using default.");
                        }

                        // Update UI and text-to-speech based on response availability
                        if (customResponse != null) {
                            // Use the custom response if it exists
                            btReadings.setText(customResponse);
                            textToSpeech.speak(customResponse, TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            // Use the default Arduino message
                            btReadings.setText(arduinoMsg);
                            textToSpeech.speak(arduinoMsg, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                }
            }
        };


//        handler = new Handler(Looper.getMainLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case ERROR_READ:
//                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
////                        long currentTime = System.currentTimeMillis();
////
////                        // Check if enough time has passed since the last processed message
////                        if (currentTime - lastReceivedTime >= INTERVAL ) {
////                            lastReceivedTime = currentTime; // Update the last received time
////
////                            // Always update the text view
////                            btReadings.setText(arduinoMsg);
////                            textToSpeech.speak(arduinoMsg, TextToSpeech.QUEUE_FLUSH, null);
////
////                        } else {
////                            // If it’s a duplicate but within the interval, just update the text view
////                            if (arduinoMsg.equals(lastResponse)) {
////                                Log.d(TAG, "Ignoring message (duplicate): " + arduinoMsg);
////                            } else {
////                                // If it's a new message but too soon, just log it
////                                Log.d(TAG, "Received new message but too soon: " + arduinoMsg);
////                            }
////                        }
//                        btReadings.setText(arduinoMsg);
//                        textToSpeech.speak(arduinoMsg, TextToSpeech.QUEUE_FLUSH, null);
//                }
//            }
//        };

        // Set a listener event on a button to clear the texts
        customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CustomizeActivity.class);
                startActivity(intent);
            }
        });

        // Create an Observable from RxAndroid
        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();
            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if (connectedThread.getValueRead() != null) {
                    emitter.onNext(connectedThread.getValueRead());
                }
                connectedThread.cancel();
            }
            connectThread.cancel();
            emitter.onComplete();
        });

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View view) {
                btReadings.setText("");
                if (arduinoBTModule != null) {
                    connectToBTObservable
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(valueRead -> {
                                btReadings.setText(valueRead);
                                textToSpeech.speak(btReadings.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                            });
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        seachDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter == null) {
                    Log.d(TAG, "Device doesn't support Bluetooth");
                } else {
                    Log.d(TAG, "Device support Bluetooth");
                    if (!bluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth is disabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "We don't BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        } else {
                            Log.d(TAG, "We have BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                    } else {
                        Log.d(TAG, "Bluetooth is enabled");
                    }
                    String btDevicesString = "";
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress();
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                            btDevicesString = btDevicesString + deviceName + " || " + deviceHardwareAddress + "\n";
                            if (deviceName.equals("HC-05")) {
                                Log.d(TAG, "HC-05 found");
                                arduinoUUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                                connectToDevice.setEnabled(true);
                            }
                            btDevices.setText(btDevicesString);
                        }
                    }
                }
                Log.d(TAG, "Button Pressed");
            }
        });
    }
}
