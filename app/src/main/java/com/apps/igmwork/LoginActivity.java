package com.apps.igmwork;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.apps.igmwork.common.APPGlobal;
import com.apps.igmwork.framework.android.AndroidUtils;
import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.bcfbaselibrary.io.PhoneService;
import com.bcfbaselibrary.string.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    //静态成员

    //控件成员
    private AutoCompleteTextView txtAccount;
    private EditText txtPassword;

    //数据成员



    //对象成员



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        UIInit("會員登入");

        txtAccount = (AutoCompleteTextView) findViewById(R.id.txtAccount);
        txtAccount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    txtPassword.requestFocus();
                    return true;
                }
                return  false;
            }
        });

        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == R.id.btnLogin || id == EditorInfo.IME_NULL||id==EditorInfo.IME_ACTION_DONE) {
                    onClick(findViewById(R.id.btnLogin));
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnLogin:
            {
                if(txtAccount.getText().toString().trim().length()==0)
                {
                    txtAccount.setError("請輸入會員帳號.");
                    txtAccount.requestFocus();
                    return;
                }

                if(txtPassword.getText().toString().trim().length()==0)
                {
                    txtPassword.setError("請輸入登入密碼.");
                    txtPassword.requestFocus();
                    return;
                }

                AndroidUtils.HideInput(this,txtPassword);

                AddHTTPRequest(view,"Login",
                        new HTTPParam("Account",txtAccount.getText().toString()),
                        new HTTPParam("Password",txtPassword.getText().toString())
                );
                break;
            }
            case R.id.btnForgetPwd:
            case R.id.lblForgetPwd:{
                //this.TransferActivity(ResetPasswordActivity.class);
                break;
            }
        }
    }


    //实现界面事件
    //实现后台数据事件
    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {
        //super.OnReceiveHTTPResp(key, contextValue, jsonServerResponse);

        if (key.equals("Login")) {
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

                        mUserProfile.DeviceID= objPhoneService.GetDeviceTaken();
                        mUserProfile.UserID=cvUserProfile.getAsInteger("UserID");
                        mUserProfile.NickName=cvUserProfile.getAsString("NickName");
                        mUserProfile.UserName=cvUserProfile.getAsString("UserName");

                        mUserProfile.Save(this);
                    }

                }

                //TransferActivity(MainActivity.class);

                Intent intent=new Intent();
                intent.setClass(LoginActivity.this,MainActivity.class);
                startActivity(intent);finish();
                //APPGlobal.CloseActivity(this);
            }
            else
            {
                FindEditText(R.id.txtPassword).setError("你輸入的登入密碼不正確，請重新輸入。");
                FindEditText(R.id.txtPassword).requestFocus();
            }
        }
    }
    //实现界面操作
    //实现后台数据操作
    //定义外部类别

}
