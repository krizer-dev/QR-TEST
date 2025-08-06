package com.example.qr_test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public abstract class SerialPortActivity extends Activity {
    protected Application mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    protected InputStream mInputStream;

    public SerialPortActivity() {
    }

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SerialPortActivity.this.finish();
            }
        });
        b.show();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mApplication = (Application)this.getApplication();

        try {
            this.mSerialPort = this.mApplication.getSerialPort();

            if (this.mSerialPort == null) {
                this.DisplayError(R.string.error_configuration);
                return;
            }

            this.mOutputStream = this.mSerialPort.getOutputStream();
            this.mInputStream = this.mSerialPort.getInputStream();
        } catch (SecurityException var3) {
            this.DisplayError(R.string.error_security);
        } catch (IOException var4) {
            this.DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException var5) {
            this.DisplayError(R.string.hello_world);
        }

    }

    protected void onDestroy() {
        if (this.mApplication != null) {
            this.mApplication.closeSerialPort();
        }

        this.mSerialPort = null;
        super.onDestroy();
    }
}