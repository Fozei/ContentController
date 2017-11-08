package com.ewedo.devsearch;

import android.app.Activity;
import android.util.Log;

import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by fozei on 17-11-7.
 */

public class DeviceScanner {
    private final Activity activity;
    private final OnGetResultCallback callback;
    private final List<String> macList;
    private Wireless wifi;

    public DeviceScanner(Activity activity, OnGetResultCallback onGetResultCallback, List<String> macList) {
        this.activity = activity;
        this.callback = onGetResultCallback;
        this.macList = macList;
    }

    public void start() {
        try {
            wifi = new Wireless(activity.getApplicationContext());
            Integer ip = wifi.getInternalWifiIpAddress(Integer.class);
            Discovery.scanHosts(ip, wifi.getInternalWifiSubnet(), 150, callback, macList);
        } catch (UnknownHostException e) {
            Log.i("***", "DeviceScanner.init error: ");
        }
    }
}
