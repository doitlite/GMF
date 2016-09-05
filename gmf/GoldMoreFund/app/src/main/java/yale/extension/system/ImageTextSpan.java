package yale.extension.system;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.DrawableRes;
import android.text.style.ReplacementSpan;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ObjectExtension;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static java.lang.Math.abs;

/**
 * Created by yale on 16/5/9.
 */
public class ImageTextSpan extends ReplacementSpan {
    public static final int FLAG_NO_LEFT_CORNER = 1;
    public static final int FLAG_NO_RIGHT_CORNER = 1 << 1;
    public static final int FLAG_DRAW_TO_END = 1 << 2;

    private int mIconResID;
    private int mTileResID;
    private int mTextColor;
    private int mBGColor;
    private int mRadius;
    private int mFlags;
    private RectF mRect = new RectF();
    private Path mPath = new Path();
    private RectF mContentPadding;
    private int mDrawablePadding;
    private Bitmap mIconBitmap;
    private Bitmap mTileBitmap;

    public ImageTextSpan(int iconResID, int tileResID, int textColor, int bgColor, int radius, RectF contentPadding, int drawablePadding, int flags) {
        super();
        mIconResID = iconResID;
        mTileResID = tileResID;
        mTextColor = textColor;
        mBGColor = bgColor;
        mRadius = radius;
        mFlags = flags;
        mContentPadding = opt(contentPadding).or(new RectF());
        mDrawablePadding = drawablePadding;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.save();
        RectF rect = mRect;
        Path path = mPath;

        boolean isDrawToEnd = (mFlags & FLAG_DRAW_TO_END) != 0;
//        isDrawToEnd = false;
        Paint.FontMetrics metrics = paint.getFontMetrics();
        rect.set(x, y - abs(-metrics.top) - mContentPadding.top, isDrawToEnd ? canvas.getWidth() : x + measureTotalWidth(paint, text, start, end), y + abs(metrics.bottom) + mContentPadding.bottom);

        path.reset();

        float[] radius = new float[8];
        if ((mFlags & FLAG_NO_LEFT_CORNER) == 0) {
            radius[0] = radius[1] = radius[6] = radius[7] = mRadius;
        }
        if ((mFlags & FLAG_NO_RIGHT_CORNER) == 0) {
            radius[2] = radius[3] = radius[4] = radius[5] = mRadius;
        }
        path.addRoundRect(mRect, radius, Path.Direction.CW);
        canvas.clipPath(path);

        canvas.drawColor(mBGColor);
        Shader preShader = paint.getShader();
        paint.setShader(new BitmapShader(getTileBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        canvas.drawRect(rect, paint);
        paint.setShader(preShader);

        int drawableWidth = getIconDrawableWidth();
        if (drawableWidth > 0) {
            Bitmap bitmap = getIconBitmap();
            float offsetY = (rect.height() - bitmap.getHeight()) / 2;
            canvas.drawBitmap(bitmap, x + mContentPadding.left, rect.top + offsetY, paint);
        }

        paint.setColor(mTextColor);
        if (drawableWidth <= 0) {
            drawableWidth = -mDrawablePadding;
        }

        canvas.drawText(text, start, end, x + mContentPadding.left + drawableWidth + mDrawablePadding, y, paint);
        canvas.restore();
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(measureTotalWidth(paint, text, start, end));
    }

    private float measureTotalWidth(Paint paint, CharSequence text, int start, int end) {
        float textWidth = measureTextWidth(paint, text, start, end);
        int drawableWidth = getIconDrawableWidth();

        if (drawableWidth > 0) {
            return mContentPadding.left + drawableWidth + mDrawablePadding + textWidth + mContentPadding.right;
        } else {
            return mContentPadding.left + textWidth + mContentPadding.right;
        }
    }

    private float measureTextWidth(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }

    private int getIconDrawableWidth() {
        Bitmap bitmap = getIconBitmap();
        if (bitmap == null) {
            return 0;
        }

        return bitmap.getWidth();
    }


    @SuppressWarnings({"ConstantConditions", "deprecation"})
    private Bitmap getIconBitmap() {
        if (mIconBitmap == null && mIconResID != 0) {
            mIconBitmap = BitmapFactory.decodeResource(MyApplication.getResource(), mIconResID);
        }

        return mIconBitmap;
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    private Bitmap getTileBitmap() {
        if (mTileBitmap == null && mTileResID != 0) {
            mTileBitmap = BitmapFactory.decodeResource(MyApplication.getResource(), mTileResID);
        }

        return mTileBitmap;
    }
}
