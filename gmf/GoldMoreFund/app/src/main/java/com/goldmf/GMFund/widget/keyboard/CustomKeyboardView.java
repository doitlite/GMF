package com.goldmf.GMFund.widget.keyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import com.goldmf.GMFund.R;

import java.util.List;

/**
 * Created by yale on 15/7/28.
 */
public class CustomKeyboardView extends KeyboardView {
    private Rect mDirtyRect = new Rect();
    private boolean mDrawPending;
    private Bitmap mBuffer;
    private boolean mKeyboardChanged;
    private Canvas mCanvas;
    private Paint mPaint;
    private Drawable mKeyBackground;
    private Drawable mAnotherKeyBackground;
    private Rect mClipRegion = new Rect(0, 0, 0, 0);
    private Rect mPadding;
    private Keyboard.Key[] mKeys;
    private Keyboard.Key mInvalidatedKey;
    private int mKeyTextColor;
    private int mLabelTextSize;
    private float mShadowRadius;
    private int mShadowColor;
    private int mKeyTextSize;
    private String[] mAnotherKeyLabelsOrNil;
//    private float mBackgroundDimAmount;
//    private boolean mMiniKeyboardOnScreen;


    public CustomKeyboardView(Context context) {
        this(context, null);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomKeyboardView, defStyleAttr, 0);

        mKeyBackground = a.getDrawable(R.styleable.CustomKeyboardView_keyBackground);
        mAnotherKeyBackground = a.getDrawable(R.styleable.CustomKeyboardView_anotherKeyBackground);
        if (mAnotherKeyBackground == null)
            mAnotherKeyBackground = mKeyBackground;
        mKeyTextSize = a.getDimensionPixelSize(R.styleable.CustomKeyboardView_keyTextSize, 18);
        mKeyTextColor = a.getColor(R.styleable.CustomKeyboardView_keyTextColor, 0xFF000000);
        mLabelTextSize = a.getDimensionPixelSize(R.styleable.CustomKeyboardView_labelTextSize, 14);
        mShadowColor = a.getColor(R.styleable.CustomKeyboardView_shadowColor, 0);
        mShadowRadius = a.getDimensionPixelOffset(R.styleable.CustomKeyboardView_shadowRadius, 0);

        a.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mKeyTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAlpha(255);

