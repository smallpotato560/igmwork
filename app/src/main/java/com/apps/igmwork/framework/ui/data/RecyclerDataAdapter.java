package com.apps.igmwork.framework.ui.data;

import android.content.ContentValues;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.apps.igmwork.R;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.bcfbaselibrary.string.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ben on 2017/11/15.
 */
public abstract class RecyclerDataAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter implements View.OnClickListener {
    public BaseActivity mContext;
    public ArrayList<ContentValues> DataSource = new ArrayList<ContentValues>();

    protected BaseActivity.OnListDataListener mDataListener;
    protected OnListItemListener mOnListItemListener=null;
    protected OnSoftInputListener mOnSoftInputListener=null;

    public int mPageSize=24;
    public int mPageIndex=1;
    public boolean bIsLoading=false;
    public int LastVisiblePosition=0;

    protected boolean bHasMore=false;
    protected int mListBottomRange;

    protected  RecyclerView rvListContainer;

    public RecyclerDataAdapter(BaseActivity context, BaseActivity.OnListDataListener listener) {
        super();
        mContext=context;
        mDataListener=listener;

        if(context.findViewById(R.id.rvListContainer)!=null)
        {
            rvListContainer=(RecyclerView)context.findViewById(R.id.rvListContainer);
        }
    }

    //列表行为操作
    public boolean IsOnBottom()
    {
        return GetLastPosition()>=(getItemCount()-1);
    }

    public void Goto(int location)
    {
        if(getItemCount()>location&&location>=0)
        {
            if((getItemCount()-1)==location)
            {
                GotoListBottom();
            }
            else
            {
                //rvListContainer.requestFocus();
                rvListContainer.smoothScrollToPosition(location);
            }
            //((LinearLayoutManager)rvListContainer.getLayoutManager()).scrollToPositionWithOffset(location,0);
        }
    }

    public void GotoListBottom()
    {
        if(getItemCount()>0)
        {
            //rvListContainer.smoothScrollToPosition(mChatMsgListAdapter.getItemCount()-1);
            ((LinearLayoutManager)rvListContainer.getLayoutManager()).scrollToPositionWithOffset(getItemCount()-1,0);
        }
    }
    //列表事件操作

    public void SetOnItemClickListener(OnListItemListener listener)
    {
        mOnListItemListener=listener;
    }

    public void SetOnSoftInputListener(OnSoftInputListener listener)
    {
        mOnSoftInputListener=listener;
    }

