package com.ewedo.contentcontroller.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ewedo.contentcontroller.R;
import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.ewedo.contentcontroller.runnable.SocketRunnable;
import com.ewedo.devsearch.DeviceScanner;
import com.ewedo.devsearch.Wireless;
import com.ewedo.devsearch.callback.OnGetResultCallback;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
    private long ip;
    private long mask;
    private DatagramSocket socket;
    private TextView tv_notice;
    private Handler handler;
    private Switch ss;
    private boolean canResetCurrentIP;
    private RadioGroup radioGroup;
    private String currentMac;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        handler = new Handler();
        selfIp = Wireless.getInternalMobileIpAddress();
        threadPool = Executors.newCachedThreadPool();
        sp = getSharedPreferences("cache_info", MODE_PRIVATE);
        currentIp = sp.getString("ip", "0.0.0.0");
        if (TextUtils.equals(currentIp, "0.0.0.0")) {
            tvCurrentIp.setText(String.format("没有设置IP\n本机IP :%s", selfIp));
        } else {
            tvCurrentIp.setText(String.format("目标IP :%s\n本机IP :%s", currentIp, selfIp));
        }
        currentMac = sp.getString("mac", null);
        macList = new ArrayList<>();
        if (!TextUtils.isEmpty(currentMac)) {
            macList.add(currentMac);
        }
        //开发广告机
//        macList.add("f2:61:e6:17:6c:61");
        //运维办公室广告机
//        macList.add("e0:b9:4d:fd:29:0a");
        //前台，演示用广告机
//        macList.add("ec:3d:fd:05:90:64");
        //本机mac
//        macList.add("50:9a:4c:26:0f:fe");

//        sendUdpMsg();

        //cal the broad cast address
        //B = (I & M)|~M
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        DhcpInfo di = wm.getDhcpInfo();
        ip = di.gateway;
        mask = di.netmask;

        if (socket == null) {
            try {
                socket = new DatagramSocket(30008);
                socket.setReuseAddress(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        //start listen the answer back
        threadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    byte[] buf = new byte[256];
                    DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
                    while (true) {
                        socket.receive(receivePacket);
                        InetAddress address = receivePacket.getAddress();
                        final String hostAddress = address.getHostAddress();
                        byte[] data = receivePacket.getData();

                        if (TextUtils.equals(hostAddress, selfIp) || !new String(data).startsWith("COFFEEBABY")) {
                            continue;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (TextUtils.equals(hostAddress, currentIp)) {
                                    tv_notice.setTextColor(Color.GREEN);
                                    tv_notice.setText(String.format("OK!!! --- Get answer from target device :\n%s", hostAddress));
                                } else {
                                    if (canResetCurrentIP) {
                                        List<String> list = new ArrayList<>();
                                        list.add(hostAddress);
                                        showDialog(list);
                                    }
                                }
                            }
                        });

                        Log.i("***", "MainActivity.run: " + hostAddress);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //udp check
        findViewById(R.id.bt_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_notice.setTextColor(Color.RED);
                tv_notice.setText("Checking ... ");
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String msg = "hello server !";
                            byte[] bytes = msg.getBytes();
                            InetAddress byName = InetAddress.getByName(long2ip(((ip & mask) | ~mask)));
                            //此处端口必须明确，是server监听"数据"的端口号
                            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, byName, 30008);
                            socket.send(packet);
                        } catch (Exception e) {
                            Log.i("***", "MainActivity.run: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        //搜索设备
        findViewById(R.id.search_devices).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searching || macList.size() == 0) {
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
                        showDialog(list);
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
                View view = inflater.inflate(R.layout.setting_dialog, null);
                radioGroup = view.findViewById(R.id.rg_container);
                etIp = view.findViewById(R.id.et_ip);
                etIp.setHint(currentIp);
                builder.setView(view);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String data = etIp.getText().toString();
                        if (TextUtils.isEmpty(data)) {
                            return;
                        }
                        int checkedId = radioGroup.getCheckedRadioButtonId();
                        switch (checkedId) {
                            case R.id.rb_IP:
                                Pattern p = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$");
                                Matcher matcher = p.matcher(data.trim());
                                if (matcher.matches()) {
                                    Log.i("***", "MainActivity.onClick: match");
                                    SharedPreferences.Editor edit = sp.edit();
                                    edit.putString("ip", data.trim());
                                    edit.apply();
                                    currentIp = data.trim();
                                    tvCurrentIp.setText(String.format("目标IP :%s\n本机IP :%s", data.trim(), selfIp));
                                } else {
                                    Log.i("***", "MainActivity.onClick: not match");
                                    Toast.makeText(MainActivity.this, "输入的IP不合法", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.rv_MAC:
                                int length = data.trim().length();
                                if (length != 12) {
                                    Toast.makeText(MainActivity.this, "输入的MAC不合法", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < length; i = i + 2) {
                                    String substring = data.substring(i, i + 2);
                                    sb.append(substring);
                                    if (i < 10) {
                                        sb.append(":");
                                    }
                                }
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("mac", sb.toString());
                                edit.apply();
                                macList.clear();
                                macList.add(sb.toString());
                                break;
                        }

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        tvCurrentIp = findViewById(R.id.tv_current_ip);
        loading = findViewById(R.id.progress);
        tv_notice = findViewById(R.id.tv_notice);
        ss = findViewById(R.id.ss);

        ss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                canResetCurrentIP = isChecked;
            }
        });
    }

    private void showDialog(final List<String> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.dialog, null);
        EditText editText = view.findViewById(R.id.et_ip);
        editText.setText(list.get(0));
        builder.setView(view);
        if (macList.size() > 0) {
            builder.setTitle(macList.get(0) + " 的IP是：");
        } else {
            builder.setTitle("New get IP from UDP：");
        }

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

    private void controlLoadingView(int visibility) {
        loading.setVisibility(visibility);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadPool.shutdown();
        handler.removeCallbacksAndMessages(null);
    }

    private void debug(SimpleResponse response) {
//        SocketRunnable runnable = new SocketRunnable("192.168.27.2", response);
//        threadPool.execute(runnable);
    }
}
