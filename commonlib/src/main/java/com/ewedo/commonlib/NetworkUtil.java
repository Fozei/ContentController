package com.ewedo.commonlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

/**
 * Created by fozei on 17-11-19.
 */

public class NetworkUtil {

    private NetworkUtil() {
        throw new UnsupportedOperationException("NetworkUtil can't instantiate...");
    }

    /**
     * 检查是否有网络
     */
    public static boolean isNetworkConnected(Context context) {
        boolean bisConnFlag = false;
        if (context != null) {
            ConnectivityManager conManager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                bisConnFlag = conManager.getActiveNetworkInfo().isAvailable();
            }
        }
        return bisConnFlag;
    }

    /**
     * 检查WIFI是否连接
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 是否在使用wap或.net上网
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo
                    (ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //B = (I & M)|~M;网络广播地址计算方式

    /**
     * 获取WIFI—IP地址
     */
    public static String getWifiIp(Context context) {
        String ip = null;
        if (context != null) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = String.format(Locale.CHINA, "%d.%d.%d.%d", (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff));
        }
        return ip;
    }

    public static String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en != null && en.hasMoreElements(); ) {
                System.out.println("************************************************************");
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    Log.i("***", "NetworkUtil.getIpAddress: <---" + inetAddress);
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            return "Unknown";
        }

        return "Unknown";
    }

    public static InetAddress getBroadCastAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en != null && en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                List<InterfaceAddress> interfaceAddresses = intf.getInterfaceAddresses();
                for (int i = 0; i < interfaceAddresses.size(); i++) {
                    InterfaceAddress interfaceAddress = interfaceAddresses.get(i);
                    InetAddress address = interfaceAddress.getAddress();
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        return interfaceAddress.getBroadcast();
                    }
                }
            }
        } catch (SocketException ex) {
            return null;
        }

        return null;
    }

    /**
     * 获取链接类型
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * 检测域名是否可访问
     */
    public static void ping(final Context ctx, final String domain, final OnNetCheckListener
            listener) {

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (isNetworkConnected(ctx)) {
                    try {
                        int timeout = 60000;
                        String host = InetAddress.getByName(domain).getHostName();
                        return InetAddress.getByName(host).isReachable(timeout);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    return false;
                }
            }

            protected void onPostExecute(Boolean result) {
                listener.onChecked(result);
            }
        }.execute();
    }

    /**
     * 获取系统MAC
     */
    public static String getMac() {
        // return "C8:0E:77:30:77:62";
        String macAddr = null;
        macAddr = getEthMacByEth0();
        if (TextUtils.isEmpty(macAddr)) {
            macAddr = getMacByNetworkInterfaces();
        }
        if (TextUtils.isEmpty(macAddr)) {
            macAddr = getEthMacByFile();
            if (macAddr != null && macAddr.startsWith("0:")) {
                macAddr = "0" + macAddr;
            }
        }
        return macAddr;
    }

    /**
     * 获取当前系统连接网络的网卡的mac地址
     */
    @SuppressLint("NewApi")
    public static String getMacByNetworkInterfaces() {
        byte[] mac = null;
        StringBuffer sb = new StringBuffer();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();

                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (ip.isAnyLocalAddress() || !(ip instanceof Inet4Address) || ip.isLoopbackAddress()) {
                        continue;
                    }
                    if (ip.isSiteLocalAddress()) {
                        mac = ni.getHardwareAddress();
                    } else if (!ip.isLinkLocalAddress()) {
                        mac = ni.getHardwareAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (mac != null) {
            for (byte aMac : mac) {
                sb.append(parseByte(aMac));
            }
            return sb.substring(0, sb.length() - 1);
        }
        return null;
    }

    private static String parseByte(byte b) {
        String s = "00" + Integer.toHexString(b) + ":";
        return s.substring(s.length() - 3);
    }

    /**
     * 通过eth0获取以太网MAC
     */
    private static String getEthMacByEth0() {
        String mac = null;
        try {
            Enumeration<NetworkInterface> localEnumeration = NetworkInterface
                    .getNetworkInterfaces();

            while (localEnumeration.hasMoreElements()) {
                NetworkInterface localNetworkInterface = localEnumeration.nextElement();
                String interfaceName = localNetworkInterface.getDisplayName();
                if (interfaceName == null) {
                    continue;
                }
                if (interfaceName.equals("eth0")) {
                    mac = convertToMac(localNetworkInterface.getHardwareAddress());
                    if (mac != null && mac.startsWith("0:")) {
                        mac = "0" + mac;
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return mac;
    }

    private static String convertToMac(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            byte b = mac[i];
            int value = 0;
            if (b >= 0 && b < 16) {// Jerry(2013-11-6): if (b>=0&&b<=16) => if
                // (b>=0&&b<16)
                value = b;
                sb.append("0" + Integer.toHexString(value));
            } else if (b >= 16) {// Jerry(2013-11-6): else if (b>16) => else if
                // (b>=16)
                value = b;
                sb.append(Integer.toHexString(value));
            } else {
                value = 256 + b;
                sb.append(Integer.toHexString(value));
            }
            if (i != mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private static String getEthMacByFile() {
        String mac = readAddrFileAsString("/sys/class/net/eth0/address");
        if (mac == null) {
            mac = "";
        } else {
            mac = mac.toUpperCase(Locale.CHINA);
            if (mac.length() > 17) {
                mac = mac.substring(0, 17);
            }
        }
        return mac;
    }

    private static String readAddrFileAsString(String filePath) {
        try {
            if (new File(filePath).exists()) {
                StringBuffer fileData = new StringBuffer(1000);
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                char[] buf = new char[1024];
                int numRead = 0;
                while ((numRead = reader.read(buf)) != -1) {
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                }
                reader.close();
                return fileData.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }

    public interface OnNetCheckListener {
        void onChecked(Boolean result);
    }
}
