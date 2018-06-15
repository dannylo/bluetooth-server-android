package com.example.dannylo.easy_app;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import proxy.ProxyBluetooth;

public class RecieverFound extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(BluetoothDevice.ACTION_FOUND)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device.getName() != null && device.getName().equalsIgnoreCase(ProxyBluetooth.DEVICE_NAME)){
                ToolsUtil.addDevice(device);
                //MainActivity.this.publishResult("Device " + device.getName() + " was found.");
            }
        }
    }
}
