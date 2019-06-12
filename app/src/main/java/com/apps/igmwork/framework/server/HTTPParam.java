package com.apps.igmwork.framework.server;

/**
 * Created by Ben on 2017/7/26.
 */

public class HTTPParam {
    public String Name="";
    public Object Value=null;
    public HTTPParam()
    {}
    public HTTPParam(final String name,final Object value)
    {
        Name=name;
        Value=value;
    }

}
