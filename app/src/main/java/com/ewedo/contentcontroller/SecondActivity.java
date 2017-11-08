package com.ewedo.contentcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.ewedo.devsearch.DeviceScanner;
import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ewedo.contentcontroller.Constants.CHANGE_CONTENT;
import static com.ewedo.contentcontroller.Constants.STANDBY;

/**
 * Created by fozei on 17-11-7.
 */

public class SecondActivity extends Activity {
    public static String TAG = "***";
    private int index;
    private List<String> macList;
    private List<String> targetIpList;
    private DeviceScanner mScanner;
    private View btChangeContent;
    private View btResume;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_second);
        initView();

        macList = new ArrayList<>();
        //广告机
        macList.add("f2:61:e6:17:6c:61");
        //台式机
        macList.add("50:9a:4c:26:0f:fe");
        //中兴白色手机
        macList.add("6c:8b:2f:f0:02:b6");

        mScanner = new DeviceScanner(this, new OnGetResultCallback() {
            @Override
            public void onGetResult(List<String> list) {
                if (list != null) {
                    Log.i("***", "SecondActivity.onGetResult: " + index + "::" + list.size() + ":::" + list.get(0));
                    Log.i("***", "SecondActivity.onGetResult: " + list);
                    targetIpList = list;
                    btChangeContent.setVisibility(View.VISIBLE);
                    btResume.setVisibility(View.VISIBLE);
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
            }
        }, macList);


    }

    private void initView() {
        btChangeContent = findViewById(R.id.bt_change_second);
        btChangeContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (targetIpList == null || targetIpList.size() <= 0) {
                    return;
                }

                ExecutorService executorService = Executors.newCachedThreadPool();
                for (int i = 0; i < targetIpList.size(); i++) {
                    SimpleResponse response = new SimpleResponse();
                    response.setMessage("OK");
                    response.setState(200);
                    response.getOrder().setType(CHANGE_CONTENT);
                    SocketRunnable runnable = new SocketRunnable(targetIpList.get(i), response);
                    executorService.execute(runnable);
                }
            }
        });

        btResume = findViewById(R.id.bt_resume_content);
        btResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (targetIpList == null || targetIpList.size() <= 0) {
                    return;
                }

                ExecutorService executorService = Executors.newCachedThreadPool();
                for (int i = 0; i < targetIpList.size(); i++) {
                    SimpleResponse response = new SimpleResponse();
                    response.setMessage("OK");
                    response.setState(200);
                    response.getOrder().setType(STANDBY);
                    SocketRunnable runnable = new SocketRunnable(targetIpList.get(i), response);
                    executorService.execute(runnable);
                }
            }
        });
    }
}
