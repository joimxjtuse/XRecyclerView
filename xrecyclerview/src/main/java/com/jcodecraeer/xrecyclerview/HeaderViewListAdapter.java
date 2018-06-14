package com.jcodecraeer.xrecyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.jcodecraeer.xrecyclerview.exception.NullArgumentException;

import java.util.List;

/**
 * Created by joim on 2018/6/13.
 */
final class HeaderViewListAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER_START = Integer.MIN_VALUE / 2;

    private static final int TYPE_FOOTER_START = Integer.MIN_VALUE / 3;

    private static final int TYPE_FOOTER_END = Integer.MIN_VALUE / 4 - 1;

    private static final int TYPE_NORMAL_START = Integer.MIN_VALUE / 4;

    private List<View> mHeaderViews;

    private List<View> mFooterViews;

    private RecyclerView.Adapter mRealAdapter;

    public HeaderViewListAdapter(@NonNull List<View> headerViews, @NonNull List<View> footerViews, RecyclerView.Adapter adapter) {

        if (headerViews == null) {
            throw new NullArgumentException("Invalid header params!");
        }
        if (footerViews == null) {
            throw new NullArgumentException("Invalid footer params!");
        }
        this.mHeaderViews = headerViews;
        this.mFooterViews = footerViews;
        this.mRealAdapter = adapter;

        init();
    }


    private void init() {
        if (mRealAdapter != null) {
            setHasStableIds(mRealAdapter.hasStableIds());
        }
    }

    RecyclerView.Adapter getOriginalAdapter() {
        return mRealAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (isHeaderType(viewType)) {

            int position = viewType - TYPE_HEADER_START;
            SimpleViewHolder holder = new SimpleViewHolder(mHeaderViews.get(position));
            holder.setIsRecyclable(false);
            return holder;
        } else if (isFooterType(viewType)) {

            int position = viewType - TYPE_FOOTER_START - getHeaderViewsCount() - getRealCount();
            SimpleViewHolder holder = new SimpleViewHolder(mFooterViews.get(position));
            holder.setIsRecyclable(false);
            return holder;
        } else if (mRealAdapter != null) {
            return mRealAdapter.onCreateViewHolder(parent, viewType);
        }
        return null;
    }

    private boolean isHeaderType(int itemViewType) {
        if (mHeaderViews.isEmpty()) {
            return false;
        }
        return itemViewType >= TYPE_HEADER_START && itemViewType < TYPE_FOOTER_START;
    }

    private boolean isFooterType(int itemViewType) {
        if (mFooterViews.isEmpty()) {
            return false;
        }
        return itemViewType >= TYPE_FOOTER_START && itemViewType < TYPE_FOOTER_END;
    }

    @Override
    public long getItemId(int position) {
        if (isHeader(position) || isFooter(position)) {
            return RecyclerView.NO_ID;
        } else if (mRealAdapter != null) {
            return mRealAdapter.getItemId(position - getHeaderViewsCount());
        }
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return position + TYPE_HEADER_START;
        } else if (isFooter(position)) {
            return position + TYPE_FOOTER_START;
        } else if (mRealAdapter != null) {
            int realType = mRealAdapter.getItemViewType(position - getHeaderViewsCount());
            if (isReservedItemViewType(realType)) {
                throw new IllegalArgumentException("what is an strange list type!");
            }
            return realType;
        }
        return super.getItemViewType(position);
    }

    //判断是否是XRecyclerView保留的itemViewType
    private boolean isReservedItemViewType(int itemViewType) {
        if (itemViewType < TYPE_NORMAL_START) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (isHeader(position) || isFooter(position)) {
            return;
        } else {
            mRealAdapter.onBindViewHolder(holder, position - getHeaderViewsCount());
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if (isHeader(position) || isFooter(position)) {
            return;
        } else {
            mRealAdapter.onBindViewHolder(holder, position - getHeaderViewsCount(), payloads);
        }
    }

    @Override
    public int getItemCount() {
        return getHeaderViewsCount() + getFooterViewsCount() + getRealCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {

            final GridLayoutManager gridManager = ((GridLayoutManager) manager);

            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

                @Override
                public int getSpanSize(int position) {
                    return (isHeader(position) || isFooter(position))
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }

        if (mRealAdapter != null) {
            mRealAdapter.onAttachedToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mRealAdapter != null) {
            mRealAdapter.onDetachedFromRecyclerView(recyclerView);
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();

        if (lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && (isHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }

        if (mRealAdapter != null) {
            mRealAdapter.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (mRealAdapter != null) {
            mRealAdapter.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (mRealAdapter != null) {
            mRealAdapter.onViewRecycled(holder);
        }
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {

        if (mRealAdapter != null) {
            return mRealAdapter.onFailedToRecycleView(holder);
        } else {
            return super.onFailedToRecycleView(holder);
        }
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        if (mRealAdapter != null) {
            mRealAdapter.unregisterAdapterDataObserver(observer);
        }
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        if (mRealAdapter != null) {
            mRealAdapter.registerAdapterDataObserver(observer);
        }
    }

    private int getRealCount() {
        return mRealAdapter != null ? mRealAdapter.getItemCount() : 0;
    }

    protected boolean isHeader(int position) {
        return position >= 0 && position < getHeaderViewsCount();
    }

    protected boolean isFooter(int position) {

        int start = getHeaderViewsCount() + getRealCount();
        return position >= start && start < getItemCount();
    }


    protected int getHeaderViewsCount() {
        return mHeaderViews.size();
    }

    protected int getFooterViewsCount() {
        return mFooterViews.size();
    }

    protected void addHeaderView(View header) {

        if (header != null) {
            mHeaderViews.add(header);

            int insertedPosition = getHeaderViewsCount() - 2;
            notifyItemRangeInserted(insertedPosition, 1);
        }
    }

    protected void addFooterView(View footer) {

        if (footer != null) {
            mFooterViews.add(footer);
            int insertedPosition = getItemCount() - 2;
            notifyItemRangeInserted(insertedPosition, 1);
        }
    }

    protected boolean removeHeaderView(View view) {

        boolean removed = false;
        int position = -1;
        if (!mHeaderViews.isEmpty()) {
            position = mHeaderViews.indexOf(view);
            removed = mHeaderViews.remove(view);
        }

        if (removed && position >= 0) {
            notifyItemRemoved(position);
        }
        return removed;
    }

    protected boolean removeFooterView(View view) {

        boolean removed = false;
        int position = -1;
        if (!mFooterViews.isEmpty()) {
            position = mFooterViews.indexOf(view);
            removed = mFooterViews.remove(view);
        }
        if (removed && position >= 0) {
            int removedPosition = getHeaderViewsCount() + mRealAdapter.getItemCount() + position;
            notifyItemRemoved(removedPosition);
        }
        return removed;
    }

    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
}