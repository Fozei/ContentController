package com.ewedo.contentcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.ewedo.devsearch.DeviceScanner;
import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ewedo.contentcontroller.Constants.CHANGE_CONTENT;
import static com.ewedo.contentcontroller.Constants.RESUME;

/**
 * Created by fozei on 17-11-7.
 */

public class MainActivity extends Activity {
    public static String TAG = "***";
    private AtomicInteger index;
    private List<String> macList;
    private List<String> targetIpList;
    private DeviceScanner mScanner;
    private View btChangeContent;
    private View btResume;
    private TextView tvInfo;
    private View loadingView;
    private View btResearch;
    private TextView tvCurrent;
    private View btBackup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();
        initMacList();

        index = new AtomicInteger(0);

        mScanner = new DeviceScanner(this, new OnGetResultCallback() {
            @Override
            public void onGetResult(List<String> list) {
                if (list != null) {
                    targetIpList = list;
                    showDoneUi();
                    tvInfo.setText(String.format("检查了%d个地址，\n应该发现%d台设备，\n实际发现%d台设备。", index.get(), macList.size(), list.size()));
                    tvCurrent.setText("");
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.i("***", "MainActivity.onError: " + e.getMessage());
            }

            @Override
            public void onProcessChange(int i) {
                index.getAndIncrement();
            }
        }, macList);


        mScanner.start();


    }

    private void initMacList() {
        macList = new ArrayList<>();
        //广告机
        macList.add("f2:61:e6:17:6c:61");
        //台式机
        macList.add("50:9a:4c:26:0f:fe");
        //中兴白色手机
        macList.add("6c:8b:2f:f0:02:b6");
        //财务室旁机器
        macList.add("e0:b9:4d:f9:0e:da");
        //前台机器
        macList.add("e0:b9:4d:f8:ac:48");
        //前台壁挂
        macList.add("ec:3d:fd:05:90:64");
    }

    private void initView() {
        btBackup = findViewById(R.id.bt_backup);
        btBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BackupActivity.class);
                startActivity(intent);
            }
        });

        tvInfo = findViewById(R.id.tv_info);
        tvCurrent = findViewById(R.id.tv_current_state);
        loadingView = findViewById(R.id.loadingView);

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

                tvCurrent.setText("当前为定制节目");
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
                    response.getOrder().setType(RESUME);
                    SocketRunnable runnable = new SocketRunnable(targetIpList.get(i), response);
                    executorService.execute(runnable);
                }
                tvCurrent.setText("当前为默认节目");
            }
        });

        btResearch = findViewById(R.id.bt_research);
        btResearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = new AtomicInteger();
                showLoadingUi();
                mScanner.start();
            }
        });
    }

    private void showLoadingUi() {
        loadingView.setVisibility(View.VISIBLE);
        btChangeContent.setVisibility(View.INVISIBLE);
        btResume.setVisibility(View.INVISIBLE);
        btResearch.setVisibility(View.INVISIBLE);
        tvInfo.setVisibility(View.INVISIBLE);
        tvCurrent.setVisibility(View.INVISIBLE);
        btBackup.setVisibility(View.INVISIBLE);
    }

    private void showDoneUi() {
        loadingView.setVisibility(View.INVISIBLE);
        btChangeContent.setVisibility(View.VISIBLE);
        btResume.setVisibility(View.VISIBLE);
        tvInfo.setVisibility(View.VISIBLE);
        btResearch.setVisibility(View.VISIBLE);
        tvCurrent.setVisibility(View.VISIBLE);
        btBackup.setVisibility(View.VISIBLE);
    }
}
