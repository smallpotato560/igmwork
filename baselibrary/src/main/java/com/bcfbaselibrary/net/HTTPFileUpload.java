package com.bcfbaselibrary.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

import com.bcfbaselibrary.string.HTMLCoding;

public class HTTPFileUpload extends AsyncTask<Object, Integer, String> {

    private UploadProgressListener mUploadProgressHandler = null;
    private String PostURL;
    private Map<String,String> FromParams;

    private String TwoHyphens ="--";
    private String HTMLBoundary = "------7DUploadFromEChatooxxlutestzzZZzz";
    private String LineEnd = "\r\n";

    //private byte[] FileBuffer;



    HttpURLConnection HTTPConnection = null;
    DataOutputStream HTTPOutputStream = null;
    InputStream HTTPInputStream = null;

    InputStream DataPostedInputSteam = null;
    private String Filename = "";

    //private String filePath = "/mnt/sdcard/Pictures/1.mp4";  
    //private String pathToOurFile = "testing2.jpg";

    //File uploadFile = new File(filePath);
    long TotalSize = 0; // Get size of file, bytes  

    public HTTPFileUpload(UploadProgressListener objUploadProgressHandler){
        mUploadProgressHandler=objUploadProgressHandler;
    }

    public void SetFileStream(String filePath)
    {
        File uploadFile = new File(filePath);
        Filename = uploadFile.getName();
        TotalSize = uploadFile.length(); // Get size of file, bytes  

        try {
            DataPostedInputSteam = new FileInputStream(uploadFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void SetBufferStream(String fileName,InputStream bufferStream)
    {
        Filename=fileName;
        DataPostedInputSteam = bufferStream;
        try {
            TotalSize=Long.valueOf(bufferStream.available());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void AddFormParam(String paramName,String paramValue)
    {
        if(FromParams==null)
        {
            FromParams=new HashMap<String,String>();
        }

        FromParams.put(paramName, paramValue);
    }

    @Override
    protected String doInBackground(Object... arg0) {
        PostURL=arg0[0].toString();

        String result = "0";
        long length = 0;
        int progress;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 10 * 1024;// 100KB  

        try {
            URL url = new URL(PostURL);
            HTTPConnection = (HttpURLConnection) url.openConnection();

            // Set size of every block for post  
            HTTPConnection.setChunkedStreamingMode(maxBufferSize);// 128KB  

            // Allow Inputs & Outputs  
            HTTPConnection.setDoInput(true);
            HTTPConnection.setDoOutput(true);
            HTTPConnection.setUseCaches(false);

            // Enable POST method  
            HTTPConnection.setRequestMethod("POST");
            HTTPConnection.setRequestProperty("Connection", "Keep-Alive");
            HTTPConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + HTMLBoundary);
            HTTPConnection.setRequestProperty("Charset", "UTF-8");

            HTTPConnection.connect();
            HTTPOutputStream = new DataOutputStream(HTTPConnection.getOutputStream());

            Log.w("Upload Log", "HTTPOutputStream opened");
            //write the form parameter to output stream  
            if(FromParams != null&&FromParams.size()>0){
                Set<String> keys = FromParams.keySet();
                for (Iterator<String> it = keys.iterator(); it.hasNext();) {
                    String key = it.next();
                    String value = FromParams.get(key);
                    HTTPOutputStream.writeBytes(TwoHyphens + HTMLBoundary + LineEnd);
                    HTTPOutputStream.writeBytes("Content-Disposition: form-data; "+
                            "name=\""+key+"\""+LineEnd);
                    HTTPOutputStream.writeBytes(LineEnd);

                    HTTPOutputStream.writeBytes(URLEncoder.encode(HTMLCoding.HTMLEncode(value),"utf-8"));
                    HTTPOutputStream.writeBytes(LineEnd);

                    Log.w("Log",key+":"+value);
                }

                FromParams.clear();
            }

            //write the file data  
            HTTPOutputStream.writeBytes(TwoHyphens + HTMLBoundary + LineEnd);
            HTTPOutputStream.writeBytes("Content-Disposition: form-data; "+
                    "name=\""+"File"+"\";filename=\""+
                    Filename +"\""+ LineEnd);
            HTTPOutputStream.writeBytes(LineEnd);

            Log.w("Log",Filename+":"+Filename);

            if(DataPostedInputSteam!=null)
            {
                bytesAvailable = DataPostedInputSteam.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);//设置每次写入的大小
                buffer = new byte[bufferSize];

                // Read input stream
                bytesRead = DataPostedInputSteam.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    HTTPOutputStream.write(buffer, 0, bufferSize);
                    length += bufferSize;
                    //Thread.sleep(500);
                    progress = (int) ((length * 100) / TotalSize);

                    Log.w("Log", "HTTPOutputStream Write:"+String.valueOf(bufferSize));
                    publishProgress(progress,(int)length);


                    bytesAvailable = DataPostedInputSteam.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);

                    bytesRead = DataPostedInputSteam.read(buffer, 0, bufferSize);
                }

                HTTPOutputStream.writeBytes(LineEnd);
                HTTPOutputStream.writeBytes(TwoHyphens + HTMLBoundary + TwoHyphens
                        + LineEnd);
                Log.w("Log", "HTTPOutputStream finsihed");
                DataPostedInputSteam.close();
            }
            //publishProgress(100,(int)length);  

            // Responses from the server (code and message)  
            /* 取得Response内容 */
            HTTPOutputStream.flush();
            HTTPOutputStream.close();
            HTTPInputStream = HTTPConnection.getInputStream();
            int ch;
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            while((ch = HTTPInputStream.read()) != -1)
            {
                bytestream.write(ch);
            }

            result = new String(bytestream.toByteArray(),"utf-8");
            bytestream.close();
            //Log.w("Log", "inputStream closed:"+result);
            HTTPInputStream.close();
            HTTPConnection.disconnect();


        } catch (Exception ex) {
            Log.e("FildUpload Error",ex.getMessage());

            mUploadProgressHandler.HandleError(ex.getMessage());
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        mUploadProgressHandler.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mUploadProgressHandler.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(String result) {
        mUploadProgressHandler.onPostExecute(result);
    }

    public interface UploadProgressListener {
        public void onPreExecute() ;
        public void onProgressUpdate(Integer... progress);
        public void onPostExecute(String result) ;
        public void HandleError(String errorMessage);
    }
}