        mPadding = new Rect(0, 0, 0, 0);
        mKeyBackground.getPadding(mPadding);
    }

    public void setAnotherKeyLabels(String[] anotherKeyLabels) {
        mAnotherKeyLabelsOrNil = anotherKeyLabels;
        invalidate();
    }

    @Override
    public void setKeyboard(Keyboard keyboard) {
        super.setKeyboard(keyboard);
        List<Keyboard.Key> keys = keyboard.getKeys();
        mKeys = keys.toArray(new Keyboard.Key[keys.size()]);
        mKeyboardChanged = true;
        invalidateAllKeys();
    }

    @Override
    public void invalidateAllKeys() {
        mDirtyRect.union(0, 0, getWidth(), getHeight());
        mDrawPending = true;
        super.invalidateAllKeys();
    }

    @Override
    public void invalidateKey(int keyIndex) {
        if (mKeys == null) return;
        if (keyIndex < 0 || keyIndex >= mKeys.length) {
            return;
        }
        super.invalidateKey(keyIndex);
        final Keyboard.Key key = mKeys[keyIndex];
        mInvalidatedKey = key;
        mDirtyRect.union(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
        onBufferDraw();
        invalidate(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(mBuffer, 0, 0, null);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBuffer = null;
    }

    @Override
    public void closing() {
        super.closing();
        mBuffer = null;
        mCanvas = null;
    }

    private void onBufferDraw() {
        if (mBuffer == null || mKeyboardChanged) {
            if (mBuffer == null || mKeyboardChanged &&
                    (mBuffer.getWidth() != getWidth() || mBuffer.getHeight() != getHeight())) {
                // Make sure our bitmap is at least 1x1
                final int width = Math.max(1, getWidth());
                final int height = Math.max(1, getHeight());
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBuffer);
            }
            invalidateAllKeys();
            mKeyboardChanged = false;
        }
        final Canvas canvas = mCanvas;
        canvas.clipRect(mDirtyRect, Region.Op.REPLACE);

        if (getKeyboard() == null) return;

        final Paint paint = mPaint;
        final Drawable keyBackground = mKeyBackground;
        final Drawable anotherKeyBackground = mAnotherKeyBackground;
        final Rect clipRegion = mClipRegion;
        final Rect padding = mPadding;
        final int kbdPaddingLeft = getPaddingLeft();
        final int kbdPaddingTop = getPaddingTop();
        final Keyboard.Key[] keys = mKeys;
        final Keyboard.Key invalidKey = mInvalidatedKey;

        paint.setColor(mKeyTextColor);
        boolean drawSingleKey = false;
        if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
            // Is clipRegion completely contained within the invalidated key?
            if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left &&
                    invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top &&
                    invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right &&
                    invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
        }
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        final int keyCount = keys.length;
        for (int i = 0; i < keyCount; i++) {
            final Keyboard.Key key = keys[i];
            if (drawSingleKey && invalidKey != key) {
                continue;
            }
            int[] drawableState = key.getCurrentDrawableState();
            keyBackground.setState(drawableState);
            anotherKeyBackground.setState(drawableState);

            // Switch the character to uppercase if shift is pressed
            String label = key.label == null ? null : adjustCase(key.label).toString();

            final Rect bounds = keyBackground.getBounds();
            if (key.width != bounds.right ||
                    key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height);
                anotherKeyBackground.setBounds(0, 0, key.width, key.height);
            }
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            if (mAnotherKeyLabelsOrNil != null && containIsArray(mAnotherKeyLabelsOrNil, label)) {
                anotherKeyBackground.draw(canvas);
            } else {
                keyBackground.draw(canvas);
            }

            if (label != null) {
                // For characters, use large font. For labels like "Done", use small font.
                if (label.length() > 1 && key.codes.length < 2) {
                    paint.setTextSize(mLabelTextSize);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    paint.setTextSize(mKeyTextSize);
                    paint.setTypeface(Typeface.DEFAULT);
                }
                // Draw a drop shadow for the text
                paint.setShadowLayer(mShadowRadius, 0, 0, mShadowColor);
                // Draw the text
                canvas.drawText(label,
                        (key.width - padding.left - padding.right) / 2
                                + padding.left,
                        (key.height - padding.top - padding.bottom) / 2
                                + (paint.getTextSize() - paint.descent()) / 2 + padding.top,
                        paint);
                // Turn off drop shadow
                paint.setShadowLayer(0, 0, 0, 0);
            } else if (key.icon != null) {
                final int drawableX = (key.width - padding.left - padding.right
                        - key.icon.getIntrinsicWidth()) / 2 + padding.left;
                final int drawableY = (key.height - padding.top - padding.bottom
                        - key.icon.getIntrinsicHeight()) / 2 + padding.top;
                canvas.translate(drawableX, drawableY);
                key.icon.setBounds(0, 0,
                        key.icon.getIntrinsicWidth(), key.icon.getIntrinsicHeight());
                key.icon.draw(canvas);
                canvas.translate(-drawableX, -drawableY);
            }
            canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);
        }
        mInvalidatedKey = null;

        mDrawPending = false;
        mDirtyRect.setEmpty();
    }

    private static boolean containIsArray(String[] values, String input) {
        for (String value : values) {
            if (value.equals(input)) return true;
        }
        return false;
    }

    private CharSequence adjustCase(CharSequence label) {
        if (getKeyboard().isShifted() && label != null && label.length() < 3
                && Character.isLowerCase(label.charAt(0))) {
            label = label.toString().toUpperCase();
        }
        return label;
    }
}
