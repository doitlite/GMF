package com.goldmf.GMFund.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import rx.functions.Action2;
import rx.functions.Func2;

/**
 * Created by yale on 15/9/16.
 */
public class SimpleOnItemTouchListener implements RecyclerView.OnItemTouchListener {

    private RecyclerView mRecyclerView;
    private GestureDetector mDetector;
    private Func2<View, Integer, Boolean> mClickCallback;
    private Action2<View, Integer> mLongClickCallback;

    public SimpleOnItemTouchListener(RecyclerView recyclerView, Func2<View, Integer, Boolean> clickCallback) {
        this(recyclerView, clickCallback, null);
    }

    public SimpleOnItemTouchListener(RecyclerView recyclerView, Func2<View, Integer, Boolean> clickCallback, Action2<View, Integer> longClickCallback) {
        Context context = recyclerView.getContext();
        mRecyclerView = recyclerView;
        mClickCallback = clickCallback;
        mLongClickCallback = longClickCallback;
        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mClickCallback != null) {
                    View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (childView != null) {
                        int position = mRecyclerView.getChildAdapterPosition(childView);
                        if (position >= 0) {
                            return mClickCallback.call(childView, position);
                        }
                    }
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mLongClickCallback != null) {
                    View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (childView != null) {
                        int position = mRecyclerView.getChildAdapterPosition(childView);
                        if (position >= 0) {
                            mLongClickCallback.call(childView, position);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return mDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
