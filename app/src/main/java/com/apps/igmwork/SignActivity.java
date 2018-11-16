package com.apps.igmwork;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.android.AndroidUtils;
import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.data.StringParam;
import com.bcfbaselibrary.internal.Logger;
import com.bcfbaselibrary.io.PhoneService;
import com.bcfbaselibrary.string.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;

import butterknife.BindView;

import static com.apps.igmwork.framework.ui.widget.AlertDialogUtils.ShowMessageDialog;

public class SignActivity extends BaseActivity implements View.OnClickListener {

    //静态成员
    private int gender;

    //控件成员
    private AutoCompleteTextView txtAccount;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private EditText txtNickName;
    private EditText txtEmail;
    @BindView(R.id.sex_rg)
    protected RadioGroup sex_rg;
    @BindView(R.id.male_rb)
    protected RadioButton male_rb;
    @BindView(R.id.famale_rb)
    protected RadioButton famale_rb;

    //数据成员



    //对象成员



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        UIInit("會員註冊");

        txtAccount = (AutoCompleteTextView) findViewById(R.id.txtAccount);
        txtAccount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    txtNickName.requestFocus();
                    return true;
                }
                return  false;
            }
        });
        /*
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    txtConfirmPassword.requestFocus();
                    return true;
                }
                return  false;
            }
        });

        txtConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);
        txtConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    txtNickName.requestFocus();
                    return true;
                }
                return  false;
            }
        });
*/
        txtNickName = (EditText) findViewById(R.id.txtNickName);
        txtNickName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    txtEmail.requestFocus();
                    return true;
                }
                return  false;
            }
        });

        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == R.id.btnLogin || id == EditorInfo.IME_NULL||id==EditorInfo.IME_ACTION_DONE) {
                    onClick(findViewById(R.id.btnSign));
                    return true;
                }
                return false;
            }
        });


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
            case R.id.btnSign: {
                if (txtAccount.getText().toString().trim().length() == 0) {
                    txtAccount.setError("請輸入會員帳號.");
                    txtAccount.requestFocus();
                    return;
                }
                if (!isMobileNo(txtAccount.getText().toString().trim())) {
                    txtAccount.setError("請輸入正確格式電話號碼.ex:0912345678");
                    txtAccount.requestFocus();
                    return;
                }
                /*
                if(txtPassword.getText().toString().trim().length()==0)
                {
                    txtPassword.setError("請輸入登入密碼.");
                    txtPassword.requestFocus();
                    return;
                }

                if(txtConfirmPassword.getText().toString().trim().length()==0)
                {
                    txtConfirmPassword.setError("請輸入確認密碼.");
                    txtConfirmPassword.requestFocus();
                    return;
                }

                if(txtConfirmPassword.getText() != txtPassword.getText())
                {

                    txtPassword.setError("密碼與確認密碼不同，請重新輸入.");
                    txtPassword.requestFocus();
                    return;
                }
                */
                if(txtNickName.getText().toString().trim().length()==0)
                {
                    txtNickName.setError("請輸入名字.");
                    txtNickName.requestFocus();
                    return;
                }
                if(txtEmail.getText().toString().trim().length()==0)
                {
                    txtEmail.setError("請輸入信箱.");
                    txtEmail.requestFocus();
                    return;
                }

                if(!isEmail(txtEmail.getText().toString().trim()))
                {
                    txtEmail.setError("請輸入正確格式Email.");
                    txtEmail.requestFocus();
                    return;
                }
                for (int i = 0; i < sex_rg.getChildCount(); i++) {
                    RadioButton rd = (RadioButton) sex_rg.getChildAt(i);
                    if (rd.isChecked()) {
                        switch ((rd.getId()))
                        {
                            case R.id.male_rb: {
                                gender = 1;
                                break;
                            }
                            case R.id.famale_rb: {
                                gender = 0;
                                break;
                            }
                        }
                        break;
                    }
                }
                //AndroidUtils.HideInput(this,txtPassword);
                //AndroidUtils.HideInput(this,txtConfirmPassword);
                AndroidUtils.HideInput(this,txtEmail);


                String url = HTTPServer.BuildURL(this, HTTPServer.AddCustomerInfoURL,
                        new HTTPParam("account",txtAccount.getText().toString()),
                        new HTTPParam("name",txtNickName.getText().toString()),
                        new HTTPParam("email",txtEmail.getText().toString()),
                        new HTTPParam("gender",gender));


                AddHTTPRequest("SignCustomer", null, url);
                break;
            }
            case R.id.btnLogin:
                TransferActivity(VerCodeActivity.class);
                break;
            case R.id.btnChecked:
                if (txtAccount.getText().toString().trim().length() == 0) {
                    txtAccount.setError("請輸入會員帳號.");
                    txtAccount.requestFocus();
                    return;
                }
                String url = HTTPServer.BuildURL(this, HTTPServer.CheckedAccountURL,
                        new HTTPParam("account",txtAccount.getText().toString()));


                AddHTTPRequest("CheckedAccount", null, url);
                break;
        }
    }

    //实现后台数据事件
    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {
        //super.OnReceiveHTTPResp(key, contextValue, jsonServerResponse);

        if (key.equals("SignCustomer")) {
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
                        mUserProfile.UserName=cvUserProfile.getAsString("UserName");
                        mUserProfile.MobileNum=cvUserProfile.getAsString("MobileNum");
                        mUserProfile.Gender=cvUserProfile.getAsInteger("Gender");

                        mUserProfile.Save(this);
                    }

                }
                ShowMessageDialog(this,"加入會員成功。請繼續驗證電話號碼。");
                TransferActivityCarryValue(VerCodeActivity.class,
                        new StringParam("mobile",txtAccount.getText().toString().trim()));
            }
            else if(jsonServerResponse.optInt("CheckedAccount") == 1) {
                FindEditText(R.id.txtAccount).setError("你輸入的電話號碼已註冊，請重新輸入或登入。");
                FindEditText(R.id.txtAccount).requestFocus();
            }
            else {
                ShowMessageDialog(this,"加入會員出錯。請聯繫管理員。");
            }
        }
        if(key.equals("CheckedAccount")) {
            if (jsonServerResponse.optInt("StatusCode") == 1) {
                ShowMessageDialog(this,"此電話號碼已經註冊過了。");
            }
            else {
                ShowMessageDialog(this,"此電話號碼可以使用。");
            }
            txtNickName.requestFocus();
        }
    }

    //使用正则表達式判斷電話號碼
    public static boolean isMobileNo(String tel) {
        Pattern p = Pattern.compile("^09\\d{8}");
        Matcher m = p.matcher(tel);
        Logger.E(TAG,"MobileNo:"+m.matches());
        return m.matches();
    }

    //使用正则表達式判斷Email
    public static boolean isEmail(String email) {
        Pattern p = Pattern.compile("([\\w!#$%&'+/=?^_{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_{|}~-]+)|[\\w!#$%&'+/=?^_{|}~-])+@(?:\\w?.)+\\w?");
        Matcher m = p.matcher(email);
        Logger.E(TAG,"Email:"+m.matches());
        return m.matches();
    }
}
