package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.goldmf.GMFund.controller.internal.SignalColorHolder;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 15/10/28.
 */
public class BonusFundBubble extends View {
    private int CENTER_RADIUS = 0;
    private final int CENTER_RADIUS_OFFSET;
    private final int CENTER_BORDER_WIDTH;
    private static final int CENTER_BORDER_COLOR = 0xFFFFDE00;
    private static final int CENTER_FILL_COLOR = 0xFFD65645;
    private static final int CENTER_ALPHA = 255;

    private int OUTER_1_RADIUS = 0;
    private final int OUTER_1_RADIUS_OFFSET;
    private final int OUTER_1_BORDER_WIDTH;
    private static final int OUTER_1_BORDER_COLOR = 0xFFFFDE00;
    private static final int OUTER_1_ALPHA = 204;

    private int OUTER_2_RADIUS = 0;
    private final int OUTER_2_RADIUS_OFFSET;
    private final int OUTER_2_BORDER_WIDTH;
    private static final int OUTER_2_BORDER_COLOR = 0xFFFFDE00;
    private static final int OUTER_2_ALPHA = 102;

    private int OUTER_3_RADIUS = 0;
    private final int OUTER_3_RADIUS_OFFSET;
    private final int OUTER_3_BORDER_WIDTH;
    private static final int OUTER_3_BORDER_COLOR = 0xFFFFDE00;
    private static final int OUTER_3_ALPHA = 26;

    private static final int WAVE_FRONT_COLOR = 0xFFFFDE00;
    private static final int WAVE_FRONT_ALPHA = 255;
    private static final int WAVE_BEHIND_COLOR = 0xFFFFDE00;
    private static final int WAVE_BEHIND_ALPHA = 51;
    private int WAVE_BASE_HEIGHT = 0;
    private int WAVE_WIDTH = 0;
    private int DOUBLE_WAVE_WIDTH = 0;
    private int HORIZONTAL_STEP_IN_PX = 0;
    private static final int WAVE_FLOW_SPEED_FRONT = 4;
    private static final int WAVE_FLOW_SPEED_BEHIND = 2;

    private int CENTER_X = 0;
    private int CENTER_Y = 0;
    private int MAX_WIDTH = 0;
    private static final int UPDATE_INTERVAL_IN_MILLS = 33;

    private Handler mHandler = new Handler();
    private Path mPath;
    private final Paint mPaint;
    private int mOffsetXFront = 0;
    private int mOffsetXBehind = 0;
    private Runnable mFlowTask = new Runnable() {
        @Override
        public void run() {
            computeOffsetX();
            invalidate();
            mHandler.postDelayed(this, UPDATE_INTERVAL_IN_MILLS);
        }
    };


    public BonusFundBubble(Context context) {
        this(context, null);
    }

    public BonusFundBubble(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BonusFundBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if (isInEditMode()) {
            SignalColorHolder.init(context);
        }

        CENTER_RADIUS_OFFSET = dp2px(this, 24);
        CENTER_BORDER_WIDTH = dp2px(this, 4);

        OUTER_1_RADIUS_OFFSET = dp2px(this, 16);
        OUTER_1_BORDER_WIDTH = dp2px(this, 2);

        OUTER_2_RADIUS_OFFSET = dp2px(this, 8);
        OUTER_2_BORDER_WIDTH = dp2px(this, 2);

        OUTER_3_RADIUS_OFFSET = 0;
        OUTER_3_BORDER_WIDTH = dp2px(this, 2);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPath = new Path();
    }

    public void onPause() {
        mHandler.removeCallbacks(mFlowTask);
    }

    public void onResume() {
        mHandler.removeCallbacks(mFlowTask);
        mFlowTask.run();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) return;

