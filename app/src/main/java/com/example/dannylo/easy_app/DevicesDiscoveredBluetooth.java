package com.example.dannylo.easy_app;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dannylo on 17/05/2018.
 */

public class DevicesDiscoveredBluetooth {

    private static WifiManager wifiManager;

    private static List<BluetoothDevice> devicesBluetoothDiscovered = new ArrayList<>();

    public static List<BluetoothDevice> getList(){
        return devicesBluetoothDiscovered;
    }

    public static void addDevice(BluetoothDevice device){
        devicesBluetoothDiscovered.add(device);
    }

    public static WifiManager getWifiManager(){ return wifiManager; }

    public static void setWifiManager(WifiManager newWifiManager){ wifiManager = newWifiManager; }

}
