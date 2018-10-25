package com.bcfbaselibrary.ui;

import java.util.ArrayList;

import android.content.ContentValues;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DataListAdapter extends BaseAdapter {

	public final String TAG = DataListAdapter.class.getSimpleName();
	public ArrayList<ContentValues> DataSource = new ArrayList<ContentValues>();
	
	
	public int mPageIndex=0;
	public int mPageSize=24;
	
	protected int mSize=0;
	
	public DataListAdapter()
	{
	}
	
	public void reset() {
		
		//HTTPConnector.Cancel();
		DataSource.clear();

		notifyDataSetChanged();
	}
	
	public void RefreshSize()
	{
		mSize=DataSource.size();
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		RefreshSize();
		super.notifyDataSetChanged();
	}
	
	
	public void AddDataSource(ArrayList<ContentValues> objDataList)
	{
		if(objDataList.size()>0)
		{
			DataSource.addAll(objDataList);
			notifyDataSetChanged();
		}

		if(getCount()%mPageSize!=0)
		{
			SetNoMoreView();
		}
	}
	
	@Override
	public int getCount() {
		return mSize;
	}

	@Override
	public ContentValues getItem(int position) {
		if(position>=0&&position<DataSource.size())
			return DataSource.get(position);
		else 
			return null;
	}

	public void onFirstPageRequested()
	{
		mPageIndex=1;
		onPageRequested(mPageIndex);

	}
	
	public void onNextPageRequested()
	{
		mPageIndex++;
		onPageRequested(mPageIndex);
	}
	
	
	public abstract void onPageRequested(int page);
	
	
	public abstract void onRefreshPageRequested();
	public abstract void SetEmptyView();
	public abstract void SetLoadingView();

	public abstract void SetNoMoreView();


	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}