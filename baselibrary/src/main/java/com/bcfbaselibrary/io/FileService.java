package com.bcfbaselibrary.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bcfbaselibrary.internal.Logger;

public class FileService {
    private Context context;
    public FileService(Context context)
    {
        this.context = context;
    }

    /**
     * 读取文件的内容 
     * @param filename 文件名称 
     * @return
     * @throws Exception
     */
    public String readFile(String filename) throws Exception
    {
        //获得输入流  
        FileInputStream inStream = null;
        try
        {
            inStream=context.openFileInput(filename);
        }
        catch(FileNotFoundException e)
        {
            return null;
        }
        //new一个缓冲区  
        byte[] buffer = new byte[1024];
        int len = 0;
        //使用ByteArrayOutputStream类来处理输出流  
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while( (len = inStream.read(buffer))!= -1)
        {
            //写入数据  
            outStream.write(buffer, 0, len);
        }
        //得到文件的二进制数据  
        byte[] data = outStream.toByteArray();
        //关闭流  
        outStream.close();
        inStream.close();
        return new String(data);
    }
    /**
     * 以默认私有方式保存文件内容至SDCard中 
     * @param filename
     * @param content
     * @throws Exception
     */
    public void saveToSDCard(String filename, String content) throws Exception
    {
        //通过getExternalStorageDirectory方法获取SDCard的文件路径  
        File file = new File(Environment.getExternalStorageDirectory(), filename);
        //获取输出流  
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(content.getBytes());
        outStream.close();
    }

    public void saveFileToSDCard(String filename,  byte[] filebuffer) throws Exception
    {
        boolean sdCardExist=Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if(sdCardExist)
        {
            File SDdir=Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS );

            if(!SDdir.exists())
            {
                SDdir.mkdir();
            }

            File file = new File(Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS ), filename);
            //获取输出流
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(filebuffer);
            outStream.close();

            //Log.w("saveFileToSDCard",SDdir.toString());
        }
        else
        {
            saveFileToFileCache(filename,filebuffer);
        }
    }

    public void saveFileToFileCache(String filename,  byte[] filebuffer) throws Exception
    {

        //File apkdir=context.getFilesDir();
        File apkdir= context.getExternalFilesDir("Audio");
        if(apkdir==null)
            apkdir=context.getCacheDir();
        if(!apkdir.exists())
        {
            apkdir.mkdirs();
        }

        File file = new File(apkdir, filename);

        Logger.E("saveFileToFileCache","saveFileToFileCache:"+file.getAbsolutePath());
        if(!file.exists()) {
            //获取输出流
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(filebuffer);
            outStream.close();
        }

    }

    public String GetSDFilePath()
    {
        boolean sdCardExist=Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if(sdCardExist)
        {
            File SDdir=Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS );
            return SDdir.getAbsolutePath();
        }
        else
        {
            return context.getFilesDir().getAbsolutePath();
        }

    }

    /**
     * 以默认私有方式保存文件内容，存放在手机存储空间中 
     * @param filename
     * @param content
     * @throws Exception
     */
    public void saveFile(String filename, String content) throws Exception
    {
        //  
        FileOutputStream outStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        outStream.write(content.getBytes());
        outStream.close();
    }

    public void saveFile(String filename, byte[] filebuffer) throws Exception
    {
        FileOutputStream outStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        outStream.write(filebuffer);
        outStream.close();
    }

    /**
     * 以追加的方式保存文件内容 
     * @param filename 文件名称 
     * @param content 文件内容 
     * @throws Exception
     */
    public void saveAppend(String filename, String content) throws Exception
    {
        FileOutputStream outStream = context.openFileOutput(filename, Context.MODE_APPEND);
        outStream.write(content.getBytes());
        outStream.close();
    }
}