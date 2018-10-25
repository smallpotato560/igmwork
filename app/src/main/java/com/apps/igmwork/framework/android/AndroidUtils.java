package com.apps.igmwork.framework.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.apps.igmwork.framework.ui.BaseActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Ben on 11/1/2017.
 */


public class AndroidUtils {
    public static final int MAX_IMAGE_WIDTH = 1920;
    public static final int MAX_IMAGE_HEIGHT = 1920;

    private Context m_Context;

    public AndroidUtils(Context context){
        m_Context=context;
    }

    public Bitmap createThumbnail(Bitmap source){
        int oldW = source.getWidth();
        int oldH = source.getHeight();
        int max_size=146;

        int w = Math.round((float)oldW/max_size);  //MAX_SIZE为缩略图最大尺寸
        int h = Math.round((float)oldH/max_size);

        int newW = 0;
        int newH = 0;

        if(w <= 1 && h <= 1){
            return source;
        }

        int i = w > h ? w : h;  //获取缩放比例

        newW = oldW/i;
        newH = oldH/i;

        Bitmap imgThumb = ThumbnailUtils.extractThumbnail(source, newW, newH);

        return imgThumb;
    }

    public Bitmap createThumbnail(Bitmap source,int newW,int newH){
        Bitmap imgThumb = ThumbnailUtils.extractThumbnail(source, newW, newH);

        return imgThumb;
    }


    public Bitmap createVideoThumbnail(String filePath){

        Bitmap videoThumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);

