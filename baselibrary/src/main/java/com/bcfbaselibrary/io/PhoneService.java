package com.bcfbaselibrary.io;

import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.bcfbaselibrary.internal.Logger;
import com.bcfbaselibrary.security.MD5Util;
import com.bcfbaselibrary.string.StringHelper;

public class PhoneService {
    public final static String SMS_SEND_ACTION = "SMS_SENT";
    //public final static String SMS_DELIVERED_ACTION = "SMS_DELIVERED_ACTION";  
    public final static String SMS_DELIVERED_ACTION = "SMS_DELIVERED";
    private final static String SECURITY_KEY="APPw7cvkd+Bym21ly";

    public String AndroidID="";
    public String DeviceTaken="";
    public String MobilePhoneNumber="";
    //International Mobile Equipment Identity
    public String IMEI="";
    //International Mobile SubscriberIdentification Number
    public String IMSI="";
    public String SIMOperator="";
    public String SIMOperatorName="";
    public String DeviceID="";

    private Context mContext;
    private boolean bNeedSentReport=false;
    private boolean bNeedDeliveryReport=false;

    public PhoneService(Context context)
    {
        mContext=context;
        LoadPhoneState();

    }

    public void RegisterMTDeliveryReport(BroadcastReceiver sentReceiver,BroadcastReceiver deviveryReceiver)
    {
        if(sentReceiver!=null)
        {
            IntentFilter sendFilter = new IntentFilter();
            sendFilter.addAction(SMS_SEND_ACTION);
            mContext.registerReceiver(sentReceiver, sendFilter);
            bNeedSentReport=true;
        }
        if(deviveryReceiver!=null)
        {
            IntentFilter deliverFilter = new IntentFilter();
            deliverFilter.addAction(SMS_DELIVERED_ACTION);
            mContext.registerReceiver(deviveryReceiver, deliverFilter);
            bNeedDeliveryReport=true;
        }
    }

    public void SendMO(String shortcode,String message)
    {

        MOHandler objMTHandler=new MOHandler(this.mContext);
        objMTHandler.bNeedSentReport=bNeedSentReport;
        objMTHandler.bNeedDeliveryReport=bNeedDeliveryReport;
        objMTHandler.mShortcode=shortcode;
        objMTHandler.mMessage=message;
        new Thread(objMTHandler).start();

    }

    public class MOHandler implements Runnable {

        public boolean bNeedSentReport=false;
        public boolean bNeedDeliveryReport=false;
        public String mShortcode;
        public String mMessage;

        private Context mContext;

        public MOHandler(Context context)
        {
            mContext=context;

        }

