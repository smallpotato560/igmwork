package com.apps.igmwork.framework.ui.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;

public class SMSContentObserver extends ContentObserver {
    private static final String TAG = "SMSContentObserver";
    private Context mContext;
    private Handler mHandler;

    public SMSContentObserver(Handler handler) {
        super(handler);
        mHandler = handler;
    }

    public SMSContentObserver(Context context, Handler handler){
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        // TODO 讀取簡訊內容
        if (uri.toString().equals("content://sms/raw")){
            return;
        }

        String[] projection = new String[]{
                Telephony.Sms.Inbox._ID,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.BODY
        };

        Cursor cursor = mContext.getContentResolver().query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                null,
                null,
                Telephony.Sms.Inbox.DATE + " DESC  limit 1"
        );

        if (cursor.moveToFirst()){
            String id = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox._ID));
            String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.ADDRESS));
            String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY));

            if (address.equals("0931181957")){
                Message msg = new Message();
                msg.what = Integer.parseInt(body);
                mHandler.sendMessage(msg);
            }

        }

        cursor.close();
    }
}
