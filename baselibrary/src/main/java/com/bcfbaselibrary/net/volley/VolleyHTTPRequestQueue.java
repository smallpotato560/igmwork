package com.bcfbaselibrary.net.volley;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bcfbaselibrary.io.PhoneService;

import java.net.URLEncoder;

/**
 * Created by Ben on 2017/7/26.
 */

public class VolleyHTTPRequestQueue {

    public final static int CONNECTION_TIME_OUT=30*1000;
    public String LastRequestUrl="";
    private static VolleyHTTPRequestQueue mInstance;
    private RequestQueue mRequestQueue;

    private static Context mCtx;

    private VolleyHTTPRequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyHTTPRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyHTTPRequestQueue(context);
        }
        return mInstance;
    }

    public Context GetBaseContext()
    {
        return mCtx;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());

        }
        return mRequestQueue;
    }

    public <T> void AddRequest(final Object key,final Object contextObject,final String url,final VolleyHTTPRequest.OnHTTPRequestListener httpListener) {
        LastRequestUrl=url;
        VolleyHTTPRequest objVolleyHTTPRequest=new VolleyHTTPRequest(key,contextObject,url,httpListener);
        httpListener.OnPreHTTPRequest(key,contextObject);
        getRequestQueue().add(objVolleyHTTPRequest);
    }


    protected <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
