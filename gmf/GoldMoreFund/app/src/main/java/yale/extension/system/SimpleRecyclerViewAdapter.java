package yale.extension.system;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.functions.Action2;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.goldmf.GMFund.extension.ViewExtension.v_removeFromParent;

/**
 * Created by yale on 16/2/22.
 */
public abstract class SimpleRecyclerViewAdapter<T> extends RecyclerView.Adapter<SimpleViewHolder<T>> {
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIew_TYPE_FOOTER = 2;

    private List<T> mItems;
    private List<View> mHeaderViews;
    private List<View> mFooterViews;
    private int mHeaderCount;
    private int mFooterCount;
    private int mItemCount;
    private int mTotalCount;
    private Object mUserData;

    public SimpleRecyclerViewAdapter(List<T> items) {
        mItems = items;
        mHeaderViews = new LinkedList<>();
        mFooterViews = new LinkedList<>();
        computeItemCount();
    }

    public void setUserData(Object userData) {
        mUserData = userData;
    }

    public Object getUserData() {
        return mUserData;
    }

    @Override
    public SimpleViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(this, parent, viewType);
    }

    public abstract SimpleViewHolder<T> onCreateViewHolder(SimpleRecyclerViewAdapter<T> adapter, ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(SimpleViewHolder<T> holder, int position) {
        onBindViewHolder(this, holder, position);
    }

    public abstract void onBindViewHolder(SimpleRecyclerViewAdapter<T> adapter, SimpleViewHolder<T> holder, int position);

    @Override
    public int getItemCount() {
        return mTotalCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaderCount) {
            return VIEW_TYPE_HEADER;
        }

        position -= mHeaderCount;

        if (position < mItemCount) {
            return VIEW_TYPE_NORMAL;
        }

        return VIew_TYPE_FOOTER;
    }

    public List<T> getItems() {
        return mItems;
    }

    public T getItem(int position) {
        return mItems.get(position - mHeaderCount);
    }

    public View getHeader(int position) {
        return mHeaderViews.get(position);
    }

    public View getFooter(int position) {
        return mFooterViews.get(position);
    }

    public void resetItems(List<T> items) {
        mItems = items;
        computeItemCount();
        notifyDataSetChanged();
    }

    public void addItems(int location, List<T> items) {
        mItems.addAll(location, items);
        computeItemCount();
        notifyDataSetChanged();
    }

    public void addItems(List<T> items) {
        mItems.addAll(items);
        computeItemCount();
        notifyDataSetChanged();
    }

    public View createHeaderView(Fragment fragment, int layoutID) {
        return createHeaderView(fragment.getActivity(), layoutID);
    }

    public View createHeaderView(Context context, int layoutID) {
        FrameLayout tempParent = new FrameLayout(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layoutID, tempParent, false);
    }

    public View createHeaderView(Context context, Func2<ViewGroup, LayoutInflater, View> operation) {
        FrameLayout tempParent = new FrameLayout(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        return operation.call(tempParent, inflater);
    }

    public View createFooterView(Fragment fragment, int layoutID) {
        return createHeaderView(fragment.getActivity(), layoutID);
    }

    public View createFooterView(Context context, int layoutID) {
        return createHeaderView(context, layoutID);
    }

    public View createFooterView(Context context, Func2<ViewGroup, LayoutInflater, View> operation) {
        return createHeaderView(context, operation);
    }


    public void addHeader(View header) {
        if (header == null)
            return;

        mHeaderViews.add(header);
        computeItemCount();
        notifyDataSetChanged();
    }

    public void addFooter(View footer) {
        if (footer == null)
            return;

        mFooterViews.add(footer);
        computeItemCount();
        notifyDataSetChanged();
    }

    private void computeItemCount() {
        mHeaderCount = mHeaderViews.size();
        mItemCount = mItems.size();
        mFooterCount = mFooterViews.size();
        mTotalCount = mHeaderCount + mItemCount + mFooterCount;
    }

    public static class Builder<T> {
        private List<T> mItems;
        private Func2<ViewGroup, Integer, View> mOnCreateItemViewFuncRef;
        private Func1<SimpleViewHolder.Builder<T>, SimpleViewHolder<T>> mOnCreateViewHolderFuncRef;
        private Action2<SimpleRecyclerViewAdapter<T>, SimpleViewHolder<T>> mOnViewHolderCreatedFuncRef;

        public Builder(List<T> items) {
            mItems = items;
        }

        public Builder(T value, int count) {
            ArrayList<T> items = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                items.add(value);
            }
            mItems = items;
        }

        public Builder<T> onCreateItemView(Func2<ViewGroup, Integer, View> operation) {
            mOnCreateItemViewFuncRef = operation;
            return this;
        }

        public Builder<T> onCreateItemView(int layoutId) {
            mOnCreateItemViewFuncRef = (parent, viewType) -> LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return this;
        }

        public Builder<T> onCreateViewHolder(Func1<SimpleViewHolder.Builder<T>, SimpleViewHolder<T>> operation) {
            mOnCreateViewHolderFuncRef = operation;
            return this;
        }

        public Builder<T> onViewHolderCreated(Action2<SimpleRecyclerViewAdapter<T>, SimpleViewHolder<T>> operation) {
            mOnViewHolderCreatedFuncRef = operation;
            return this;
        }

        public SimpleRecyclerViewAdapter<T> create() {
            return createImpl(mItems, mOnCreateItemViewFuncRef, mOnCreateViewHolderFuncRef, mOnViewHolderCreatedFuncRef);
        }

        public static <T> SimpleRecyclerViewAdapter<T> createImpl(List<T> items,
                                                                  Func2<ViewGroup, Integer, View> onCreateItemViewFuncRef,
                                                                  Func1<SimpleViewHolder.Builder<T>, SimpleViewHolder<T>> onCreateViewHolderFuncRef,
                                                                  Action2<SimpleRecyclerViewAdapter<T>, SimpleViewHolder<T>> onViewHolderCreatedFuncRef) {
            return new SimpleRecyclerViewAdapter<T>(items) {
                @Override
                public SimpleViewHolder<T> onCreateViewHolder(SimpleRecyclerViewAdapter<T> adapter, ViewGroup parent, int viewType) {
                    if (viewType == VIEW_TYPE_HEADER || viewType == VIew_TYPE_FOOTER) {
                        return createEdgeViewHolder(parent.getContext());
                    }

                    View itemView = onCreateItemViewFuncRef.call(parent, viewType);
                    SimpleViewHolder<T> holder = onCreateViewHolderFuncRef.call(new SimpleViewHolder.Builder<>(itemView));
                    if (onViewHolderCreatedFuncRef != null) {
                        onViewHolderCreatedFuncRef.call(this, holder);
                    }
                    return holder;
                }

                @SuppressWarnings("unchecked")
                public void onBindViewHolder(SimpleRecyclerViewAdapter<T> adapter, SimpleViewHolder<T> holder, int position) {
                    int viewType = adapter.getItemViewType(position);
                    if (viewType == VIEW_TYPE_NORMAL) {
                        T item = adapter.getItem(position);
                        holder.configureView(item, position);
                    } else if (viewType == VIEW_TYPE_HEADER) {
                        Object header = getHeader(position);
                        holder.configureView((T) header, position);
                    } else if (viewType == VIew_TYPE_FOOTER) {
                        Object footer = getFooter(position - adapter.mHeaderCount - adapter.mItemCount);
                        holder.configureView((T) footer, position);
                    }
                }
            };
        }

        @SuppressWarnings("unchecked")
        private static <T> SimpleViewHolder<T> createEdgeViewHolder(Context context) {
            FrameLayout wrapper = new FrameLayout(context);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, -2);
            wrapper.setLayoutParams(params);
            return new SimpleViewHolder(wrapper) {
                @Override
                public void configureView(Object child, int pos) {
                    View castChild = (View) child;
                    v_removeFromParent(castChild);
                    wrapper.removeAllViewsInLayout();
                    wrapper.addView(castChild);
                }
            };
        }
    }
}
