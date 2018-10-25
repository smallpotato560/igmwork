package com.bcfbaselibrary.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bcfbaselibrary.internal.Logger;

public abstract class SQLiteHelper {
	
	protected Context mContext;
	private SQLiteDatabase mSQLiteDatabase = null;
	
	private DatabaseHelper mDatabaseHelper = null;  
	
	protected ArrayList<String> IndexCollection=new ArrayList<String>();
	
	protected ArrayList<String> ColumnNameCollection=new ArrayList<String>();
	protected ArrayList<String> ColumnTypeCollection=new ArrayList<String>();
		
	protected abstract String GetDatabaseName();
	
	protected abstract  String GetTableName();
	
	protected abstract int GetTableVersion();
	
	protected abstract void Init();
	 
	protected String GetCreationSql()
	{
		StringBuilder SqlBuilder=new StringBuilder();
		SqlBuilder.append("CREATE TABLE "+GetTableName());
		SqlBuilder.append(" (");
		
		for(int i=0;i<ColumnNameCollection.size();i++)
		{
			if(i>0)
			{
				SqlBuilder.append(",");
			}
			SqlBuilder.append(ColumnNameCollection.get(i));
			SqlBuilder.append(" "+ColumnTypeCollection.get(i));
			
			if(i==0)
			{
				SqlBuilder.append(" PRIMARY KEY");
			}
		}
		SqlBuilder.append(")");
		
		return SqlBuilder.toString();
	}
	
	
	
	public SQLiteHelper(Context context){
		Init();
        mContext = context;
	  }
	
	
	  public void Open() throws SQLException{
	
	        mDatabaseHelper = new DatabaseHelper(mContext,this);
		  try {
			  mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();

	  }catch (Exception e) {
			  Close();
			  mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
			  e.printStackTrace();
		  }
	  }
	
	
	  public void Close(){
		  try {
			  mDatabaseHelper.close();
		  }catch (Exception e){
			  e.printStackTrace();
		  }
	  }
	
	protected void AddColumn(String name,String type)
	{
		ColumnNameCollection.add(name);
		
		ColumnTypeCollection.add(type);
	}
	
	protected void AddIndex(String name)
	{
		AddIndex(name, "ASC");
	}
	
	protected void AddIndex(String name,String direction)
	{
		IndexCollection.add(name+" "+direction);
	}
	
	
	protected String[] GetColumnNames()
	{
		return ColumnNameCollection.toArray(new String[ColumnNameCollection.size()]); 
	}

	private static class DatabaseHelper extends SQLiteOpenHelper{

		SQLiteHelper mSQLiteHandler;
		/* create database */
		DatabaseHelper(Context context,SQLiteHelper objSQLiteHandler){
	
			
	     super(context, objSQLiteHandler.GetDatabaseName(), null, objSQLiteHandler.GetTableVersion());
			
			mSQLiteHandler=objSQLiteHandler;
		}
	
		@Override
		public void onCreate(SQLiteDatabase db){
		  //execute script to create batabase.
			db.execSQL(mSQLiteHandler.GetCreationSql());
			
			for(int i=0;i<mSQLiteHandler.IndexCollection.size();i++)
			{
				db.execSQL("CREATE INDEX "+mSQLiteHandler.GetTableName()+"_idx_"+i+" ON "+mSQLiteHandler.GetTableName()+" ("+mSQLiteHandler.IndexCollection.get(i)+")");
			}
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	
			for(int i=0;i<mSQLiteHandler.IndexCollection.size();i++)
			{
				db.execSQL("DROP INDEX IF EXISTS "+mSQLiteHandler.GetTableName()+"_idx_"+i);
			}
			
			db.execSQL("DROP TABLE IF EXISTS "+mSQLiteHandler.GetTableName());
			onCreate(db);
		}
   }
	
	  public long Insert(ContentValues initialValues){
        return mSQLiteDatabase.insert(GetTableName(), null, initialValues);
	  }
	  
