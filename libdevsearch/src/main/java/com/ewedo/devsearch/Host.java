package com.ewedo.devsearch;

import android.content.Context;
import android.database.sqlite.SQLiteException;

import com.ewedo.devsearch.asynsic.ScanPortsAsyncTask;
import com.ewedo.devsearch.asynsic.WolAsyncTask;
import com.ewedo.devsearch.response.HostAsyncResponse;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by fozei on 17-11-7.
 */

public class Host implements Serializable {

    private String hostname;
    private String ip;
    private String mac;

    /**
     * Constructor to set necessary information without a known hostname
     *
     * @param ip  This host's IP address
     * @param mac This host's MAC address
     */
    public Host(String ip, String mac) {
        this(null, ip, mac);
    }

    /**
     * Constructor to set necessary information with a known hostname
     *
     * @param hostname This host's hostname
     * @param ip       This host's IP address
     * @param mac      This host's MAC address
     */
    public Host(String hostname, String ip, String mac) {
        this.hostname = hostname;
        this.ip = ip;
        this.mac = mac;
    }

    /**
     * Starts a port scan
     *
     * @param ip        IP address
     * @param startPort The port to start scanning at
     * @param stopPort  The port to stop scanning at
     * @param timeout   Socket timeout
     * @param delegate  Delegate to be called when the port scan has finished
     */
    public static void scanPorts(String ip, int startPort, int stopPort, int timeout, HostAsyncResponse delegate) {
        new ScanPortsAsyncTask(delegate).execute(ip, startPort, stopPort, timeout);
    }

    /**
     * Fetches the MAC vendor from the database
     *
     * @param mac     MAC address
     * @param context Application context
     */
    public static String getMacVendor(String mac, Context context) throws IOException, SQLiteException {
//        Database db = new Database(context);
//        db.openDatabase("network.db");
//        Cursor cursor = db.queryDatabase("SELECT vendor FROM ouis WHERE mac LIKE ?", new String[]{mac});
//        String vendor;
//
//        if (cursor.moveToFirst()) {
//            vendor = cursor.getString(cursor.getColumnIndex("vendor"));
//        } else {
//            vendor = "Vendor not in database";
//        }
//
//        cursor.close();
//        db.close();

        return null;
    }

    /**
     * Returns this host's hostname
     *
     * @return
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets this host's hostname to the given value
     *
     * @param hostname Hostname for this host
     * @return
     */
    public Host setHostname(String hostname) {
        this.hostname = hostname;

        return this;
    }

    /**
     * Returns this host's IP address
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns this host's MAC address
     *
     * @return
     */
    public String getMac() {
        return mac;
    }

    public void wakeOnLan() {
        new WolAsyncTask().execute(mac, ip);
    }
}
