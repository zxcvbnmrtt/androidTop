package com.example.myapplication;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Process;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyService extends Service {
    public static final String TAG = "MyService";
    private NotificationManager notificationManager;
    private static final String notificationId = "channelId";
    private static final String notificationName = "channelName";
    public static final String RECEIVER_ACTION = "com.example.findtopactivity";

    private static final int KUKA = 0;
    private Looper looper;
    private ServiceHandler handler;


    public MyService() {
    }

    public IBinder onBind(Intent intent) {
        Log.i(TAG,"call onBind...");
        return null;
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle("测试服务")
                .setContentText("我正在运行");
        //设置Notification的ChannelID,否则不能正常显示
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        return notification;
    }

    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {

            while (true) {
                try {
                    Thread.sleep(3000);
                    Calendar cal = Calendar.getInstance();
                    int dayofyear = cal.get(Calendar.SECOND);
                    Log.i(TAG, "The obj field of msg:" + dayofyear);
                    //String strClsName = getTopActivity();
                    //Log.i(TAG, "Activity ClsName:" + strClsName);
                    String strClsName = getTopApp();
                    Intent intent = new Intent();
                    intent.putExtra("topActivity", strClsName);
                    intent.setAction(RECEIVER_ACTION);
                    // 通知显示
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


                } catch (InterruptedException ex) {
                    Log.i(TAG, "出现异常");
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG,"call onCreate...");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            startForeground(1,getNotification());
        }


        // 默认情况下Service是运行在主线程中，而服务一般又十分耗费时间，如果
        // 放在主线程中，将会影响程序与用户的交互，因此把Service
        // 放在一个单独的线程中执行
        HandlerThread thread = new HandlerThread("MessageDemoThread",  Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // 获取当前线程中的looper对象
        looper = thread.getLooper();
        //创建Handler对象，把looper传递过来使得handler、
        //looper和messageQueue三者建立联系
        handler = new ServiceHandler(looper);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.i(TAG,"call onStart...");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(1,getNotification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"call onStartCommand...");


        //从消息池中获取一个Message实例
        Message msg = handler.obtainMessage();
        // arg1保存线程的ID，在handleMessage()方法中
        // 我们可以通过stopSelf(startId)方法，停止服务
        msg.arg1 = startId;
        // msg的标志
        msg.what = KUKA;
        // 在这里我创建一个date对象，赋值给obj字段
        // 在实际中我们可以通过obj传递我们需要处理的对象
        Date date = new Date();
        msg.obj = date;
        // 把msg添加到MessageQueue中
        handler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"call onDestroy...");
    }

    //判断当前界面显示的是哪个Activity
    public String getTopActivity(){
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.i(TAG, "pkg:"+cn.getPackageName());//包名
        Log.i(TAG, "cls:"+cn.getClassName());//包名加类名
        return cn.getClassName();
    }

    private String getTopApp() {
        String szTopActivity = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager m = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long now = System.currentTimeMillis();
                //获取60秒之内的应用数据
                List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);
                Log.i(TAG, "Running app number in last 60 seconds : " + stats.size());

                String topActivity = "";

                //取得最近运行的一个app，即当前运行的app
                if ((stats != null) && (!stats.isEmpty())) {
                    int j = 0;
                    for (int i = 0; i < stats.size(); i++) {
                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                            j = i;
                        }
                    }
                    topActivity = stats.get(j).getPackageName();
                }
                Log.i(TAG, "top running app is : " + topActivity);
                szTopActivity = topActivity;
            }
        }

        return szTopActivity;
    }

}