package com.example.dannylo.easy_app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.californium.core.network.config.NetworkConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import proxy.ProxyBluetooth;


public class MainActivity extends AppCompatActivity {

    private static final String CONFIG = "config.properties";
    private Button startFramework;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.startFramework = (Button)findViewById(R.id.buttonStartFramework);

        WifiManager wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        ToolsUtil.setWifiManager(wifiManager);
        ToolsUtil.setContext(this);

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

    }

    public void publishResult(String result){
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

}
