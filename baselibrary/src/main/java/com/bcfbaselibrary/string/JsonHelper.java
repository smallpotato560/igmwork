package com.bcfbaselibrary.string;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.os.Bundle;

public class JsonHelper {
	
	public static ContentValues ConvertToContentValues(JSONObject jsonItem)
	{
		
		JSONArray nameList=jsonItem.names();
		
		ContentValues objContentValues=new ContentValues();
		for(int j=0;j<nameList.length();j++)
		{
			String name;
			try {
				name = nameList.get(j).toString();
				if(jsonItem.has(name))
				{
					if(jsonItem.get(name).getClass()==Long.TYPE)
					{
						objContentValues.put(name,jsonItem.getLong(name));
					}
					else if(jsonItem.get(name).getClass()==Integer.TYPE)
					{
						objContentValues.put(name,jsonItem.getInt(name));
					}
					else if(jsonItem.get(name).getClass()==Boolean.TYPE)
					{
						objContentValues.put(name,jsonItem.getBoolean(name));
					}
					else {
						objContentValues.put(name, jsonItem.get(name).toString());
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				objContentValues=null;
			}
			
		}
		
		return objContentValues;
	}
	
	public static ArrayList<ContentValues> ConvertToContentList(JSONArray jsonArray)
	{
		if(jsonArray==null)
			return null;

		ArrayList<ContentValues> newList=new ArrayList<ContentValues>();
		
		for(int i=0;i<jsonArray.length();i++)
		{
			try {
				JSONObject jsonItem=jsonArray.getJSONObject(i);
				
				ContentValues objContentValues=ConvertToContentValues(jsonItem);
				if(objContentValues!=null)
				{
					newList.add(objContentValues);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return newList;
	}

}
