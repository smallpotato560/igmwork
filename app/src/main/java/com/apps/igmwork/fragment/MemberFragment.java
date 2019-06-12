package com.apps.igmwork.fragment;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.igmwork.MainActivity;
import com.apps.igmwork.MyCollectionActivity;
import com.apps.igmwork.R;
import com.apps.igmwork.SignActivity;
import com.apps.igmwork.WebActivity;
import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.BaseFragment;
import com.apps.igmwork.framework.ui.widget.AlertDialogUtils;
import com.bcfbaselibrary.io.PhoneService;
import com.bcfbaselibrary.string.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MemberFragment extends BaseFragment implements View.OnClickListener{

    //静态成员

    //控件成员

    protected View mContentView;

    @BindView(R.id.lblNickname)
    protected TextView lblNickname;
    @BindView(R.id.lblEmail)
    protected TextView lblEmail;
    @BindView(R.id.lblGender)
    protected TextView lblGender;
    @BindView(R.id.lblMobileNo)
    protected TextView lblMobileNo;

    protected Unbinder mUnbinder;
    //数据成员



    //对象成员


    public static MemberFragment newInstance() {
        MemberFragment fragment = new MemberFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    //实现界面事件
    @Override
    public void onResume()
    {
        super.onResume();

        if(mUserProfile.IsLogin())
        {



            mContentView.findViewById(R.id.layoutMemberTitle).setVisibility(View.VISIBLE);
            lblNickname.setText(mUserProfile.NickName);
            lblEmail.setText(mUserProfile.Email);
            lblMobileNo.setText(mUserProfile.MobileNum);
            lblGender.setText((mUserProfile.Gender==1? "男":"女"));

        }
        else
        {
            mBaseActivity.TransferActivityNoBack(SignActivity.class);
        }
        //Toast.makeText(mBaseActivity,"onResume",Toast.LENGTH_LONG).show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView= inflater.inflate(R.layout.fragment_member, container, false);

        mUnbinder= ButterKnife.bind(this,mContentView);
        mContentView.findViewById(R.id.layoutPrivacy).setOnClickListener(this);
        mContentView.findViewById(R.id.layoutRefresh).setOnClickListener(this);
        mContentView.findViewById(R.id.layoutCollection).setOnClickListener(this);
        mContentView.findViewById(R.id.layoutLogout).setOnClickListener(this);

        onResume();

        return mContentView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
    @Override
    public void onClick(View view) {
        //Toast.makeText(mBaseActivity,"onResume",Toast.LENGTH_LONG).show();

        if(mUserProfile.IsLogin()) {
            if(view.getId()==R.id.layoutRefresh) {
                if (mUserProfile.IsLogin()) {
                    AddHTTPRequest(view,"GetCustomerInfo", new HTTPParam("uid", mUserProfile.UserID));
                }
            }
            else if(view.getId()==R.id.layoutPrivacy)
            {
                mUserProfile.Title="服務條款";
                mUserProfile.LoadURL= HTTPServer.PrivacyURL;
                mUserProfile.Save(mBaseActivity);
                mBaseActivity.TransferActivity(WebActivity.class);
            }
            else if(view.getId()==R.id.layoutCollection)
            {
                mBaseActivity.TransferActivity(MyCollectionActivity.class);
            }
            else if (view.getId() == R.id.layoutLogout) {

                AlertDialogUtils.ShowMessageDialog(mBaseActivity, null, "您確認要退出會員狀態嗎？", true,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mUserProfile.UserID = 0;
                                mUserProfile.Save(mBaseActivity);
                                mBaseActivity.TransferActivityNoBack(SignActivity.class);
                            }
                        });

            }
        }
    }


    //实现后台数据事件
    @Override
    public void OnReceiveHTTPResp(Object key, Object contextValue, JSONObject jsonServerResponse) {
        super.OnReceiveHTTPResp(key, contextValue, jsonServerResponse);

        if (key.equals("GetCustomerInfo")) {
            if (jsonServerResponse.optInt("StatusCode") == 1) {
                mUserProfile = UserProfile.Load(mBaseActivity);
                JSONArray dataList = jsonServerResponse.optJSONArray("DataSource");

                if (dataList != null) {

                    ArrayList<ContentValues> objAccountData = JsonHelper.ConvertToContentList(dataList);

                    if (objAccountData.size() > 0) {
                        PhoneService objPhoneService = new PhoneService(mBaseActivity);
                        ContentValues cvUserProfile = objAccountData.get(0);

                        mUserProfile.DeviceID = objPhoneService.GetDeviceTaken();
                        mUserProfile.UserID = cvUserProfile.getAsInteger("UserID");
                        mUserProfile.UserName = cvUserProfile.getAsString("UserName");
                        mUserProfile.NickName = cvUserProfile.getAsString("NickName");
                        mUserProfile.Email = cvUserProfile.getAsString("Email");
                        mUserProfile.MobileNum = cvUserProfile.getAsString("MobileNum");
                        mUserProfile.Gender = cvUserProfile.getAsInteger("Gender");

                        mUserProfile.Save(mBaseActivity);

                        onResume();
                    }
                }
            }
        }
    }
}
