package com.apps.igmwork;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.igmwork.common.model.UserProfile;
import com.apps.igmwork.framework.map.EvilTransform;
import com.apps.igmwork.framework.map.SphericalUtil;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.widget.AlertDialogUtils;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.bcfbaselibrary.internal.Logger;
import com.bcfbaselibrary.io.PhoneService;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements LocationListener, View.OnClickListener {

    //静态成员
    private static final int DEFAULT_ZOOM = 16;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //数据成员
    private int mPosition = 0;
    private ArrayList<String> mTitleList;
    private boolean mLocationPermissionGranted;
    private boolean mLocationGranted;
    //private final LatLng mDefaultLocation = new LatLng(24.2131498, 120.7065008);
    private final LatLng mDefaultLocation = new LatLng(24.1817706, 120.6148122);
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
        UIInit("IGM打卡");
        btn_scan.setVisibility(View.INVISIBLE);
        btn_reset.setVisibility(View.INVISIBLE);
        wvWebBrowser.setVerticalScrollbarOverlay(true);
        //设置WebView支持JavaScript
        wvWebBrowser.getSettings().setJavaScriptEnabled(true);

        //webView.loadUrl("http://97.74.238.39/WorkTime/PunchTime.aspx?UserID="+mUserProfile.UserID);

        if(mUserProfile.UserID == 0)
        {
            Intent intent=new Intent();
            intent.setClass(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }
        else{
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            CheckLocationPermission();
            InitLocation();
        }


        /*
        //初始化底部按键栏
        UIInit();
        AddTab("Home",R.drawable.outline_view_headline_white_24);
        AddTab("Scan",R.drawable.outline_view_headline_white_24);


        //按键栏默认选定位置
        int defaultPosition = 0;
        if(getIntent().hasExtra("Position")) {
            defaultPosition = getIntent().getIntExtra("Position",0);
        }
        if(savedInstanceState!=null) {
            defaultPosition = savedInstanceState.getInt("Position");
        }

        //生成内容fragment控件

        if (savedInstanceState != null)
        {
            // 防止fragment没被回收
            mFragments = LoadFragments();

        }
        else {
            mFragments = GetFragments();
        }

        SetDefaultFragment(defaultPosition);
        //设置按键栏事件
        btnNavigationBar.setTabSelectedListener(this);
        btnNavigationBar.setFirstSelectedPosition(defaultPosition)
                .initialise();

        // listen for navigation events
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);


                this.btn_scan = (Button)findViewById(R.id.btn_scan);
                this.txt_url = (TextView) findViewById(R.id.txt_url);

                this.btn_scan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new IntentIntegrator(MainActivity.this).initiateScan();
                    }
                });*/
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result!=null){
            String scanContent = result.getContents();
            String scanFormat = result.getFormatName();

            if(mUserProfile.UserID != 0)
            {
                if(scanContent != null) {
                    wvWebBrowser.setVisibility(View.VISIBLE);

                    if (scanContent.indexOf("PunchTime") > 0) {
                        btn_scan.setVisibility(View.INVISIBLE);
                        scanContent += "?UserID=" + mUserProfile.UserID;
                    } else {
                        btn_scan.setVisibility(View.VISIBLE);
                    }
                    //LoadPage(scanContent);

                    //在js中调用本地java方法
                    wvWebBrowser.addJavascriptInterface(new JsInterface(this), "AndroidWebView");

                    wvWebBrowser.loadUrl(scanContent);
                }
                else {
                    btn_scan.setVisibility(View.VISIBLE);
                    wvWebBrowser.setVisibility(View.INVISIBLE);
                }
            }
        }else{
            Toast.makeText(getApplicationContext(), "nothing", Toast.LENGTH_LONG).show();
        }

    }


    //实现界面事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan: {
                new IntentIntegrator(MainActivity.this).initiateScan();
            }
            case  R.id.btn_reset: {
                TransferActivity(MainActivity.class);
            }
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
    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {
        //Logger.E(TAG,"Key:"+key+",Response:"+jsonServerResponse.toString());
        if(key.equals("UserAccountAdd"))
        {
            if(jsonServerResponse.optInt("StatusCode")==1) {
                mUserProfile.UserID=jsonServerResponse.optInt("UserID");
                PhoneService objPhoneService=new PhoneService(this);
                mUserProfile.DeviceID= objPhoneService.GetDeviceTaken();
                mUserProfile.Save(this);
                Logger.E(TAG,"UserID:"+mUserProfile.UserID);
            }
        }
    }
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
                            mStartLocation=new Location(mLastKnownLocation);
                            if(mLastKnownLocation!=null) {
                                //SetLabelText(lblGameLog, "Lat:" + mLastKnownLocation.getLatitude() + ",Lng:" + mLastKnownLocation.getLongitude());

                                /*
                                                                if(!EvilTransform.OutOfChina(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                                                                {
                                                                    LatLng latLng= EvilTransform.WGS2GCJ(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                                                                    mLastKnownLocation.setLatitude(latLng.latitude);
                                                                    mLastKnownLocation.setLongitude(latLng.longitude);
                                                                }*/
                                Distance= SphericalUtil.computeDistanceBetween(mDefaultLocation,
                                        new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                                //Logger.E(TAG,"經緯度: "+mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                                //Logger.E(TAG,"距離打卡地:"+Distance);

                                if(Distance < 300) {
                                    btn_reset.setVisibility(View.INVISIBLE);
                                    new IntentIntegrator(MainActivity.this).initiateScan();
                                }
                                else {
                                    btn_reset.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), Distance +" ,你的定位並沒有在打卡範圍內，請重新定位後按Reset。", Toast.LENGTH_LONG).show();
                                }
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

    public void LoadPage(String Url)
    {
        wvWebBrowser.getSettings().setJavaScriptEnabled(true);
        wvWebBrowser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //在js中调用本地java方法
        wvWebBrowser.addJavascriptInterface(new JsInterface(this), "AndroidWebView");

        //mContentView.findViewById(R.id.pbWebPageLoadingBar).setVisibility(View.GONE);
        wvWebBrowser.setWebViewClient(new WebViewClient()
        {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                ShowLoadingBar();
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);

                HideLoadingBar();

                //view.loadUrl("javascript: var allImages = document.getElementsByTagName('img'); if (allImages) {var i;for (i=0; i<allImages.length; i++) {var image = allImages[i]; imagelink.setAttribute('width','100%');}}");

                //view.loadUrl("javascript: var allLinks = document.getElementsByTagName('a'); if (allLinks) {var i;for (i=0; i<allLinks.length; i++) {var link = allLinks[i];var target = link.getAttribute('target'); if (target && target == '_blank') {link.setAttribute('target','_self');link.href = 'newtab:'+link.href;}}}");
            }
        });


        wvWebBrowser.setWebChromeClient(new WebChromeClient()
                                        {
                                            @Override
                                            public boolean onJsAlert(WebView view, String url, final String message, JsResult result) {
                                                String showMessage=message.replace("[CloseWindow]","").replace("[MemberUpgrade]","");

                                                AlertDialogUtils.ShowMessageDialog(MainActivity.this, showMessage,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if(message.startsWith("[CloseWindow]"))
                                                                {
                                                                    finish();
                                                                }
                                                                else if(message.startsWith("[MemberUpgrade]"))
                                                                {
                                                                    //TransferActivity(MemberUpgradeActivity.class);
                                                                    setResult(RESULT_OK);
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                                result.cancel();
                                                return true;
                                            }
                                        }
        );
        Logger.E("wvWebBrowser load","URL:"+Url);
        wvWebBrowser.loadUrl(Url);


    }
    private class JsInterface {
        private Context mContext;

        public JsInterface(Context context) {
            this.mContext = context;
        }

        //在js中调用window.AndroidWebView.showInfoFromJs(name)，便会触发此方法。
        @JavascriptInterface
        public void showInfoFromJs(String msg) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            new IntentIntegrator(MainActivity.this).initiateScan();
            wvWebBrowser.setVisibility(View.INVISIBLE);
            //Intent intent=new Intent(WebPayActivity.this,MainActivity.class);
            //intent.putExtra("Position",3);
            //TransferActivityForResult(intent,1);
        }
    }
    public void sendInfoToJs(View view) {
        String msg = ""; //((EditText) findViewById(R.id.input_et)).getText().toString();
        //调用js中的函数：showInfoFromJava(msg)
        wvWebBrowser.loadUrl("javascript:showInfoFromJava('" + msg + "')");
    }
}
