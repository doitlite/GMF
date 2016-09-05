package com.goldmf.GMFund.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ViewExtension;

import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 15/7/24.
 */
public class NotificationCenterCell extends FrameLayout {

    private View mContentContainer;
    private View mDeleteButton;
    private Delegate mDelegate;
    private OnDeleteButtonClickListener mListener;

    public NotificationCenterCell(Context context) {
        this(context, null);
    }

    public NotificationCenterCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationCenterCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDeleteButton = createDeleteButton();
        addView(mDeleteButton);
        mContentContainer = LayoutInflater.from(getContext()).inflate(R.layout.cell_notification_center, this, false);
        addView(mContentContainer);
        mContentContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams params = mDeleteButton.getLayoutParams();
                params.height = mContentContainer.getHeight();
                mDeleteButton.setLayoutParams(params);
            }
        });
        v_setClick(mDeleteButton, v -> {
            if (mListener != null) mListener.onDeleteButtonClick();
        });
        mContentContainer.setOnTouchListener(new OnTouchListener() {
            private float mDownRawX;
            private float mDownRawY;
            private float mMoveRawX;
            private float mDirection = 0;   // 1 for left、2 for right, －1 for abort
            private float DIRECTION_ABORT = -1;
            private float DIRECTION_UNKNOWN = 0;
            private float DIRECTION_LEFT = 1;
            private float DIRECTION_RIGHT = 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mDelegate.setAllowInterceptTouchEvent(false);
                    mDownRawX = event.getRawX();
                    mDownRawY = event.getRawY();
                    mMoveRawX = mDownRawX;
                    mDirection = DIRECTION_UNKNOWN;
                    return true;
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    resetView(event.getRawX() - mDownRawX);
                    return true;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (mDirection == DIRECTION_UNKNOWN) {
                        final float distanceX = event.getRawX() - mDownRawX;
                        final float distanceY = event.getRawY() - mDownRawY;
                        final float absDistanceX = Math.abs(distanceX);
                        final float absDistanceY = Math.abs(distanceY);
                        final int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

                        if (absDistanceX < scaledTouchSlop && absDistanceY < scaledTouchSlop) {
                            mMoveRawX = event.getRawX();
                            return true;
                        } else if (absDistanceX >= scaledTouchSlop) {
                            mDirection = (distanceX > 0) ? DIRECTION_RIGHT : DIRECTION_LEFT;
                        } else {
                            mDirection = DIRECTION_ABORT;
                            resetView(distanceX);
                        }
                    }
                    if (mDirection == DIRECTION_ABORT) {
                        return false;
                    }

                    final float lastMoveRawX = mMoveRawX;
                    mMoveRawX = event.getRawX();
                    final float deltaX = mMoveRawX - lastMoveRawX;
                    mContentContainer.setTranslationX(mContentContainer.getTranslationX() + deltaX);
                    return true;
                }
                return false;
            }

            private void resetView(float distanceX) {
                mDelegate.setAllowInterceptTouchEvent(true);
                ObjectAnimator animator = ObjectAnimator.ofFloat(mContentContainer, "translationX", shouldShowDeleteButton(distanceX) ? -mDeleteButton.getWidth() : 0);
                animator.setDuration(300L);
                animator.start();
            }

            private boolean shouldShowDeleteButton(float distanceX) {
                return mDirection == DIRECTION_LEFT && Math.abs(distanceX) > mDeleteButton.getWidth() / 2;
            }
        });
    }

    public void setToDefault() {
        if (mContentContainer.getTranslationX() != 0) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mContentContainer, "translationX", 0);
            animator.setDuration(300L);
            animator.start();
        }
    }

    public void setDelegate(Delegate delegate) {
        if (delegate == null)
            mDelegate = Delegate.NULL;
        else
            mDelegate = delegate;
    }

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener) {
        mListener = listener;
    }

    private View createDeleteButton() {
        TextView deleteButton = new TextView(getContext());
        deleteButton.setGravity(Gravity.CENTER);
        deleteButton.setText("删除");

        deleteButton.setBackgroundColor(Color.parseColor("#ff0000"));
        deleteButton.setTextColor(Color.parseColor("#ffffff"));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(200, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        deleteButton.setLayoutParams(params);
        return deleteButton;
    }

    public interface Delegate {
        void setAllowInterceptTouchEvent(boolean value);

        Delegate NULL = new Delegate() {
            @Override
            public void setAllowInterceptTouchEvent(boolean value) {
            }
        };
    }

    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick();
    }
}