    public void SetAutoFitContentWindow(View view)
    {
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //处理软键盘弹出
                if(oldBottom>bottom&&(oldBottom>0&&bottom>0))
                {
                    final int y=oldBottom-bottom;
                    mContext.UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            rvListContainer.scrollBy(0,y);

                            mListBottomRange=rvListContainer.computeVerticalScrollRange()-(y+rvListContainer.computeVerticalScrollOffset()+rvListContainer.computeVerticalScrollExtent());

                            /*
                            Logger.E("onLayoutChange","computeVerticalScrollRange:"+rvListContainer.computeVerticalScrollRange());
                            Logger.E("onLayoutChange","computeVerticalScrollOffset:"+rvListContainer.computeVerticalScrollOffset());
                            Logger.E("onLayoutChange","computeVerticalScrollExtent:"+rvListContainer.computeVerticalScrollExtent());
                            Logger.E("onLayoutChange","mListBottomRange:"+mListBottomRange);*/
                            if(mOnSoftInputListener!=null)
                            {
                                mOnSoftInputListener.OnSoftInputOpen();
                            }

                        }
                    } );

                }//处理软键盘收起
                else if(oldBottom<bottom&&(oldBottom>0&&bottom>0))
                {
                    final int y=oldBottom-bottom;

                    mContext.UIHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if(mListBottomRange>0)
                                rvListContainer.scrollBy(0,y);

                            if(mOnSoftInputListener!=null)
                            {
                                mOnSoftInputListener.OnSoftInputClose();
                            }
                        }
                    });

                }

            }
        });
    }

    public void SetRecyclerView(RecyclerView view)
    {
        rvListContainer=view;

        /*if(setupScrollListener) {
            rvListContainer.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    ;
                    // 当不滚动时
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        //获取最后一个完全显示的ItemPosition
                        RefreshLastPosition();
                    }
                }

                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                }
            });
        }*/
    }

    public void RemoveItem(int position)
    {
        DataSource.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,DataSource.size());
    }

    public boolean HasMore(int position)
    {
        boolean bResult = position==(DataSource.size()-1)&&bHasMore&&!bIsLoading;
        //return position==(DataSource.size()-1)&&DataSource.size()%mPageSize==0&&!bIsLoading;

        return bResult;
    }

    @Override
    public void onClick(View view)
    {
        if(mOnListItemListener!=null)
        {
            //Logger.E("onclick","ID:"+view.getId()+"layoutLeftMsg:"+R.id.layoutChatMsg);
            //Logger.E("onclick","ID:"+view.getId()+"imgRecordPlayLeft:"+R.id.imgRecordPlayLeft);
            //Logger.E("onclick","ID:"+view.getId()+"lblLeftMsg:"+R.id.lblLeftMsg);
            // Logger.E("onclick","ID:"+view.getId()+"lblLeftMsg:"+R.id.lblLeftMsg);
            int position=(int)view.getTag();
            //RecyclerView.ViewHolder holder=(RecyclerView.ViewHolder)view.getTag(R.id.ID_Recycler_Holder);
            mOnListItemListener.OnListItemClick(position,null);
        }
    }
    @Override
    public int getItemCount() {
        return DataSource.size();
    }

    public void ClearDataSource()
    {
        DataSource.clear();
    }
    public void AddDataSource(ArrayList<ContentValues> objDataList)
    {
        if(objDataList.size()>0)
        {
            DataSource.addAll(objDataList);
            notifyDataSetChanged();
        }

    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //holder.itemView.setTag(position);
        SetupOnListItemListener(holder,position);

    }

    protected void SetupOnListItemListener(RecyclerView.ViewHolder holder, int position)
    {
        if(mOnListItemListener!=null) {
            holder.itemView.setTag( position);
            //holder.itemView.setTag(R.id.ID_Recycler_Position, position);
            //holder.itemView.setTag(R.id.ID_Recycler_Holder, holder);

            holder.itemView.setOnClickListener(this);
        }
    }
    public ContentValues GetLastData()
    {
        if(DataSource.size()>0)
            return DataSource.get(getItemCount()-1);
        return null;
    }

    public void FillData(JSONObject jsonServerResponse, String sourceName)
    {
        try {
            bHasMore=false;
            JSONArray dataList = jsonServerResponse.optJSONArray(sourceName);

            if (dataList != null) {
                ArrayList<ContentValues> objDataList = JsonHelper.ConvertToContentList(dataList);
                AddDataSource(objDataList);

                //新加载的数据条数等于设定的加载数据条，充许读取下一页数据
                if(objDataList.size()==mPageSize)
                    bHasMore=true;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void RefreshLastPosition()
    {
        LastVisiblePosition=GetLastPosition();
    }


    public int GetLastPosition() {
        int position;
        if (rvListContainer.getLayoutManager() instanceof LinearLayoutManager) {
            //Logger.E("List Position","findLastVisibleItemPosition:"+ ((LinearLayoutManager) rvListContainer.getLayoutManager()).findLastVisibleItemPosition());
            //Logger.E("List Position","findLastCompletelyVisibleItemPosition:"+ ((LinearLayoutManager) rvListContainer.getLayoutManager()).findLastCompletelyVisibleItemPosition());

            position = ((LinearLayoutManager) rvListContainer.getLayoutManager()).findLastVisibleItemPosition();
        } else if (rvListContainer.getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) rvListContainer.getLayoutManager()).findLastVisibleItemPosition();
        } else if (rvListContainer.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) rvListContainer.getLayoutManager();
            int[] lastPositions = layoutManager.findLastVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMaxPosition(lastPositions);
        } else {
            position = rvListContainer.getLayoutManager().getItemCount() - 1;
        }
        return position;
    }

    protected int getMaxPosition(int[] positions) {
        int size = positions.length;
        int maxPosition = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

    public interface OnListItemListener
    {
        public void OnListItemClick(int position, RecyclerView.ViewHolder holder);
    }


    public interface OnSoftInputListener
    {
        public void OnSoftInputOpen();
        public void OnSoftInputClose();
    }


}