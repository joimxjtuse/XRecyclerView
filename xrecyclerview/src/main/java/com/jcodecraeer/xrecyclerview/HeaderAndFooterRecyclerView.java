package com.jcodecraeer.xrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joim on 2018/6/13.
 * Only Decorate recycler-view with header/footer.
 */
public class HeaderAndFooterRecyclerView extends RecyclerView {

    private List<View> mHeaderViews = new ArrayList<>();

    private List<View> mFooterViews = new ArrayList<>();

    private HeaderViewListAdapter mWrapAdapter;

    private View mEmptyView;

    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();

    public HeaderAndFooterRecyclerView(Context context) {
        this(context, null);
    }

    public HeaderAndFooterRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderAndFooterRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs, defStyle);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyle) {

    }

    public void destroy() {
        setAdapter(null);

        mHeaderViews.clear();

        mFooterViews.clear();
    }

    public void addHeaderView(View view) {
        if (view != null) {
            if (mWrapAdapter != null) {
                mWrapAdapter.addHeaderView(view);
            } else {
                mHeaderViews.add(view);
            }
        }
    }

    public void addFooterView(View view) {
        if (view != null) {
            if (mWrapAdapter != null) {
                mWrapAdapter.addFooterView(view);
            } else {
                mFooterViews.add(view);
            }
        }
    }

    public int getHeadersCount() {
        if (mHeaderViews == null) {
            return 0;
        }
        return mHeaderViews.size();
    }

    // if you can't sure that you are 100% going to
    // have no data load back from server anymore,do not use this
    @Deprecated
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        mDataObserver.onChanged();
    }

    public View getEmptyView() {
        return mEmptyView;
    }

    protected final int getItemCount() {
        return mWrapAdapter != null ? mWrapAdapter.getItemCount() : 0;
    }


    @Override
    public void setAdapter(Adapter adapter) {

        if (adapter != null) {

            mWrapAdapter = new HeaderViewListAdapter(mHeaderViews, mFooterViews, adapter);
            adapter.registerAdapterDataObserver(mDataObserver);

            super.setAdapter(mWrapAdapter);

            checkIfEmpty();
        } else {
            mWrapAdapter = null;
            super.setAdapter(adapter);
        }
    }

    @Override
    public final Adapter getAdapter() {
        if (mWrapAdapter != null) {
            return mWrapAdapter.getOriginalAdapter();
        }
        return super.getAdapter();
    }

    @Override
    public final void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        initSpanSizeIfNecessary(layout);
    }

    private void initSpanSizeIfNecessary(LayoutManager layout) {
        if (mWrapAdapter != null) {
            if (layout instanceof GridLayoutManager) {

                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (mWrapAdapter.isHeader(position) || mWrapAdapter.isFooter(position)
                                ? gridManager.getSpanCount() : 1);
                    }
                });
            }
        }
    }

    private void checkIfEmpty() {

        if (mWrapAdapter != null && mEmptyView != null) {
            int emptyCount = mWrapAdapter.getHeaderViewsCount();

            //TODO bug: while has header, show empty view?
            if (mWrapAdapter.getItemCount() == emptyCount) {
                mEmptyView.setVisibility(View.VISIBLE);
                HeaderAndFooterRecyclerView.this.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                HeaderAndFooterRecyclerView.this.setVisibility(View.VISIBLE);
            }
        }
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
            }
            checkIfEmpty();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
            }
            checkIfEmpty();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            }
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
            checkIfEmpty();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
            }
            checkIfEmpty();
        }
    }
}
