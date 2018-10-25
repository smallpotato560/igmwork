package com.bcfbaselibrary.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.util.Log;

public class Logger {
	final static public int Debug=1;
	
	public static void W(final String tag,final String msg)
	{
		if(Debug==1)
		{
			try
			{
				Log.w(tag,msg);
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	public static void E(final String tag,final String msg)
	{
		if(Debug==1)
		{
			try
			{
				Log.e(tag,msg);
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	public static void I(final String tag,final String msg)
	{
		if(Debug==1)
		{
			try
			{
				Log.i(tag,msg);
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	public static String FormatStackTrace(Throwable throwable) {   
	    if(throwable==null) return "";   
	    String rtn = throwable.getStackTrace().toString();   
	    try {   
	        Writer writer = new StringWriter();   
	        PrintWriter printWriter = new PrintWriter(writer);   
	        throwable.printStackTrace(printWriter);        
	        printWriter.flush();   
	        writer.flush();   
	        rtn = writer.toString();   
	        printWriter.close();               
	        writer.close();   
	    } catch (IOException e) {   
	        e.printStackTrace();   
	    } catch (Exception ex) {   
	    }   
	    return rtn;   
	}  

	
}