package com.bcfbaselibrary.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class SDCardFile {
	public static String getSDPicturePath(){
        File SDdir=null;
        boolean sdCardExist=Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdCardExist){
                SDdir=Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES );
                if(!SDdir.exists())
                {
                	SDdir.mkdir();
                }
        }
        if(SDdir!=null){
                return SDdir.toString();
        }
        else{
                return null;
        }
	}
	
	public static void CreatePath(String path) {
	    File file = new File(path);
	    if (!file.exists()) {
         file.mkdir();
	     }
	 }
	
	public static void DeleteFile(String filepath) {
	    File file = new File(filepath);
	    
	    file.delete();
	    
	 }
	
	public static boolean ExistPath(String path) {
	    File file = new File(path);
	    return file.exists();
	 }
	
	public static void SaveBitmap(Drawable drawable,String filePath,String fileName,int quality) throws IOException {        
		Bitmap bmp=drawable2Bitmap(drawable);
		File file = new File(filePath + fileName);
		
        file.createNewFile();
	    FileOutputStream fOut = null;
	    
	    try {
	    	fOut = new FileOutputStream(file);       	            
	    } 
	    catch (FileNotFoundException e) 
	    {
	    	e.printStackTrace();
	    }
	    
	    bmp.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
	    Log.w("SaveBitmap","bmp.compress");
	    try {
	    	fOut.flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    try {
	    	fOut.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
		
	}
	
	public static void SaveBitmap(Bitmap bmp,String filePath,String fileName,int quality) throws IOException {        
		
		CreatePath(filePath);
		File file = new File(filePath + fileName);
		if(!file.exists())
		{
			file.createNewFile();
		}
		FileOutputStream fOut = null;
	    try {
	    	fOut = new FileOutputStream(file);       	            
	    } 
	    catch (FileNotFoundException e) 
	    {
	    	e.printStackTrace();
	    }
	    
	    bmp.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
	    
	    try {
	    	fOut.flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    try {
	    	fOut.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
		
	}
	
	static Bitmap drawable2Bitmap(Drawable drawable){
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), 
	            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565); 
	    Canvas canvas = new Canvas(bitmap); 
	 
	    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()); 
	    drawable.draw(canvas); 
	    return bitmap; 
    }	
}
