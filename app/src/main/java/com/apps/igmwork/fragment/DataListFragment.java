package com.apps.igmwork.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apps.igmwork.R;
import com.apps.igmwork.WebActivity;
import com.apps.igmwork.framework.server.HTTPParam;
import com.apps.igmwork.framework.server.HTTPServer;
import com.apps.igmwork.framework.ui.BaseActivity;
import com.apps.igmwork.framework.ui.BaseFragment;
import com.apps.igmwork.framework.ui.data.RecyclerDataAdapter;
import com.apps.igmwork.framework.ui.image.DraweeViewHelper;
import com.bcfbaselibrary.internal.Logger;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;

public class DataListFragment extends BaseFragment implements RecyclerDataAdapter.OnListItemListener,View.OnClickListener,BaseActivity.OnListDataListener,Toolbar.OnMenuItemClickListener,SwipeRefreshLayout.OnRefreshListener {

    //静态成员
    public static final String TAG="DataListFragment";

    public static DataListFragment newInstance() {
        DataListFragment fragment = new DataListFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //控件成员
    protected RecyclerView rvListContainer;
    SwipeRefreshLayout mSwipeRefreshLayout;
    //对象成员
    protected PageListAdapter mPageListAdapter;
    //数据成员
    protected boolean bStartRequest=false;


    //对象成员
    protected BaseActivity mBaseActivity;



    public DataListFragment() {
        // Required empty public constructor
    }

    //实现界面事件
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBaseActivity=(BaseActivity)getActivity();
        mPageListAdapter=new PageListAdapter(mBaseActivity,this);
        mPageListAdapter.mPageSize=3;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_data_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layoutRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorLoadingBar));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        rvListContainer =(RecyclerView)view.findViewById(R.id.rvListContainer);

        SetupRecyclerView();

        if(!bStartRequest)
        {
            bStartRequest=true;
            OnFirstPageRequested(R.id.rvListContainer);
        }
        return view;
    }

    @Override
    public void onRefresh() {
        OnFirstPageRequested(R.id.rvListContainer);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void OnListItemClick(int position, RecyclerView.ViewHolder holder) {
        Logger.E(TAG,"mid=" + mPageListAdapter.DataSource.get(position).getAsString("MeetingID"));
        Logger.E(TAG,"vid=" + mPageListAdapter.DataSource.get(position).getAsString("VendorID"));

        String mid = mPageListAdapter.DataSource.get(position).getAsString("MeetingID");
        String vid = mPageListAdapter.DataSource.get(position).getAsString("VendorID");
        String dataName = mPageListAdapter.DataSource.get(position).getAsString("DataName");

        if(mid != "0") {
            String url = "";

            url = HTTPServer.BuildURL(getActivity(), HTTPServer.GetUserDataDetailURL,
                    new HTTPParam("mid", mid),
                    new HTTPParam("vid", vid),
                    new HTTPParam("dataName", dataName));

            Logger.E("TAG", "URL:" + url);
            mUserProfile.LoadURL = url;
            mUserProfile.Save(mBaseActivity);
            mBaseActivity.TransferActivity(WebActivity.class);
            //Intent intent=new Intent();
            //intent.putExtra("ResourceItem",mPageListAdapter.DataSource.get(position));
            //intent.setClass(getActivity(),ComicsDetailActivity.class);
            //startActivity(intent);
        }
    }

    @Override
    public void OnFirstPageRequested(int viewID) {

        mPageListAdapter.mPageIndex=0;
        mPageListAdapter.LastResourceID=0;
        OnNextPageRequested(viewID,"OnFirstPageRequested");

    }

    @Override
    public void OnNextPageRequested(int viewID, Object holder) {

        int nextPageIndex=mPageListAdapter.mPageIndex+1;
        String url="";

        url = HTTPServer.BuildURL(getActivity(), HTTPServer.GetUserDataListURL,
                new HTTPParam("MobileNo", mUserProfile.MobileNum),
                new HTTPParam("PageIndex",nextPageIndex ),
                new HTTPParam("PageSize", mPageListAdapter.mPageSize));


        Logger.E("TAG","URL:"+url);
        mBaseActivity.AddHTTPRequest("GetDataList",nextPageIndex,url,this);

        //http://m.avshow321.com/APIPage.aspx?Method=GetComicsList&Comicstype=-1&PageSize=24&PageIndex=1
    }

    @Override
    public void OnRefreshPageRequested(int viewID) {
        OnFirstPageRequested(viewID);
    }



    //实现后台数据事件

    @Override
    public void OnReceiveHTTPResp(Object key, Object contextObject, JSONObject jsonServerResponse) {
        //Logger.E(TAG,"Key:"+key+",Response:"+jsonServerResponse.toString());
        if(key.equals("GetDataList"))
        {
            if(jsonServerResponse.optInt("StatusCode")==1) {
                if (contextObject.equals(1)) {
                    mPageListAdapter.ClearDataSource();
                }

                mPageListAdapter.FillData(jsonServerResponse, "DataSource");
                mPageListAdapter.notifyDataSetChanged();

                mPageListAdapter.mPageIndex=(Integer)contextObject;
            }
        }
    }

    @Override
    public void OnPostHTTPRequest(Object key, Object contextObject) {
        super.OnPostHTTPRequest(key,contextObject);
        mSwipeRefreshLayout.setRefreshing(false);
    }



    //实现界面操作
    protected void SetupRecyclerView()
    {
        rvListContainer.setLayoutManager(new LinearLayoutManager(mBaseActivity));

        rvListContainer.setAdapter(mPageListAdapter);
        mPageListAdapter.SetOnItemClickListener(this);
    }
    //实现后台数据操作
    public void RequestContent()
    {
        if(!bStartRequest) {
            bStartRequest=true;
            mSwipeRefreshLayout.setRefreshing(true);
            OnFirstPageRequested(R.id.rvListContainer);
        }
    }
    //定义外部类别
    public static class PageListAdapter
            extends RecyclerDataAdapter<RecyclerView.ViewHolder> {

        public int LastResourceID=0;

        public PageListAdapter(BaseActivity context, BaseActivity.OnListDataListener listener) {
            super(context, listener);
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_data_item, parent, false);

            return new DataListFragment.PageListAdapter.HeaderViewHolder(view);
        }

        //设置子控件内容数据
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            ContentValues objResourceItem = DataSource.get(position);

            DataListFragment.PageListAdapter.HeaderViewHolder itemHolder = (DataListFragment.PageListAdapter.HeaderViewHolder) holder;

            itemHolder.lblMeetingID.setText(objResourceItem.getAsString("MeetingID"));
            itemHolder.lblVendorID.setText(objResourceItem.getAsString("VendorID"));
            itemHolder.lblDataName.setText(objResourceItem.getAsString("DataName"));

            itemHolder.lblTime.setText(objResourceItem.getAsString("Time"));
            itemHolder.lblName.setText(objResourceItem.getAsString("MeetingName"));
            /*
            String ThumbnailImageAddress = objResourceItem.getAsString("DisplayImageAddress").replace("\\", "/");

            if (!ThumbnailImageAddress.startsWith("http")) {
                ThumbnailImageAddress = HTTPServer.ImageResourceImageHomeURL + ThumbnailImageAddress;
            }

            //DraweeViewHelper.SetImage(ThumbnailImageAddress, itemHolder.imgPageAD);
            //DraweeViewHelper.SetFullScreenImage(mContext,itemHolder.imgPageAD,ThumbnailImageAddress);
*/
            if(HasMore(position))
            {
                mDataListener.OnNextPageRequested(R.id.rvListContainer,holder);
            }
        }

        //定义子项数据控件

        public static class HeaderViewHolder extends RecyclerView.ViewHolder {

            public final TextView lblMeetingID;
            public final TextView lblVendorID;
            public final TextView lblTime;
            public final TextView lblName;
            public final TextView lblDataName;


            public HeaderViewHolder(View view) {
                super(view);
                lblTime = (TextView) view.findViewById(R.id.lblTime);
                lblName = (TextView) view.findViewById(R.id.lblName);
                lblVendorID = (TextView) view.findViewById(R.id.lblVendorID);
                lblMeetingID = (TextView) view.findViewById(R.id.lblMeetingID);
                lblDataName = (TextView) view.findViewById(R.id.lblDataName);
            }

        }
    }

}
