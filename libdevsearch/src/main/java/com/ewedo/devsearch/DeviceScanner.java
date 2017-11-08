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
    private Wireless wifi;

    public DeviceScanner(Activity activity, OnGetResultCallback onGetResultCallback, List<String> macList) {
        init(activity, onGetResultCallback, macList);
    }

    private void init(Activity activity, OnGetResultCallback callback, List<String> macList) {
        try {
            wifi = new Wireless(activity.getApplicationContext());
            Integer ip = wifi.getInternalWifiIpAddress(Integer.class);
            Discovery.scanHosts(ip, wifi.getInternalWifiSubnet(), 150, callback, macList);
        } catch (UnknownHostException e) {
            Log.i("***", "DeviceScanner.init error: ");
        }
    }
}
