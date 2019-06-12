package com.apps.igmwork;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.android.AndroidUtils;
import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.widget.SMSBroadcastReceiver;
import com.apps.igmwork.framework.ui.widget.SMSContentObserver;
import com.bcfbaselibrary.internal.Logger;
import com.bcfbaselibrary.io.PhoneService;
import com.bcfbaselibrary.string.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;

import static com.apps.igmwork.framework.ui.widget.AlertDialogUtils.ShowMessageDialog;

public class VerCodeActivity  extends BaseActivity implements View.OnClickListener, SMSBroadcastReceiver.OnReceiveSMSListener {

    //静态成员
    private static final int PERMISSIONS_REQUEST_READ_SMS = 1;

    //控件成员
    protected EditText txtNickName;
    protected EditText txtEmail;
    @BindView(R.id.txtMobileNo)
    protected EditText txtMobileNo;
    @BindView(R.id.txtVerifyCode)
    protected EditText txtVerifyCode;
    @BindView(R.id.btn_getcord)
    protected Button btn_getcord;
    @BindView(R.id.btn_register)
    protected Button btn_register;

    //数据成员
    private int countSeconds = 60;  //倒計時秒數
    private Context mContext;
    private int gender;
    private boolean mReadSMSPermissionGranted;


    //对象成员
    private SMSBroadcastReceiver mSMSBroadcastReceiver = new SMSBroadcastReceiver();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vercode);
        UIInit("會員驗證");
        String mobile = "";
        if(mUserProfile.MobileNum != null) {
            mobile = mUserProfile.MobileNum;
        }
        mContext = this;
        CheckReadSMSPermission();

        txtMobileNo.setText(mobile);
        txtMobileNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    txtVerifyCode.requestFocus();
                    return true;
                }
                return  false;
            }
        });

        txtVerifyCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    btn_getcord.requestFocus();
                    return true;
                }
                return  false;
            }
        });


    }

    @Override
    public void onReceived(String message) {
        txtVerifyCode.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSMSBroadcastReceiver);
    }

    private SMSContentObserver smsContentObserver;

    private void registerContentObservers() {
        Uri uri = Telephony.Sms.CONTENT_URI;
        smsContentObserver = new SMSContentObserver(this, handler);
        getContentResolver().registerContentObserver(uri, true, smsContentObserver);
    }

    private void unregisterContentObservers() {
        getContentResolver().unregisterContentObserver(smsContentObserver);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            onKeycodeBack();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_getcord: {
                startCountBack();
                if(txtMobileNo.getText().toString().trim().length()==0)
                {
                    txtMobileNo.setError("請輸入電話號碼.");
                    txtMobileNo.requestFocus();
                    return;
                }
                if(!isMobileNo(txtMobileNo.getText().toString().trim()))
                {
                    txtMobileNo.setError("請輸入正確格式電話號碼.ex:0912345678.");
                    txtMobileNo.requestFocus();
                    return;
                }


                String url = HTTPServer.BuildURL(this, HTTPServer.PuchVerCodeURL,
                        new HTTPParam("uid",mUserProfile.UserID),
                        new HTTPParam("mobileNo",txtMobileNo.getText().toString().trim()));


                AddHTTPRequest("PushVerCode", null, url);
                break;
            }
            case R.id.btn_register: {
                if(txtVerifyCode.getText().toString().trim().length()==0)
                {
                    txtVerifyCode.setError("請輸入驗證碼.");
                    txtVerifyCode.requestFocus();
                    return;
                }

                String url = HTTPServer.BuildURL(this, HTTPServer.CheckedVerCodeURL,
                        new HTTPParam("uid",mUserProfile.UserID),
                        new HTTPParam("mobileNo",txtMobileNo.getText().toString()),
                        new HTTPParam("verCode",txtVerifyCode.getText().toString()));


                AddHTTPRequest("CheckedVerCode", null, url);
                break;
            }

            case R.id.btn_sign: {
                TransferActivity(SignActivity.class);
            }
        }
    }

    //實現後台數據事件
    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {
        //super.OnReceiveHTTPResp(key, contextValue, jsonServerResponse);

        if (key.equals("PushVerCode")) {
            if (jsonServerResponse.optInt("StatusCode") == 1) {
                //registerContentObservers();
                mSMSBroadcastReceiver.setOnReceiveSMSListener(this);
                // 註冊廣播
                IntentFilter intentFilter = new IntentFilter(SMSBroadcastReceiver.SMS_RECEIVED_ACTION);
                // 設置優先級
                intentFilter.setPriority(Integer.MAX_VALUE);
                registerReceiver(mSMSBroadcastReceiver,intentFilter);
            }
            if (jsonServerResponse.optInt("NoMobileNum") == 1) {

                ShowMessageDialog(this,"此電話號碼並不是會員。請重新輸入或重新註冊。");
                txtMobileNo.requestFocus();
            }
        }

        if (key.equals("CheckedVerCode")) {
            if (jsonServerResponse.optInt("StatusCode") == 1) {

                mUserProfile= UserProfile.Load(this);
                JSONArray dataList=jsonServerResponse.optJSONArray("DataSource");

                if(dataList!=null)
                {

                    ArrayList<ContentValues> objAccountData= JsonHelper.ConvertToContentList(dataList);

                    if(objAccountData.size()>0)
                    {
                        PhoneService objPhoneService=new PhoneService(this);
                        ContentValues cvUserProfile=objAccountData.get(0);

                        mUserProfile.DeviceID = objPhoneService.GetDeviceTaken();
                        mUserProfile.UserID = cvUserProfile.getAsInteger("UserID");
                        mUserProfile.UserName = cvUserProfile.getAsString("UserName");
                        mUserProfile.NickName = cvUserProfile.getAsString("NickName");
                        mUserProfile.Email = cvUserProfile.getAsString("Email");
                        mUserProfile.MobileNum = cvUserProfile.getAsString("MobileNum");
                        mUserProfile.Gender = cvUserProfile.getAsInteger("Gender");
                        mUserProfile.Save(this);
                    }

                }
                TransferActivity(MainFirstActivity.class);
            }
            else
            {
                if (jsonServerResponse.optInt("OverTime") == 1) {
                    Toast.makeText(getApplicationContext(), "您的驗證碼已過期，請重新獲取驗證碼", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "您的驗證碼錯誤。", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void CheckReadSMSPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            mReadSMSPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_SMS},
                    PERMISSIONS_REQUEST_READ_SMS);
        }
    }

    //獲取驗證碼按鈕進行倒數
    private Handler mCountHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (countSeconds > 0) {
                --countSeconds;
                btn_getcord.setText(" " + countSeconds + "秒 後獲取驗證碼 ");
                btn_getcord.setEnabled(false);
                mCountHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                countSeconds = 10;
                btn_getcord.setEnabled(true);
                btn_getcord.setText("獲取驗證碼");
                //finish();
            }
        }
    };

    //取得簡訊內容
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String code = String.valueOf(msg.what);
            txtVerifyCode.setText(code);
        }
    };
    //使用正则表達式判斷電話號碼
    public static boolean isMobileNo(String tel) {
        Pattern p = Pattern.compile("^09\\d{8}");
        Matcher m = p.matcher(tel);
        Logger.E(TAG,"MobileNo:"+m.matches());
        return m.matches();
    }

    //獲取驗證碼信息,並進行計時操作
    private void startCountBack() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCountHandler.sendEmptyMessage(0);
            }
        });
    }
}
