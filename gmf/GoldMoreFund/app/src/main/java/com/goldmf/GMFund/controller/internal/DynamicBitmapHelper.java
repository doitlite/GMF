package com.goldmf.GMFund.controller.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;

import com.goldmf.GMFund.MyApplication;

import java.security.PrivilegedExceptionAction;

import yale.extension.common.Optional;

/**
 * Created by yale on 15/12/28.
 */
public class DynamicBitmapHelper {
    private static Optional<Bitmap> createBitmapFromView(View view, int width, int height) {
        if (view == null || width <= 0 || height <= 0)
            return Optional.empty();

        Context ctx = MyApplication.SHARE_INSTANCE;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.setDensity((int) metrics.density);
        view.draw(canvas);
        return Optional.of(bitmap);
    }
}
