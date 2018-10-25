package com.bcfbaselibrary.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.bcfbaselibrary.internal.Logger;
import com.bcfbaselibrary.io.FileService;
import com.bcfbaselibrary.io.TextFileCache;
import com.bcfbaselibrary.io.TextFileCache.FileCacheAsyncListener;

public class HTTPRequest implements FileCacheAsyncListener  {

	public static boolean IsDebug=true;
	static protected String HTTPCookie="";
	
	public enum RequestFileType{ Text, XML, Image,File }
	public enum UIType{ Activity, FragmentActivity }
	public enum HTTPErrorType{NetworkNoTAvailable,ServerError,PageCannotRead,RequestTimeout,XMLParseError,NoError,Canceled,APPException}
	public enum NetworkState {NoTAvailable,Connected}
	// Whether there is a Wi-Fi connection.
	private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;    
    
    private Context UIContext;
    private HTTPConnectionListener mHTTPHandler;
   
    //private FragmentActivity UIFragmentActivity;

    private Map<String,String> FromParams; 
    
    public boolean mSingleRequest=false;
    private NetworkReceiver NetworkListener;
 // The BroadcastReceiver that tracks network connectivity changes.
    
    private DownloadContentTask mCurrentTask;
    
    public boolean mSupportLocalCache=false;
    protected TextFileCache mTextFileCache;
    
    public HTTPRequest(Context context,HTTPConnectionListener objHTTPHandler)
    {
    	//UIFragmentActivity=context;
    	UIContext=context;
    	mHTTPHandler=objHTTPHandler;
    	NetworkListener = new NetworkReceiver(mHTTPHandler);    	
    	
    	TextFileCache mTextFileCache=new TextFileCache(context,this);
    }
    
    public void InitiateNetwork()
    {
    	updateConnectedFlags();
    	// Register BroadcastReceiver to track connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        UIContext.registerReceiver(NetworkListener, filter);
    }
    
    public void DestrotyNetwork()
    {
    	 if (NetworkListener != null) {
    		 UIContext.unregisterReceiver(NetworkListener);
         }
    }
    
    public boolean NetworkAvailable()
    {
    	updateConnectedFlags();
    	return wifiConnected || mobileConnected;
    }
    
    public boolean WifiAvailable()
    {
    	updateConnectedFlags();
    	return wifiConnected;
    }
    
    public Context GetBaseContext()
    {
    	return UIContext;
    }
    public void AddFormParam(String paramName,String paramValue)
    {
    	if(FromParams==null)
    	{
    		FromParams=new HashMap<String,String>();
    	}
    	
    	FromParams.put(paramName, paramValue);
    }
    
    public void LoadPage(Object key,String url)
    {
    	LoadPage( RequestFileType.Text, key, url,null);
    }
    
    public void LoadPage(RequestFileType pageType,Object key,String url)
    {
    	LoadPage( pageType, key, url,null);
    }
    public void LoadPage(RequestFileType pageType,Object key,String url,Map<String,String> fromParams)
    {
    	//updateConnectedFlags();
		if(IsDebug) {
			Log.w("HTTPRequest", "Network:" + NetworkAvailable() + ",URL:" + url);
		}
    	 if (wifiConnected || mobileConnected) {  
    		 if(this.mSingleRequest==true
    				 &&mCurrentTask!=null)
    		 {
    			 mCurrentTask.cancel(false);
    		 }
    		 // AsyncTask subclass
    		 mCurrentTask=new DownloadContentTask(pageType,key,mHTTPHandler,fromParams);
    		 mCurrentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    	     
         } else {        	 
        	 if(mSupportLocalCache&&mTextFileCache.ExistCache(url))
        	 {
	        	 String filename=url.replace(":", "").replace("/","").replace("?", "")+".xml";
	         	
	         		mTextFileCache.ReadFile(key, filename);
        	 }
        	 else
        	 {
        		 mHTTPHandler.OnHandleError(pageType,key,HTTPErrorType.NetworkNoTAvailable,null);
        	 }
         }
    }
    
