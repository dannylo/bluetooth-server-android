package com.example.dannylo.easy_app;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ComunicationDevice {

    public InputStream inputStream;
    public OutputStream outputStream;

    public BluetoothSocket bluetoothSocket;

    public ComunicationDevice(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket = bluetoothSocket;
        try {
            this.inputStream = this.bluetoothSocket.getInputStream();
            this.outputStream = this.bluetoothSocket.getOutputStream();

        }catch (IOException ex){ }
    }

    public void write(byte[] bytes) {
        try {
            System.out.println("Escrevendo bytes...");
            outputStream.write(bytes);
        } catch (IOException e) { }
    }

}
