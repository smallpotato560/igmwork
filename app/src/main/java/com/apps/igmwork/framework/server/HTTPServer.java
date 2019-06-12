package com.apps.igmwork.framework.server;


import android.content.Context;

import java.net.URLEncoder;

public class HTTPServer {
    public final static int GCMRegIDUpdate = 904;


    //for server setting
    final public static String ServerAddress="http://97.74.238.39/WorkTime/";
    final public static String PhoneUserAPIURL=ServerAddress+"Login.aspx";
    final public static String ImageResourceImageHomeURL=ServerAddress+"/Resource/Photos/";
    final public static String PhotoHomeURL=ServerAddress+"/Resource/Photos/";

    final public static String NewVerionAPPURL=ServerAddress+"/APK/EChat_v";


    final public static String AddCustomerInfoURL=ServerAddress + "/AddCustomerInfo.aspx";
    final public static String PuchVerCodeURL=ServerAddress +"PushVerCode.aspx";
    final public static String CheckedVerCodeURL=ServerAddress +"CheckedVerCode.aspx";
    final public static String CheckedAccountURL=ServerAddress +"CheckedAccount.aspx";
    final public static String GetUserDataListURL=ServerAddress +"GetUserDataList.aspx";
    final public static String GetUserDataDetailURL=ServerAddress +"GetUserDataDetail.aspx";
    final public static String PrivacyURL=ServerAddress +"Privacy.aspx";
    final public static String ActivityURL=ServerAddress +"Activity.aspx";




    public static String BuildServerURL(Context context,String key,HTTPParam... params)
    {
        String url=null;
        String requestMethod=key.toString();
        try {


            url= HTTPServer.PhoneUserAPIURL+"?Method="+requestMethod;
            for(int i=0;i<params.length;i++)
            {
                url+="&"+params[i].Name+"="+ URLEncoder.encode(params[i].Value.toString(),"utf-8");
            }



        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return url;
    }

    public static String BuildURL(Context context,String urlHost,HTTPParam... params)
    {
        String url=null;
        try {


            url= urlHost+"?From=QKeeAPP";
            for(int i=0;i<params.length;i++)
            {
                url+="&"+params[i].Name+"="+ URLEncoder.encode(params[i].Value.toString(),"utf-8");
            }


        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return url;
    }
}
