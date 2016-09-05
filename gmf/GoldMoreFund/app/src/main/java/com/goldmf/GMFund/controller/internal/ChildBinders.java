package com.goldmf.GMFund.controller.internal;

import android.util.SparseArray;
import android.view.View;

import yale.extension.system.SimpleViewHolder;

/**
 * Created by yalez on 2016/7/29.
 */
public class ChildBinders {
    private ChildBinders() {
    }

    public static ChildBinder createWithView(View itemView) {
        return new ChildBinder() {
            private SparseArray<View> mViewDic = new SparseArray<>();

            @Override
            public View itemView() {
                return itemView;
            }

            @Override
            public ChildBinder bindChildWithTag(String tag, int... layoutIDs) {
                View target = itemView;
                for (int layoutID : layoutIDs) {
                    target = target.findViewById(layoutID);
                }
                mViewDic.put(tag.hashCode(), target);
                return this;
            }

            @Override
            public <T extends View> T getChildWithTag(String tag) {
                View child = mViewDic.get(tag.hashCode());
                return (T) child;
            }
        };
    }

    public static ChildBinder createWithBuilder(SimpleViewHolder.Builder<?> builder) {
        return new ChildBinder() {

            @Override
            public View itemView() {
                return builder.mItemView;
            }

            @Override
            public ChildBinder bindChildWithTag(String tag, int... layoutIDs) {
                builder.bindChildWithTag(tag, layoutIDs);
                return this;
            }

            @Override
            public <T extends View> T getChildWithTag(String tag) {
                return builder.getChildWithTag(tag);
            }
        };
    }
}
