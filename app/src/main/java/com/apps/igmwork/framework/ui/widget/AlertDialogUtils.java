package com.apps.igmwork.framework.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.apps.igmwork.R;


/**
 * Created by Ben on 2017/8/1.
 */

public class AlertDialogUtils {
    static private int  mWhich;
    static public void ShowSingleChoiceDialog(Context context,String title,String[] itemList,int checkedItem,boolean showCancelButton,final DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle(title);

        builder.setSingleChoiceItems(itemList, checkedItem,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                mWhich=which;
            }
        });
        builder.setPositiveButton(context.getString(R.string.action_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                listener.onClick(dialogInterface,mWhich);
            }
        });
        builder.setCancelable(false);
        if(showCancelButton)
            builder.setNegativeButton(context.getString(R.string.action_cancel), null);
        try {
            builder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void ShowSimpleSingleChoiceDialog(Context context,String title,String[] itemList,int checkedItem,final DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle(title);

        builder.setSingleChoiceItems(itemList, checkedItem,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                listener.onClick(dialogInterface,which);
                dialogInterface.dismiss();

            }
        });
        builder.setCancelable(false);
        try {
            builder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void ShowMessageDialog(Context context,String message)
    {
        ShowMessageDialog(context,message,null);
    }
    static public void ShowMessageDialog(Context context,String message,DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);

        builder.setMessage(message);

        builder.setPositiveButton(context.getString(R.string.action_confirm), listener);

        builder.setCancelable(true);
        try {
            builder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void ShowMessageDialog(Context context,String title,String message,boolean bShowCancelButton,DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);

        if(!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setMessage(message);

        builder.setPositiveButton(context.getString(R.string.action_confirm), listener);
        if(bShowCancelButton) {
            builder.setNegativeButton(context.getString(R.string.action_cancel), null);
        }
        builder.setCancelable(false);
        try {
            builder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void ShowMessageDialog(Context context,String title,String message,String confirmText,String cancelText, DialogInterface.OnClickListener confirmListener, DialogInterface.OnClickListener cancelListener)
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        if(!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(confirmText, confirmListener);
        builder.setNegativeButton(cancelText, cancelListener);
        try {
            builder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
