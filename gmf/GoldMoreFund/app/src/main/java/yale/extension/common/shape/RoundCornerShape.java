package yale.extension.common.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.shapes.Shape;

/**
 * Created by yale on 15/10/6.
 */
public class RoundCornerShape extends Shape {
    private final RectF mRect = new RectF();
    private final int mFillColor;
    private final int mRadiusInPx;
    private int mBorderWidthInPx;
    private int mBorderColor;

    public RoundCornerShape(int fillColor, int radiusInPx) {
        mFillColor = fillColor;
        mRadiusInPx = radiusInPx;
    }


    public RoundCornerShape border(int borderColor, int borderWidthInPx) {
        mBorderColor = borderColor;
        mBorderWidthInPx = borderWidthInPx;
        return this;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        mRect.set(0, 0, getWidth(), getHeight());
        if (mFillColor != 0) {
            paint.setColor(mFillColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(mRect, mRadiusInPx, mRadiusInPx, paint);
        }

        if (mBorderWidthInPx > 0 && mBorderColor != 0) {
            paint.setColor(mBorderColor);
            paint.setStrokeWidth(mBorderWidthInPx);
            paint.setStyle(Paint.Style.STROKE);
            mRect.inset(mBorderWidthInPx, mBorderWidthInPx);
            canvas.drawRoundRect(mRect, mRadiusInPx, mRadiusInPx, paint);
        }
    }
}