    public void LoadPage(RequestFileType pageType,boolean saveToSDCard,Object key,String url)
    {
    	//updateConnectedFlags();
    	
    	 if (wifiConnected || mobileConnected) {    
    		 if(this.mSingleRequest==true
    				 &&mCurrentTask!=null)
    		 {
    			 mCurrentTask.cancel(false);
    		 }
    		 // AsyncTask subclass
    		 mCurrentTask=new DownloadContentTask(pageType,key,mHTTPHandler,null);
    		 mCurrentTask.bAutoSaveToSDCard=saveToSDCard;
    		 mCurrentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    	     
         } else {        	 
        	 mHTTPHandler.OnHandleError(pageType,key,HTTPErrorType.NetworkNoTAvailable,null);
         }
    }
    
    public void Cancel()
    {
    	if(mCurrentTask!=null)
		 {
    		try
    		{
    			mCurrentTask.cancel(false);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
		 }
    }
    
    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr ;
        
        connMgr=(ConnectivityManager) UIContext.getSystemService(Context.CONNECTIVITY_SERVICE);
       

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }
    
    public class NetworkReceiver extends BroadcastReceiver {

    	private HTTPConnectionListener mHTTPHandler;
    	
    	public NetworkReceiver(HTTPConnectionListener objHTTPHandler)
        {
    		mHTTPHandler=objHTTPHandler;
        }
    	
    	
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                mobileConnected = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
                