        mHandler.removeCallbacks(mFlowTask);
        mFlowTask.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isInEditMode()) return;

        mHandler.removeCallbacks(mFlowTask);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int maxRadius = Math.min(getMeasuredWidth(), getMeasuredHeight()) >> 1;
        CENTER_X = getMeasuredWidth() >> 1;
        CENTER_Y = getMeasuredHeight() >> 1;
        WAVE_BASE_HEIGHT = getMeasuredHeight() * 60 / 100;
        MAX_WIDTH = getMeasuredWidth();
        HORIZONTAL_STEP_IN_PX = getMeasuredWidth() >> 3;
        WAVE_WIDTH = HORIZONTAL_STEP_IN_PX << 1;
        DOUBLE_WAVE_WIDTH = WAVE_WIDTH << 1;
        CENTER_RADIUS = maxRadius - CENTER_RADIUS_OFFSET - CENTER_BORDER_WIDTH;
        OUTER_1_RADIUS = maxRadius - OUTER_1_RADIUS_OFFSET - OUTER_1_BORDER_WIDTH;
        OUTER_2_RADIUS = maxRadius - OUTER_2_RADIUS_OFFSET - OUTER_2_BORDER_WIDTH;
        OUTER_3_RADIUS = maxRadius - OUTER_3_RADIUS_OFFSET - OUTER_3_BORDER_WIDTH;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw center circle
        mPaint.setXfermode(null);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(CENTER_BORDER_WIDTH);
        mPaint.setColor(CENTER_BORDER_COLOR);
        mPaint.setAlpha(CENTER_ALPHA);
        canvas.drawCircle(CENTER_X, CENTER_Y, CENTER_RADIUS, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(CENTER_FILL_COLOR);
        canvas.drawCircle(CENTER_X, CENTER_Y, CENTER_RADIUS, mPaint);

        // draw outer 1 circle
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(OUTER_1_BORDER_WIDTH);
        mPaint.setColor(OUTER_1_BORDER_COLOR);
        mPaint.setAlpha(OUTER_1_ALPHA);
        canvas.drawCircle(CENTER_X, CENTER_Y, OUTER_1_RADIUS, mPaint);


        // draw outer 2 circle
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(OUTER_2_BORDER_WIDTH);
        mPaint.setColor(OUTER_2_BORDER_COLOR);
        mPaint.setAlpha(OUTER_2_ALPHA);
        canvas.drawCircle(CENTER_X, CENTER_Y, OUTER_2_RADIUS, mPaint);


        // draw outer 3 circle
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(OUTER_3_BORDER_WIDTH);
        mPaint.setColor(OUTER_3_BORDER_COLOR);
        mPaint.setAlpha(OUTER_3_ALPHA);
        canvas.drawCircle(CENTER_X, CENTER_Y, OUTER_3_RADIUS, mPaint);


        // clip path
        canvas.save();
        mPath.reset();
        mPath.addCircle(CENTER_X, CENTER_Y, CENTER_RADIUS + 1, Path.Direction.CW);
        canvas.clipPath(mPath);

        // draw water behind
        mPath.reset();
        mPath.moveTo(0, WAVE_BASE_HEIGHT);
        float startX = MAX_WIDTH - mOffsetXBehind + DOUBLE_WAVE_WIDTH + WAVE_WIDTH;
        float startY = WAVE_BASE_HEIGHT;
        int vertical = 1;
        mPath.moveTo(startX, startY);
        while (startX > 0) {
            mPath.quadTo(startX - HORIZONTAL_STEP_IN_PX, vertical == 1 ? WAVE_BASE_HEIGHT - 20 : WAVE_BASE_HEIGHT + 20, startX - WAVE_WIDTH, WAVE_BASE_HEIGHT);
            vertical = vertical == 1 ? 0 : 1;
            startX -= WAVE_WIDTH;
        }
        mPath.lineTo(0, getHeight());
        mPath.lineTo(MAX_WIDTH, getHeight());
        mPath.close();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(WAVE_BEHIND_COLOR);
        mPaint.setAlpha(WAVE_BEHIND_ALPHA);
        canvas.drawPath(mPath, mPaint);

        // draw water front
        mPath.reset();
        mPath.moveTo(0, WAVE_BASE_HEIGHT);
        startX = mOffsetXFront - DOUBLE_WAVE_WIDTH;
        startY = WAVE_BASE_HEIGHT;
        vertical = 1;
        mPath.moveTo(startX, startY);
        while (startX < MAX_WIDTH) {
            mPath.quadTo(startX + HORIZONTAL_STEP_IN_PX, vertical == 1 ? WAVE_BASE_HEIGHT - 20 : WAVE_BASE_HEIGHT + 20, startX + WAVE_WIDTH, WAVE_BASE_HEIGHT);
            vertical = vertical == 1 ? 0 : 1;
            startX += WAVE_WIDTH;
        }

        mPath.lineTo(MAX_WIDTH, getHeight());
        mPath.lineTo(0, getHeight());
        mPath.close();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(WAVE_FRONT_COLOR);
        mPaint.setAlpha(WAVE_FRONT_ALPHA);
        canvas.drawPath(mPath, mPaint);

        canvas.restore();
    }

    private void computeOffsetX() {
        if (mOffsetXFront >= DOUBLE_WAVE_WIDTH) {
            mOffsetXFront += WAVE_FLOW_SPEED_FRONT - DOUBLE_WAVE_WIDTH;
        } else {
            mOffsetXFront += WAVE_FLOW_SPEED_FRONT;
        }
        if (mOffsetXBehind >= DOUBLE_WAVE_WIDTH) {
            mOffsetXBehind += WAVE_FLOW_SPEED_BEHIND - DOUBLE_WAVE_WIDTH;
        } else {
            mOffsetXBehind += WAVE_FLOW_SPEED_BEHIND;
        }
    }
}
