package com.ewedo.devsearch;

import android.app.Activity;

import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.net.UnknownHostException;

/**
 * Created by fozei on 17-11-7.
 */

public class DeviceScanner {
    private Wireless wifi;

    public DeviceScanner(Activity activity, OnGetResultCallback onGetResultCallback) {
        init(activity, onGetResultCallback);
    }

    private void init(Activity activity, OnGetResultCallback callback) {
        try {
            wifi = new Wireless(activity.getApplicationContext());
            Integer ip = wifi.getInternalWifiIpAddress(Integer.class);
            Discovery.scanHosts(ip, wifi.getInternalWifiSubnet(), 150, callback);
        } catch (UnknownHostException e) {
        }
    }
}
