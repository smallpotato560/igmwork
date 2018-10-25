package com.apps.igmwork.common;

public class APPGlobal {
    //玩家角色类型: 0 - 一般骑士玩家 1 - 狐狸玩家
    public final static int Role_Hunter = 0;
    public final static int Role_Fox = 1;

    //地图对象类型
    //0 general user 1 fox 2 fox's pawprint
    public final static int ObjectType_Hunter = 0;
    public final static int ObjectType_Fox = 1;
    public final static int ObjectType_Fox_Pawprint = 2;

    //数据状态
    public final static int Response_status_OK = 1;
    public final static int Response_status_Failed = 0;
    public final static int Response_status_UnknownError = -1;

    //数据库记录状态
    public final static int DB_Status_OK = 0;

    //Message Feed消息种类
    public final static int Message_NewPlayer = 1001;
    public final static int Message_NewPawPrint = 1002;

    public final static int Message_GameEnd = 1003;
    public final static int Message_GameStart = 1004;
    public final static int Message_GameReset = 1005;

    //Message Feed消息所属类型
    public final static int Message_Type_User = 0;
    public final static int Message_Type_System = 1;

    //游戏状态: 0 未开始 1 已开始 2 已结束 -1 未加入
    public final static int Game_Status_Unready = -1;
    public final static int Game_Status_Ready = 0;
    public final static int Game_Status_Start = 1;
    public final static int Game_Status_End = 2;

    public final static int Game_Status_End_With_Caught_Fox = 3;

    //游戏玩家状态
    public final static int Game_Player_Status_Unready = -1;
    public final static int Game_Player_Status_Ready = 0;
    public final static int Game_Player_Status_Finish = 1;
    public final static int Game_Player_Status_CaughtFox = 2;

    //设置参数
    public final static int Default_Page_Size = 12;
    public final static int ErrorCode_FoxExists = -1001;

    //配置参数
    public class Config {
        //脚印打卡范围，单位：米
        public final static int Range_PawPrint_CheckIn = 5;
        public final static int Range_End_Point = 10;

        //位置更新Timer刷新时间，单位：毫秒
        public final static long Duration_Location_Timer=2000l;

    }

}

