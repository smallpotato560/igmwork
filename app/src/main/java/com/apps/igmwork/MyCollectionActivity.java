package com.apps.igmwork;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.data.RecyclerDataAdapter;
import com.apps.igmwork.framework.ui.image.DraweeViewHelper;
import com.apps.igmwork.framework.ui.widget.AlertDialogUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;

public class MyCollectionActivity extends BaseActivity implements View.OnClickListener,RecyclerDataAdapter.OnListItemListener,BaseActivity.OnListDataListener, SwipeRefreshLayout.OnRefreshListener {

    //静态成员

    //控件成员
    protected RecyclerView rvListContainer;
    SwipeRefreshLayout mSwipeRefreshLayout;
    //数据成员
    protected PageListAdapter mPageListAdapter;


    //对象成员
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);
        UIInit("最愛商家",true);

        mPageListAdapter=new PageListAdapter(this,this);
        mPageListAdapter.mPageSize=24;

        SetupRecyclerView();
        OnFirstPageRequested(R.id.rvListContainer);
    }


    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btnDelete)
        {
            final int position=(int)view.getTag();
            AlertDialogUtils.ShowMessageDialog(this, null,"您確定要刪除此收藏記錄嗎？",true,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AddHTTPRequest("DeleteCollectList",
                                    new HTTPParam("CollectListID",mPageListAdapter.DataSource.get(position).getAsInteger("CollectListID")));
                        }
                    });

        }
    }

    @Override
    public void OnListItemClick(int position, RecyclerView.ViewHolder holder) {

    }

    @Override
    public void OnFirstPageRequested(int viewID) {
        mPageListAdapter.mPageIndex=0;
        mPageListAdapter.LastResourceID=0;
        OnNextPageRequested(viewID,"OnFirstPageRequested");
    }

    @Override
    public void OnNextPageRequested(int viewID, Object holder) {
        AddHTTPRequest("GetCollection",holder,
                new HTTPParam("CustomerID",mUserProfile.UserID),
                new HTTPParam("PageSize",mPageListAdapter.mPageSize),
                new HTTPParam("PageIndex",mPageListAdapter.mPageIndex+1));
    }


    @Override
    public void OnRefreshPageRequested(int viewID) {
        OnFirstPageRequested(viewID);
    }

    @Override
    public void onRefresh() {
        OnRefreshPageRequested(rvListContainer.getId());
    }

//实现后台数据事件

    //实现界面操作
    protected void SetupRecyclerView()
    {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layoutRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorLoadingBar));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        rvListContainer =(RecyclerView)findViewById(R.id.rvListContainer);

        rvListContainer.setLayoutManager(new LinearLayoutManager(this));

        rvListContainer.setAdapter(mPageListAdapter);
        mPageListAdapter.SetOnItemClickListener(this);
    }
    //实现后台数据操作
    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {
        //Logger.E(TAG,"Key:"+key+",Response:"+jsonServerResponse.toString());
        if(key.equals("GetCollection"))
        {
            if(jsonServerResponse.optInt("StatusCode")==1) {

                if (contextObject.equals("OnFirstPageRequested")) {
                    mPageListAdapter.ClearDataSource();
                }

                mPageListAdapter.FillData(jsonServerResponse, "DataSource");
                mPageListAdapter.notifyDataSetChanged();
                mPageListAdapter.mPageIndex++;
            }
        }
        else if(key.equals("DeleteCollectList"))
        {
            if(jsonServerResponse.optInt("StatusCode")==1) {
                OnRefreshPageRequested(rvListContainer.getId());
            }
        }
    }

    @Override
    public void OnPostHTTPRequest(Object key, Object contextObject) {
        super.OnPostHTTPRequest(key,contextObject);
        mSwipeRefreshLayout.setRefreshing(false);
    }
    //实现后台数据事件

    //实现界面操作
    public static class PageListAdapter
            extends RecyclerDataAdapter<RecyclerView.ViewHolder> {

        public int LastResourceID=0;

        public PageListAdapter(BaseActivity context, BaseActivity.OnListDataListener listener) {
            super(context, listener);
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_collection_item, parent, false);

            return new HeaderViewHolder(view);
        }

        //设置子控件内容数据
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            ContentValues objResourceItem = DataSource.get(position);

            HeaderViewHolder itemHolder = (HeaderViewHolder) holder;

            itemHolder.btnDelete.setTag(position);
            itemHolder.btnDelete.setOnClickListener((View.OnClickListener) mContext);
            String ThumbnailImageAddress ="";
            if(objResourceItem.getAsInteger("VideoImageType")==0)
            {
                itemHolder.lblType.setText("漫畫");
                ThumbnailImageAddress = objResourceItem.getAsString("ComicsDisplayImageAddress").replace("\\", "/");

            }
            else
            {
                itemHolder.lblType.setText("影片");
                ThumbnailImageAddress = objResourceItem.getAsString("VideoDisplayImageAddress").replace("\\", "/");

            }

            itemHolder.lblPageTitle.setText(objResourceItem.getAsString("Title"));


            if (!ThumbnailImageAddress.startsWith("http")) {
                ThumbnailImageAddress = HTTPServer.ImageResourceImageHomeURL + ThumbnailImageAddress;
            }

            //DraweeViewHelper.SetImage(ThumbnailImageAddress, itemHolder.imgPageAD);
            DraweeViewHelper.SetFullScreenImage(mContext,itemHolder.imgPageAD,ThumbnailImageAddress);

            if(HasMore(position))
            {
                mDataListener.OnNextPageRequested(R.id.rvListContainer,holder);
            }
        }

        //定义子项数据控件

        public static class HeaderViewHolder extends RecyclerView.ViewHolder {

            public final TextView lblPageTitle;
            public final TextView lblType;
            public final SimpleDraweeView imgPageAD;
            public final ImageButton btnDelete;


            public HeaderViewHolder(View view) {
                super(view);
                lblPageTitle = (TextView) view.findViewById(R.id.lblPageTitle);
                lblType = (TextView) view.findViewById(R.id.lblType);
                imgPageAD = (SimpleDraweeView) view.findViewById(R.id.imgPageAD);
                btnDelete = (ImageButton) view.findViewById(R.id.btnDelete);
            }

        }
    }
}