                mHTTPHandler.OnNetworkStateChange(NetworkState.Connected);        
            } else {
                wifiConnected = false;
                mobileConnected = false;                
               
                mHTTPHandler.OnNetworkStateChange(NetworkState.NoTAvailable);                
            }
        }
    }
    
    //Implementation of AsyncTask used to download XML.
    private class DownloadContentTask extends AsyncTask<String, Integer, String> {

    	public boolean bAutoSaveToSDCard=false;
    	private Object Key;
        private HTTPConnectionListener mHTTPHandler;
        
    	private RequestFileType RequestFileType = HTTPRequest.RequestFileType.XML;
    	private Bitmap NetworkImage = null;
    	private String HTTPContent = null;
    	private String HTTPFileName = null;
    	private HTTPErrorType ErrorType=HTTPErrorType.NoError;
    	private Exception HTTPError=null;
    	private Map<String,String> SubFromParams=null; 
    	
    	public DownloadContentTask(HTTPRequest.RequestFileType fileType,Object key,HTTPConnectionListener objHTTPHandler,
    			Map<String,String> fromParams)
    	{
    		super();
    		RequestFileType=fileType;
    		Key=key;
    		mHTTPHandler=objHTTPHandler;
    		
    		SubFromParams=fromParams;
    	}
    	
    	
    	private void OnPreHTTPRequest(Object Key)
    	{
    		mHTTPHandler.OnPreHTTPRequest(Key);    		
    	}
    	
    	private void OnReceiveHTTPResp(String xmlFromHTTP)
    	{
    		try
    		{
				if(IsDebug)
				{
					Logger.W("HTTPRequest","key:"+Key+"Resp:");
					Logger.W("HTTPRequest",xmlFromHTTP);
				}
    			mHTTPHandler.OnReceiveHTTPResp(Key,xmlFromHTTP); 
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			Logger.W("OnReceiveHTTPResp",""+e.getMessage());
    			Logger.W("OnReceiveHTTPResp",""+e.getCause());
    			
    			try
    			{
    				mHTTPHandler.OnHandleError(RequestFileType, Key, HTTPErrorType.APPException, e);
    			}
    			catch(Exception e2)
        		{
    				
        		}
    		}    		
    	}
    	
    	private void OnReceiveHTTPResp(Bitmap imageFromHTTP)
    	{
    		try
    		{
    			mHTTPHandler.OnReceiveHTTPResp(Key,imageFromHTTP);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			Logger.W("OnReceiveHTTPResp",""+e.getMessage());
    			Logger.W("OnReceiveHTTPResp",""+e.getCause());
    			
    			try
    			{
    				mHTTPHandler.OnHandleError(RequestFileType, Key, HTTPErrorType.APPException, e);
    			}
    			catch(Exception e2)
        		{
    				
        		}
    		}  
    	}

    	private void OnHandleError(HTTPErrorType errorType, Exception e)
    	{
    		try
    		{
    			mHTTPHandler.OnHandleError(RequestFileType,Key,errorType,e);
    		}
    		catch(Exception e2)
    		{
    			e.printStackTrace();
    			Logger.W("OnHandleError",""+e.getMessage());
    			Logger.W("OnHandleError",""+e.getCause());
    			
    			try
    			{
    				mHTTPHandler.OnHandleError(RequestFileType, Key, HTTPErrorType.APPException, e);
    			}
    			catch(Exception e3)
        		{
    				
        		}
    		}  
    	}
    	
    	 @Override  
         protected void onProgressUpdate(Integer... progress) {	
    		 try
    		 {
    			 mHTTPHandler.OnProgressUpdate(Key, progress);
    		 }
     		catch(Exception e)
     		{
     			e.printStackTrace();
     			Logger.W("onProgressUpdate",""+e.getMessage());
     			Logger.W("onProgressUpdate",""+e.getCause());
     			
     			try
     			{
     				mHTTPHandler.OnHandleError(RequestFileType, Key, HTTPErrorType.APPException, e);
     			}
     			catch(Exception e2)
         		{
     				
         		}
     		}  
    	 }

			    	
    	@Override
    	protected void onPreExecute()
    	{
    		try
    		{
    		OnPreHTTPRequest(Key);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			Logger.W("onPreExecute",""+e.getMessage());
    			Logger.W("onPreExecute",""+e.getCause());
    			
    			try
    			{
    				mHTTPHandler.OnHandleError(RequestFileType, Key, HTTPErrorType.APPException, e);
    			}
    			catch(Exception e2)
        		{
    				
        		}
    		}  
    	}
    	
        @Override
        protected String doInBackground(String... httpparams) {
        	
        	ErrorType=HTTPErrorType.NoError;
        	HTTPError=null;	
        	String url=httpparams[0];
           try {           
            		
                if(RequestFileType==HTTPRequest.RequestFileType.XML
                		||RequestFileType==HTTPRequest.RequestFileType.Text)
                {
                	
                	HTTPResponse response = downloadUrl(url);
                	if(response.HTTPState==HttpURLConnection.HTTP_OK)		                		
                	{
                		HTTPContent=response.HTTPContent;    
                		
                		if(mSupportLocalCache)
                		{
                			if(!HTTPContent.equals("0"))
            				{
            					String filename=url.replace(":", "").replace("/","").replace("?", "")+".xml";
            					mTextFileCache.WriteFile(Key, filename, HTTPContent);
            				}
                		}
                	}
                	else
                	{
                		switch(response.HTTPState)
                		{
                			case HttpURLConnection.HTTP_INTERNAL_ERROR:
                				ErrorType=HTTPErrorType.ServerError;
                				break;
                			default:
                				ErrorType=HTTPErrorType.PageCannotRead;    
                				break;
                		}
                	}
                }
                else if(RequestFileType==HTTPRequest.RequestFileType.Image)
                {
                	byte[] imageData = downloadImage(httpparams[0]);
                	if(imageData!=null&&imageData.length>0)
                		NetworkImage=ProcessImage(imageData);
                	else
                	{
                		ErrorType=HTTPErrorType.PageCannotRead;	
                		Log.w("Image Loading Error","PageCannotRead");
                	}
                }  
                else if(RequestFileType==HTTPRequest.RequestFileType.File)
                {
                	byte[] fileData = downloadImage(httpparams[0]);
                	if(fileData!=null&&fileData.length>0)
                	{
                		FileService objFileService=new FileService(UIContext);
                		HTTPFileName=httpparams[0].substring(httpparams[0].lastIndexOf("/")+1);
                		if(bAutoSaveToSDCard==true)
                      		objFileService.saveFileToSDCard(HTTPFileName, fileData);
                		else 
                			objFileService.saveFileToFileCache(HTTPFileName, fileData);
                	}
                	else
                	{
                		ErrorType=HTTPErrorType.PageCannotRead;	
                		Log.w("File Loading Error","PageCannotRead");
                	}
                }  	
            } catch (IOException ioe) {
            	
            	ErrorType=HTTPErrorType.RequestTimeout;
            	HTTPError=ioe;	                		
            	
            
        	} catch (Exception e) {
        		ErrorType=HTTPErrorType.ServerError;
            	HTTPError=e;	  
        	} finally {
        		
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        	if(ErrorType==HTTPErrorType.NoError)
        	{
        		if(this.isCancelled())
        		{
        			OnHandleError(HTTPErrorType.Canceled, null);
        			return;
        		}
        		
	        	if(RequestFileType==HTTPRequest.RequestFileType.XML
	            		||RequestFileType==HTTPRequest.RequestFileType.Text)
	        	{
	        		OnReceiveHTTPResp( HTTPContent);
	        	}
	        	else if(RequestFileType==HTTPRequest.RequestFileType.Image)
	        	{
	        		OnReceiveHTTPResp( NetworkImage);
	        	}
	        	else if(RequestFileType==HTTPRequest.RequestFileType.File)
	        	{
	        		OnReceiveHTTPResp( HTTPFileName);
	        	}
        	}
        	else
        	{
        		OnHandleError(ErrorType, HTTPError);
        	}
        }
        
        // Given a string representation of a URL, sets up a connection and gets
        // an input stream.
        private HTTPResponse downloadUrl(String urlString) throws IOException {
        	HTTPResponse objHTTPResponse=new HTTPResponse();
        			
            try
            {
            	boolean bHasInputs = (FromParams != null&&FromParams.size()>0)||
            			(SubFromParams!=null&&SubFromParams.size()>0);
	        	URL url = new URL(urlString);
	            HttpURLConnection HTTPConnection = (HttpURLConnection) url.openConnection();
	            HTTPConnection.setReadTimeout(20000 /* milliseconds */);
	            HTTPConnection.setConnectTimeout(8000 /* milliseconds */);
	            
	            // Allow Inputs & Outputs  
	            if(bHasInputs)
	            {
		            HTTPConnection.setDoInput(true);  
		            HTTPConnection.setDoOutput(true);  
		            HTTPConnection.setUseCaches(false);  
		            
		            HTTPConnection.setRequestMethod("POST");
		            HTTPConnection.setRequestProperty("Connection", "Keep-Alive");  
		            HTTPConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  
		            HTTPConnection.setRequestProperty("Charset", "UTF-8");  
		            
		            if(HTTPCookie!=null && HTTPCookie.length()>0){      
		            	HTTPConnection.setRequestProperty("Cookie", HTTPCookie);                              
		            }
		            
		            HTTPConnection.connect();
		            DataOutputStream HTTPOutputStream = new DataOutputStream(HTTPConnection.getOutputStream()); 
		            
		            if(SubFromParams != null&&SubFromParams.size()>0)
		            {
		            	Set<String> keys = SubFromParams.keySet(); 
			            boolean bIsFirstParam=true;
		                for (Iterator<String> it = keys.iterator(); it.hasNext();) {  
		                    String key = it.next();  
		                    String value = SubFromParams.get(key);  
		                    
		                    if(bIsFirstParam==true)
		                    {
		                    	bIsFirstParam=false;
		                    }
		                    else
		                    {
		                    	HTTPOutputStream.writeBytes("&"); 
		                    }
		                    HTTPOutputStream.writeBytes(key);  
		                    HTTPOutputStream.writeBytes("=");  
		                    HTTPOutputStream.writeBytes(URLEncoder.encode(value,"utf-8"));  
		                }  
		            }
		            else if(FromParams != null&&FromParams.size()>0)
		            {
			            Set<String> keys = FromParams.keySet(); 
			            boolean bIsFirstParam=true;
		                for (Iterator<String> it = keys.iterator(); it.hasNext();) {  
		                    String key = it.next();  
		                    String value = FromParams.get(key);  
		                    
		                    if(bIsFirstParam==true)
		                    {
		                    	bIsFirstParam=false;
		                    }
		                    else
		                    {
		                    	HTTPOutputStream.writeBytes("&"); 
		                    }
		                    HTTPOutputStream.writeBytes(key);  
		                    HTTPOutputStream.writeBytes("=");  
		                    HTTPOutputStream.writeBytes(URLEncoder.encode(value,"utf-8"));  
		                }  
		            }
	                
	                HTTPOutputStream.flush();
	                HTTPOutputStream.close();  
	                FromParams.clear();
	            }
	            else
	            {
	            	HTTPConnection.setRequestMethod("GET");
	            	
	            	 if(HTTPCookie!=null && HTTPCookie.length()>0){      
			            	HTTPConnection.setRequestProperty("Cookie", HTTPCookie);                              
			         }
	            }
	            // Starts the query
	            //HTTPConnection.connect();
	            objHTTPResponse.HTTPState=HTTPConnection.getResponseCode();
		        if (objHTTPResponse.HTTPState == HttpURLConnection.HTTP_OK) {

		        	String cookie = HTTPConnection.getHeaderField("Set-Cookie"); 
		        	
		        	if(cookie!=null && cookie.length()>0){   
		        		HTTPCookie = cookie;   
		        	}
		        	
		        	InputStream stream = HTTPConnection.getInputStream();
		        	objHTTPResponse.HTTPContent = ReceiveText(stream);
		            
		            stream.close();
		            HTTPConnection.disconnect();
		            stream=null;
		            return objHTTPResponse;
		        }
            } catch (Exception e) {
            	HTTPError=e;	
        	} 
            return objHTTPResponse;
        }
//
//        private Bitmap  downloadImageEx(String urlString) throws IOException
//        {
//        	 HttpGet httpRequest = new HttpGet(urlString);
//             HttpClient httpclient = new DefaultHttpClient();
//             HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
//             HttpEntity entity = response.getEntity();
//             BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
//             InputStream is = bufferedHttpEntity.getContent();
//
//             Bitmap bitmap = BitmapFactory.decodeStream(is);
//             return bitmap;
//        }
        
        private byte[] downloadImage(String urlString) throws IOException {
        	byte[] imgData=null;
            URL url = new URL(urlString);
            HttpURLConnection HTTPConnection = (HttpURLConnection) url.openConnection();
            HTTPConnection.setReadTimeout(300000 /* milliseconds */);
            HTTPConnection.setConnectTimeout(10000 /* milliseconds */);
            HTTPConnection.setRequestMethod("GET");
            //HTTPConnection.setDoInput(true);
      
            // Starts the query
            HTTPConnection.connect();
            InputStream stream = HTTPConnection.getInputStream();
            
            int length = (int) HTTPConnection.getContentLength();

            if (length != -1) {
            	imgData = new byte[length];
            	byte[] temp=new byte[1024*2];
                int readLen=0;
                int destPos=0;
                int progress=0;
                while((readLen=stream.read(temp))>0){
                  System.arraycopy(temp, 0, imgData, destPos, readLen);
              	  destPos+=readLen;
              	  progress = (int) ((length * 100) / destPos);
              	  this.publishProgress(progress,destPos,length);
                }
            }
            
            stream.close();
            HTTPConnection.disconnect();
            stream=null;            
           
            return imgData;
        }
        
        private String ReceiveText(InputStream HTTPInputStream) throws UnsupportedEncodingException,IOException
        {
        	
        	int ch;  
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();  
            
            while((ch = HTTPInputStream.read()) != -1)  
            {  
            	bytestream.write(ch);
            }  

            String xml = new String(bytestream.toByteArray(),"utf-8");  
            bytestream.close();
            return xml;
        }
        
        private Bitmap ProcessImage(byte[] imageData) throws UnsupportedEncodingException,IOException
        {
        	Bitmap bitmap = BitmapFactory.decodeByteArray(imageData,0,imageData.length);
            return bitmap;
        }
    }
    
    public interface HTTPConnectionListener {
    	public void OnPreHTTPRequest(Object key);
    	public void OnReceiveHTTPResp(Object key,String xmlFromHTTP);
    	public void OnReceiveHTTPResp(Object key,Bitmap imageFromHTTP);
    	public void OnProgressUpdate(Object key,Integer... progress);
    	public void OnHandleError(RequestFileType requestFileType,Object key,HTTPErrorType errorType,Exception e);	
    	public void OnNetworkStateChange(NetworkState state);	
    }
    
    public class HTTPResponse
    {
    	public int HTTPState=0;
    	public String HTTPContent="";
    }



	@Override
	public void OnPoseWritingFile(Object key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnPoseReadingFile(Object key, String xmlFromCache) {				
		if(xmlFromCache==null)				
			xmlFromCache="0";
		mHTTPHandler.OnReceiveHTTPResp(key,xmlFromCache);		
	}	
}