        return videoThumb;

    }

    public InputStream ConvertBitmap(Bitmap sourceImage)
    {
        return ConvertBitmap(Bitmap.CompressFormat.JPEG,sourceImage,70);
    }
    public InputStream ConvertBitmap(Bitmap.CompressFormat format,Bitmap sourceImage, int quality)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sourceImage.compress(format, quality, baos);

        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    public String GetFilePathFromUri(BaseActivity objActivity, Uri uri)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor actualimagecursor = objActivity.managedQuery(uri,proj,null,null,null);
        int actual_file_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String file_path = actualimagecursor.getString(actual_file_column_index);
        return file_path;
    }

    public Bitmap getSrcImage(Uri imageUri){

        Display display = ((Activity) this.m_Context).getWindowManager().getDefaultDisplay();

        int inSampleSize=1;
        float maxSize=display.getHeight()>display.getWidth()?display.getHeight():display.getWidth();
        Log.w("MaxSize", "" + maxSize);
        Bitmap bmp=null;
        try {
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeStream(m_Context.getContentResolver().openInputStream(imageUri),null,ops);
            int wRatio = (int)Math.ceil(ops.outWidth/(float)maxSize);
            int hRatio = (int)Math.ceil(ops.outHeight/(float)maxSize);

            if(wRatio > 1 && hRatio > 1){
                if(wRatio > hRatio){
                    inSampleSize=wRatio;
                    ops.inSampleSize = wRatio;
                }else{
                    inSampleSize=hRatio;
                    ops.inSampleSize = hRatio;
                }
            }

            ops.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeStream(m_Context.getContentResolver().openInputStream(imageUri),null,ops);

            return bmp;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(this.getClass().getName(), e.getMessage());

        } catch (final OutOfMemoryError e){

            try
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                inSampleSize=inSampleSize+inSampleSize;
                options.inSampleSize = inSampleSize;
                try {
                    bmp = BitmapFactory.decodeStream(m_Context.getContentResolver().openInputStream(imageUri),null,options);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                return bmp;
            } catch (final OutOfMemoryError e2){
                try
                {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    inSampleSize=inSampleSize+inSampleSize;
                    options.inSampleSize = inSampleSize;
                    try {
                        bmp = BitmapFactory.decodeStream(m_Context.getContentResolver().openInputStream(imageUri),null,options);
                    } catch (FileNotFoundException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    return bmp;
                } catch (final OutOfMemoryError e3){
                    e.printStackTrace();
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean HasExif(String filePath)
    {
        try{
            ExifInterface exif = new ExifInterface(filePath);
            String TAG_ORIENTATION=exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            Log.w("TAG_ORIENTATION",TAG_ORIENTATION);
            return TAG_ORIENTATION.equals("6");
        }
        catch(Exception ee){

        }
        return false;
    }

    public Bitmap RotateImage(Bitmap bitmap,int angle)
    {
        Bitmap rotateddBitmap=null;
        try{
            Matrix matrix = new Matrix();
            //matrix.postScale(bitmap.getWidth(), bitmap.getHeight());
            matrix.postRotate(angle);
            rotateddBitmap = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //matrix.postRotate(90);

        }
        catch(OutOfMemoryError e)
        {
            return bitmap;
        }
        catch(Exception ee){
            return bitmap;
        }
        return rotateddBitmap;
    }

    public int ReadPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap GetLoacalBitmap(String filePath) {
        FileInputStream fis = null;
        Bitmap bitmap=null;
        BitmapFactory.Options ops = new BitmapFactory.Options();
        try {

            ops.inJustDecodeBounds = true;

            fis = new FileInputStream(filePath);
            bitmap = BitmapFactory.decodeStream(fis,null,ops);

            int wRatio = (int)Math.ceil(ops.outWidth/(float)MAX_IMAGE_WIDTH);
            int hRatio = (int)Math.ceil(ops.outHeight/(float)MAX_IMAGE_HEIGHT);

            if(wRatio > 1 && hRatio > 1){
                if(wRatio > hRatio){
                    ops.inSampleSize = wRatio;
                }else{
                    ops.inSampleSize = hRatio;
                }
            }

            ops.inJustDecodeBounds = false;

            fis = new FileInputStream(filePath);
            return BitmapFactory.decodeStream(fis,null,ops);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (final OutOfMemoryError e){

            try
            {
                if( ops.inSampleSize<=1)
                {
                    ops.inSampleSize = 2;
                }
                else
                {
                    ops.inSampleSize=ops.inSampleSize*ops.inSampleSize;
                }
                bitmap = BitmapFactory.decodeStream(fis,null,ops);
                return bitmap;
            } catch (final OutOfMemoryError e2){
                try
                {
                    if( ops.inSampleSize<=1)
                    {
                        ops.inSampleSize = 2;
                    }
                    else
                    {
                        ops.inSampleSize=ops.inSampleSize*ops.inSampleSize;
                    }

                    bitmap = BitmapFactory.decodeStream(fis,null,ops);
                    return bitmap;
                } catch (final OutOfMemoryError e3){
                    e.printStackTrace();
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getRealPathFromURI(BaseActivity act, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = act.managedQuery(contentUri, proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void SetBackgroundWithBitmap(View view, Bitmap bitmap)
    {
        Drawable drawable = new BitmapDrawable(bitmap);
        try {
            Method setBackground = View.class.getMethod("setBackground", Drawable.class);
            try {
                setBackground.invoke(view, drawable);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            Method setBackgroundDrawable;
            try {
                setBackgroundDrawable = View.class.getMethod("setBackgroundDrawable", Drawable.class);
                try {
                    setBackgroundDrawable.invoke(view, drawable);
                } catch (IllegalArgumentException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void ShowLongTextAlert(Context context,String content)
    {
        Toast toast=Toast.makeText(context,content, Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void ShowShortTextAlert(String content)
    {
        Toast toast=Toast.makeText(m_Context,content, Toast.LENGTH_SHORT);
        //toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static boolean IsSameMinute(String dateString1,String dateString2)
    {
        SimpleDateFormat timeFormatReader=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            String date1=timeFormat.format(timeFormatReader.parse(dateString1));
            String date2=timeFormat.format(timeFormatReader.parse(dateString2));

            //Logger.W("IsSameMinute Date1", "IsSameMinute Date1:"+date1);
            //Logger.W("IsSameMinute Date2", "IsSameMinute Date2:"+date2);
            return date1.equals(date2);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static boolean IsSameHour(String dateString1,String dateString2)
    {
        SimpleDateFormat timeFormatReader=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            return timeFormat.format(timeFormatReader.parse(dateString1)).equals(timeFormat.format(timeFormatReader.parse(dateString2)));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static boolean IsSameDay(String dateString1,String dateString2)
    {
        SimpleDateFormat timeFormatReader=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd");
        try {
            return timeFormat.format(timeFormatReader.parse(dateString1)).equals(timeFormat.format(timeFormatReader.parse(dateString2)));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static boolean IsTodayDay(String dateString1)
    {
        SimpleDateFormat timeFormatReader=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd");
        try {
            return timeFormat.format(timeFormatReader.parse(dateString1)).equals(timeFormat.format(new Date()));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }


    /*public static String GetDateStringInStyle1(Context context,String dateString)
    {

        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //String nowString = timeFormat.format(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd");

        SimpleDateFormat today_formatter = new SimpleDateFormat ("HH:mm");
        String formatedDate="";
        String formatedNow="";

        try
        {

            Date datePosted = timeFormat.parse(dateString);
            Date now = new Date();

            formatedNow = formatter.format(now);
            formatedDate = formatter.format(datePosted);


            long diff = now.getTime() - datePosted.getTime();

            long seconds=diff / (1000);
            long minutes = diff / (1000 * 60);
            long hours = diff / (1000 * 60 * 60);
            long days = diff / (1000 * 60 * 60 * 24);


            if(seconds>0&&seconds<60){
                formatedDate=seconds+context.getResources().getString(R.string.app_msg_label_date_second);
            }
            else if(minutes>0&&minutes<60)
            {
                formatedDate=String.format("%d"+context.getResources().getString(R.string.app_msg_label_date_minute),
                        minutes);
            }
            else if(hours>0&&hours<24)
            {
                formatedDate=String.format("%d"+context.getResources().getString(R.string.app_msg_label_date_hour),
                        hours);
            }
            else if(days>0&&days<24)
            {
                formatedDate=String.format("%d"+context.getResources().getString(R.string.app_msg_label_date_day),
                        days);
            }

            if(formatedNow.equals(formatedDate))
            {
                formatedDate = today_formatter.format(datePosted);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return formatedDate;
    }*/

    public static String GetDateStringInStyle2(Context context,String dateString)
    {
        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //String nowString = timeFormat.format(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd\nHH:mm");

        SimpleDateFormat today_formatter = new SimpleDateFormat ("HH:mm");
        String formatedDate="";
        String formatedNow="";
        try
        {

            Date datePosted = timeFormat.parse(dateString);
            Date now = new Date();

            formatedNow = formatter.format(now);
            formatedDate = formatter.format(datePosted);


            long diff = now.getTime() - datePosted.getTime();

            long seconds=diff / (1000);
            long minutes = diff / (1000 * 60);
            long hours = diff / (1000 * 60 * 60);
            long days = diff / (1000 * 60 * 60 * 24);

			/*
			if(seconds>0&&seconds<60){
				formatedDate=context.getResources().getString(R.string.resource_list_label_date_6);
			}
			else if(minutes<60)
			{
				formatedDate=String.format("%d"+context.getResources().getString(R.string.resource_list_label_date_1),
						minutes);
			})*/

            if(formatedNow.equals(formatedDate))
            {
                formatedDate = today_formatter.format(datePosted);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return formatedDate;
    }

    public static String GetDateStringInStyle3(Context context,String dateString)
    {
        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //String nowString = timeFormat.format(new Date());

        SimpleDateFormat today_formatter = new SimpleDateFormat ("MM-dd HH:mm");
        String formatedDate="";
        try
        {

            Date datePosted = timeFormat.parse(dateString);
            formatedDate = today_formatter.format(datePosted);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return formatedDate;
    }

    public static String GetTimeStringInSimpleStyle(Context context,String dateString)
    {

        SimpleDateFormat timeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd");

        SimpleDateFormat today_formatter = new SimpleDateFormat ("HH:mm");
        String formatedDate="";

        try
        {

            Date datePosted = timeFormat.parse(dateString);

            formatedDate = today_formatter.format(datePosted);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return formatedDate;
    }

    public static String GetMonthlyString(Context context,Date datePosted)
    {

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月");

        String formatedDate="";

        try
        {


            formatedDate = formatter.format(datePosted);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return formatedDate;
    }

	/*
	public String GetKindedDateString(Context context,String nowString,String dateString)
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat formatter = new SimpleDateFormat (context.getResources().getString(R.string.resource_list_label_date_4));
		SimpleDateFormat formatter2 = new SimpleDateFormat (context.getResources().getString(R.string.resource_list_label_date_5));
		String formatedDate=dateString;

		try
		{
			Date datePosted = df.parse(dateString);
			Date now = df.parse(nowString);

			if(datePosted.getYear()==now.getYear())
			{
				formatedDate = formatter2.format(datePosted);
			}
			else
			{
				formatedDate = formatter.format(datePosted);
			}

			//Date now = new Date();



			long diff = now.getTime() - datePosted.getTime();
			Log.w("Date", String.valueOf(diff));
			long seconds=diff / (1000);
			long minutes = diff / (1000 * 60);
			long hours = diff / (1000 * 60 * 60);
			long days = diff / (1000 * 60 * 60 * 24);

			if(seconds<60){
				formatedDate="1"+context.getResources().getString(R.string.resource_list_label_date_1);
			}
			else if(minutes<60)
			{
				formatedDate=String.format("%d"+context.getResources().getString(R.string.resource_list_label_date_1),
						minutes);
			}
			else if(hours<24)
			{
				formatedDate=String.format("%d"+context.getResources().getString(R.string.resource_list_label_date_2)
						, hours);
			}
			else if(days<8)
			{
				formatedDate=String.format("%d"+context.getResources().getString(R.string.resource_list_label_date_3)
						, days);
			}
		}
		catch (Exception e)
		{
		}

		return formatedDate;
	}
	*/

    public static int GetAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;

        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
            versionCode=0;
        }
        return versionCode;
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int GetTitleHeight(Context context)
    {
        return dip2px(context,50-2);
    }

    public static void HideInput(Context context,View view)
    {
        final InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void ShowInput(Context context,View view)
    {
        final InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void CreateShortCut(Context context,String title,Class<?> cls,int shortcurt_resource){
        if(!ShortcutExists(context,title))
        {
            Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            shortcutintent.putExtra("duplicate", false);
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME,title);
            Parcelable icon = Intent.ShortcutIconResource.fromContext(context, shortcurt_resource);
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(context , cls));

            context.sendBroadcast(shortcutintent);
        }
    }



    private static boolean ShortcutExists(Context context,String title)
    {
        String AUTHORITY = GetAuthorityFromPermission(context,
                "com.android.launcher.permission.READ_SETTINGS");
        if (AUTHORITY == null) {
            AUTHORITY = GetAuthorityFromPermission(context,
                    "com.android.launcher.permission.WRITE_SETTINGS");
        }
        if (AUTHORITY == null) {
            return false;
        }
        boolean isInstallShortcut = false;
        final ContentResolver cr = context.getContentResolver();

        final Uri CONTENT_URI = Uri.parse("content://" +AUTHORITY + "/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI,new String[] {"title" },"title=?",
                new String[] {title}, null);
        if(c!=null && c.getCount()>0){
            isInstallShortcut = true ;
        }
        return isInstallShortcut ;
    }



    private static String GetAuthorityFromPermission(Context context, String permission) {
        if (permission == null)
            return null;
        List<PackageInfo> packs = context.getPackageManager()
                .getInstalledPackages(PackageManager.GET_PROVIDERS);
        if (packs != null) {
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (permission.equals(provider.readPermission))
                            return provider.authority;
                        if (permission.equals(provider.writePermission))
                            return provider.authority;
                    }
                }
            }
        }
        return null;
    }

    public static void ShowMarket(Activity activity,String appPackageName ) {
        try {
            Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage("com.android.vending");
            // package name and activity
            ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity");
            launchIntent.setComponent(comp);
            launchIntent.setData(Uri.parse("market://details?id=" + appPackageName));

            activity.startActivity(launchIntent);

        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
    public static int GetDrawableResourceID(Context context,String resourceIDString)
    {
        return GetResourceID(context,"drawable",resourceIDString);
    }
    public static int GetResourceID(Context context,String resourceType,String resourceIDString)
    {
        int resourceID=context.getResources().getIdentifier(resourceIDString,//需要转换的资源名称
                resourceType,        //资源类型
                "com.a168tv");//R类所在的包名
        return resourceID;
    }

    public static int GetScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static int GetScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }
}