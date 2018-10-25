package com.apps.igmwork.framework.ui.widget;

/**
 * Created by Ben on 2017/8/23.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.view.ViewGroup;

import com.apps.igmwork.framework.android.AndroidUtils;


public abstract class InternalBottomSheetDialogFragment extends BottomSheetDialogFragment {

    //静态成员

    //控件成员
    protected View rootView;
    protected BottomSheetBehavior mBehavior;

    protected Dialog mDialog;
    //数据成员
    protected int mScreenHeight = 0;
    protected int mViewHeight = 0;
    //对象成员


    //实现界面事件
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //每次打开都调用该方法 类似于onCreateView 用于返回一个Dialog实例
        mDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if (rootView == null) {
            //缓存下来的View 当为空时才需要初始化 并缓存
            rootView = InitContentView();
        }

        if (mScreenHeight == 0) {
            mScreenHeight= AndroidUtils.GetScreenHeight(getContext());
            mViewHeight = mScreenHeight - AndroidUtils.dip2px(getContext(), 50 + 25);
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDialog.setContentView(rootView, params);//设置View重新关联
        mBehavior = BottomSheetBehavior.from((View) rootView.getParent());


        mBehavior.setPeekHeight(mViewHeight);
        setBehaviorCallback();
        return mDialog;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //解除缓存View和当前ViewGroup的关联
        try {
            ((ViewGroup) (rootView.getParent())).removeView(rootView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //实现后台数据事件
    protected abstract View InitContentView();

    protected BottomSheetBehavior.BottomSheetCallback GetBaseBottomSheetCallback() {
        return new BaseBottomSheetCallback();
    }

    //实现界面操作

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    private void setBehaviorCallback() {
        mBehavior.setBottomSheetCallback(GetBaseBottomSheetCallback());
    }


    //实现后台数据操作
    //定义外部类别

    public class BaseBottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            //Logger.E("onStateChanged", "newState:" + newState + ",top:" + bottomSheet.getTop());
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                mBehavior.setPeekHeight(0);
            }
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                if (mBehavior.getPeekHeight() == 0) {
                    dismiss();
                    mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    }
}