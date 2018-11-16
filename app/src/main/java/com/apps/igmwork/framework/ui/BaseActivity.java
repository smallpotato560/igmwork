package com.apps.igmwork.framework.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apps.igmwork.R;
import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.android.AndroidUtils;
import com.apps.igmwork.framework.network.NetworkUtils;
import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.data.StringParam;
import com.bcfbaselibrary.net.HTTPRequest;
import com.bcfbaselibrary.net.volley.VolleyHTTPRequest;
import com.bcfbaselibrary.net.volley.VolleyHTTPRequestQueue;

import org.json.JSONObject;

import java.net.URLEncoder;

import butterknife.ButterKnife;

/**
 * Created by Ben on 11/1/2017.
 */

public class BaseActivity extends AppCompatActivity implements Handler.Callback,VolleyHTTPRequest.OnHTTPRequestListener {


    //静态成员
    public static String TAG="APP_2_0";
    final public static int MESSAGE_HTTP_REQUEST_TIME_OUT=1000011;
    final public static int Flag_LoadingBar_Disabled=1000012;

    public static final int HandlerMsg_LoadingDailogClosedByTimeout=0;

    //数据成员
    protected boolean bShowLoadingBar=true;
    //控件成员
    protected Dialog dlgLoading;
    protected TextView lblMainTitle;
    //UI Components
    protected Toolbar mMainToolbar;
    //界面上的进度条
    protected View progressBarLoading=null;
    //进交网络操作的控件
    protected View viewStartLoading=null;

    //对象成员
    //Data members
    //protected APPConfig mPhoneConfig;
    protected UserProfile mUserProfile=null;

    protected AndroidUtils mAndroidUtils;
    public Handler UIHandler;


