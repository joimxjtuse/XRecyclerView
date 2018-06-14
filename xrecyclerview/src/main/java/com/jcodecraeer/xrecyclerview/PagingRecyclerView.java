package com.jcodecraeer.xrecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import static com.jcodecraeer.xrecyclerview.BaseRefreshHeader.STATE_DONE;

/**
 * Created by joim on 2018/6/14.
 */

public class PagingRecyclerView extends HeaderAndFooterRecyclerView {

    private boolean isLoadingData = false;
    private boolean isNoMore = false;

    private boolean loadingMoreEnabled = true;

    private int mLoadingMoreProgressStyle = ProgressStyle.SysProgress;

    private View mFootView;

    // limit number to call load more
    // 控制多出多少条的时候调用 onLoadMore
    private int limitNumberToCallLoadMore = 1;

    private int mFooterViewItem = Integer.MAX_VALUE;

    private CustomFooterViewCallBack mFooterViewCallBack;

    private PagingListener mPagingListener;

    public PagingRecyclerView(Context context) {
        this(context, null);
    }

    public PagingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LoadingMoreFooter footView = new LoadingMoreFooter(getContext());
        footView.setProgressStyle(mLoadingMoreProgressStyle);
        mFootView = footView;
        mFootView.setVisibility(GONE);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mFootView instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootView).destroy();
            mFootView = null;
        }
    }

    public void setOnPagingListener(PagingListener pagingListener) {

        this.mPagingListener = pagingListener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        /**
         * i think loading more footer should be at the end of all the footers.
         */
        if (adapter != null && mFootView != null) {
            addFooterView(mFootView);
        }
    }

    @SuppressWarnings("all")
    public void setFootView(@NonNull final View view, @NonNull CustomFooterViewCallBack footerViewCallBack) {
        if (view == null || footerViewCallBack == null) {
            return;
        }
        mFootView = view;
        this.mFooterViewCallBack = footerViewCallBack;
    }

    public void reset() {
        setNoMore(false);
        loadMoreComplete();
    }

    public void loadMoreComplete() {
        isLoadingData = false;
        if (mFootView instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootView).setState(LoadingMoreFooter.STATE_COMPLETE);
        } else {
            if (mFooterViewCallBack != null) {
                mFooterViewCallBack.onLoadMoreComplete(mFootView);
            }
        }
    }

    public void setNoMore(boolean noMore) {
        isLoadingData = false;
        isNoMore = noMore;
        if (mFootView instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootView).setState(isNoMore ? LoadingMoreFooter.STATE_NOMORE : LoadingMoreFooter.STATE_COMPLETE);
        } else {
            if (mFooterViewCallBack != null) {
                mFooterViewCallBack.onSetNoMore(mFootView, noMore);
            }
        }
    }

    public void setLoadingMoreEnabled(boolean enabled) {
        loadingMoreEnabled = enabled;
        if (!enabled) {
            if (mFootView instanceof LoadingMoreFooter) {
                ((LoadingMoreFooter) mFootView).setState(LoadingMoreFooter.STATE_COMPLETE);
            }
        }
    }

    public void setLoadingMoreProgressStyle(int style) {
        mLoadingMoreProgressStyle = style;
        if (mFootView instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootView).setProgressStyle(style);
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mPagingListener != null && !isLoadingData && loadingMoreEnabled) {


            LayoutManager layoutManager = getLayoutManager();

            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            int visibleItemCount = layoutManager.getChildCount();

            int totalItemCount = getItemCount();

            if ((visibleItemCount > 0 && (lastVisibleItemPosition) >= totalItemCount - limitNumberToCallLoadMore) && !isNoMore) {
                isLoadingData = true;
                if (mFootView instanceof LoadingMoreFooter) {
                    ((LoadingMoreFooter) mFootView).setState(LoadingMoreFooter.STATE_LOADING);
                } else {
                    if (mFooterViewCallBack != null) {
                        mFooterViewCallBack.onLoadingMore(mFootView);
                    }
                }
                mPagingListener.onLoadMore();
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    // set the number to control call load more,see the demo on linearActivity
    public void setLimitNumberToCallLoadMore(int limitNumberToCallLoadMore) {
        this.limitNumberToCallLoadMore = limitNumberToCallLoadMore;
    }

    public void setLoadMoreText(String loading, String noMore) {
        if (mFootView instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootView).setLoadingHint(loading);
            ((LoadingMoreFooter) mFootView).setNoMoreHint(noMore);
        }
    }
}
