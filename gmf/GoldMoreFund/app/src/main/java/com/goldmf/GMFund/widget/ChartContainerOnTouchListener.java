package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.controller.QuotationFragments;
import com.goldmf.GMFund.extension.ListExtension;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.ChartController.ChartInfo;

import java.util.List;

import io.yale.infinitychartview.lib.ChartViewContainer;
import io.yale.infinitychartview.lib.LayoutDelegate;
import rx.functions.Action0;
import rx.functions.Action3;

import static android.view.View.OnTouchListener;

/**
 * Created by Evan on 16/4/13 上午10:30.
 */
public class ChartContainerOnTouchListener implements OnTouchListener {

    private int gestureState = STATE_GESTURE_NONE;
    public static final int STATE_GESTURE_NONE = 0;
    public static final int STATE_GESTURE_DOWN = 1;
    public static final int STATE_GESTURE_MOVE = 2;
    public static final int STATE_GESTURE_LONG_DOWN = 3;
    public static final int STATE_GESTURE_LONG_MOVE = 4;
    private final static int MIN_MOVE_DISTANCE = 15;

    private ChartViewContainer<ChartInfo> mContainer;
    private Action0 mClickCallBack;
    private Action3<Float, ChartInfo, List<ChartInfo>> mLongClickCallBack;
    private Action0 mLongClickUpCallBack;
    private boolean mIsConsume;
    private int mChartType;
    private GestureDetector mDetector;
    private int mTouchY;
    private int mTouchX;
    private float drawLineX;
    private float mDownX;
    private float mDownY;
    private float mMoveX;
    private float mMoveY;
    private long mTouchDownTime;

    private LayoutDelegate.PageInfo<ChartInfo> pageInfo = new LayoutDelegate.PageInfo<>();


    public ChartContainerOnTouchListener(ChartViewContainer<ChartInfo> container, Action0 clickCallBack, Action3<Float, ChartInfo, List<ChartInfo>> longClickCallBack, Action0 longClickUpCallBack, boolean isConsume, int chartType) {
        Context context = container.getContext();
        mContainer = container;
        mClickCallBack = clickCallBack;
        mLongClickCallBack = longClickCallBack;
        mLongClickUpCallBack = longClickUpCallBack;
        mIsConsume = isConsume;
        mChartType = chartType;

        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent event) {
                if (gestureState == STATE_GESTURE_DOWN) {
                    mContainer.getParent().requestDisallowInterceptTouchEvent(true);
                    performLongClickEvent(event);
                    gestureState = STATE_GESTURE_LONG_MOVE;
                }
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mTouchDownTime = System.currentTimeMillis();
            mDownX = event.getX();
            mDownY = event.getY();
            gestureState = STATE_GESTURE_DOWN;
        } else if (action == MotionEvent.ACTION_MOVE) {
            mMoveX = event.getX();
            mMoveY = event.getY();
            long touchUpTime = System.currentTimeMillis();
            long pressDuration = touchUpTime - mTouchDownTime;
            if (pressDuration < ViewConfiguration.getLongPressTimeout()) {
                if (Math.abs(mMoveX - mDownX) < MIN_MOVE_DISTANCE) {
                    gestureState = STATE_GESTURE_DOWN;
                } else {
                    gestureState = STATE_GESTURE_MOVE;
                }
            } else {
                if (Math.abs(mMoveX - mDownX) < MIN_MOVE_DISTANCE) {
                    performLongClickEvent(event);
                }
            }

            if (gestureState == STATE_GESTURE_DOWN || gestureState == STATE_GESTURE_MOVE) {

                if (Math.abs(mMoveX - mDownX) > Math.abs(mMoveY - mDownY)) {
                    mContainer.getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    mContainer.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return mIsConsume;
            } else if (gestureState == STATE_GESTURE_LONG_MOVE) {
                performLongClickEvent(event);
                mContainer.getParent().requestDisallowInterceptTouchEvent(true);
                gestureState = STATE_GESTURE_LONG_MOVE;
                return true;
            }
        } else if (action == MotionEvent.ACTION_UP) {

            if (gestureState == STATE_GESTURE_DOWN) {
                mClickCallBack.call();
            } else if (gestureState == STATE_GESTURE_MOVE) {
                return mIsConsume;
            } else if (gestureState == STATE_GESTURE_LONG_MOVE) {
                mLongClickUpCallBack.call();
                return true;
            }
        }
        return mDetector.onTouchEvent(event);
    }