        private void SendSMS()
        {
            SmsManager smsManager = SmsManager.getDefault();


            PendingIntent sentPI  = null;

            if(bNeedSentReport)
            {
                sentPI=PendingIntent.getBroadcast(mContext, 0,
                        new Intent(SMS_SEND_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
            }

            PendingIntent deliverPI = null;
            if(bNeedDeliveryReport)
            {
                Intent deliverIntent = new Intent(SMS_DELIVERED_ACTION);
                deliverPI = PendingIntent.getBroadcast(mContext, 0,
                        deliverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            smsManager.sendTextMessage(mShortcode, null, mMessage, sentPI , deliverPI);
            //Log.w("SendSMS", "to:"+mShortcode);
            //Log.w("SendSMS", "msg:"+mMessage);
        }

        @Override
        public void run() {
            SendSMS();

        }
    }

    public String GetDeviceTaken()
    {
        if(!TextUtils.isEmpty(DeviceID))
        {
            DeviceTaken=DeviceID;
        }
        else if(!TextUtils.isEmpty(AndroidID)
                &&(!"9774d56d682e549c".equals(AndroidID)))
        {
            DeviceTaken=AndroidID;
        }
        else if(!TextUtils.isEmpty(IMSI))
        {
            DeviceTaken=IMSI;
        }
        else
        {
            DeviceTaken="09" +
                    Build.BOARD.length()%10+ Build.BRAND.length()%10 + Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 + Build.DISPLAY.length()%10 + Build.HOST.length()%10 + Build.ID.length()%10 + Build.MANUFACTURER.length()%10 + Build.MODEL.length()%10 + Build.PRODUCT.length()%10 + Build.TAGS.length()%10 + Build.TYPE.length()%10 + Build.USER.length()%10 ;;
        }

        return MD5Util.MD5("APP@"+DeviceTaken);
    }

    public String GetDeviceOpenKey()
    {
        String deviceTaken=GetDeviceTaken();
        return MD5Util.MD5(SECURITY_KEY+deviceTaken);
    }

    public static String GetLBSInfo(Context content)
    {

        try
        {
            int mcc=-1;
            int mnc=-1;
            StringBuffer lbsInfo=new StringBuffer();

            TelephonyManager mTelephonyManager = (TelephonyManager)
                    content.getSystemService(Context.TELEPHONY_SERVICE);



            CellLocation objCellLocation =mTelephonyManager.getCellLocation();

            if(objCellLocation instanceof GsmCellLocation)
            {

                String operator = mTelephonyManager.getNetworkOperator();
                mcc = Integer.parseInt(operator.substring(0, 3));
                mnc = Integer.parseInt(operator.substring(3));

                // 中国移动和中国联通获取LAC、CID的方式
                GsmCellLocation location = (GsmCellLocation) objCellLocation;

                int lac = location.getLac();
                int cellId = location.getCid();


                if(mcc>=0&&mnc>=0&&lac>=0&&cellId>=0)
                {
                    lbsInfo.append(String.format("%s-%s-%s-%s-FF",
                            StringHelper.EncodeHex(mcc),
                            StringHelper.EncodeHex(mnc),
                            StringHelper.EncodeHex(lac),
                            StringHelper.EncodeHex(cellId)));
                }

                //String log=" MCC = " + mcc + "\t MNC = " + mnc + "\t LAC = " + lac + "\t CID = " + cellId;
                //Logger.E("LBS GSM", log);

                // 获取邻区基站信息
	            /*
	            List<NeighboringCellInfo> infos = mTelephonyManager.getNeighboringCellInfo();
	            for (NeighboringCellInfo info1 : infos) { // 根据邻区总数进行循环
	            	
	            	lbsInfo.append(String.format("%s-%s-%s-%s-%s", 
	            			StringHelper.EncodeHex(mcc),
	            			StringHelper.EncodeHex(mnc),
	            			StringHelper.EncodeHex(info1.getLac()),
	            			StringHelper.EncodeHex(info1.getCid()),
	            			StringHelper.EncodeHex((-113 + 2 * info1.getRssi())+220)));
	            	
	            }
	            */
            }
            else
            {
                String operator = mTelephonyManager.getNetworkOperator();
                if(operator!=null&&operator.length()>=5){
                    mcc = Integer.parseInt(operator.substring(0, 3));
                    mnc = Integer.parseInt(operator.substring(3));
                }

                // 中国电信获取LAC、CID的方式
                CdmaCellLocation cdma_location = (CdmaCellLocation) objCellLocation;

                int lac = cdma_location.getNetworkId();
                int cellId = cdma_location.getBaseStationId();

                if(mcc>=0&&mnc>=0&&lac>=0&&cellId>=0)
                {
                    lbsInfo.append(String.format("%s-%s-%s-%s-FF",
                            StringHelper.EncodeHex(mcc),
                            StringHelper.EncodeHex(mnc),
                            StringHelper.EncodeHex(lac),
                            StringHelper.EncodeHex(cellId)));
                }

                //String log=" MCC = " + mcc + "\t MNC = " + mnc + "\t LAC = " + lac + "\t CID = " + cellId;
                //Logger.E("LBS CDMA", log);

            }

            return lbsInfo.toString();
        }
        catch(Exception e)
        {
            return "";
        }
    }

    private int LoadPhoneState()
    {
        int nResult=1;
        TelephonyManager objTelephonyManager = null;
        DeviceID="";
        MobilePhoneNumber="";
        IMEI="";
        IMSI="";

        try
        {
            objTelephonyManager =(TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        }
        catch(Exception e)
        {
            objTelephonyManager=null;
        }
        if(objTelephonyManager!=null)
        {
            try
            {
                DeviceID = objTelephonyManager.getDeviceId();
            }
            catch(Exception e)
            {
                nResult=0;
            }

            try
            {
                AndroidID = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
            }
            catch(Exception e)
            {
                AndroidID=null;
            }
            try
            {
                MobilePhoneNumber = objTelephonyManager.getLine1Number();
            }
            catch(Exception e)
            {
                nResult=0;
            }
            try
            {
                IMEI = objTelephonyManager.getDeviceId();
            }
            catch(Exception e)
            {
                nResult=0;
            }
            try
            {
                IMSI = objTelephonyManager.getSubscriberId();
            }
            catch(Exception e)
            {
                nResult=0;
            }

            try
            {
                SIMOperator = objTelephonyManager.getSimOperator();
            }
            catch(Exception e)
            {
                nResult=0;
            }

            try
            {
                SIMOperatorName = objTelephonyManager.getSimOperatorName();
            }
            catch(Exception e)
            {
                nResult=0;
            }
        }

        return nResult;
    }
}
