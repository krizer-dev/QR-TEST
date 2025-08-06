package com.example.qr_test;

import android.content.SharedPreferences;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class Application extends android.app.Application {
    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    private static final String TAG = "Application";
    public Application() {
    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        String shartpath = this.getPackageName() + "_preferences";
        if (this.mSerialPort == null) {
            SharedPreferences sp = this.getSharedPreferences(shartpath, 0);
            String path = sp.getString("DEVICE", "");
            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
            if (path.length() <= 0 || baudrate == -1) {
                return null;
            }
            Log.d(TAG, "path = " + path);
            Log.d(TAG, "baudrate = " + baudrate);
            this.mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        Log.d(TAG, "mSerialPort = " + mSerialPort);
        return this.mSerialPort;
    }

    public void closeSerialPort() {
        if (this.mSerialPort != null) {
            this.mSerialPort.close();
            this.mSerialPort = null;
        }

    }
}
