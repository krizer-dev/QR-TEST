package com.example.qr_test;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class SerialPortPreferences extends PreferenceActivity {
    private static final String TAG = "SerialPortPreferences";
    private Application mApplication;
    private SerialPortFinder mSerialPortFinder;

    public SerialPortPreferences() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mApplication = (Application)this.getApplication();
        this.mSerialPortFinder = this.mApplication.mSerialPortFinder;
        this.addPreferencesFromResource(R.xml.serial_port_preferences);
        ListPreference devices = (ListPreference)this.findPreference("DEVICE");
        String[] entries = this.mSerialPortFinder.getAllDevices();
        String[] entryValues = this.mSerialPortFinder.getAllDevicesPath();
        devices.setEntries(entries);
        devices.setEntryValues(entryValues);
        devices.setSummary(devices.getValue());
        devices.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });
        ListPreference baudrates = (ListPreference)this.findPreference("BAUDRATE");
        baudrates.setSummary(baudrates.getValue());
        baudrates.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);
                return true;
            }
        });
    }
}
