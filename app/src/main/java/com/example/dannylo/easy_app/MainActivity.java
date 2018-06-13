package com.example.dannylo.easy_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.californium.core.network.config.NetworkConfig;
import org.teleal.cling.Main;
import org.ufrn.framework.core.Core;
import org.ufrn.framework.database.access.DeviceManager;
import org.ufrn.framework.virtualentity.VirtualDevice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import proxy.ProxyBluetooth;


public class MainActivity extends AppCompatActivity {

    private static final String DEVICE_NAME = "XT1068";
    private static final String CONFIG = "config.properties";
    private static final UUID SENSOR_UUID = UUID.nameUUIDFromBytes("BLUETOOTH SENSOR".getBytes());




    private Button startFramework;
    private Button forceDiscoveryBluetooth;


    private final BroadcastReceiver discoveryDevices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName() != null && device.getName().equalsIgnoreCase(DEVICE_NAME)){
                    DevicesDiscoveredBluetooth.addDevice(device);
                    MainActivity.this.publishResult("Device " + device.getName() + " was found.");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.startFramework = (Button)findViewById(R.id.buttonStartFramework);
        this.forceDiscoveryBluetooth = (Button) findViewById(R.id.forceDiscovery);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryDevices, intentFilter);

        WifiManager wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        DevicesDiscoveredBluetooth.setWifiManager(wifiManager);

        this.discoveryBluetooth();

        this.startFramework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    NetworkConfig.createStandardWithoutFile();
                    InputStream inputStream = getAssets().open(CONFIG);
                    IotApplication iotApplication = new IotApplication(inputStream, MainActivity.this);
                    iotApplication.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.forceDiscoveryBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.discoveryBluetooth();
            }
        });
    }

    private void discoveryBluetooth(){
        boolean find = false;
        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for(BluetoothDevice device: devices){
            if(device.getName().equalsIgnoreCase(DEVICE_NAME)){
                DevicesDiscoveredBluetooth.getList().add(device);
                this.publishResult("Device " + device.getName() + " was found.");
                find = true;
                break;
            }
        }

        if(!find){
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
            this.publishResult("Discovery begin...");
        }
    }

    public void publishResult(String result){
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(discoveryDevices);
    }
}