    private void performLongClickEvent(MotionEvent event) {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDSimpleLongTouch);

        mTouchX = (int) (event.getRawX());
        mTouchY = (int) (event.getRawY());

        if (event.getX() >= 0 && event.getX() <= mContainer.getWidth() && event.getY() >= 0 && event.getY() <= mContainer.getHeight()) {
            mContainer.findPageInfoUnder(mTouchX, mTouchY, pageInfo);
            Rect mHitRect = pageInfo.hitRect;
            Rect mDrawRect = pageInfo.drawingRect;
            Point mPoint = pageInfo.pointRelativeByRect;
            int candleCount = pageInfo.maxDataCountPerPage;
            List<ChartInfo> currentPageData = pageInfo.pageData;
            List<ChartInfo> visibleData = pageInfo.visibleData;

            if (pageInfo.childViewOrNil == null)
                return;

            ChartInfo chartinfo;
            if (mChartType == QuotationFragments.StockDetailFragment.TYPE_TIMES_TLINE) {
                float candleWith = mHitRect.width() * 1f / (candleCount - 1);
                int defCount = candleCount - currentPageData.size();
                List<Double> priceData = Stream.of(currentPageData).filter(info -> info.last > -1).map(info -> info.last).collect(Collectors.toList());

                if (mPoint.x > (priceData.size() - 1) * candleWith)
                    return;

                int leftIndex = (int) ((mPoint.x - mHitRect.left) * 1f / (mHitRect.width() - defCount * candleWith) * currentPageData.size());
                float leftIndexOffset = mHitRect.left + leftIndex * candleWith;

                if (leftIndex == currentPageData.size() - 2) {
                    chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1);
                    drawLineX = leftIndexOffset;
                } else {
                    if (Math.abs(mPoint.x - leftIndex * candleWith) <= Math.abs(mPoint.x - (leftIndex + 1) * candleWith)) {
                        chartinfo = ListExtension.getFromList(currentPageData, leftIndex);
                        drawLineX = mHitRect.left + leftIndex * candleWith - mDrawRect.left;
                    } else {
                        chartinfo = ListExtension.getFromList(currentPageData, leftIndex + 1);
                        drawLineX = mHitRect.left + (leftIndex + 1) * candleWith - mDrawRect.left;
                    }
                }
            } else {
                float candleWith = mHitRect.width() * 1f / candleCount;
                int defCount = candleCount - currentPageData.size();
                float candleSpace = (candleWith - candleWith / 4) / 2;

                int leftIndex = (int) ((mPoint.x - candleSpace - mHitRect.left) / mHitRect.width() * candleCount) - defCount;
                float leftIndexOffset = mHitRect.left + leftIndex * candleWith + candleSpace + defCount * candleWith;

                if (mHitRect.left == mDrawRect.left) {
                    if (leftIndex == 0) {
                        if (leftIndexOffset >= mDrawRect.left && mPoint.x < leftIndexOffset) {
                            chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1 - leftIndex);
                            drawLineX = leftIndexOffset + mContainer.getWidth() - mDrawRect.right;
                        } else if (leftIndexOffset < mDrawRect.left) {
                            chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1 - (leftIndex + 1));
                            drawLineX = mHitRect.left + (leftIndex + 1) * candleWith + candleSpace + defCount * candleWith + mContainer.getWidth() - mDrawRect.right;
                        } else {
                            if (Math.abs(mPoint.x - leftIndexOffset) <= Math.abs(mPoint.x - (mHitRect.left + (leftIndex + 1) * candleWith + candleSpace))) {
                                chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1 - leftIndex);
                                drawLineX = leftIndexOffset + mContainer.getWidth() - mDrawRect.right;
                            } else {
                                chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1 - (leftIndex + 1));
                                drawLineX = mHitRect.left + (leftIndex + 1) * candleWith + candleSpace + defCount * candleWith + mContainer.getWidth() - mDrawRect.right;
                            }
                        }
                    } else if (leftIndex == currentPageData.size() - 1) {
                        chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1 - leftIndex);
                        drawLineX = leftIndexOffset + mContainer.getWidth() - mDrawRect.right;
                    } else {
                        if (Math.abs(mPoint.x - leftIndexOffset) <= Math.abs(mPoint.x - (mHitRect.left + (leftIndex + 1) * candleWith + candleSpace))) {
                            chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1 - leftIndex);
                            drawLineX = leftIndexOffset + mContainer.getWidth() - mDrawRect.right;
                        } else {
                            chartinfo = ListExtension.getFromList(currentPageData, currentPageData.size() - 1 - (leftIndex + 1));
                            drawLineX = mHitRect.left + (leftIndex + 1) * candleWith + candleSpace + mContainer.getWidth() - mDrawRect.right;
                        }
                    }

                } else {
                    if (leftIndex == 0) {
                        if (leftIndexOffset >= mDrawRect.left && mPoint.x < leftIndexOffset) {
                            chartinfo = ListExtension.getFromList(currentPageData,currentPageData.size() - 1 - leftIndex);
                            drawLineX = leftIndexOffset - mDrawRect.left;
                        } else if (leftIndexOffset < mDrawRect.left) {
                            chartinfo = ListExtension.getFromList(currentPageData,currentPageData.size() - 1 - (leftIndex + 1));
                            drawLineX = mHitRect.left + (leftIndex + 1) * candleWith + candleSpace + defCount * candleWith - mDrawRect.left;
                        } else {
                            if (Math.abs(mPoint.x - leftIndexOffset) <= Math.abs(mPoint.x - (mHitRect.left + (leftIndex + 1) * candleWith + candleSpace))) {
                                chartinfo = ListExtension.getFromList(currentPageData,currentPageData.size() - 1 - leftIndex);
                                drawLineX = leftIndexOffset - mDrawRect.left;
                            } else {
                                chartinfo = ListExtension.getFromList(currentPageData,currentPageData.size() - 1 - (leftIndex + 1));
                                drawLineX = mHitRect.left + (leftIndex + 1) * candleWith + candleSpace + defCount * candleWith - mDrawRect.left;
                            }
                        }
                    } else if (leftIndex == currentPageData.size() - 1) {
                        chartinfo = ListExtension.getFromList(currentPageData,currentPageData.size() - 1 - leftIndex);
                        drawLineX = leftIndexOffset - mDrawRect.left;
                    } else {
                        if (Math.abs(mPoint.x - leftIndexOffset) <= Math.abs(mPoint.x - (mHitRect.left + (leftIndex + 1) * candleWith + candleSpace + defCount * candleWith))) {
                            chartinfo = ListExtension.getFromList(currentPageData,currentPageData.size() - 1 - leftIndex);
                            drawLineX = leftIndexOffset - mDrawRect.left;
                        } else {
                            chartinfo = ListExtension.getFromList(currentPageData,currentPageData.size() - 1 - (leftIndex + 1));
                            drawLineX = mHitRect.left + (leftIndex + 1) * candleWith + candleSpace + defCount * candleWith - mDrawRect.left;
                        }
                    }
                }

            }
            mLongClickCallBack.call(drawLineX, chartinfo, visibleData);
        }
    }


}
