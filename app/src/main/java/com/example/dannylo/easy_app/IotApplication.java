package com.example.dannylo.easy_app;


import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;
import org.ufrn.framework.core.Core;
import org.ufrn.framework.database.access.DeviceManager;
import org.ufrn.framework.exceptions.ResourceException;
import org.ufrn.framework.util.JsonExtractUtil;
import org.ufrn.framework.virtualentity.VirtualDevice;

import java.io.InputStream;
import java.util.HashMap;

import br.ufrn.framework.virtualentity.resources.ResourceManager;
import proxy.ProxyBluetooth;
import proxy.ProxyUPnpForAndroid;

public class IotApplication extends Thread {

    private InputStream stream;
    private MainActivity activity;
    private VirtualDevice termostate;
    private VirtualDevice sensorTemp;
    private final String RETURN_TERMOSTATE = "temperature";
    private static final String LOW_TEMPERATURE = "low";
    private static final String HIGH_TEMPERATURE = "high";

    public IotApplication(InputStream stream, MainActivity activity) {
        this.stream = stream;
        this.activity = activity;
    }

    @Override
    public void run() {
        Core.start(stream);
        try {
            this.termostate = DeviceManager.discovery("XT").get(0);
            this.sensorTemp = DeviceManager.discovery("Sen").get(0);
            this.notifyVirtualizationActivity();
            //possible values for termostate.
            HashMap<String, String> mappedValues = new HashMap<>();
            mappedValues.put(ResourceManager.ConfigureTermostate.NewValue, "30");
            String returnSensor = sensorTemp.getDataEvent(ResourceManager.VerifyTemperature.GetTemperature);

            if (Double.parseDouble(JsonExtractUtil.extractValue(returnSensor, RETURN_TERMOSTATE)) < 25.0) {
                this.notifyStateTemperatureActivity(LOW_TEMPERATURE);
                termostate.sendEvent(ResourceManager.ConfigureTermostate.NewValueTemp, mappedValues);
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (ResourceException e) {
            e.printStackTrace();
        }
    }

    public void notifyVirtualizationActivity() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.publishResult("Device virtualized for Framework: " + termostate.getIdentification().getDescriptionName()
                        + ", " + sensorTemp.getIdentification().getDescriptionName());
            }
        });
    }

    public void notifyStateTemperatureActivity(String state) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals(IotApplication.LOW_TEMPERATURE)) {
                    activity.publishResult("Temperature is low, adjusting Termostate." + termostate.getIdentification().getDescriptionName());
                } else if (state.equals(IotApplication.HIGH_TEMPERATURE)) {
                    activity.publishResult("Temperature is HIGH.");
                }
            }
        });
    }
}
