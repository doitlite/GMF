package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.extension.ListExtension;

import java.util.List;

import rx.functions.Action2;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.goldmf.GMFund.extension.ListExtension.splitFromList;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 16/2/27.
 */
public class StaticTableView extends LinearLayout {
    public StaticTableView(Context context) {
        this(context, null);
    }

    public StaticTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StaticTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    public <T> Builder<T> resetTable(List<T> items, int columnNum) {
        return new Builder<>(this, items, columnNum);
    }

    public static <T> void resetTable(StaticTableView table, List<T> items, int columnNum, Func1<ViewGroup, View> onCreateItemView, Action2<View, T> onConfigureItemView, Action2<View, T> onClick) {
        List<List<T>> chunks = splitFromList(items, columnNum);
        Stream.of(chunks).forEach(it -> {
            while (it.size() < columnNum) {
                it.add(null);
            }
        });

        Func2<LinearLayout, T, ViewGroup> toColumn = (parent, item) -> {
            FrameLayout column = new FrameLayout(parent.getContext());
            LayoutParams params = new LayoutParams(0, -2);
            params.weight = 1;
            column.setLayoutParams(params);

            if (item != null) {
                View view = onCreateItemView.call(parent);
                onConfigureItemView.call(view, item);
                if (onClick != null) {
                    view.setOnClickListener(v -> onClick.call(v, item));
                }
                column.addView(view);
            }

            return column;
        };

        Func1<LinearLayout, View> newLine = parent -> {
            View view = new View(parent.getContext());
            view.setBackgroundColor(0x4D979797);
            LayoutParams params = new LayoutParams(dp2px(1), dp2px(72));
            params.gravity = Gravity.CENTER_VERTICAL;
            view.setLayoutParams(params);
            return view;
        };

        Func2<LinearLayout, List<T>, LinearLayout> toRow = (parent, item) -> {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams params = new LayoutParams(-1, -2);
            row.setLayoutParams(params);
            Stream.of(item)
                    .map(it -> toColumn.call(row, it))
                    .forEach(it -> {
                        if (row.getChildCount() > 0 && it.getChildCount() > 0) {
                            row.addView(newLine.call(row));
                        }
                        row.addView(it);
                    });
            return row;
        };


        table.removeAllViewsInLayout();
        Stream.of(chunks)
                .map(it -> toRow.call(table, it))
                .forEach(it -> table.addView(it));
    }

    public static class Builder<T> {
        private StaticTableView mTableView;
        private List<T> mItems;
        private int mColumnNum;
        private Func1<ViewGroup, View> mOnCreateItemView;
        private Action2<View, T> mOnConfigureItemView;
        private Action2<View, T> mOnClick;

        public Builder(StaticTableView tableView, List<T> items, int columnNum) {
            mTableView = tableView;
            mItems = items;
            mColumnNum = columnNum;
        }

        public Builder<T> setOnCreateItemView(Func1<ViewGroup, View> onCreateItemView) {
            mOnCreateItemView = onCreateItemView;
            return this;
        }

        public Builder<T> setOnCreateItemView(int layoutId) {
            mOnCreateItemView = parent -> LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return this;
        }

        public Builder<T> setOnConfigureItemView(Action2<View, T> onConfigureItemView) {
            mOnConfigureItemView = onConfigureItemView;
            return this;
        }

        public Builder<T> setOnClick(Action2<View, T> onClick) {
            mOnClick = onClick;
            return this;
        }

        public void done() {
            resetTable(mTableView, mItems, mColumnNum, mOnCreateItemView, mOnConfigureItemView, mOnClick);
        }
    }
}