    //Network members
    protected VolleyHTTPRequestQueue mVolleyHTTPRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);

        mAndroidUtils=new AndroidUtils(this);
        UIHandler =  new Handler(Looper.getMainLooper());
        mUserProfile=GetUserProfile();

        mVolleyHTTPRequestQueue=VolleyHTTPRequestQueue.getInstance(this);

        //getWindow().setSoftInputMode(
        //       WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        super.onCreate(savedInstanceState);

    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        InitContentView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        InitContentView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        InitContentView();
    }

    private void InitContentView() {
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setListener();
    }

    protected void setListener() {
    }


    private void setSystemBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // LOLLIPOP解决方案
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // KITKAT解决方案
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         
    }

    protected void onDestroy()
    {
        super.onDestroy();

    }


    @Override
    public boolean handleMessage(Message message) {
        //处理网络HTTP请求超时事件
        if(message.what==MESSAGE_HTTP_REQUEST_TIME_OUT)
        {
            OnPostHTTPRequest("",null);
            //this.ShowConfirmMessage(getString(R.string.app_common_msg_error_network),"["+getString(R.string.action_confirm)+"]");
        }
        return false;
    }

    @Override
    public void OnPreHTTPRequest(Object key, Object contextObject) {

        if(progressBarLoading!=null&&bShowLoadingBar&&!((Integer)Flag_LoadingBar_Disabled).equals(contextObject)
                &&!key.equals("GetGeoLocation"))
        {
            progressBarLoading.setVisibility(View.VISIBLE);
        }
        if(viewStartLoading!=null)
        {
            viewStartLoading.setEnabled(false);
        }

        UIHandler.removeMessages(MESSAGE_HTTP_REQUEST_TIME_OUT);

        if(!key.equals("GetGeoLocation")) {
            UIHandler.sendEmptyMessageDelayed(MESSAGE_HTTP_REQUEST_TIME_OUT, VolleyHTTPRequestQueue.CONNECTION_TIME_OUT);
        }
    }

    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {
        Log.e(key.toString(),jsonServerResponse.toString());

        if(jsonServerResponse.has("StatusCode"))
        {
            //处理API出错情况
            if(jsonServerResponse.optInt("StatusCode")<0)
            {
                Exception e=new Exception("APPException");
                OnHandleError( key, contextObject, HTTPRequest.HTTPErrorType.APPException, e);
            }
        }


    }

    @Override
    public void OnPostHTTPRequest(Object key, Object contextObject) {
        if(progressBarLoading!=null&&bShowLoadingBar)
        {
            progressBarLoading.setVisibility(View.GONE);
        }
        if(viewStartLoading!=null)
        {
            viewStartLoading.setEnabled(true);
            viewStartLoading=null;
        }
        UIHandler.removeMessages(MESSAGE_HTTP_REQUEST_TIME_OUT);
    }

    @Override
    public void OnHandleError(Object key,Object contextObject, HTTPRequest.HTTPErrorType errorType, Exception e) {
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

    public void onKeycodeBack()
    {

    }

    //UI初始化
    protected  void UIInit(String title)
    {
        UIInit(title,false);
    }
    protected  void UIInit(String title,boolean showBackButton)
    {
        InitToolbar(title);
        InitButtonClickEvent(getWindow().getDecorView());

        progressBarLoading=findViewById(R.id.progressBarLoading);
        /*
        if(progressBarLoading.getVisibility()==View.GONE&&findViewById(R.id.progressBarLoadingCustom)!=null)
        {
            progressBarLoading=findViewById(R.id.progressBarLoadingCustom);
        }
*/
        if(showBackButton) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mMainToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();;
                }
            });
        }
    }

    public void SetLoadingBarVisible(boolean show)
    {
        bShowLoadingBar=show;
    }

    public void SetTitle(String title)
    {
        lblMainTitle = (TextView) findViewById(R.id.lblMainTitle);
        if(lblMainTitle!=null)
            lblMainTitle.setText(title);
    }

    private void InitToolbar(String title)
    {
        View view = findViewById(R.id.MainToolbar);

        if(view!=null) {
            mMainToolbar = (Toolbar) view;
            mMainToolbar.setTitle(title);
            setSupportActionBar(mMainToolbar);
            if(this instanceof View.OnClickListener)
                mMainToolbar.setNavigationOnClickListener((View.OnClickListener)this);
        }
    }
    private void InitButtonClickEvent(View mainView) {
        View.OnClickListener listener=null;
        if (!(this instanceof View.OnClickListener)) {
            return;
        }
        else
        {
            listener=(View.OnClickListener)this;
        }
        if (mainView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) mainView;
            if (viewGroup == null) {
                return;
            }
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);
                if (view instanceof Button) {
                    Button button = (Button) view;
                    button.setOnClickListener(listener);
                } else if (view instanceof ViewGroup) {
                    this.InitButtonClickEvent((ViewGroup) view);
                }
            }
        }
    }

    //实现后台数据操作



    public UserProfile GetUserProfile()
    {
        if(mUserProfile==null)
        {
            mUserProfile= UserProfile.Load(this);
        }
        return mUserProfile;
    }

    //UI控制，行为操作方法
    public TextView FindTextView(int id)
    {
        return (TextView)findViewById(id);
    }

    public EditText FindEditText(int id)
    {
        return (EditText)findViewById(id);
    }

    public void ShowLoadingBar()
    {
        if(progressBarLoading!=null)
        {
            progressBarLoading.setVisibility(View.VISIBLE);
        }
    }
    public void HideLoadingBar()
    {
        if(progressBarLoading!=null)
        {
            progressBarLoading.setVisibility(View.GONE);
        }
    }

    public void TransferActivity(Class<?> cls)
    {
        Intent intent;
        intent = new Intent(this,cls);

        startActivity(intent);
        //overridePendingTransition(R.anim.animzoomin, R.anim.animzoomout);
        //overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    public void TransferActivity(Intent intent)
    {


        startActivity(intent);
        //overridePendingTransition(R.anim.animzoomin, R.anim.animzoomout);
        //overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    public void TransferActivityCarryValue(Class<?> cls, StringParam... params)
    {
        Intent intent;
        intent = new Intent(this,cls);

        Bundle bundle = new Bundle();
        for(int i=0;i<params.length;i++) {
            bundle.putString(params[i].Name,params[i].Value);
        }
        intent.putExtras(bundle);   // 記得put進去，不然資料不會帶過去哦

        startActivity(intent);
    }

    public void TransferActivityForResult(Class<?> cls,int requestCode)
    {
        Intent intent;
        intent = new Intent(this,cls);
        startActivityForResult(intent, requestCode);
        //overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        //overridePendingTransition(R.anim.animzoomin, R.anim.animzoomout);
    }

    public void TransferActivityForResult(Intent intent,int requestCode)
    {
        startActivityForResult(intent, requestCode);
        //overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        //overridePendingTransition(R.anim.animzoomin, R.anim.animzoomout);

    }

    public void FinishByTimer()
    {
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
    }

    public void FinishByTimer(final int requestCode, final Intent intent)
    {
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK,intent);
                finish();
            }
        },2000);
    }

    public void SetLoadingBar(View view)
    {
        progressBarLoading=view;
    }

    //for Snackbar操作


    //数据操作方法
    public VolleyHTTPRequestQueue GetHTTPRequestQueue()
    {
        return mVolleyHTTPRequestQueue;
    }
    public boolean CheckNetwork()
    {
        if(!NetworkUtils.isOnline(getBaseContext()))
        {
            //this.ShowErrorMessage(getString(R.string.app_common_msg_alert_no_network));
            return false;
        }
        return true;
    }

    public void AddHTTPRequest(View startView,final Object key,HTTPParam... params)
    {
        AddHTTPRequest(startView, key,null, params);
    }

    public void AddHTTPRequest(final Object key,final Object contextObject,HTTPParam... params)
    {
        AddHTTPRequest(null, key,contextObject, params);
    }

    public void AddHTTPRequest(final Object key,HTTPParam... params)
    {
        AddHTTPRequest(null, key,null, params);
    }

    public void AddHTTPRequest(View startView,final Object key,final Object contextObject,HTTPParam... params)
    {
        String url=null;
        String requestMethod=key.toString();
        try {

            url= HTTPServer.PhoneUserAPIURL+"?Action="+requestMethod;
            for(int i=0;i<params.length;i++)
            {
                url+="&"+params[i].Name+"="+ URLEncoder.encode(params[i].Value.toString(),"utf-8");
            }


            Log.e(key.toString(),url);
            if(contextObject==null)
            {
                AddHTTPRequest(startView, key, url, url);
            }
            else {
                AddHTTPRequest(startView, key, contextObject, url);
            }

        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void AddHTTPRequest(View startView,final Object key,final Object contextObject,final String url)
    {
        AddHTTPRequest(startView,key,contextObject,url,this);
    }
    public void AddHTTPRequest(View startView,final Object key,final Object contextObject,final String url,final VolleyHTTPRequest.OnHTTPRequestListener httpListener)
    {
        if(viewStartLoading!=null)
        {
            viewStartLoading.setEnabled(true);
        }

        if(startView!=null) {
            viewStartLoading = startView;
        }

        mVolleyHTTPRequestQueue.AddRequest(key,contextObject,url,httpListener);
    }

    public void AddHTTPRequest(final Object key,final Object contextObject,final String url)
    {
        mVolleyHTTPRequestQueue.AddRequest(key,contextObject,url,this);
    }

    public void AddHTTPRequest(final Object key,final Object contextObject,final String url,final VolleyHTTPRequest.OnHTTPRequestListener httpListener)
    {
        mVolleyHTTPRequestQueue.AddRequest(key,contextObject,url,httpListener);
    }

    //提示窗口操作
    public void ShowLoadingDialog()
    {
        //dlgLoadingView.setMinimumHeight(200);
        //dlgLoadingView.setMinimumWidth(500);

        if(dlgLoading!=null)
        {
            EndLoadingDialog();
        }

        dlgLoading = new Dialog(this);
        dlgLoading.setCancelable(false);
        dlgLoading.show();
        dlgLoading.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dlgLoading.setContentView(R.layout.view_loading);

        UIHandler.postDelayed(new Runnable(){
            public void run() {
                EndLoadingDialog();
            }}, 30000);

    }

    public void SetLoadingDialogText(String message)
    {
        if(dlgLoading!=null)
        {
            TextView lblLoadingText=(TextView)dlgLoading.findViewById(R.id.lblLoadingText);
            lblLoadingText.setText(message);
        }
    }

    public void EndLoadingDialog()
    {
        if(dlgLoading!=null)
        {
            dlgLoading.dismiss();
            dlgLoading.cancel();
            dlgLoading=null;
        }

    }

    //定义外部类别
    public interface OnListDataListener
    {
        public void OnFirstPageRequested(int viewID);

        public void OnNextPageRequested(int viewID, Object holder);
        public void OnRefreshPageRequested(int viewID);
    }
}
