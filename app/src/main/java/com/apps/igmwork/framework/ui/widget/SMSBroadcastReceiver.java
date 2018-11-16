package com.apps.igmwork.framework.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSBroadcastReceiver";
    private OnReceiveSMSListener mOnReceiveSMSListener;
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            for(Object pdu:pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte [])pdu);
                // 短信號碼
                String sender = smsMessage.getDisplayOriginatingAddress();
                Log.d(TAG,sender+"");
                //短信内容
                String content = smsMessage.getDisplayMessageBody();
                // 篩選
                if ("0931181957".equals(sender) && mOnReceiveSMSListener!= null) {
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(content);
                    if(matcher.find()) {
                        mOnReceiveSMSListener.onReceived(matcher.group());
                    }
                    abortBroadcast();
                }
            }
        }

    }

    /**
     * 回調接口
     */
    public interface OnReceiveSMSListener {
        void onReceived(String message);
    }


    public void setOnReceiveSMSListener(OnReceiveSMSListener onReceiveSMSListener) {
        mOnReceiveSMSListener = onReceiveSMSListener;
    }
}