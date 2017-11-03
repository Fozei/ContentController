package com.ewedo.contentcontroller;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.ewedo.contentcontroller.server.SimpleServer;
import com.ewedo.contentcontroller.server.Util;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int STANDBY = 1;
    private static final int CHANGE_CONTENT = 2;
    private static final int STOP_PLAY = 3;
    private SimpleServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        server = new SimpleServer(Util.getIp(this), 10000);

        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SimpleResponse response = new SimpleResponse();
                    response.setMessage("OK");
                    response.setState(200);
                    response.getOrder().setType(STANDBY);
                    server.setResponse(response);
                    server.start();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "开启失败！！！", Toast.LENGTH_SHORT);
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
                response.getOrder().setType(CHANGE_CONTENT);
                server.setResponse(response);
            }
        });

        findViewById(R.id.bt_quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(STOP_PLAY);
                server.setResponse(response);
            }
        });

        findViewById(R.id.bt_stop_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                server.stop();
            }
        });
    }

}
