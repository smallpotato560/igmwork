package com.bcfbaselibrary.string;

public class HTMLCoding {
	public static String HTMLEncode(String value)
	{
		value=value.replace("\"","&quot;");
		value=value.replace("<","&lt;");
		value=value.replace(">","&gt;");
		value=value.replace(" ","&nbsp;");
		value=value.replace("\n","<br>");
		String result=value.replace("\r","&nbsp;&nbsp;&nbsp;&nbsp;");		
		
		return result;
	}
	
	public static String HTMLDecode(String value)
	{
		value=value.replace("&quot;","\"");
		value=value.replace("&lt;","<");
		value=value.replace("&gt;",">");
		value=value.replace("&nbsp;"," ");
		String result=value.replace("<br>","\n");

		return result;
	}
}
