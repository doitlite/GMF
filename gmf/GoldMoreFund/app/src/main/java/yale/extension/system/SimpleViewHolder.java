package yale.extension.system;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import rx.functions.Action2;
import rx.functions.Action3;
import rx.functions.Func1;

/**
 * Created by yale on 16/2/22.
 */
public abstract class SimpleViewHolder<T> extends RecyclerView.ViewHolder {
    public SimpleViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void configureView(T item, int pos);

    public static class Builder<T> {
        public View mItemView;
        private Action3<SimpleViewHolder<T>, T, Integer> mConfigureViewFuncRef;
        private SparseArray<View> mViewDict = new SparseArray<>();

        public Builder(View itemView) {
            mItemView = itemView;
        }

        public Builder<T> configureView(Action2<T, Integer> operation) {
            if (operation != null) {
                mConfigureViewFuncRef = (holder, item, pos) -> {
                    operation.call(item, pos);
                };
            } else {
                mConfigureViewFuncRef = null;
            }

            return this;
        }

        public Builder<T> configureView(Action3<SimpleViewHolder<T>, T, Integer> operation) {
            mConfigureViewFuncRef = operation;
            return this;
        }

        public Builder<T> bindChildWithTag(String tag, Func1<View, View> findChild) {
            mViewDict.put(tag.hashCode(), findChild.call(mItemView));
            return this;
        }

        public Builder<T> bindChildWithTag(String tag, int viewId) {
            mViewDict.put(tag.hashCode(), mItemView.findViewById(viewId));
            return this;
        }

        public Builder<T> bindChildWithTag(String tag, int... viewId) {
            View child = null;
            for (int childID : viewId) {
                child = mItemView.findViewById(childID);
            }
            mViewDict.put(tag.hashCode(), child);
            return this;
        }

        public <R extends View> R getChildWithTag(String tag) {
            return (R) mViewDict.get(tag.hashCode());
        }

        public SimpleViewHolder<T> create() {
            return createImpl(mItemView, mConfigureViewFuncRef);
        }

        private static <T> SimpleViewHolder<T> createImpl(View itemView, Action3<SimpleViewHolder<T>, T, Integer> configureView) {
            return new SimpleViewHolder<T>(itemView) {
                @Override
                public void configureView(T item, int pos) {
                    configureView.call(this, item, pos);
                }

            };
        }
    }
}
