package com.goldmf.GMFund.extension;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.view.View;
import android.widget.ImageView;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.util.FastBlur;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import rx.functions.Func0;
import rx.functions.Func1;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.UIControllerExtension.getScreenSize;
import static yale.extension.common.PreCondition.checkNotNull;

/**
 * Created by yale on 15/9/21.
 */
public class BitmapExtension {
    private BitmapExtension() {
    }

    public static boolean compressBitmap(File bitmapFile, File saveFile) {
        FileOutputStream output = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap origin = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);
            if (origin != null) {
                Rect screenSize = getScreenSize(MyApplication.SHARE_INSTANCE);
                int bWidth = origin.getWidth();
                int bHeight = origin.getHeight();
                if (bWidth > screenSize.width() * 2 || bHeight > screenSize.height() * 2) {
                    float horizontalScale = (float) screenSize.width() * 2 / bWidth;
                    float verticalScale = (float) screenSize.height() * 2 / bHeight;
                    float minScale = Math.min(horizontalScale, verticalScale);
                    Matrix matrix = new Matrix();
                    matrix.postScale(minScale, minScale);
                    Bitmap resizeBitmap = Bitmap.createBitmap(origin, 0, 0, bWidth, bHeight, matrix, false);
                    origin.recycle();
                    System.gc();
                    output = new FileOutputStream(saveFile);
                    boolean isSuccess = resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);
                    resizeBitmap.recycle();
                    System.gc();
                    return isSuccess;

                } else {
                    output = new FileOutputStream(saveFile);
                    boolean isSuccess = origin.compress(Bitmap.CompressFormat.JPEG, 80, output);
                    origin.recycle();
                    return isSuccess;
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            close(output);
        }
        return false;
    }

    public static Bitmap redrawBitmap(Bitmap bitmap, int bgColor, Rect bounds, Rect outset) {
        if (bounds == null) {
            bounds = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }

        if (outset == null)
            outset = new Rect();

        Bitmap newBitmap = Bitmap.createBitmap(bounds.width() + outset.left + outset.right, bounds.height() + outset.top + outset.bottom, bitmap.getConfig());
        if (!newBitmap.isMutable()) {
            newBitmap = newBitmap.copy(newBitmap.getConfig(), true);
        }
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(bgColor);
        canvas.save();
        canvas.translate(outset.left, outset.top);
        if (bounds.width() != bitmap.getWidth()) {
            float scaleFactor = (float) bounds.width() / bitmap.getWidth();
            canvas.scale(scaleFactor, scaleFactor);
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.restore();
        return newBitmap;
    }

    public static Rect getBitmapSize(String filePath, boolean autoRotate) {
        if (filePath != null) {
            File file = new File(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (options.outWidth > 0 && options.outHeight > 0) {
                try {
                    ExifInterface exif = new ExifInterface(filePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        return new Rect(0, 0, options.outHeight, options.outWidth);
                    }
                } catch (IOException ignored) {
                }
            }

            return new Rect(0, 0, options.outWidth, options.outHeight);
        }
        return new Rect();
    }

    public static Optional<Bitmap> getCacheImage(View view) {
        Optional<Bitmap> ret = Optional.of(null);
        if (view == null) {
            return ret;
        } else {
            if (view.getWidth() > 0 && view.getHeight() > 0 && view.getWindowToken() != null) {
                Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                view.draw(canvas);
                ret.set(bitmap);
            }
            //                boolean isDrawingCacheEnabled = view.isDrawingCacheEnabled();
            //                if (!isDrawingCacheEnabled) {
            //                    view.setDrawingCacheEnabled(true);
            //                }
            //                int color = view.getDrawingCacheBackgroundColor();
            //                if (color != 0) {
            //                    view.destroyDrawingCache();
            //                }
            //                view.buildDrawingCache(true);
            //                Bitmap cacheImage = view.getDrawingCache();
            //                if (cacheImage != null) {
            //                    ret.set(Bitmap.createBitmap(cacheImage));
            //                }
            //                view.setDrawingCacheEnabled(isDrawingCacheEnabled);
            //                view.setDrawingCacheBackgroundColor(color);

            return ret;
        }
    }

    public static byte[] toByteArray(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        checkNotNull(bitmap, format, quality);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(format, quality, output);
        byte[] ret = output.toByteArray();
        close(output);
        return ret;
    }

    public static Bitmap decodeBitmap(String filePath, Func1<BitmapFactory.Options, Integer> scaleFactorComputer) {
        return decodeBitmap(() -> new File(filePath), scaleFactorComputer);
    }

    public static Bitmap decodeBitmap(File file, Func1<BitmapFactory.Options, Integer> scaleFactorComputer) {
        return decodeBitmap(() -> file, scaleFactorComputer);
    }

    public static Func1<BitmapFactory.Options, Integer> newScreenScaleFactor(Context context) {
        Rect rect = getScreenSize(context);
        final int screenWidth = rect.width();
        final int screenHeight = rect.height();
        return newDefaultScaleFactor(screenWidth, screenHeight);
    }

    public static Func1<BitmapFactory.Options, Integer> newRelativeWidthscaleFactor(int maxWidth, float ratio) {
        return newDefaultScaleFactor(maxWidth, (int) ((float) maxWidth / ratio));
    }

    public static Func1<BitmapFactory.Options, Integer> newRelativeHeightScaleFactor(int maxHeight, float ratio) {
        return newDefaultScaleFactor((int) (maxHeight * ratio), maxHeight);
    }

    public static Func1<BitmapFactory.Options, Integer> newDefaultScaleFactor(int maxWidth, int maxHeight) {
        return options -> {
            int bitWidth = options.outWidth;
            int bitHeight = options.outHeight;
            int bitMaxLength = Math.max(bitWidth, bitHeight);
            if (bitWidth == bitHeight) {
                // square
                int maxLength = Math.min(maxWidth, maxHeight);
                if (maxLength < bitMaxLength) {
                    return new BigDecimal(bitMaxLength).divide(new BigDecimal(maxLength), BigDecimal.ROUND_HALF_UP).intValue();
                }
            } else if (bitWidth > bitHeight) {
                // landscape
                if (bitWidth > maxWidth) {
                    return new BigDecimal(bitWidth).divide(new BigDecimal(maxWidth), BigDecimal.ROUND_HALF_UP).intValue();
                }
            } else {
                // portrait
                if (bitHeight > maxHeight) {
                    return new BigDecimal(bitWidth).divide(new BigDecimal(maxHeight), BigDecimal.ROUND_HALF_UP).intValue();
                }
            }
            return 1;
        };
    }

    private static Bitmap decodeBitmap(Func0<File> fileGetter, Func1<BitmapFactory.Options, Integer> scaleFactorComputer) {
        File file = fileGetter.call();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int scaleFactor = scaleFactorComputer.call(options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static byte[] getBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        } finally {
            close(os);
        }
        return os.toByteArray();
    }

    public static void blur(Bitmap bkg, ImageView view, Context context) {
        float radius = 10;
        float scaleFactor = 8;
        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor), (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        view.setImageDrawable(new BitmapDrawable(context.getResources(), overlay));
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
