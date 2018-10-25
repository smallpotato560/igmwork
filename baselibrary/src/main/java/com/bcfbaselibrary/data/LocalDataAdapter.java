package com.bcfbaselibrary.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LocalDataAdapter {
	public static String TAG="LocalDataAdapterData";
	private static HashMap<String, String> BufferedDataMap = new HashMap<String, String>();
	private static HashMap<String, String> EditedDataMap = new HashMap<String, String>();

	
	public static String GetString(Context objContext,String paramName,String defValue)
	{
		if(BufferedDataMap.containsKey(paramName))
		{
			return BufferedDataMap.get(paramName);
		}
		
		SharedPreferences LocalDataPreferences = objContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		String paramValue = LocalDataPreferences.getString(paramName, defValue);
		
		if(paramValue.length()>0)
		{
			BufferedDataMap.put(paramName, paramValue);
		}
		return paramValue;
	}
	
	public static String GetString(Context objContext,String paramName)
	{
		return GetString(objContext,paramName,"");
	}
	
	public static int GetInt(Context objContext,String paramName,int defaultValue)
	{
		if(BufferedDataMap.containsKey(paramName))
		{
			return Integer.valueOf(BufferedDataMap.get(paramName));
		}
		
		SharedPreferences LocalDataPreferences = objContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		int paramValue = defaultValue;
		
		try
		{
			paramValue=Integer.valueOf(LocalDataPreferences.getString(paramName, ""+defaultValue));
		}
		catch(Exception e)
		{
			
		}
		
		BufferedDataMap.put(paramName, paramValue+"");
		return paramValue;
	}
	
	public static long GetLong(Context objContext,String paramName,long defaultValue)
	{
		if(BufferedDataMap.containsKey(paramName))
		{
			return Long.valueOf(BufferedDataMap.get(paramName));
		}
		
		SharedPreferences LocalDataPreferences = objContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		long paramValue = defaultValue;
		
		try
		{
			paramValue=Long.valueOf(LocalDataPreferences.getString(paramName, ""+defaultValue));
		}
		catch(Exception e)
		{
			
		}
		
		BufferedDataMap.put(paramName, paramValue+"");
		return paramValue;
	}
	
	public static void SetParameterToBuffer(String paramName,Object paramValue)
	{
		BufferedDataMap.put(paramName, paramValue.toString());
		EditedDataMap.put(paramName, paramValue.toString());
	}
	
	public static void CommitChanges(Context objContext)
	{
		SharedPreferences LocalDataPreferences = objContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		Editor editor = LocalDataPreferences.edit();
		Set<Entry<String, String>>   entrySet=EditedDataMap.entrySet(); 
		for   (Iterator<Entry<String, String>> iterator = entrySet.iterator();iterator.hasNext();) 
		{ 
			Entry<String, String> entry = (Entry<String, String>) iterator.next();
   	     	editor.putString(entry.getKey(), entry.getValue());			
		}
		editor.commit();
		EditedDataMap.clear();
	}
	
	public static void ClearBufferredData()
	{		
		BufferedDataMap.clear();
		EditedDataMap.clear();
	}
}