	  public long AddByParams(Object... params)
	  {
		  ContentValues objContentValue =new ContentValues();
		  for(int i=0;i<params.length;i++)
		  {
			  int columnIndex=i+1;
			  
			  if(ColumnTypeCollection.get(columnIndex).startsWith("INT")||ColumnTypeCollection.get(columnIndex).startsWith("INTEGER") )
				{
					objContentValue.put(ColumnNameCollection.get(columnIndex), Integer.valueOf(params[i].toString()));
				}
				else if(ColumnTypeCollection.get(columnIndex).startsWith("VARCHAR")||ColumnTypeCollection.get(columnIndex).startsWith("NVARCHAR")
						||ColumnTypeCollection.get(columnIndex).startsWith("DATETIME"))
				{
					objContentValue.put(ColumnNameCollection.get(columnIndex), params[i].toString());
				}
				else if(ColumnTypeCollection.get(columnIndex).startsWith("FLOAT"))
				{
					objContentValue.put(ColumnNameCollection.get(columnIndex), Float.valueOf(params[i].toString()));
				}
				else if(ColumnTypeCollection.get(columnIndex).startsWith("LONG")||ColumnTypeCollection.get(columnIndex).startsWith("BIGINT"))
				{
					objContentValue.put(ColumnNameCollection.get(columnIndex), Long.valueOf(params[i].toString()));
				}
				else if(ColumnTypeCollection.get(columnIndex).startsWith("BOOLEAN"))
				{
					objContentValue.put(ColumnNameCollection.get(i+1), Integer.valueOf(params[i].toString()));
				}
		  }
		  
		  return Insert(objContentValue);
	  }
	
	  public boolean Delete(String columnName,Object columnValue){
	
		  if(columnValue.getClass()==String.class)
		  {
			  columnValue="'"+columnValue.toString().replace("'", "''")+"'";
		  }
	        return mSQLiteDatabase.delete(GetTableName(), columnName + "=" + columnValue, null) > 0;
	  }

	public boolean Delete(String whereSql)
	{
		return mSQLiteDatabase.delete(GetTableName(),whereSql, null) > 0;
	}

	public boolean Delete(String[] strings, Object[] columnValue){

		String where_sql="";
		for(int i=0;i<strings.length;i++) {
			String columnName=strings[i];
			Object objColumnValue=columnValue[i];
			if (objColumnValue.getClass() == String.class) {
				objColumnValue = "'" + objColumnValue.toString().replace("'", "''") + "'";
			}

			if(where_sql.length()>0)
			{
				where_sql+=" And ";
			}

			where_sql+=columnName + "=" + objColumnValue;
		}
		return mSQLiteDatabase.delete(GetTableName(), where_sql, null) > 0;
	}
	  
	  public ArrayList<ContentValues> Select(String[] strings, Object[] columnValue, int pageIndex, int pageSize, String orderBy) throws SQLException{
		  //return Select(0,0,null);


		  String where_sql="";
		  for(int i=0;i<strings.length;i++) {
			  String columnName=strings[i];
			  Object objColumnValue=columnValue[i];
			  if (objColumnValue.getClass() == String.class) {
				  objColumnValue = "'" + objColumnValue.toString().replace("'", "''") + "'";
			  }

			  if(where_sql.length()>0)
			  {
				  where_sql+=" And ";
			  }

			  where_sql+=columnName + "=" + objColumnValue;
		  }



		  return Select(where_sql,pageIndex,pageSize,orderBy);
	  }
	  
