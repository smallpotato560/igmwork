package com.apps.igmwork.common.model;

import android.content.Context;
import android.text.TextUtils;

import com.apps.igmwork.framework.android.AndroidUtils;
import com.bcfbaselibrary.data.LocalDataAdapter;

/**
 * Created by Ben on 11/1/2017.
 */

public class UserProfile {

    public int UserID=0;
    public String NickName="";
    public String UserName="";
    public String MobileNum="";
    public String Email="";
    public String Email2="";
    public String Email3="";
    public String LineID="";
    public String SkypeID="";
    public String WechatID="";
    public String FacebookID="";
    public String InstagramID="";
    public String Address="";
    public String LocationAddress="";
    public int Gender=-1;
    public int LocationAddresResult = 0;

    public String Title="";
    public String LoadURL = "";
    public int Role=0;
    public String DeviceID="";
    public String LastMessageTime="";
    public int LastVersionCode=0;
    protected static UserProfile mUserProfile=null;

    private void UserProfile()
    {}


    public static UserProfile Load(Context context)
    {
        if(mUserProfile==null)
        {
            mUserProfile=new UserProfile();

            mUserProfile.DeviceID= LocalDataAdapter.GetString(context, "UserProfile.DeviceID");
            mUserProfile.Title = LocalDataAdapter.GetString(context,"UserProfile.Title");
            mUserProfile.LoadURL = LocalDataAdapter.GetString(context,"UserProfile.LoadURL");
            mUserProfile.LocationAddress = LocalDataAdapter.GetString(context,"UserProfile.LocationAddress");
            mUserProfile.LocationAddresResult = LocalDataAdapter.GetInt(context,"UserProfile.LocationAddresResult", 0);

            if(!TextUtils.isEmpty(mUserProfile.DeviceID))
            {
                mUserProfile.UserID=LocalDataAdapter.GetInt(context, "UserProfile.UserID", 0);

                mUserProfile.NickName=LocalDataAdapter.GetString(context, "UserProfile.NickName");
                mUserProfile.UserName=LocalDataAdapter.GetString(context, "UserProfile.UserName");
                mUserProfile.MobileNum=LocalDataAdapter.GetString(context, "UserProfile.MobileNum");
                mUserProfile.Email=LocalDataAdapter.GetString(context, "UserProfile.Email");
                mUserProfile.Gender=LocalDataAdapter.GetInt(context, "UserProfile.Gender", -1);
                mUserProfile.LastMessageTime=LocalDataAdapter.GetString(context, "UserProfile.LastMessageTime");
                mUserProfile.Role=LocalDataAdapter.GetInt(context, "UserProfile.Role", 0);

                if(mUserProfile.LastVersionCode==0)
                {
                    mUserProfile.LastVersionCode= AndroidUtils.GetAppVersionCode(context);
                }

            }
        }
        return mUserProfile;
    }

    public boolean IsLogin()
    {
        return mUserProfile.UserID>0;
    }

    public void Save(Context context)
    {
        UserProfile.CommitChanges(context, this);
    }




    protected static void CommitChanges(Context context, UserProfile objUserProfile)
    {
        LocalDataAdapter.SetParameterToBuffer("UserProfile.UserID", objUserProfile.UserID);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.DeviceID", objUserProfile.DeviceID);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.UserName", objUserProfile.UserName);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.NickName", objUserProfile.NickName);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.MobileNum", objUserProfile.MobileNum);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.Email", objUserProfile.Email);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.Email2", objUserProfile.Email2);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.Email3", objUserProfile.Email3);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.LineID", objUserProfile.LineID);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.SkypeID", objUserProfile.SkypeID);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.WechatID", objUserProfile.WechatID);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.FacebookID", objUserProfile.FacebookID);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.InstagramID", objUserProfile.InstagramID);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.Address", objUserProfile.Address);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.Gender", objUserProfile.Gender);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.LastMessageTime", objUserProfile.LastMessageTime);

        LocalDataAdapter.SetParameterToBuffer("UserProfile.LastVersionCode", objUserProfile.LastVersionCode);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.Role", objUserProfile.Role);

        LocalDataAdapter.SetParameterToBuffer("UserProfile.Title", objUserProfile.Title);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.LoadURL", objUserProfile.LoadURL);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.LocationAddress", objUserProfile.LocationAddress);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.LocationAddresResult", objUserProfile.LocationAddresResult);

        LocalDataAdapter.CommitChanges(context);

        mUserProfile=objUserProfile;
    }
}
