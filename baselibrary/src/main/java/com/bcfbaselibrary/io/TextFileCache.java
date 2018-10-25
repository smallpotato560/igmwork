package com.bcfbaselibrary.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;

public class TextFileCache {
	public boolean HasLoaded=false;
	
	Context mContext;
	FileService mFileService;
	FileCacheAsyncListener mFileCacheAsyncListener=null;
	String mFileName;
	
	
	public TextFileCache(Context context,FileCacheAsyncListener fileCacheAsyncListener)
	{
		mContext=context;
		mFileService=new FileService(context);
		mFileCacheAsyncListener=fileCacheAsyncListener;
	}
	
	public String CreateFileNameFromUrl(String url)
	{
		String filename=url.replace(":", "").replace("/","").replace("?", "")+".xml";
		
		return filename;
	}
	
	public void WriteFile(Object key,String filename,String content)
	{
		new WritingFileTask(key).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filename, content);
	}
	
	public void ReadFile(Object key,String filename)
	{
		new ReadingFileTask(key).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,filename);
	}
	
	public boolean ExistCache(String url)
	{
		boolean bResult=false;
		String filename=url.replace(":", "").replace("/","").replace("?", "")+".xml";
		
		 FileInputStream inStream = null;
        try
        {
        	inStream=mContext.openFileInput(filename);  
        }
        catch(FileNotFoundException e)
        {
        	inStream=null;
        }
        finally
        {
        	if(inStream!=null)
        	{
        		try {
					inStream.close();
					bResult=true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        return bResult;
	}
	
	public interface FileCacheAsyncListener {
		public void OnPoseWritingFile(Object key);
		public void OnPoseReadingFile(Object key,String xmlFromCache);
	}
	
	private class ReadingFileTask extends AsyncTask<String, Void, String> {

		private Object mkey;
		
		public ReadingFileTask(Object key)
		{
			mkey=key;
		}
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String fileContent=null;
			
			try {
				fileContent=mFileService.readFile(params[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return fileContent;
		}
		
		 @Override
	        protected void onPostExecute(String result) {
			 
			 HasLoaded=true;
			 if(mFileCacheAsyncListener!=null)
			 {
				 mFileCacheAsyncListener.OnPoseReadingFile(mkey, result);
			 }
		 }
	}

	
	private class WritingFileTask extends AsyncTask<String, Void, String> {

		private Object mkey;
		
		public WritingFileTask(Object key)
		{
			mkey=key;
		}
		@Override
		protected String doInBackground(String... params) {		
			try {
				mFileService.saveFile(params[0],params[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		 @Override
	        protected void onPostExecute(String result) {
			 if(mFileCacheAsyncListener!=null)
			 {
				 mFileCacheAsyncListener.OnPoseWritingFile(mkey);
			 }
		 }
	}
}
