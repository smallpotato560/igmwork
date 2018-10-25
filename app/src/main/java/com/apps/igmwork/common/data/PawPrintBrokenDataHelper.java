package com.apps.igmwork.common.data;

import android.content.Context;

import com.bcfbaselibrary.data.SQLiteHelper;

/**
 * Created by Ben on 2018/1/29.
 */

public class PawPrintBrokenDataHelper extends SQLiteHelper {

    //public static final int Done=1;

    public PawPrintBrokenDataHelper(Context context) {
        super(context);

    }

    @Override
    protected String GetDatabaseName()
    {
        return PawPrintBrokenDataHelper.class.getSimpleName();
    }

    @Override
    protected String GetTableName()
    {
        return "PawPrintBroken";
    }

    @Override
    protected int GetTableVersion()
    {
        return 1;
    }

    @Override
    protected void Init() {
        AddColumn("UniqueID", "INTEGER");
        AddColumn("ObjectID", "INT");
        AddColumn("GameID", "INT");

        AddIndex("UniqueID");
        AddIndex("ObjectID");
    }

}