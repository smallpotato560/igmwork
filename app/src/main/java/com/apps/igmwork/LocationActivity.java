package com.apps.igmwork;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.apps.igmwork.framework.map.SphericalUtil;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.bcfbaselibrary.internal.Logger;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class LocationActivity extends BaseActivity implements LocationListener {

    //静态成员
    private static final int DEFAULT_ZOOM = 16;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //数据成员
    private int mPosition = 0;
    private ArrayList<String> mTitleList;
    private boolean mLocationPermissionGranted;
    private boolean mLocationGranted;
    //private final LatLng mDefaultLocation = new LatLng(24.2131498, 120.7065008);      //家裡
    //private LatLng mDefaultLocation = new LatLng(24.1817706, 120.6148122);              //公司
    private LatLng mDefaultLocation = null;
    private double Distance;

    //控件成员
    private DrawerLayout mDrawerLayout;
    private BottomNavigationBar btnNavigationBar;
    @BindView(R.id.wvWebBrowser)
    protected WebView wvWebBrowser;
    @BindView(R.id.btn_scan)
    protected Button btn_scan;
    @BindView(R.id.btn_reset)
    protected Button btn_reset;
    //@BindView(R.id.lblGameLog)
    //protected TextView lblGameLog;

    //对象成员
    private ArrayList<Fragment> mFragments;



    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    protected Location mLastKnownLocation;
    protected Location mStartLocation;
    protected LocationManager mLocationManager;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //地图成员
    protected Circle mCheckInCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_scan.setVisibility(View.INVISIBLE);
        btn_reset.setVisibility(View.INVISIBLE);

        //webView.loadUrl("http://97.74.238.39/WorkTime/PunchTime.aspx?UserID="+mUserProfile.UserID);
        //GetFromLocation("台中市潭子區潭陽路59巷40號");
        if(mUserProfile.UserID == 0)
        {
            TransferActivity(SignActivity.class);
        }
        else{
            if(mUserProfile.LocationAddress == "") {
                TransferActivity(WebActivity.class);
            }
            else {
                GetFromLocation(mUserProfile.LocationAddress);
            }
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            CheckLocationPermission();
            InitLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        mLastKnownLocation.set(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    //实现后台数据事件

    private void CheckLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void GetDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.

                            mLastKnownLocation = task.getResult();
                            //如果並未開啟過map定位，提示用戶開啟
                            if(mLastKnownLocation != null) {
                                mStartLocation = new Location(mLastKnownLocation);
                                if (mLastKnownLocation != null) {
                                    if (mDefaultLocation != null) {

                                        Distance = SphericalUtil.computeDistanceBetween(mDefaultLocation,
                                                new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                                        Logger.E(TAG,"經緯度: "+mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                                        Logger.E(TAG,"距離打卡地:"+Distance);

                                        if (Distance < 300) {
                                            //btn_reset.setVisibility(View.INVISIBLE);
                                            //new IntentIntegrator(LocationActivity.this).initiateScan();

                                            mUserProfile.LocationAddresResult = 1;
                                            mUserProfile.Save(LocationActivity.this);
                                        } else {
                                            //btn_reset.setVisibility(View.VISIBLE);
                                            //Toast.makeText(getApplicationContext(), Distance + " ,你的定位並沒有在打卡範圍內，請重新定位後按Reset。", Toast.LENGTH_LONG).show();
                                            Toast.makeText(getApplicationContext(), "你的定位並沒有在限定範圍內，請到定位後再掃描QRCode。", Toast.LENGTH_LONG).show();
                                        }
                                        TransferActivity(MainFirstActivity.class);
                                    }
                                }
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "請開啟你的地圖重新定位你的位置。", Toast.LENGTH_LONG).show();
                                btn_reset.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

                //start gps
                //Criteria criteria = new Criteria();
                //String locationProvider = LocationManager.GPS_PROVIDER;

                if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    final Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location!=null) {
                        /*LatLng lastLocation=new LatLng(location.getLatitude(),
                                location.getLongitude());;*/

                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1500,  0.1f, this);

                    }
                }
                else
                {
                    //fail to start gps
                    //Log.e("locationManager", "GPS --> failed");
                }
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //從地址取得經緯度
    private void  GetFromLocation(String address)
    {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressLocation = null;
        try {
            addressLocation = geoCoder.getFromLocationName(address, 1);

            if(addressLocation != null)
            {
                double latitude = addressLocation.get(0).getLatitude();
                double longitude = addressLocation.get(0).getLongitude();
                mDefaultLocation = new LatLng(latitude,longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        GetDeviceLocation();
    }
    //实现界面操作

    protected void UIInit()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        btnNavigationBar = (BottomNavigationBar) findViewById(R.id.btnNavigationBar);
        btnNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        btnNavigationBar
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC
                );
    }
    private void InitLocation() {
        try {
            //check if has gained the permission or not
            if (mLocationPermissionGranted) {
                //enable the my location function

                GetDeviceLocation();
            } else {
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        wvWebBrowser.destroy();
    }

    //實現後台數據操作

}
