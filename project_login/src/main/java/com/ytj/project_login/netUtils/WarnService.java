package com.ytj.project_login.netUtils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.ytj.project_login.BaseBDMapActivity;
import com.ytj.project_login.DetailActivity;
import com.ytj.project_login.R;
import com.ytj.project_login.WarnMapActivity;
import com.ytj.project_login.utils.ConstantUtil;
import com.ytj.project_login.utils.SharePreferencesUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * Created by ddanyang on 2016/10/17.
 * 任何情况下后台轮训，检查预警消息
 * 在DetailActivity.getInfo(),当获取到用户信息之后，userID
 */

public class WarnService extends Service {

    JSONObject jsonObject;
    NotificationManager manager;
    private final static int SHOW_NOTIFICATION = 1;
    private final static int UPDATA_IS_LOG = 2;
    private static boolean IS_UPDATA_LOG = true;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_NOTIFICATION:
                    warnTaskInBackground();
                    break;
                case UPDATA_IS_LOG:
                    IS_UPDATA_LOG = true;
                    break;
            }
        }
    };

    public WarnService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (IS_UPDATA_LOG) {
                            mHandler.sendEmptyMessage(SHOW_NOTIFICATION);
                            IS_UPDATA_LOG = false;
                        }
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
//        mHandler.sendEmptyMessage(SHOW_NOTIFICATION);
        return super.onStartCommand(intent, flags, startId);
    }

    //请求预警信息
    void warnTaskInBackground() {
        Log.i("WarnService", "onResponse: yes i am map" + ConstantUtil.IP);

        OkHttpUtils
                .post()
                .url(ConstantUtil.IP + "/MapLocal/android/checkNewLog")
                .addParams("id", (String) SharePreferencesUtil.getParam(this, "userId", ""))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(WarnService.this, "预警请求有误", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onResponse(String s) {
                        Log.i("WarnService", "onResponse: yes i am map" + s);
//                        Toast.makeText(WarnService.this, s, Toast.LENGTH_LONG);
                        if (!s.equals("null")) {
                            try {
                                jsonObject = new JSONObject(s);
                                if (jsonObject.length() != 0) {

                                    Intent intent = new Intent(WarnService.this, BaseBDMapActivity.class);
                                    intent.putExtra("lat", jsonObject.getString("lat"));
                                    intent.putExtra("lon", jsonObject.getString("lon"));
                                    intent.putExtra("wid", jsonObject.getString("wid"));
                                    intent.putExtra("tid", jsonObject.getString("tid"));
                                    intent.putExtra("radius", jsonObject.getString("radius"));
                                    intent.putExtra("caseId", jsonObject.getString("caseid"));
                                    intent.putExtra("wType", jsonObject.getString("wType"));

                                    PendingIntent pendingIntent = PendingIntent.getActivity(WarnService.this,
                                            0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                                    Notification.Builder builder = new Notification.Builder(WarnService.this)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle("聊天导航")
                                            .setContentText("预警信息，请点击查看");
                                    builder.setTicker("预警");
                                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher, null));
                                    builder.setAutoCancel(true);
                                    builder.setContentIntent(pendingIntent);
                                    if (Build.VERSION.SDK_INT > 15) {
                                        manager.notify(122, builder.build());
                                    }
                                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    long[] pattern = {100, 400, 100, 400};   // 停止 开启 停止 开启
                                    vibrator.vibrate(pattern, 3);
                                    mHandler.sendEmptyMessage(UPDATA_IS_LOG);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return;
                        } else {
                            mHandler.sendEmptyMessage(UPDATA_IS_LOG);
                            Log.i("WarnService", "onResponse: " + IS_UPDATA_LOG);
                        }
                    }
                });

    }
}
