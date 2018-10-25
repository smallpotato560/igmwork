package com.apps.igmwork.framework.map;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ben on 2018/8/17.
 */


public class EvilTransform
{
    private static final boolean Use_Chinese_GCJ = true;

    private static final double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

    private static final double pi = 3.14159265358979324;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;

    public static boolean OutOfChina(double lat, double lng)
    {
        if(!Use_Chinese_GCJ)
            return true;

        if ((lng < 72.004) || (lng > 137.8347))
        {
            return true;
        }
        if ((lat < 0.8293) || (lat > 55.8271))
        {
            return true;
        }
        return false;
    }

    public static double TransformLat(double x, double y)
    {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double TransformLon(double x, double y)
    {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    protected static LatLng Delta(double lat, double lng)
    {
        double a = 6378137.0;
        double ee = 0.00669342162296594323;
        double dLat = TransformLat(lng - 105.0, lat - 35.0);
        double dLng = TransformLon(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);

        LatLng ret = new LatLng(dLat,dLng);
        return ret;
    }

    public static LatLng WGS2GCJ(double wgsLat, double wgsLng)
    {
        if (OutOfChina(wgsLat, wgsLng))
        {
            return new LatLng(wgsLat, wgsLng);
        }
        LatLng d = Delta(wgsLat, wgsLng);
        return new LatLng(wgsLat + d.latitude, wgsLng + d.longitude);
    }

    public static LatLng GCJ2WGS(double gcjLat, double gcjLng)
    {
        if (OutOfChina(gcjLat, gcjLng))
        {
            return new LatLng(gcjLat, gcjLng);
        }
        LatLng d = Delta(gcjLat, gcjLng);
        return new LatLng(gcjLat - d.latitude, gcjLng - d.longitude);
    }

    public static LatLng Wgs_gcj_encrypts(double wgLat, double wgLon) {

        if (OutOfChina(wgLat, wgLon)) {
            return new LatLng(wgLat, wgLon);
        }
        double dLat = TransformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = TransformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double lat = wgLat + dLat;
        double lon = wgLon + dLon;
        return new LatLng(lat,dLon);
    }

    public static LatLng GCJ2WGSExact(double gcjLat, double gcjLng)
    {
        double initDelta = 0.01;
        double threshold = 0.000001;
        double dLat = initDelta;
        double dLng = initDelta;
        double mLat = gcjLat - dLat;
        double mLng = gcjLng - dLng;
        double pLat = gcjLat + dLat;
        double pLng = gcjLng + dLng;
        double wgsLat = 0;
        double wgsLng = 0;

        for (int i = 0; i < 30; i++)
        {
            wgsLat = (mLat + pLat) / 2;
            wgsLng = (mLng + pLng) / 2;
            LatLng tmp = WGS2GCJ(wgsLat, wgsLng);
            dLat = tmp.latitude - gcjLat;
            dLng = tmp.longitude - gcjLng;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLng) < threshold))
            {
                return new LatLng(wgsLat, wgsLng);
            }
            if (dLat > 0)
            {
                pLat = wgsLat;
            }
            else
            {
                mLat = wgsLat;
            }
            if (dLng > 0)
            {
                pLng = wgsLng;
            }
            else
            {
                mLng = wgsLng;
            }
        }
        return new LatLng(wgsLat, wgsLng);
    }

    public static double Distance(double latA, double lngA, double latB, double lngB)
    {
        double earthR = 6371000;
        double x = Math.cos(latA * Math.PI / 180) * Math.cos(latB * Math.PI / 180) * Math.cos((lngA - lngB) * Math.PI / 180);
        double y = Math.sin(latA * Math.PI / 180) * Math.sin(latB * Math.PI / 180);
        double s = x + y;
        if (s > 1)
            s = 1;
        if (s < -1)
            s = -1;
        double alpha = Math.acos(s);
        double distance = alpha * earthR;
        return distance;
    }

}