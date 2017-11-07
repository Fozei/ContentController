package com.ewedo.contentcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ewedo.devsearch.DeviceScanner;
import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fozei on 17-11-7.
 */

public class SecondActivity extends Activity {
    public static String TAG = "***";
    private DeviceScanner mScanner;
    private ArrayList<String> macList;
    private int index;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mScanner = new DeviceScanner(this, new OnGetResultCallback() {
            @Override
            public void onGetResult(List<String> list) {
                if (list != null) {
                    Log.i("***", "SecondActivity.onGetResult: " + list.size() + ":::");
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
        });

    }
}
