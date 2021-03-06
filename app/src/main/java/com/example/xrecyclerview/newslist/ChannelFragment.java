package com.example.xrecyclerview.newslist;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xrecyclerview.MyAdapter;
import com.example.xrecyclerview.R;
import com.jcodecraeer.xrecyclerview.HeaderAndFooterRecyclerView;
import com.jcodecraeer.xrecyclerview.PagingListener;
import com.jcodecraeer.xrecyclerview.PagingRecyclerView;
import com.jcodecraeer.xrecyclerview.PullRefreshRecyclerView;
import com.jcodecraeer.xrecyclerview.RefreshListener;

import java.util.ArrayList;

/**
 * Created by joim on 2018/6/4.
 */

public class ChannelFragment extends Fragment {

    private PullRefreshRecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    private RecyclerView.RecycledViewPool mPool;


    private String mTitle = "NoTitle";

    private ArrayList<String> listData;

    private int refreshTime = 0;
    private int times = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        if (extras != null) {
            mTitle = extras.getString("title");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI(view);
    }

    private void initUI(View container) {

        mRecyclerView = (PullRefreshRecyclerView) container.findViewById(R.id.recyclerview);

        mPool = UniversalPool.getUniversalPool();
        mRecyclerView.setRecycledViewPool(mPool);

        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);

        mRecyclerView.setItemViewCacheSize(10);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setRecycleChildrenOnDetach(true);
        mRecyclerView.setLayoutManager(layoutManager);

        for (int i = 0; i < 8; i++) {
            TextView titleHeader = (TextView) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_header_title, mRecyclerView, false);
            titleHeader.setText("header -->" + mTitle + i);
            mRecyclerView.addHeaderView(titleHeader);
        }

        for (int i = 0; i < 8; i++) {

            TextView titleFooter = (TextView) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_header_title, mRecyclerView, false);
            titleFooter.setText("footer -->" + mTitle + i);
            mRecyclerView.addFooterView(titleFooter);
        }

        final int itemLimit = 5;

        // When the item number of the screen number is list.size-2,we call the onLoadMore
        mRecyclerView.setLimitNumberToCallLoadMore(2);

        mRecyclerView.setOnPagingListener(new PagingListener() {
            @Override
            public void onLoadMore() {

                Log.e("aaaaa", "call onLoadMore");
                if (times < 10) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            for (int i = 0; i < itemLimit; i++) {
                                listData.add("item" + (1 + listData.size()));
                            }
                            if (mRecyclerView != null) {
                                mRecyclerView.loadMoreComplete();
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }, 1000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            for (int i = 0; i < itemLimit; i++) {
                                listData.add("item" + (1 + listData.size()));
                            }
                            if (mRecyclerView != null) {
                                mRecyclerView.setNoMore(true);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }, 1000);
                }
                times++;

            }
        });

        mRecyclerView.setOnRefreshListener(new RefreshListener() {
            @Override
            public void onRefresh() {

                refreshTime++;
                times = 0;
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        listData.clear();
                        for (int i = 0; i < itemLimit; i++) {
                            listData.add("Channel: " + mTitle + "; item" + i + "after " + refreshTime + " times of refresh");
                        }
                        mAdapter.notifyDataSetChanged();
                        if (mRecyclerView != null)
                            mRecyclerView.refreshComplete();
                    }

                }, 1000);            //refresh data here

            }
        });

        listData = new ArrayList();
        for (int i = 0; i < 30; i++) {
            listData.add("item" + (1 + listData.size()));
        }
        mAdapter = new MyAdapter(listData);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        if (mRecyclerView != null) {
            mRecyclerView.destroy();
        }
        mPool = null;
        super.onDestroy();
    }
}
