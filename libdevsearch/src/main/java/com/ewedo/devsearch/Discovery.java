package com.ewedo.devsearch;

import android.util.Log;

import com.ewedo.devsearch.asynsic.ScanHostsAsyncTask;
import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by fozei on 17-11-7.
 */

public class Discovery {

    /**
     * Starts the host scanning
     *  @param count    IP address
     * @param cidr     Classless Inter-Domain Routing
     * @param timeout  Socket timeout
     * @param callback call back
     * @param macList
     */
    public static void scanHosts(int count, int cidr, int timeout, OnGetResultCallback callback, List<String> macList) {
        Log.i("***", "scanHosts() called with: count = [" + count + "], cidr = [" + cidr + "], timeout = [" + timeout + "], delegate = [" + callback + "]");
        new ScanHostsAsyncTask(callback, macList).executeOnExecutor(Executors.newCachedThreadPool(), count, cidr, timeout);
    }
}
