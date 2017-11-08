package com.ewedo.contentcontroller;

import android.util.Log;

import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by fozei on 17-11-8.
 */

public class SocketRunnable implements Runnable {
    private final String targetIp;
    private final SimpleResponse simpleResponse;

    public SocketRunnable(String ip, SimpleResponse simpleResponse) {
        this.targetIp = ip;
        this.simpleResponse = simpleResponse;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(targetIp, 10005);
            OutputStream outputStream = socket.getOutputStream();
            Gson gson = new Gson();
            String s = gson.toJson(simpleResponse);
            outputStream.write(s.getBytes());
            outputStream.flush();
            socket.shutdownOutput();
        } catch (IOException e) {
            Log.i("***", "SecondActivity.onClick: send immor error" + e.getMessage() + "\n" + targetIp);
            e.printStackTrace();
        }
    }
}
