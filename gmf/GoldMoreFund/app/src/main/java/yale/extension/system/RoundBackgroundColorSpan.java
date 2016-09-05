package yale.extension.system;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;
import android.util.Log;

/**
 * Created by yale on 16/5/7.
 */
public class RoundBackgroundColorSpan extends ReplacementSpan {
    private int mFGColor;
    private int mBGColor;
    private int mRadius;
    private RectF mRect = new RectF();

    public RoundBackgroundColorSpan(int fgColor, int bgColor, int radius) {
        super();
        mFGColor = fgColor;
        mBGColor = bgColor;
        mRadius = radius;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        RectF rect = mRect;
        rect.set(x, top, x + measureText(paint, text, start, end), bottom);
        int preColor = paint.getColor();
        paint.setColor(mBGColor);
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);
        paint.setColor(mFGColor);
        canvas.drawText(text, start, end, x, y, paint);
        paint.setColor(preColor);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}
