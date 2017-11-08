package com.ewedo.contentcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.ewedo.devsearch.DeviceScanner;
import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fozei on 17-11-7.
 */

public class SecondActivity extends Activity {
    public static String TAG = "***";
    private DeviceScanner mScanner;
    private int index;
    private View viewById;
    private List<String> macList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_second);
        viewById = findViewById(R.id.bt);

        macList = new ArrayList<>();
        //广告机
        macList.add("f2:61:e6:17:6c:61");
        //台式机
        macList.add("50:9a:4c:26:0f:fe");

        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Log.i("***", "SecondActivity.onClick: send");
                            Socket socket = new Socket("192.168.0.4", 10005);
                            OutputStream outputStream = socket.getOutputStream();
                            String s = "this is message from phone" + "\n";
                            outputStream.write(s.getBytes());
                            outputStream.flush();
                            socket.shutdownOutput();
                            Log.i("***", "SecondActivity.run: ++++++++");
                        } catch (IOException e) {
                            Log.i("***", "SecondActivity.onClick: send immor error");
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        mScanner = new DeviceScanner(this, new OnGetResultCallback() {
            @Override
            public void onGetResult(List<String> list) {
                if (list != null) {
                    Log.i("***", "SecondActivity.onGetResult: " + list.size() + ":::" + list.get(0));
                    viewById.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.i("***", "SecondActivity.onError: " + e.getMessage());
            }

            @Override
            public void onProcessChange(int i) {
                index++;
                Log.i("***", "SecondActivity.onProcessChange: " + index);
            }
        }, macList);


    }
}
