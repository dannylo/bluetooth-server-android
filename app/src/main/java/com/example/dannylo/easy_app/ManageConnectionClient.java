package com.example.dannylo.easy_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Dannylo on 17/05/2018.
 */

public class ManageConnectionClient extends Thread {

    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter adapter;
    private BluetoothSocket bluetoothSocket;
    private String message = "";

    private final String NAME_BLUETOOTH = "SENSOR_SIMULATE";
    private final UUID NAME_UUID = UUID.nameUUIDFromBytes(NAME_BLUETOOTH.getBytes());


    public ManageConnectionClient(BluetoothDevice bluetoothDevice,
                                  BluetoothAdapter adapter,
                                  String message){

        this.bluetoothDevice = bluetoothDevice;
        this.adapter = adapter;
        this.message = message;
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(NAME_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        if(adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        try {
            System.out.println("Begin connection....");
            System.out.println("Device connected: " + bluetoothSocket.getRemoteDevice().getName());
            System.out.println("Connected: "+ bluetoothSocket.isConnected());
            bluetoothSocket.connect();
            System.out.println("Connected: "+ bluetoothSocket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Class<?> clazz = bluetoothSocket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};

                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};

                bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothSocket.getRemoteDevice(), params);

                bluetoothSocket.connect();
                System.out.println("Connected: " + bluetoothSocket.isConnected());

            } catch (IOException closeException) {
                closeException.printStackTrace();
                try {
                    bluetoothSocket.close();
                    System.out.println("Socket closed.");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            }
        }

        if(bluetoothSocket != null){
            startConnection(bluetoothSocket, message);
        }
    }


    public void startConnection(BluetoothSocket bluetoothSocket, String message){
        System.out.println("Comunication is begin... ");
        System.out.println("Message: "+ message);
        ComunicationDevice comunicationDevice = new ComunicationDevice(bluetoothSocket);
        comunicationDevice.write(message.getBytes());
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) { }
    }
}
