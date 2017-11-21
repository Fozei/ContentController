package com.ewedo.devsearch;

import android.app.Activity;
import android.net.ConnectivityManager;

import com.ewedo.devsearch.callback.OnGetResultCallback;
import com.ewedo.devsearch.util.CommonUtil;
import com.ewedo.devsearch.util.EthernetUtil;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Created by fozei on 17-11-7.
 */

public class DeviceScanner {
    private final Activity activity;
    private final OnGetResultCallback callback;
    private final List<String> macList;

    public DeviceScanner(Activity activity, OnGetResultCallback onGetResultCallback, List<String> macList) {
        this.activity = activity;
        this.callback = onGetResultCallback;
        this.macList = macList;
    }

    public void start() {
        int connectedType = CommonUtil.getConnectedType(activity.getApplicationContext());
        Integer ip = 0;
        int subnet = 0;
        switch (connectedType) {
            case ConnectivityManager.TYPE_WIFI:
                try {
                    Wireless wifi = new Wireless(activity.getApplicationContext());
                    ip = wifi.getInternalWifiIpAddress(Integer.class);
                    subnet = wifi.getInternalWifiSubnet();
                } catch (UnknownHostException e) {
                    callback.onError(e);
                    e.printStackTrace();
                }
                break;
            case ConnectivityManager.TYPE_ETHERNET:

                try {
                    InetAddress inetAddress = EthernetUtil.getIpAddress();
                    byte[] address = inetAddress.getAddress();
                    ip = ((address[3] & 0xff) << 24) | ((address[2] & 0xff) << 16) | ((address[1] & 0xff) << 8) | (address[0] & 0xff);

                    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                        ip = Integer.reverseBytes(ip);
                    }
                    byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
                    ip = new BigInteger(InetAddress.getByAddress(ipByteArray).getAddress()).intValue();

                    NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                    for (InterfaceAddress iface : networkInterface.getInterfaceAddresses()) {
                        if (inetAddress.equals(iface.getAddress())) {
                            subnet = iface.getNetworkPrefixLength(); // This returns a short of the CIDR notation.
                        }
                    }
                } catch (UnknownHostException e) {
                    callback.onError(e);

                } catch (SocketException e) {
                    callback.onError(e);
                }
                break;
            default:
                callback.onError(new Exception("连接类型不匹配"));
                return;
        }

        Discovery.scanHosts(ip, subnet, 150, callback, macList);
    }
}
