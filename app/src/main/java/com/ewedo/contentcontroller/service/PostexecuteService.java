package com.ewedo.contentcontroller.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by fozei on 17-11-3.
 */

public class PostexecuteService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Runtime runtime = Runtime.getRuntime();
                try {
                    Log.i("***", "PostexecuteService.run: ++++++++++++++");
                    Process exec = runtime.exec("adb shell am force-stop com.ewedo.goodluck"+ " \n");
                    OutputStream outputStream = exec.getOutputStream();
                    Log.i("***", "PostexecuteService.run: " + outputStream);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();

                    InputStream inputStream = exec.getInputStream();
                    int i;
                    while ((i = inputStream.read()) != -1) {
                        Log.i("***", "PostexecuteService.run: ");
                        bao.write(i);
                    }
                    String str = bao.toString();
                    Log.i("***", "PostexecuteService.run: " + str);
                } catch (IOException e) {
                    Log.i("***", "PostexecuteService.run: error");
                    e.printStackTrace();
                }
            }
        }, 5000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
