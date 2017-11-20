package com.ewedo.contentcontroller.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ewedo.commonlib.NetworkUtil;
import com.ewedo.contentcontroller.R;
import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.ewedo.contentcontroller.runnable.SocketRunnable;
import com.ewedo.devsearch.DeviceScanner;
import com.ewedo.devsearch.Wireless;
import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ewedo.contentcontroller.Constants.JUMP_SUB_1;
import static com.ewedo.contentcontroller.Constants.JUMP_SUB_2;
import static com.ewedo.contentcontroller.Constants.RESUME;
import static com.ewedo.contentcontroller.Constants.SHOW_HOME_PAGE;
import static com.ewedo.contentcontroller.Constants.START_SHOW;
import static com.ewedo.contentcontroller.Constants.SWAP_CARD;

/**
 * Created by fozei on 17-11-14.
 */

public class MainActivity extends Activity {

    private ExecutorService threadPool;
    private String currentIp;
    private TextView tvCurrentIp;
    private SharedPreferences sp;
    private EditText etIp;
    private ArrayList<String> macList;
    private View loading;
    private boolean searching;
    private String selfIp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        selfIp = Wireless.getInternalMobileIpAddress();
        threadPool = Executors.newCachedThreadPool();
        sp = getSharedPreferences("ip", MODE_PRIVATE);
        currentIp = sp.getString("ip", "0.0.0.0");
        if (TextUtils.equals(currentIp, "0.0.0.0")) {
            tvCurrentIp.setText(String.format("没有设置IP\n本机IP :%s", selfIp));
        } else {
            tvCurrentIp.setText(String.format("目标IP :%s\n本机IP :%s", currentIp, selfIp));
        }

        macList = new ArrayList<>();
        //开发广告机
//        macList.add("f2:61:e6:17:6c:61");
        //运维办公室广告机
//        macList.add("e0:b9:4d:fd:29:0a");
        //前台，演示用广告机
        macList.add("ec:3d:fd:05:90:64");
        //本机mac
//        macList.add("50:9a:4c:26:0f:fe");

        final InetAddress broadCastAddress = NetworkUtil.getBroadCastAddress();
        Log.i("***", "MainActivity.initData: " + broadCastAddress);

        new Thread() {
            @Override
            public void run() {
                try {
                    //表示创建一个默认的套接字，并绑定到本地地址和一个随机的端口号
                    DatagramSocket socket = new DatagramSocket(30008);
                    socket.setReuseAddress(true);
                    String msg = "hello server !";
                    byte[] bytes = msg.getBytes();
                    InetAddress byName = InetAddress.getByName("192.168.1.255");
                    Log.i("***", "MainActivity.run: " + byName);
                    //此处端口必须明确，是server监听"数据"的端口号
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, byName, 30008);
                    socket.send(packet);
                    Log.i("***", "MainActivity.run: send finish");
                } catch (Exception e) {
                    Log.i("***", "MainActivity.run: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void initView() {
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //搜索设备
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searching) {
                    return;
                }

                controlLoadingView(View.VISIBLE);
                DeviceScanner scanner = new DeviceScanner(MainActivity.this, new OnGetResultCallback() {
                    @Override
                    public void onGetResult(final List<String> list) {
                        if (list.size() == 0) {
                            searching = false;
                            Toast.makeText(MainActivity.this, "没有搜索到设备", Toast.LENGTH_SHORT).show();
                            controlLoadingView(View.INVISIBLE);
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                        View view = inflater.inflate(R.layout.dialog, null);
                        EditText editText = view.findViewById(R.id.et_ip);
                        editText.setText(list.get(0));
                        builder.setView(view);
                        builder.setTitle(macList.get(0) + " 的IP是：");

                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                controlLoadingView(View.INVISIBLE);
                            }
                        });
                        builder.setPositiveButton("确定替换？", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("ip", list.get(0));
                                edit.apply();

                                currentIp = list.get(0);
                                tvCurrentIp.setText(String.format("目标IP :%s\n本机IP :%s", list.get(0), selfIp));
                                controlLoadingView(View.INVISIBLE);
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                searching = false;
                            }
                        });
                        alertDialog.show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MainActivity.this, "搜索设备出错", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProcessChange(int i) {

                    }
                }, macList);
                searching = true;
                scanner.start();
            }
        });

        //停止原来节目，展示主界面
        findViewById(R.id.change_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(START_SHOW);
                SocketRunnable runnable = new SocketRunnable(currentIp, response);
                threadPool.execute(runnable);
                debug(response);
            }
        });

        //展示次级页面
        findViewById(R.id.show_sub_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(JUMP_SUB_1);
                SocketRunnable runnable = new SocketRunnable(currentIp, response);
                threadPool.execute(runnable);
                debug(response);
            }
        });

        //展示次级页面
        findViewById(R.id.show_sub_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(JUMP_SUB_2);
                SocketRunnable runnable = new SocketRunnable(currentIp, response);
                threadPool.execute(runnable);
                debug(response);
            }
        });

        //刷卡
        findViewById(R.id.swap_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(SWAP_CARD);
                SocketRunnable runnable = new SocketRunnable(currentIp, response);
                threadPool.execute(runnable);
                debug(response);
            }
        });

        //回退到主界面
        findViewById(R.id.show_home_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(SHOW_HOME_PAGE);
                SocketRunnable runnable = new SocketRunnable(currentIp, response);
                threadPool.execute(runnable);
                debug(response);
            }
        });

        //返回原来节目
        findViewById(R.id.resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleResponse response = new SimpleResponse();
                response.setMessage("OK");
                response.setState(200);
                response.getOrder().setType(RESUME);
                SocketRunnable runnable = new SocketRunnable(currentIp, response);
                threadPool.execute(runnable);
                debug(response);
            }
        });

        //setting
        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("设置目标IP");
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View view = inflater.inflate(R.layout.dialog, null);
                etIp = view.findViewById(R.id.et_ip);
                etIp.setHint(currentIp);
                builder.setView(view);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String desiredIp = etIp.getText().toString();
                        if (!TextUtils.isEmpty(desiredIp)) {
                            Pattern p = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$");
                            Matcher matcher = p.matcher(desiredIp.trim());
                            if (matcher.matches()) {
                                Log.i("***", "MainActivity.onClick: match");
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("ip", desiredIp.trim());
                                edit.apply();
                                currentIp = desiredIp.trim();
                                tvCurrentIp.setText(String.format("目标IP :%s\n本机IP :%s", desiredIp.trim(), selfIp));
                            } else {
                                Log.i("***", "MainActivity.onClick: not match");
                                Toast.makeText(MainActivity.this, "输入的IP不合法", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        tvCurrentIp = findViewById(R.id.tv_current_ip);
        loading = findViewById(R.id.progress);
    }

    private void controlLoadingView(int visibility) {
        loading.setVisibility(visibility);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadPool.shutdown();
    }

    private void debug(SimpleResponse response) {
//        SocketRunnable runnable = new SocketRunnable("192.168.27.2", response);
//        threadPool.execute(runnable);
    }
}
