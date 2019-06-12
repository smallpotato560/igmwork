package com.apps.igmwork;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.apps.igmwork.fragment.BinnerListFragment;
import com.apps.igmwork.fragment.DataListFragment;
import com.apps.igmwork.fragment.MemberFragment;
import com.apps.igmwork.fragment.ScanFragment;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.data.StringParam;
import com.apps.igmwork.framework.ui.widget.AlertDialogUtils;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class MainFirstActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener, View.OnClickListener {

    //静态成员

    //控件成员

    //数据成员

    //数据成员
    private int mPosition = 0;
    private int mHomeMessage;
    private ArrayList<String> mTitleList;

    private BottomNavigationBar btnNavigationBar;
    private BadgeItem mBadgeItem;

    //对象成员
    private ArrayList<Fragment> mFragments;




    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainfirst);

        UIInit(getString(R.string.app_name));
        //初始化底部按键栏

        FirebaseMessaging.getInstance().subscribeToTopic("android");
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        //設定雲端通知標籤

        //按键栏默认选定位置
        int defaultPosition = 0;

        if(savedInstanceState!=null) {
            defaultPosition = savedInstanceState.getInt("Position");

            if(getIntent().hasExtra("Position")) {
                defaultPosition = getIntent().getIntExtra("Position",0);
            }
        }

        //按键栏文字设置
        mTitleList = new ArrayList<String>();
        mTitleList.add("掃描");
        mTitleList.add("紀錄");
        mTitleList.add("活動");
        mTitleList.add("個人");
        //生成按键栏控件

        /*
        mBadgeItem=new BadgeItem();
        mBadgeItem.setBorderWidth(2);    //Badge的Border(邊界)寬度
        mBadgeItem.setBorderColor("#FF0000");    //Badge的Border顏色
        mBadgeItem.setBackgroundColor("#f28e00");    //Badge背景顏色
        mBadgeItem.setGravity(Gravity.RIGHT| Gravity.TOP);   //位置，默認右上角
        mBadgeItem.setText("2"); //顯示的文本
        mBadgeItem.setTextColor("#F0F8FF"); //文本顏色
        mBadgeItem.setAnimationDuration(2000);
        mBadgeItem.setHideOnSelect(true);    //當選中狀態時消失，非選中狀態顯示
*/


        btnNavigationBar = (BottomNavigationBar) findViewById(R.id.btnNavigationBar);
        btnNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        btnNavigationBar
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setBarBackgroundColor(R.color.navigationBarColor);
        btnNavigationBar.addItem(new BottomNavigationItem(R.drawable.ic_tv_white_24dp,
                mTitleList.get(0))
                .setActiveColorResource(R.color.ActiveColor)
                .setInActiveColorResource(R.color.InActiveColor))
                .addItem(new BottomNavigationItem(R.drawable.ic_stars_white_24dp, mTitleList.get(1))
                        .setActiveColorResource(R.color.ActiveColor)
                        .setInActiveColorResource(R.color.InActiveColor))
                .addItem(new BottomNavigationItem(R.drawable.ic_thumb_up_white_24dp, mTitleList.get(2))
                        .setActiveColorResource(R.color.ActiveColor)
                        .setBadgeItem(mBadgeItem)
                        .setInActiveColorResource(R.color.InActiveColor))
                .addItem(new BottomNavigationItem(R.drawable.ic_account_box_white_24dp, mTitleList.get(3))
                        .setActiveColorResource(R.color.ActiveColor)
                        .setInActiveColorResource(R.color.InActiveColor))
                .setFirstSelectedPosition(defaultPosition)
                .initialise();

        // TODO 设置 BadgeItem 默认隐藏 注意 这句代码在添加 BottomNavigationItem 之后
        //mBadgeItem.hide();

        //生成内容fragment控件
        if (savedInstanceState != null)
        {
            // 防止fragment没被回收
            mFragments = LoadFragments();

        }
        else {
            mFragments = GetFragments();
        }

        //设置默认fragment位置
        SetDefaultFragment(defaultPosition);
        //设置按键栏事件
        btnNavigationBar.setTabSelectedListener(this);


        //初始界面数据
        //mAPPConfig = APPConfig.Load(this);


        //TransferActivity(VideoExoPlayerActivity.class);

        if(mUserProfile.LocationAddress != "") {
            if(mUserProfile.LocationAddresResult != 0) {
                mUserProfile.LocationAddresResult = 0;
                mUserProfile.Save(this);
                TransferActivity(WebActivity.class);
            }
        }
    }

        @Override
        protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String aid = intent.getStringExtra("aid");
        String url = intent.getStringExtra("url");
        if (url!=null) {
            //Log.e("FCM", "aid:" + msg);
            mUserProfile.Title="活動頁面";
            mUserProfile.LoadURL= url;
            mUserProfile.Save(mBaseActivity);
            mBaseActivity.TransferActivity(WebActivity.class);
        }
        else {
            if(aid != null)
            {
                mUserProfile.Title="活動頁面";
                mUserProfile.LoadURL= HTTPServer.ActivityURL + "?aid=" + aid;
                mUserProfile.Save(mBaseActivity);
                mBaseActivity.TransferActivity(WebActivity.class);
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String Content = result.getContents();
            final String scanContent = Content;

            if (mUserProfile.IsLogin()) {
                if (Content != null) {
                    if (Content.indexOf(("qkee")) > 0) {
                        if (Content.indexOf("PunchTime") > 0) {
                            Content += "?uid=" + mUserProfile.UserID;
                        } else if (Content.indexOf(("")) > 0) {

                        } else {
                            Content = MergeString(scanContent, new StringParam("MobileNo", mUserProfile.MobileNum));
                        }
                        mUserProfile.LoadURL = Content;
                        mUserProfile.Save(this);
                        mBaseActivity.TransferActivity(LocationActivity.class);

                    } else {
                        if (Content.indexOf("http") == 0) {
                            AlertDialogUtils.ShowMessageDialog(MainFirstActivity.this, "掃描結果", "您掃描的QRcode結果：\n " + Content, "打開網頁", "取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Uri uri = Uri.parse(scanContent);
                                            Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                            startActivity(it);
                                            onResume();
                                        }
                                    },
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            onResume();

                                        }
                                    });
                        }
                        else {
                            AlertDialogUtils.ShowMessageDialog(MainFirstActivity.this,"掃描結果", "您掃描的QRcode結果：\n" + scanContent, false,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            onResume();
                                        }
                                    });
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "掃描出錯，請重新掃描。", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            new AlertDialog.Builder(MainFirstActivity.this)
                    .setTitle("確認視窗")
                    .setMessage("確定要結束應用程式嗎?")
                    .setIcon(R.drawable.ic_launcher)
                    .setPositiveButton("確定",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub

                                }
                            }).show();
        }
        return true;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onTabSelected(int position) {
        mPosition=position;
        if (mFragments != null) {
            if (position < mFragments.size()) {

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment fragment = mFragments.get(position);
                if (fragment.isAdded()) {
                    if(position==0) {
                        ft.replace(R.id.content, fragment);
                    }
                    else  {
                        ft.show(fragment);
                    }

                    //ft.replace(R.id.content, fragment);
                } else {
                    ft.add(R.id.content, fragment);
                }
                ft.commitAllowingStateLoss();

                SetTitle(mTitleList.get(position));
            }
        }
    }

    @Override
    public void onTabUnselected(int position) {
        if (mFragments != null) {
            if (position < mFragments.size()) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment fragment = mFragments.get(position);
                if(position==0) {
                    ft.remove(fragment);
                }
                else {
                    ft.hide(fragment);
                }
                ft.commitAllowingStateLoss();
            }
        }

    }

    @Override
    public void onTabReselected(int position) {

    }

    //实现后台数据操作
    private ArrayList<Fragment> LoadFragments() {
        FragmentManager objFragmentManager = getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<>();
        Fragment objFragment=objFragmentManager.findFragmentByTag(ScanFragment.class.getName());

        if(objFragment==null)
            objFragment= ScanFragment.newInstance();
        fragments.add(objFragment);

        objFragment=objFragmentManager.findFragmentByTag(DataListFragment.class.getName());

        if(objFragment==null)
            objFragment=DataListFragment.newInstance();
        fragments.add(objFragment);

        objFragment=objFragmentManager.findFragmentByTag(BinnerListFragment.class.getName());
        if(objFragment==null)
            objFragment=BinnerListFragment.newInstance();
        fragments.add(objFragment);

        objFragment=objFragmentManager.findFragmentByTag(MemberFragment.class.getName());
        if(objFragment==null)
            objFragment=MemberFragment.newInstance();
        fragments.add(objFragment);


        return fragments;

    }

    private ArrayList<Fragment> GetFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(ScanFragment.newInstance( ));
        fragments.add(DataListFragment.newInstance());
        fragments.add(BinnerListFragment.newInstance());
        fragments.add(MemberFragment.newInstance());
        return fragments;
    }

    private void SetDefaultFragment(int position) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.content, mFragments.get(position));
        transaction.commit();
        SetTitle(mTitleList.get(position));
    }

    public void addMessage(){
        mHomeMessage ++ ;
        mBadgeItem.setText(mHomeMessage + "");
        mBadgeItem.show();
    }
    //定义外部类别
}

