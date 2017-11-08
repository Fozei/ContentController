package com.ewedo.libserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

/**
 * Created by fozei on 17-11-8.
 */

public class OrderListener {
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        if (serverSocket == null) {
            try {
                serverSocket = ServerSocketFactory.getDefault().createServerSocket(1005);
                while (true) {
                    Socket socket = serverSocket.accept();
                    InputStream inputStream = socket.getInputStream();
                    String s = convertIs2String(inputStream);
                    System.out.println(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String convertIs2String(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
