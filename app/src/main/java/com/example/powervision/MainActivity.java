package com.example.powervision;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static java.lang.Thread.sleep;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText editText;
    static String userName;
    static String appName;
    static String scenarioNumber;
    static String scene;
    Intent intent;

    MyReceiver myReceiver;
    LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        editText=(EditText)findViewById(R.id.editText);

        setReceiver();
        setButton();
        setSpinners();

    }

    public void setReceiver()
    {
        myReceiver=new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.shi.zhu.bin");
        registerReceiver(myReceiver, intentFilter);

    }

    public void setButton()
    {
        Button btn3 = (Button) findViewById(R.id.button3);
        Button btn4 = (Button) findViewById(R.id.button4);
        Button btn5 = (Button) findViewById(R.id.button5);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
    }

    public void setSpinners()
    {
        Spinner mSpinner2 = (Spinner)findViewById(R.id.spinner2);
        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("游戏");
        list2.add("浏览网页");
        list2.add("微信打字");
        final ArrayAdapter<String> ad2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list2);
        ad2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner2.setAdapter(ad2);
        mSpinner2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                scene =parent.getItemAtPosition(position).toString();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                scene ="游戏";
            }
        });
    }



    @Override
    public void onClick(View v) {
        intent= new Intent(this, MyService.class);
        switch (v.getId()) {
            case R.id.button3:
                userName=editText.getText().toString();
                scenarioNumber="1";
                if(scene =="游戏")
                {
                    appName="NFS";
                    openTestScene(2649600,80000,85000,90);
                }
                else if(scene =="浏览网页")
                {
                    appName="chrome";
                    openTestScene(2649600,500,3000,50);
                }
                else
                {
                    appName="wechat";
                    openTestScene(2649600,500,3000,50);
                }
                break;
            case R.id.button4:
                userName=editText.getText().toString();
                scenarioNumber="2";
                if(scene =="游戏") {
                    appName="NFS";
                    openTestScene(652800,80000,85000,90);
                }
                else if(scene =="浏览网页")
                {
                    appName="chrome";
                    openTestScene(422400,500,3000,50);
                }
                else
                {
                    appName="wechat";
                    openTestScene(300000,500,3000,50);
                }
                break;
            case R.id.button5:
                userName=editText.getText().toString();
                scenarioNumber="3";
                if(scene =="游戏") {
                    appName="NFS";
                    openTestScene(300000,80000,85000,90);
                }
                else if(scene =="浏览网页")
                {
                    appName="chrome";
                    openTestScene(300000,500,3000,50);
                }
                else
                {
                    appName="wechat";
                    openTestScene(300000,500,3000,50);
                }
                break;
        }
    }

    public int sceneHZ;
    public int promoptDelayTime;
    public int waitTime;
    public int testTime;
    public void openTestScene(int hz, int promoptDelayMills, int waitMills, int testtime) {
        sceneHZ=hz;
        promoptDelayTime=promoptDelayMills;
        waitTime=waitMills;
        testTime=testtime;
        new Thread(new Runnable() {

            @Override
            public void run() {

                //以cpu最高频率启动测试app
                new CmdAsyncTask(2649600).execute();
                if(appName=="NFS")
                {
                    openApp("com.ea.nfs13c.aligames");
                }
                else if(appName=="chrome")
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.baidu.com")));
                }
                else
                {
                    openApp("com.tencent.mm");
                }

                //Reminder users that the test is starting
                promptTestStart(promoptDelayTime);

                //等待app启动
                try {
                    sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //设置测试的cpu频率
                new CmdAsyncTask(sceneHZ).execute();
                startService(intent);
                stopTest(testTime);
            }
        }).start();
    }

    public void promptTestStart(int delayMills)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopService(intent);
                Toast toast=Toast.makeText(MainActivity.this, "测试即将开始", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        },delayMills);
    }

    private void openApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            Toast.makeText(MainActivity.this, "no package", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    private void stopTest(final int time) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        stopService(intent);
                        Toast toast=Toast.makeText(MainActivity.this, "测试结束", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        new CmdAsyncTask(2649600).execute();
                    }
                },time*1000);
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    static String getAppName()
    {
        return appName;
    }

    static String getUserName()
    {
        return userName;
    }

    static String getScenarioNumber()
    {
        return scenarioNumber;
    }
}