package com.ewedo.devsearch.asynsic;

import android.os.AsyncTask;
import android.util.Log;

import com.ewedo.devsearch.Host;
import com.ewedo.devsearch.callback.OnGetResultCallback;
import com.ewedo.devsearch.runnable.ScanHostsRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by fozei on 17-11-7.
 */

public class ScanHostsAsyncTask extends AsyncTask<Integer, Void, Void> {
    private static final String ARP_TABLE = "/proc/net/arp";
    private static final String ARP_INCOMPLETE = "0x0";
    private static final String ARP_INACTIVE = "00:00:00:00:00:00";
    private final WeakReference<OnGetResultCallback> delegate;

    /**
     * Constructor to set the callback
     *
     * @param callback Called when host discovery has finished
     */
    public ScanHostsAsyncTask(OnGetResultCallback callback) {
        this.delegate = new WeakReference<>(callback);
    }

    /**
     * Scans for active hosts on the network
     *
     * @param params IP address
     */
    @Override
    protected Void doInBackground(Integer... params) {
        int ipv4 = params[0];
        int cidr = params[1];
        int timeout = params[2];

        OnGetResultCallback callback = delegate.get();
        File file = new File(ARP_TABLE);
        if (!file.exists() || !file.canRead()) {
            callback.onError();
            return null;
        }

        ExecutorService executor = Executors.newCachedThreadPool();
        Log.i("***", "ScanHostsAsyncTask.doInBackground: -----------");
        double hostBits = 32.0d - cidr; // How many bits do we have for the hosts.
        int netmask = (0xffffffff >> (32 - cidr)) << (32 - cidr); // How many bits for the netmask.
        int numberOfHosts = (int) Math.pow(2.0d, hostBits) - 2; // 2 ^ hostbits = number of hosts in integer.
        int firstAddr = (ipv4 & netmask) + 1; // AND the bits we care about, then first addr.

        int SCAN_THREADS = (int) hostBits;
        int chunk = (int) Math.ceil((double) numberOfHosts / SCAN_THREADS); // Chunk hosts by number of threads.
        int previousStart = firstAddr;
        int previousStop = firstAddr + (chunk - 2); // Ignore network + first addr
        Log.i("***", "ScanHostsAsyncTask.doInBackground: " + (previousStart + -+previousStop));

        for (int i = 0; i < SCAN_THREADS; i++) {
            Log.i("***", "ScanHostsAsyncTask.doInBackground: +++++");
            executor.execute(new ScanHostsRunnable(previousStart, previousStop, timeout, delegate));
            previousStart = previousStop + 1;
            previousStop = previousStart + (chunk - 1);
        }
        Log.i("***", "ScanHostsAsyncTask.doInBackground: shutdown");
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            callback.onError();
            return null;
        }

        publishProgress();
        return null;
    }

    /**
     * Scans the ARP table and updates the list with hosts on the network
     * Resolves both DNS and NetBIOS
     * Don't update the UI in onPostExecute since we want to do multiple UI updates here
     * onPostExecute seems to perform all UI updates at once which would hinder what we're doing here
     * TODO: this method is gross, refactor it and break it up
     *
     * @param params
     */
    @Override
    protected final void onProgressUpdate(Void... params) {
        Log.i("***", "onProgressUpdate() called with: params = [" + params + "]");
        BufferedReader reader = null;
        OnGetResultCallback callback = delegate.get();
        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            reader = new BufferedReader(new FileReader(ARP_TABLE));
            reader.readLine(); // Skip header.
            String line;

            final List<String> result = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String[] arpLine = line.split("\\s+");
                final String ip = arpLine[0];
                final String flag = arpLine[2];
                final String macAddress = arpLine[3];

                if (!ARP_INCOMPLETE.equals(flag) && !ARP_INACTIVE.equals(macAddress)) {
                    executor.execute(new Runnable() {

                        @Override
                        public void run() {
                            Host host = new Host(ip, macAddress);
                            OnGetResultCallback callback1 = delegate.get();
                            try {
                                InetAddress add = InetAddress.getByName(ip);
                                String hostname = add.getCanonicalHostName();
                                Log.i("***", "ScanHostsAsyncTask.onProgressUpdate: " + ip + "::" + flag + "::" + macAddress);
                                host.setHostname(hostname);

                                if (callback1 != null) {
                                    result.add(hostname);
                                }
                            } catch (UnknownHostException e) {
                                callback1.onError();
                                return;
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onError();
            }

        } finally {
            executor.shutdown();
            if (callback != null) {
                callback.onError();
            }

            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
                // Something's really wrong if we can't close the stream...
            }
        }
    }
}