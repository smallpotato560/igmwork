package com.bcfbaselibrary.string;

import android.text.TextUtils;

public class StringHelper {
	public static String EncodeHex(int integer) { 

		String hex=Integer.toHexString(integer).toUpperCase();
		if(hex.length()==1)
		{
			hex="0"+hex;
		}
        return hex;
    }
	
	public static String GetValue(String objTry,String objDefault)
	{
		if(!TextUtils.isEmpty(objTry)
				&&!objTry.startsWith("0000-00-00"))
		{
			return objTry;
		}
		
		return objDefault;
	}
}
