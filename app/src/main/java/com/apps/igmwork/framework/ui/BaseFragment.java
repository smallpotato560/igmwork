package com.apps.igmwork.framework.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.data.StringParam;
import com.bcfbaselibrary.net.HTTPRequest;
import com.bcfbaselibrary.net.volley.VolleyHTTPRequest;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by Ben on 2017/8/10.
 */

public class BaseFragment extends Fragment implements VolleyHTTPRequest.OnHTTPRequestListener{

    protected BaseActivity mBaseActivity;
    protected UserProfile mUserProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBaseActivity=(BaseActivity)getActivity();
        mUserProfile= UserProfile.Load(mBaseActivity);

    }


    @Override
    public void OnPreHTTPRequest(Object key, Object contextObject) {
        mBaseActivity.OnPreHTTPRequest(key,contextObject);
    }

    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {

    }

    @Override
    public void OnPostHTTPRequest(Object key, Object contextObject) {
        mBaseActivity.OnPostHTTPRequest(key,contextObject);
    }

    public void AddHTTPRequest(View startView,final Object key,HTTPParam... params)
    {
        AddHTTPRequest(startView, key,null, params);
    }

    @Override
    public void OnHandleError(Object key, Object contextObject, HTTPRequest.HTTPErrorType errorType, Exception e) {
        mBaseActivity.OnHandleError(key,contextObject,errorType,e);
    }

    public void AddHTTPRequest(final Object key,final Object contextObject,HTTPParam... params)
    {
        AddHTTPRequest(null,key,contextObject,params);
    }

    public void AddHTTPRequest(View startView,final Object key,final Object contextObject,HTTPParam... params)
    {
        String url=null;
        String requestMethod=key.toString();
        try {

            url= HTTPServer.PhoneUserAPIURL+"?Method="+requestMethod;
            for(int i=0;i<params.length;i++)
            {
                url+="&"+params[i].Name+"="+ URLEncoder.encode(params[i].Value.toString(),"utf-8");
            }

            Log.e(key.toString(),url);
            mBaseActivity.AddHTTPRequest(startView,key,contextObject,url,this);

        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public String MergeString (String url, StringParam... params) {
        try {
            for(int i=0; i<params.length; i++) {
                if(url.indexOf("?") > 0) {
                    url += "&";
                }
                else {
                    url += "?";
                }

                url += params[i].Name + "=" + URLEncoder.encode(params[i].Value.toString(),"utf-8");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    }
}
