package yale.extension.system;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by yale on 16/2/25.
 */
public class SimpleOnTouchEventHandler {
    private Handler mHandler;
    private ViewConfiguration mConfiguration;
    private long mIntervalInMills;
    private boolean mIsTouchUp = true;

    public SimpleOnTouchEventHandler(Context context) {
        this(context, 100);
    }

    public SimpleOnTouchEventHandler(Context context, long intervalInMills) {
        mHandler = new Handler(context.getMainLooper());
        mConfiguration = ViewConfiguration.get(context);
        mIntervalInMills = intervalInMills;
    }

    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            onRepeat();
            if (!mIsTouchUp && !mHasMoveOut) {
                mHandler.postDelayed(this, mIntervalInMills);
            }
        }
    };

    private long mTouchDownTime;
    private boolean mHasMoveOut = false;

    public boolean onTouchEvent(View view, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mTouchDownTime = System.currentTimeMillis();
            mIsTouchUp = false;
            mHasMoveOut = false;
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(mTask, ViewConfiguration.getLongPressTimeout());
            onDown(event);
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!mHasMoveOut) {
                float x = event.getX();
                float y = event.getY();
                mHasMoveOut = x < 0 || y < 0 || x > view.getMeasuredWidth() || y > view.getMeasuredHeight();
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mHandler.removeCallbacksAndMessages(null);
            mIsTouchUp = true;
            if (!mHasMoveOut) {
                long touchUpTime = System.currentTimeMillis();
                long pressDuration = touchUpTime - mTouchDownTime;
                if (pressDuration < ViewConfiguration.getLongPressTimeout()) {
                    onPress(event);
                } else {
                    onLongPress(event);
                }
            }
            mHasMoveOut = true;
        }
        return true;
    }

    public void onDown(MotionEvent event) {
    }

    public void onRepeat() {
    }

    public void onPress(MotionEvent event) {
    }

    public void onLongPress(MotionEvent event) {
    }
}
