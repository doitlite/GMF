package yale.extension.system;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import rx.functions.Action2;
import rx.functions.Func1;

/**
 * Created by yale on 16/2/26.
 */
public abstract class SimpleListViewAdapter<T> extends BaseAdapter {
    private List<T> mItems;
    private int mCount;

    public SimpleListViewAdapter(List<T> items) {
        mItems = items;
        mCount = mItems.size();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class Builder<T> {
        private List<T> mItems;
        private Func1<ViewGroup, View> mCreateItemViewFunc;
        private Func1<SimpleViewHolder.Builder<T>, SimpleViewHolder<T>> mCreateViewHolderFunc;
        private Action2<SimpleListViewAdapter<T>, SimpleViewHolder<T>> mViewHolderCreatedFunc;

        public Builder(List<T> items) {
            mItems = items;
        }

        public Builder<T> onCreateItemView(Func1<ViewGroup, View> operation) {
            mCreateItemViewFunc = operation;
            return this;
        }

        public Builder<T> onCreateItemView(int layoutId) {
            mCreateItemViewFunc = parent -> LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return this;
        }

        public Builder<T> onCreateViewHolder(Func1<SimpleViewHolder.Builder<T>, SimpleViewHolder<T>> operation) {
            mCreateViewHolderFunc = operation;
            return this;
        }

        public Builder<T> onViewHolderCreated(Action2<SimpleListViewAdapter<T>, SimpleViewHolder<T>> operation) {
            mViewHolderCreatedFunc = operation;
            return this;
        }

        public SimpleListViewAdapter<T> create() {
            return new SimpleListViewAdapter<T>(mItems) {
                @SuppressWarnings("unchecked")
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = mCreateItemViewFunc.call(parent);
                        SimpleViewHolder<T> holder = mCreateViewHolderFunc.call(new SimpleViewHolder.Builder<>(convertView));
                        convertView.setTag(holder);
                        if (mViewHolderCreatedFunc != null) {
                            mViewHolderCreatedFunc.call(this, holder);
                        }
                    }
                    SimpleViewHolder<T> holder = (SimpleViewHolder<T>) convertView.getTag();
                    holder.configureView(getItem(position), position);
                    return convertView;
                }
            };
        }
    }
}
