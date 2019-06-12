package com.apps.igmwork.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.igmwork.CustomCaptureActivity;
import com.apps.igmwork.LocationActivity;
import com.apps.igmwork.MainActivity;
import com.apps.igmwork.MainFirstActivity;
import com.apps.igmwork.R;
import com.apps.igmwork.SignActivity;
import com.apps.igmwork.WebActivity;
import com.apps.igmwork.framework.ui.BaseFragment;
import com.apps.igmwork.framework.ui.data.StringParam;
import com.apps.igmwork.framework.ui.widget.AlertDialogUtils;
import com.apps.igmwork.framework.ui.widget.CaptureManager;
import com.bcfbaselibrary.internal.Logger;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ScanFragment extends BaseFragment implements View.OnClickListener  {

    //静态成员

    //控件成员

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    protected View mContentView;

    @BindView(R.id.btnScan)
    protected Button btnScan;

    protected Unbinder mUnbinder;
    //数据成员



    //对象成员


    public static ScanFragment newInstance() {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    //实现界面事件
    @Override
    public void onResume()
    {
        super.onResume();
        capture.onResume();

        if(mUserProfile.IsLogin()) {
        }
        else
        {
            mBaseActivity.TransferActivityNoBack(SignActivity.class);
        }
        //Toast.makeText(mBaseActivity,"onResume",Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView= inflater.inflate(R.layout.fragment_scan, container, false);

        barcodeScannerView = (DecoratedBarcodeView) mContentView.findViewById(R.id.dbv_custom);

        capture = new CaptureManager(getActivity(), barcodeScannerView);
        capture.initializeFromIntent(mBaseActivity.getIntent(), savedInstanceState);
        capture.setResultCallBack(new CaptureManager.ResultCallBack() {
            @Override
            public void callBack(int requestCode, int resultCode, Intent intent) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (null != result && null != result.getContents()) {
                    showDialog(result.getContents());
                }
            }
        });
        capture.decode();
        mUnbinder= ButterKnife.bind(this,mContentView);
        mContentView.findViewById(R.id.btnScan).setOnClickListener(this);

        return mContentView;
    }

    public void setCapture()
    {

    }

    public void showDialog(String result) {
        //Toast.makeText(mBaseActivity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
        Logger.E("QRCodeResult",result);

        if (mUserProfile.IsLogin()) {
            if (result != null) {
                if (result.indexOf(("qkee")) > 0) {
                    if (result.indexOf("PunchTime") > 0) {
                        if(result.indexOf("address") > 0) {
                            String[] array = result.split("address=");
                            mUserProfile.LocationAddress = array[1];
                        }
                        result += "&uid=" + mUserProfile.UserID;
                    } else if (result.indexOf(("")) > 0) {

                    } else {
                        if(result.indexOf("address") > 0) {
                            String[] array = result.split("address=");
                            mUserProfile.LocationAddress = array[1];
                        }
                        result = MergeString(result, new StringParam("MobileNo", mUserProfile.MobileNum));
                    }
                    mUserProfile.LoadURL = result;
                    mUserProfile.Save(mBaseActivity);
                    if(result.indexOf("address") > 0) {
                        mBaseActivity.TransferActivity(LocationActivity.class);
                    }
                    else {
                        mBaseActivity.TransferActivity(WebActivity.class);
                    }

                } else {
                    if (result.indexOf("http") == 0) {
                        final String scanContent = result;
                        AlertDialogUtils.ShowMessageDialog( mBaseActivity, "掃描結果", "您掃描的QRcode結果：\n " + result, "打開網頁", "取消",
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
                        AlertDialogUtils.ShowMessageDialog(mBaseActivity,"掃描結果", "您掃描的QRcode結果：\n" + result, false,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onResume();
                                    }
                                });
                    }
                }
            } else {
                Toast.makeText(mBaseActivity, "掃描出錯，請重新掃描。", Toast.LENGTH_LONG).show();
            }
        }


        // 重新載入頁面
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        capture.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btnScan)
        {
            barcodeScannerView.setVisibility(View.VISIBLE);
        }
    }
}
