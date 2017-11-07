package com.ewedo.devsearch.asynsic;

import android.os.AsyncTask;

import com.ewedo.devsearch.response.HostAsyncResponse;
import com.ewedo.devsearch.runnable.ScanPortsRunnable;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by fozei on 17-11-7.
 */

public class ScanPortsAsyncTask extends AsyncTask<Object, Void, Void> {
    private final WeakReference<HostAsyncResponse> delegate;

    /**
     * Constructor to set the delegate
     *
     * @param delegate Called when a port scan has finished
     */
    public ScanPortsAsyncTask(HostAsyncResponse delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    /**
     * Chunks the ports selected for scanning and starts the process
     * Chunked ports are scanned in parallel
     *
     * @param params IP address, start port, and stop port
     */
    @Override
    protected Void doInBackground(Object... params) {
        String ip = (String) params[0];
        int startPort = (int) params[1];
        int stopPort = (int) params[2];
        int timeout = (int) params[3];

        HostAsyncResponse activity = delegate.get();
        if (activity != null) {

            try {
                InetAddress address = InetAddress.getByName(ip);
                ip = address.getHostAddress();
            } catch (UnknownHostException e) {
//                activity.onProcessChange(false);
//                activity.onProcessChange(e);

                return null;
            }

            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
            Random rand = new Random();

            int chunk = (int) Math.ceil((double) (stopPort - startPort) / 8);
            int previousStart = startPort;
            int previousStop = startPort + chunk;

            for (int i = 0; i < 8; i++) {
                if (previousStop >= stopPort) {
                    executor.execute(new ScanPortsRunnable(ip, previousStart, stopPort, timeout, delegate));
                    break;
                }

                int schedule = rand.nextInt((int) ((((stopPort - startPort) / 8) / 1.5)) + 1) + 1;
                executor.schedule(new ScanPortsRunnable(ip, previousStart, previousStop, timeout, delegate), i % schedule, TimeUnit.SECONDS);

                previousStart = previousStop + 1;
                previousStop = previousStop + chunk;
            }

            executor.shutdown();

            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
                executor.shutdownNow();
            } catch (InterruptedException e) {
//                activity.onProcessChange(e);
            }
//            activity.onProcessChange(true);
        }

        return null;
    }
}
