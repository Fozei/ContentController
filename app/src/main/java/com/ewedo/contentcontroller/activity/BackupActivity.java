package com.ewedo.contentcontroller.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ewedo.contentcontroller.R;
import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.ewedo.contentcontroller.server.SimpleServer;
import com.ewedo.contentcontroller.server.Util;

import java.io.IOException;

import static com.ewedo.contentcontroller.Constants.RESUME;
import static com.ewedo.contentcontroller.Constants.SHOW_HOME_PAGE;
import static com.ewedo.contentcontroller.Constants.STANDBY;

public class BackupActivity extends AppCompatActivity {

    private SimpleServer server;
    private TextView tvInfoSecond;
    private TextView tvCurrentContent;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        ip = Util.getIp(this);
        server = new SimpleServer(ip, 10000);

        tvInfoSecond = findViewById(R.id.tv_info_board);
        tvInfoSecond.setText("服务器状态：未开启");

        tvCurrentContent = findViewById(R.id.tv_current_content);

        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("***", "BackupActivity.onClick: " + server.isAlive());
                if (server.isAlive()) {
                    return;
                }
                try {
                    SimpleResponse response = new SimpleResponse();
                    response.setMessage("OK");
                    response.setState(200);
                    response.getOrder().setType(STANDBY);
                    server.setResponse(response);
                    server.start();
                    tvInfoSecond.setText(String.format("服务器状态：已开启 %s port : %d", ip, 10000));
                } catch (IOException e) {
                    tvInfoSecond.setText("服务器状态：开启失败");
                    Toast.makeText(BackupActivity.this, "开启失败！！！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.bt_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(SHOW_HOME_PAGE);
                server.setResponse(response);
                tvCurrentContent.setText("已设置自定义节目");
            }
        });

        findViewById(R.id.bt_quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(RESUME);
                server.setResponse(response);
                tvCurrentContent.setText("已恢复默认节目");
            }
        });

        findViewById(R.id.bt_stop_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                server.stop();
                tvInfoSecond.setText("服务器状态：已关闭");
                tvCurrentContent.setText("");
            }
        });
    }

}
