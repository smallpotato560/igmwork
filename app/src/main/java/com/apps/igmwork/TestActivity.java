package com.apps.igmwork;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.data.StringParam;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class TestActivity extends BaseActivity {

    Button btn_Notify;
    //震動時間長度參數
    long[] vibrate = {0,100,200,300};
    //音樂Uri參數
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    // uri = Uri.parse("file:///sdcard/Notifications/hangout_ringtone.m4a");
    // uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ring);
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String msg = intent.getStringExtra("aid");
        if (msg!=null) {
            Log.e("FCM", "aid:" + msg);
            TransferActivityCarryValue(MainFirstActivity.class,
                    new StringParam("aid",msg));

        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_testnotification);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("token", "refresh token:"+refreshedToken);
        FirebaseMessaging.getInstance().subscribeToTopic("android");
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        btn_Notify = (Button)findViewById(R.id.btn_Notify);
        btn_Notify.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Step1. 初始化NotificationManager，取得Notification服務
                NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                //Step2. 設定當按下這個通知之後要執行的activity
                Intent notifyIntent = new Intent(TestActivity.this, MainFirstActivity.class);
                notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent appIntent = PendingIntent.getActivity(TestActivity.this, 0, notifyIntent, 0);

                //Step3. 透過 Notification.Builder 來建構 notification，
                //並直接使用其.build() 的方法將設定好屬性的 Builder 轉換
                //成 notification，最後開始將顯示通知訊息發送至狀態列上。
                Notification notification
                        = new Notification.Builder(TestActivity.this)
                        .setContentIntent(appIntent)
                        .setSmallIcon(R.mipmap.ic_launcher) // 設置狀態列裡面的圖示（小圖示）　　
                        .setLargeIcon(BitmapFactory.decodeResource(TestActivity.this.getResources(), R.mipmap.ic_launcher)) // 下拉下拉清單裡面的圖示（大圖示）
                        .setTicker("notification on status bar.") // 設置狀態列的顯示的資訊
                        .setWhen(System.currentTimeMillis())// 設置時間發生時間
                        .setAutoCancel(true) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                        .setContentTitle("活動標題") // 設置下拉清單裡的標題
                        .setContentText("活動內文")// 設置上下文內容
                        .setOngoing(true)      //true使notification變為ongoing，用戶不能手動清除// notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;
                        .setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
//                 .setDefaults(Notification.DEFAULT_VIBRATE) //使用默認手機震動提示
//                 .setDefaults(Notification.DEFAULT_SOUND) //使用默認聲音提示
//                 .setDefaults(Notification.DEFAULT_LIGHTS) //使用默認閃光提示
//                 .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND) //使用默認閃光提示 與 默認聲音提示

                        .setVibrate(vibrate) //自訂震動長度
//                 .setSound(uri) //自訂鈴聲
//                 .setLights(0xff00ff00, 300, 1000) //自訂燈光閃爍 (ledARGB, ledOnMS, ledOffMS)

                        .build();

                // 將此通知放到通知欄的"Ongoing"即"正在運行"組中
                notification.flags = Notification.FLAG_ONGOING_EVENT;

                // 表明在點擊了通知欄中的"清除通知"後，此通知不清除，
                // 經常與FLAG_ONGOING_EVENT一起使用
                notification.flags = Notification.FLAG_NO_CLEAR;

                //閃爍燈光
                notification.flags = Notification.FLAG_SHOW_LIGHTS;

                // 重複的聲響,直到用戶響應。
                //notification.flags = Notification.FLAG_INSISTENT;


                // 把指定ID的通知持久的發送到狀態條上.
                mNotificationManager.notify(0, notification);

                // 取消以前顯示的一個指定ID的通知.假如是一個短暫的通知，
                // 試圖將之隱藏，假如是一個持久的通知，將之從狀態列中移走.
//              mNotificationManager.cancel(0);

                //取消以前顯示的所有通知.
//              mNotificationManager.cancelAll();
            }
        });
    }
}
