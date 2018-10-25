package com.apps.igmwork.common.data;

import android.content.Context;

import com.bcfbaselibrary.data.SQLiteHelper;

/**
 * Created by Ben on 2018/1/29.
 */

public class OnlineMusicDataHelper extends SQLiteHelper {

    //public static final int Done=1;

    public OnlineMusicDataHelper(Context context) {
        super(context);

    }

    @Override
    protected String GetDatabaseName()
    {
        return OnlineMusicDataHelper.class.getSimpleName();
    }

    @Override
    protected String GetTableName()
    {
        return "OnlineMusic";
    }

    @Override
    protected int GetTableVersion()
    {
        return 1;
    }

    @Override
    protected void Init() {
        AddColumn("ResourceID", "INTEGER");
        AddColumn("SongID", "INT");
        AddColumn("Duration", "INT");
        AddColumn("Artist", "NVARCHAR(200)");
        AddColumn("Album", "NVARCHAR(200)");
        AddColumn("SongName", "NVARCHAR(200)");
        AddColumn("Lyric", "NVARCHAR(5000)");
        AddColumn("CoverImageURL", "VARCHAR(2000)");
        AddColumn("URL", "VARCHAR(2000)");

        AddIndex("ResourceID");
    }

}