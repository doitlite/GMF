package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.extension.ViewExtension;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;

/**
 * Created by yale on 16/5/11.
 */
public class CircleTextView extends View {
    private int mTileResId;
    private int mContentBGColor;
    private int mIconResId;
    private int mDrawablePadding;
    private int mRadius;
    private Rect mPadding = new Rect();
    private CharSequence mText;
    private int mTextSize;
    private int mTextColor;
    private Bitmap mIconBitmap;
    private Bitmap mTileBitmap;
    private BitmapShader mTileShader;
    private Paint mPaint = new Paint();
    private RectF mBounds = new RectF();
    private RectF mFrame = new RectF();

    public CircleTextView(Context context) {
        this(context, null);
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint.setFlags(mPaint.getFlags() | Paint.ANTI_ALIAS_FLAG);

        mTextSize = sp2px(this, 14);
        mDrawablePadding = dp2px(this, 4);
        mPadding = new Rect(dp2px(this,4), dp2px(this,0), dp2px(this,4), dp2px(this,0));
        mRadius = dp2px(this, 4);
        prepare(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        prepareBounds(true);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) mBounds.width() + dp2px(this, 2), MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) mBounds.height() + dp2px(this, 2), MeasureSpec.EXACTLY);
        }
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    public CircleTextView setContentBackgroundColor(int color) {
        this.mContentBGColor = color;
        return this;
    }

    public CircleTextView setTileResId(int tileResId) {
        this.mTileResId = tileResId;
        return this;
    }

    public CircleTextView setIconResId(int iconResId) {
        this.mIconResId = iconResId;
        return this;
    }

    public CircleTextView setContentPadding(int left, int top, int right, int bottom) {
        mPadding.set(left, top, right, bottom);
        return this;
    }

    public CircleTextView setDrawablePadding(int padding) {
        this.mDrawablePadding = padding;
        return this;
    }

    public CircleTextView setRadius(int radius) {
        this.mRadius = radius;
        return this;
    }

    public CircleTextView setText(CharSequence text) {
        this.mText = text;
        return this;
    }

    public CircleTextView setTextSize(int textSize) {
        this.mTextSize = textSize;
        return this;
    }

    public CircleTextView setTextColor(int textColor) {
        this.mTextColor = textColor;
        return this;
    }

    public void prepare(boolean reset) {
        prepareIconImage(reset);
        prepareTileShader(reset);
        prepareBounds(reset);
    }

    private void prepareIconImage(boolean reset) {
        if (mIconBitmap == null || reset) {
            if (mIconResId != 0) {
                mIconBitmap = BitmapFactory.decodeResource(MyApplication.getResource(), mIconResId);
            }
        }
    }

    private void prepareTileShader(boolean reset) {
        if (mTileShader == null || reset) {
            if (mTileResId != 0) {
                mTileBitmap = BitmapFactory.decodeResource(getResources(), mTileResId);
                if (mTileBitmap != null) {
                    mTileShader = new BitmapShader(mTileBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                }
            }
        }
    }


    private void prepareBounds(boolean reset) {

        if (mBounds.isEmpty() || reset) {
            prepareTileShader(false);

            mBounds.setEmpty();
            mBounds.right += mPadding.left + mPadding.right;
            mBounds.bottom += mPadding.top + mPadding.bottom;
            if (mTileBitmap != null) {
                mBounds.right += mTileBitmap.getWidth();
                mBounds.right += mDrawablePadding;
            }
            if (!TextUtils.isEmpty(mText)) {
                mPaint.setTextSize(mTextSize);
                mBounds.right += mPaint.measureText(mText, 0, mText.length());
                Rect tmp = new Rect();
                mPaint.getTextBounds(mText.toString(), 0, mText.length(), tmp);
                mBounds.bottom += tmp.height();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        prepare(false);

        float offsetX = (getMeasuredWidth() - mBounds.width()) / 2;
        float offsetY = (getMeasuredHeight() - mBounds.height()) / 2;
        mFrame.set(mBounds);
        mFrame.offset(offsetX, offsetY);

        Paint paint = mPaint;
        paint.setColor(mContentBGColor);
        canvas.drawRoundRect(mFrame, mRadius, mRadius, paint);
        Shader preShader = paint.getShader();
        paint.setShader(mTileShader);
        canvas.drawRoundRect(mFrame, mRadius, mRadius, paint);
        paint.setShader(preShader);

        if (mIconBitmap != null) {
            canvas.drawBitmap(mIconBitmap, mFrame.left + mPadding.left, mFrame.top + (mFrame.height() - mIconBitmap.getHeight()) / 2, paint);
        }

        if (!TextUtils.isEmpty(mText)) {
            paint.setColor(mTextColor);
            paint.setTextSize(mTextSize);
            if (mIconBitmap == null) {
                canvas.drawText(mText, 0, mText.length(), mFrame.left + mPadding.left, mFrame.bottom - mPadding.bottom, paint);
            } else {
                canvas.drawText(mText, 0, mText.length(), mFrame.left + mPadding.left + mIconBitmap.getWidth() + mDrawablePadding, mFrame.bottom - mPadding.bottom, paint);
            }
        }

        canvas.restore();
    }
}
