package com.goldmf.GMFund.controller.circle;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.goldmf.GMFund.MyApplication;

import java.io.OutputStream;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;

/**
 * Created by yale on 16/5/9.
 */
public class CircleRichImageCreator {
    private CircleRichImageCreator() {
    }

    public static Bitmap create(@DrawableRes int iconResId, String text) {
        Rect outset = new Rect(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
        int iconImagePaddingRight = dp2px(2);

        Resources res = MyApplication.getResource();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Bitmap iconImage = BitmapFactory.decodeResource(res, iconResId);
        Rect minOutImageBounds = new Rect();

        // append image width
        minOutImageBounds.right += iconImage.getWidth();

        RectF textBounds = new RectF();
        if (!TextUtils.isEmpty(text)) {
            Rect inout = new Rect();
            paint.setTextSize(sp2px(14));
            paint.getTextBounds(text, 0, text.length(), inout);
            textBounds.right += paint.measureText(text);
            textBounds.bottom += inout.height();

        }
        minOutImageBounds.right += textBounds.width();
        minOutImageBounds.bottom += textBounds.height();

        Bitmap outImage = Bitmap.createBitmap(minOutImageBounds.width() + outset.left + outset.right + iconImagePaddingRight, minOutImageBounds.height() + outset.top + outset.bottom, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(outImage);

        canvas.drawColor(RED_COLOR);

        int expectHeightOfIcon = minOutImageBounds.height();
        float scaleFactor = expectHeightOfIcon != iconImage.getHeight() ? (float) expectHeightOfIcon / iconImage.getHeight() : 1.0f;
        if (scaleFactor != 1.0f) {
            canvas.save();
            canvas.translate(outset.left, outset.top);
            canvas.scale(scaleFactor, scaleFactor);
            canvas.drawBitmap(iconImage, 0, 0, paint);
            canvas.restore();
        } else {
            canvas.drawBitmap(iconImage, outset.left, outset.top, paint);
        }


        canvas.save();
        if (scaleFactor != 1.0f) {
            canvas.translate(outset.left + scaleFactor * iconImage.getWidth() + iconImagePaddingRight, minOutImageBounds.height());
        } else {
            canvas.translate(outset.left + iconImage.getWidth() + iconImagePaddingRight, minOutImageBounds.height());
        }

        paint.setColor(TEXT_WHITE_COLOR);
        canvas.drawText(text, 0, 0, paint);
        canvas.restore();

        return outImage;
    }
}
