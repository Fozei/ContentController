package com.ewedo.devsearch.runnable;

import android.util.Log;

import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by fozei on 17-11-7.
 */

public class ScanHostsRunnable implements Runnable {
    private final WeakReference<OnGetResultCallback> delegate;
    private int start;
    private int stop;
    private int timeout;

    /**
     * Constructor to set the necessary data to scan for hosts
     *
     * @param start    Host to start scanning at
     * @param stop     Host to stop scanning at
     * @param timeout  Socket timeout
     * @param delegate Called when host discovery has finished
     */
    public ScanHostsRunnable(int start, int stop, int timeout, WeakReference<OnGetResultCallback> delegate) {
        Log.i("***", "ScanHostsRunnable() called with: start = [" + start + "], stop = [" + stop + "], timeout = [" + timeout + "], delegate = [" + delegate + "]");
        Log.i("***", "ScanHostsRunnable.ScanHostsRunnable: " + (stop - start + 1));
        this.start = start;
        this.stop = stop;
        this.timeout = timeout;
        this.delegate = delegate;
    }

    /**
     * Starts the host discovery
     */
    @Override
    public void run() {
        for (int i = start; i <= stop; i++) {
            Socket socket = new Socket();
            try {
                socket.setTcpNoDelay(true);
                byte[] bytes = BigInteger.valueOf(i).toByteArray();
                InetAddress byAddress = InetAddress.getByAddress(bytes);
                socket.connect(new InetSocketAddress(byAddress, 7), timeout);
            } catch (IOException ignored) {
                // Connection failures aren't errors in this case.
                // We want to fill up the ARP table with our connection attempts.
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Something's really wrong if we can't close the socket...
                }

                OnGetResultCallback activity = delegate.get();
                if (activity != null) {
                    activity.onProcessChange(1);
                }
            }
        }
    }
}