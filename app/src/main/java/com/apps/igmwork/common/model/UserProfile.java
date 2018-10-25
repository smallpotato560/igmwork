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

            if(!TextUtils.isEmpty(mUserProfile.DeviceID))
            {
                mUserProfile.UserID=LocalDataAdapter.GetInt(context, "UserProfile.UserID", 0);

                mUserProfile.NickName=LocalDataAdapter.GetString(context, "UserProfile.NickName");
                mUserProfile.UserName=LocalDataAdapter.GetString(context, "UserProfile.UserName");
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
        LocalDataAdapter.SetParameterToBuffer("UserProfile.LastMessageTime", objUserProfile.LastMessageTime);

        LocalDataAdapter.SetParameterToBuffer("UserProfile.LastVersionCode", objUserProfile.LastVersionCode);
        LocalDataAdapter.SetParameterToBuffer("UserProfile.Role", objUserProfile.Role);

        LocalDataAdapter.CommitChanges(context);

        mUserProfile=objUserProfile;
    }
}
