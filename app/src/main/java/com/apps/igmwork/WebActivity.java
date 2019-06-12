package com.apps.igmwork;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.widget.AlertDialogUtils;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.bcfbaselibrary.internal.Logger;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONObject;

import butterknife.BindView;

public class WebActivity extends BaseActivity implements View.OnClickListener {

    //控件成员
    private DrawerLayout mDrawerLayout;
    private BottomNavigationBar btnNavigationBar;
    @BindView(R.id.wWebBrowser)
    protected WebView wWebBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_webview);
        UIInit(mUserProfile.Title,true);
        //findViewById(R.id.layoutBack).setOnClickListener(this);
        if(mUserProfile.LoadURL != "") {
            LoadPage(mUserProfile.LoadURL);
        }
        else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){

            if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
                finish();
            }
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    //实现界面事件
    @Override
    public void onClick(View view) {
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        wWebBrowser.destroy();
        mUserProfile.Title="";
        mUserProfile.LoadURL="";
        mUserProfile.Save(this);
    }

    //实现后台数据事件
    @Override
    public void OnReceiveHTTPResp(Object key, Object contextValue, JSONObject jsonServerResponse) {
        super.OnReceiveHTTPResp(key, contextValue, jsonServerResponse);

        Logger.E("WebPay jsonServerResponse",""+jsonServerResponse);
        if (key.equals("GetPayUrl")) {
            if (jsonServerResponse.optInt("StatusCode") == 1) {
                String payUrl=jsonServerResponse.optString("payUrl");

                Logger.E("WebPay payUrl",""+payUrl);
                LoadPage(payUrl);
            }
            else
            {
                AlertDialogUtils.ShowMessageDialog(this, "訪問支付接口失敗，請稍后重試！附加信息:" + jsonServerResponse.optString("comment")
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
            }
        }
    }

    //实现后台数据操作
    public void LoadPage(String Url)
    {
        wWebBrowser.getSettings().setJavaScriptEnabled(true);
        wWebBrowser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //在js中调用本地java方法
        wWebBrowser.addJavascriptInterface(new WebActivity.JsInterface(this), "AndroidWebView");

        //mContentView.findViewById(R.id.pbWebPageLoadingBar).setVisibility(View.GONE);
        wWebBrowser.setWebViewClient(new WebViewClient()
        {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                ShowLoadingBar();
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);

                HideLoadingBar();

                //view.loadUrl("javascript: var allImages = document.getElementsByTagName('img'); if (allImages) {var i;for (i=0; i<allImages.length; i++) {var image = allImages[i]; imagelink.setAttribute('width','100%');}}");

                //view.loadUrl("javascript: var allLinks = document.getElementsByTagName('a'); if (allLinks) {var i;for (i=0; i<allLinks.length; i++) {var link = allLinks[i];var target = link.getAttribute('target'); if (target && target == '_blank') {link.setAttribute('target','_self');link.href = 'newtab:'+link.href;}}}");
            }
        });


        wWebBrowser.setWebChromeClient(new WebChromeClient()
                                        {
                                            @Override
                                            public boolean onJsAlert(WebView view, String url, final String message, JsResult result) {
                                                String showMessage=message.replace("[CloseWindow]","").replace("[MemberUpgrade]","");
                                                result.cancel();
                                                return true;
                                            }
                                        }
        );
        //Logger.E("wvWebBrowser load","URL:"+Url);
        wWebBrowser.loadUrl(Url);
        //wvWebBrowser.loadUrl("http://www.sina.com.cn");
        //wvWebPage.addJavascriptInterface(this, "Android");
        //wvWebPage.addJavascriptInterface(objBaseFragmentActivity, "Activity");

    }

    private class JsInterface {
        private Context mContext;

        public JsInterface(Context context) {
            this.mContext = context;
        }

        //在js中调用window.AndroidWebView.showInfoFromJs(name)，便会触发此方法。
        @JavascriptInterface
        public void showInfoFromJs(String msg) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            TransferActivity(MainFirstActivity.class);
        }

        @JavascriptInterface
        public void showInfoFromJs(String url, int mid, int vid) {
            url += "?mid="+mid+"&vid="+vid+"&name="+mUserProfile.NickName+"&mobileNo="+mUserProfile.MobileNum+"&email="+mUserProfile.Email+
                    "&gender="+mUserProfile.Gender;

            //LoadPage(url);
        }

        //加入會員成功
        @JavascriptInterface
        public void showInfoFromJs(String msg, int id) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }
    public void sendInfoToJs(View view) {
        String msg = ""; //((EditText) findViewById(R.id.input_et)).getText().toString();
        //调用js中的函数：showInfoFromJava(msg)
        //wvWebBrowser.loadUrl("javascript:showInfoFromJava('" + msg + "')");
    }
}
