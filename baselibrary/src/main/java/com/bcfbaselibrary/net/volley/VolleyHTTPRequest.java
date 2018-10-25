package com.bcfbaselibrary.net.volley;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bcfbaselibrary.net.HTTPRequest;

import org.json.JSONObject;

/**
 * Created by Ben on 2017/7/26.
 */

public class VolleyHTTPRequest extends JsonObjectRequest {

    public VolleyHTTPRequest(final Object key,final Object contextValue,final String url,final OnHTTPRequestListener httpListener) {

        super(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                httpListener.OnReceiveHTTPResp(key,contextValue,response);
                httpListener.OnPostHTTPRequest(key,contextValue);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(httpListener!=null)
                {
                    try {
                        httpListener.OnHandleError(key, contextValue, HTTPRequest.HTTPErrorType.PageCannotRead, error);
                        httpListener.OnPostHTTPRequest(key, contextValue);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });

        setShouldCache(true);

        setRetryPolicy(new DefaultRetryPolicy(VolleyHTTPRequestQueue.CONNECTION_TIME_OUT, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void deliverError(VolleyError error) {
        if(error instanceof NoConnectionError)
        {
            Cache.Entry entry = this.getCacheEntry();
            if (entry != null)
            {
                Response<JSONObject> response = parseNetworkResponse(new NetworkResponse(entry.data, entry.responseHeaders));
                deliverResponse(response.result);
                return ;
            }
        }
        super.deliverError(error);
    }

    public interface OnHTTPRequestListener {
        public void OnPreHTTPRequest(Object key,Object contextObject);
        public void OnReceiveHTTPResp(Object key,Object contextObject,JSONObject jsonServerResponse);
        public void OnPostHTTPRequest(Object key,Object contextObject);
        public void OnHandleError(Object key,Object contextObject, HTTPRequest.HTTPErrorType errorType, Exception e);
    }

}
