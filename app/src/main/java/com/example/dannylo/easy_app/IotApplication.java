package com.example.dannylo.easy_app;

import android.bluetooth.BluetoothAdapter;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.ufrn.framework.core.Core;
import org.ufrn.framework.database.access.DeviceManager;
import org.ufrn.framework.exceptions.ResourceException;
import org.ufrn.framework.virtualentity.VirtualDevice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import proxy.ProxyBluetooth;
import proxy.ProxyUPnpForAndroid;

public class IotApplication extends Thread {

    private InputStream stream;
    private MainActivity activity;
    private final String RETURN_TERMOSTATE = "temperature";


    public IotApplication(InputStream stream, MainActivity activity) {
        this.stream = stream;
        this.activity = activity;
    }

    @Override
    public void run() {
        Core.start(stream);

        VirtualDevice termostato = DeviceManager.discovery("XT").get(0);
        VirtualDevice sensor = DeviceManager.discovery("Sen").get(0);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.publishResult("Device virtualized for Framework: " + termostato.getIdentification().getDescriptionName()
                        + ", " + sensor.getIdentification().getDescriptionName());
            }
        });

        HashMap<String, String> mappedValues = new HashMap<>();
        mappedValues.put(ProxyBluetooth.VALUE_TERM, "30");
        String returnSensor = "";
        try {
            returnSensor = sensor.getDataEvent(ProxyUPnpForAndroid.GetTemperature);
        } catch (ResourceException e) {
            e.printStackTrace();
        }

        if (returnSensor != null && !returnSensor.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(returnSensor);
                if (Double.parseDouble(jsonObject.getString(RETURN_TERMOSTATE)) < 25.0) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.publishResult("Temperature is low, adjusting Termostate." + termostato.getIdentification().getDescriptionName());
                        }
                    });
                    termostato.sendEvent(ProxyBluetooth.ACTION, mappedValues);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.publishResult("Process was done.");
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }
}