	  public ArrayList<ContentValues> Select(int pageIndex,int pageSize,String orderBy) throws SQLException{
		  if(pageSize>0)
		  {
			  if(orderBy==null)
			  {
				  orderBy=ColumnNameCollection.get(0);
			  }
			  int offset=(pageIndex-1)*pageSize;
			  orderBy+=" LIMIT "+pageSize+" OFFSET "+offset;
		  }
		  ArrayList<ContentValues> SelectResult=new ArrayList<ContentValues>();
	      Cursor mCursor = mSQLiteDatabase.query(true, GetTableName(), GetColumnNames(), null, null, null, null, orderBy, null);
	      
	      if(mCursor!=null)
			{
				while(mCursor.moveToNext())
				{			
					ContentValues objContentValue=new ContentValues();
					
					for(int i=0;i<ColumnNameCollection.size();i++)
					{
						if(ColumnTypeCollection.get(i).startsWith("INT")||ColumnTypeCollection.get(i).startsWith("INTEGER"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("VARCHAR")||ColumnTypeCollection.get(i).startsWith("NVARCHAR")
								||ColumnTypeCollection.get(i).startsWith("DATETIME"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getString(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("FLOAT"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getFloat(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("LONG")||ColumnTypeCollection.get(i).startsWith("BIGINT"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getLong(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("BOOLEAN"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
					}
					
					//Log.w("LoadAPPParams - GetData",""+appParam.ParamID+","+appParam.ParamValue);
					SelectResult.add(objContentValue);
				}
			}
	      mCursor.close();
	      return SelectResult;

	   }
	  
	  public ArrayList<ContentValues> Select(String columnName,Object columnValue)throws SQLException{
		  return Select(columnName,columnValue,0,0,null);
	  }
	  
	  public ArrayList<ContentValues> Select(String columnName,Object columnValue,String orderBy)throws SQLException{
		  return Select(columnName,columnValue,0,0,orderBy);
	  }
	  
	  public ArrayList<ContentValues> Select(String columnName,Object columnValue,int pageIndex,int pageSize,String orderBy)throws SQLException{
		  if(columnValue.getClass()==String.class)
		  {
			  columnValue="'"+columnValue.toString().replace("'", "''")+"'";
		  }
		  
		  String where_sql=columnName + "=" + columnValue;
		  
		  return Select(where_sql,pageIndex,pageSize,orderBy);
	  } 
	  
	  public ArrayList<ContentValues> Select(String where_sql,int pageIndex,int pageSize,String orderBy) throws SQLException{

		  if(pageSize>0)
		  {
			  if(orderBy==null)
			  {
				  orderBy=ColumnNameCollection.get(0);
			  }
			  int offset=(pageIndex-1)*pageSize;
			  orderBy+=" LIMIT "+pageSize+" OFFSET "+offset;
		  }
		  
		 
		  
		  ArrayList<ContentValues> SelectResult=new ArrayList<ContentValues>();
		  
		  
	      Cursor mCursor = mSQLiteDatabase.query(true, GetTableName(), GetColumnNames(),where_sql , null, null, null, orderBy, null);

	      if(mCursor!=null)
			{
				while(mCursor.moveToNext())
				{			
					ContentValues objContentValue=new ContentValues();
					
					for(int i=0;i<ColumnNameCollection.size();i++)
					{
						if(ColumnTypeCollection.get(i).startsWith("INT")||ColumnTypeCollection.get(i).startsWith("INTEGER"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("VARCHAR")||ColumnTypeCollection.get(i).startsWith("NVARCHAR")
								||ColumnTypeCollection.get(i).startsWith("DATETIME"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getString(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("FLOAT"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getFloat(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("LONG")||ColumnTypeCollection.get(i).startsWith("BIGINT"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getLong(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("BOOLEAN"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
					}
					
					SelectResult.add(objContentValue);
				}
			}
	      mCursor.close();
	      return SelectResult;

	   }
	  
	  public ArrayList<ContentValues> Select(String[] columnNames,Object[] columnValues,String orderBy) throws SQLException
	  {
		  return Select(columnNames,columnValues,null,0,0,orderBy);
	  }
	  
	  public ArrayList<ContentValues> Select(String[] columnNames,Object[] columnValues,String[] equalValues) throws SQLException
	  {
		  return Select(columnNames,columnValues,equalValues,0,0,null);
	  }
	  
	  public ArrayList<ContentValues> Select(String[] columnNames,Object[] columnValues) throws SQLException
	  {
		  return Select(columnNames,columnValues,null,0,0,null);
	  }
	public ArrayList<ContentValues> Select(String[] columnNames,Object[] columnValues,String[] equalValues,int pageIndex,int pageSize,String orderBy) throws SQLException{

		if(pageSize>0)
		{
			if(orderBy==null)
			{
				orderBy=ColumnNameCollection.get(0);
			}
			int offset=(pageIndex-1)*pageSize;
			orderBy+=" LIMIT "+pageSize+" OFFSET "+offset;
		}

		String whereSql=null;

		for(int i=0;i<columnNames.length;i++)
		{
			Object columnValue=columnValues[i];
			if(i>0)
			{
				whereSql+=" And ";
			}
			else
			{
				whereSql="";
			}
			if(columnValue.getClass()==String.class)
			{
				columnValue="'"+columnValue.toString().replace("'", "''")+"'";
			}

			if(equalValues==null)
			{
				whereSql+=""+columnNames[i]+"="+columnValue;
			}
			else
			{
				whereSql+=""+columnNames[i]+equalValues[i]+columnValue;
			}
		}


		ArrayList<ContentValues> SelectResult=new ArrayList<ContentValues>();
		Cursor mCursor = mSQLiteDatabase.query(true, GetTableName(), GetColumnNames(), whereSql, null, null, null, orderBy, null);

		if(mCursor!=null)
		{
			while(mCursor.moveToNext())
			{
				ContentValues objContentValue=new ContentValues();

				for(int i=0;i<ColumnNameCollection.size();i++)
				{
					if(ColumnTypeCollection.get(i).startsWith("INT")||ColumnTypeCollection.get(i).startsWith("INTEGER"))
					{
						objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
					}
					else if(ColumnTypeCollection.get(i).startsWith("VARCHAR")||ColumnTypeCollection.get(i).startsWith("NVARCHAR")
							||ColumnTypeCollection.get(i).startsWith("DATETIME"))
					{
						objContentValue.put(ColumnNameCollection.get(i), mCursor.getString(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
					}
					else if(ColumnTypeCollection.get(i).startsWith("FLOAT"))
					{
						objContentValue.put(ColumnNameCollection.get(i), mCursor.getFloat(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
					}
					else if(ColumnTypeCollection.get(i).startsWith("LONG")||ColumnTypeCollection.get(i).startsWith("BIGINT"))
					{
						objContentValue.put(ColumnNameCollection.get(i), mCursor.getLong(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
					}
					else if(ColumnTypeCollection.get(i).startsWith("BOOLEAN"))
					{
						objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
					}
				}

				SelectResult.add(objContentValue);
			}
		}
		mCursor.close();
		return SelectResult;

	}

	  public ArrayList<ContentValues> SelectByColumnName(String[] columnNames,String[] whereColumnNames,Object[] whereColumnValues,String[] equalValues,int pageIndex,int pageSize,String orderBy) throws SQLException{

		  if(pageSize>0)
		  {
			  if(orderBy==null)
			  {
				  orderBy=ColumnNameCollection.get(0);
			  }
			  int offset=(pageIndex-1)*pageSize;
			  orderBy+=" LIMIT "+pageSize+" OFFSET "+offset;
		  }
		  
		  String whereSql=null;
		  
		  for(int i=0;i<whereColumnNames.length;i++)
		  {
			  Object columnValue=whereColumnValues[i];
			  if(i>0)
			  {
				  whereSql+=" And ";
			  }
			  else
			  {
				  whereSql="";
			  }
			  if(columnValue.getClass()==String.class)
			  {
				  columnValue="'"+columnValue.toString().replace("'", "''")+"'";
			  }
			  
			  if(equalValues==null)
			  {
				  whereSql+=""+whereColumnNames[i]+"="+columnValue;
			  }
			  else
			  {
				  whereSql+=""+whereColumnNames[i]+equalValues[i]+columnValue;
			  }
		  }
		  
		  
		  ArrayList<ContentValues> SelectResult=new ArrayList<ContentValues>();
	      Cursor mCursor = mSQLiteDatabase.query(true, GetTableName(), columnNames, whereSql, null, null, null, orderBy, null);

	      if(mCursor!=null)
			{
				while(mCursor.moveToNext())
				{			
					ContentValues objContentValue=new ContentValues();
					
					for(int i=0;i<ColumnNameCollection.size();i++)
					{
						if(ColumnTypeCollection.get(i).startsWith("INT")||ColumnTypeCollection.get(i).startsWith("INTEGER"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("VARCHAR")||ColumnTypeCollection.get(i).startsWith("NVARCHAR")
								||ColumnTypeCollection.get(i).startsWith("DATETIME"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getString(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("FLOAT"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getFloat(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("LONG")||ColumnTypeCollection.get(i).startsWith("BIGINT"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getLong(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
						else if(ColumnTypeCollection.get(i).startsWith("BOOLEAN"))
						{
							objContentValue.put(ColumnNameCollection.get(i), mCursor.getInt(mCursor.getColumnIndex(ColumnNameCollection.get(i))));
						}
					}
					
					SelectResult.add(objContentValue);
				}
			}
	      mCursor.close();
	      return SelectResult;

	   }
	  
	  public void Clear()
	  {
		   mSQLiteDatabase.delete(GetTableName(), null, null);
	  }
	  
	  public boolean Update(String columnName,Object columnValue,ContentValues initialValues){
		  if(columnValue.getClass()==String.class)
		  {
			  columnValue="'"+columnValue.toString().replace("'", "''")+"'";
		  }
		  
	      return mSQLiteDatabase.update(GetTableName(), initialValues, columnName + "=" + columnValue, null) > 0;
	   }

	public boolean Update(String[] strings,Object[] columnValue,ContentValues initialValues){

		String where_sql="";
		for(int i=0;i<strings.length;i++) {
			String columnName=strings[i];
			Object objColumnValue=columnValue[i];
			if (objColumnValue.getClass() == String.class) {
				objColumnValue = "'" + objColumnValue.toString().replace("'", "''") + "'";
			}

			if(where_sql.length()>0)
			{
				where_sql+=" And ";
			}

			where_sql+=columnName + "=" + objColumnValue;
		}


		return mSQLiteDatabase.update(GetTableName(), initialValues, where_sql, null) > 0;
	}

	public boolean Update(String where_sql,ContentValues initialValues){
		return mSQLiteDatabase.update(GetTableName(), initialValues, where_sql, null) > 0;
	}

	public boolean UpdateWithFilter(String columnName,Object columnValue,String filterColumnName,Object filterColumnValue,ContentValues initialValues){
		if(columnValue.getClass()==String.class)
		{
			columnValue="'"+columnValue.toString().replace("'", "''")+"'";
		}

		if(filterColumnValue.getClass()==String.class)
		{
			filterColumnValue="'"+filterColumnValue.toString().replace("'", "''")+"'";
		}

		return mSQLiteDatabase.update(GetTableName(), initialValues, columnName + "=" + columnValue+" And "+filterColumnName + "!=" + filterColumnValue, null) > 0;
	}

	public boolean UpdateWithFilter(String filterColumnName,Object filterColumnValue,ContentValues initialValues){

		if(filterColumnValue.getClass()==String.class)
		{
			filterColumnValue="'"+filterColumnValue.toString().replace("'", "''")+"'";
		}

		return mSQLiteDatabase.update(GetTableName(), initialValues, filterColumnName + "!=" + filterColumnValue, null) > 0;
	}

	  public static String GetNowTime()
		{
			Date date=new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			return dateFormat.format(date);
		}
}