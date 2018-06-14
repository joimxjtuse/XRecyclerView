package com.jcodecraeer.xrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Switch;

/**
 * Created by joim on 2018/6/14.
 * <p>r'
 * if you just want to use pull down refresh ,just extends HeaderAndFooterRecyclerView.
 */
public class PullRefreshRecyclerView extends PagingRecyclerView {

    private static final float DRAG_RATE = 3;

    private int mRefreshProgressStyle = ProgressStyle.SysProgress;

    private boolean pullRefreshEnabled = true;

    private ArrowRefreshHeader mRefreshHeader;

    private RefreshListener mRefreshListener;


    public PullRefreshRecyclerView(Context context) {
        this(context, null);
    }

    public PullRefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    @Override
    public void reset() {
        super.reset();
        refreshComplete();
    }

    private void init() {
        if (pullRefreshEnabled) {
            mRefreshHeader = new ArrowRefreshHeader(getContext());
            mRefreshHeader.setProgressStyle(mRefreshProgressStyle);
        }
        addHeaderView(mRefreshHeader);
    }

    public void refreshComplete() {
        if (mRefreshHeader != null)
            mRefreshHeader.refreshComplete();
        setNoMore(false);
    }

    public void setOnRefreshListener(RefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
    }

    private float mLastY = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // sometimes onTouch() may ignore down action.
                mLastY = ev.getRawY();
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                //mLastY = e.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && pullRefreshEnabled) {
                    if (mRefreshHeader == null)
                        break;
                    mRefreshHeader.onMove(deltaY / DRAG_RATE);
                    if (mRefreshHeader.getVisibleHeight() > 0 && mRefreshHeader.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
                        return false;
                    }
                }
                break;
            }
            default: {
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled) {
                    if (mRefreshHeader != null && mRefreshHeader.releaseAction()) {
                        if (mRefreshListener != null) {
                            mRefreshListener.onRefresh();
                        }
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(ev);
    }

    private boolean isOnTop() {
        if (mRefreshHeader == null)
            return false;
        if (mRefreshHeader.getParent() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mRefreshHeader != null) {
            mRefreshHeader.destroy();
            mRefreshHeader = null;
        }
    }
}
