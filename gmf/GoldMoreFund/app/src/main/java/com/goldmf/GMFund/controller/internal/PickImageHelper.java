package com.goldmf.GMFund.controller.internal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.extension.BitmapExtension;
import com.goldmf.GMFund.extension.FileExtension;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.InputStream;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.BitmapExtension.compressBitmap;
import static com.goldmf.GMFund.extension.BitmapExtension.decodeBitmap;
import static com.goldmf.GMFund.extension.BitmapExtension.newDefaultScaleFactor;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;

/**
 * Created by yale on 16/5/16.
 */
public class PickImageHelper {
    private PickImageHelper() {
    }

    public static boolean createPickImageFromCameraIntent(File[] outFile, Intent[] outIntent, String imageFileName) {
        File file = createTempImageFile(imageFileName);
        if (file != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            outFile[0] = file;
            outIntent[0] = intent;
            return true;
        }

        return false;
    }

    public static boolean createPickImageFromGalleryIntent(File[] outFile, Intent[] outIntent, String imageFileName) {
        File file = createTempImageFile(imageFileName);
        if (file != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            outFile[0] = file;
            outIntent[0] = intent;
            return true;
        }

        return false;
    }

    @SuppressWarnings("UnusedParameters")
    public static boolean handlePickImageFromCameraActivityResult(int requestCode, int resultCode, Intent data, File pictureFile, Bitmap[] outThumbnail, Rect thumbnailSize) {
        try {
            if (resultCode == Activity.RESULT_OK && thumbnailSize.width() > 0 && thumbnailSize.height() > 0) {
                Bitmap thumbnail = decodeBitmap(pictureFile, newDefaultScaleFactor(thumbnailSize.width(), thumbnailSize.height()));
                outThumbnail[0] = thumbnail;
                return pictureFile.length() > 0 && thumbnail != null && thumbnail.getWidth() > 0 && thumbnail.getHeight() > 0;
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return false;
    }

    @SuppressWarnings("UnusedParameters")
    public static boolean handlePickImageFromGalleryActivityResult(int requestCode, int resultCode, Intent data, File pictureFile, Bitmap[] outThumbnail, Rect thumbnailSize) {
        try {
            if (resultCode == Activity.RESULT_OK && thumbnailSize.width() > 0 && thumbnailSize.height() > 0) {
                Uri uri = data.getData();
                InputStream stream = MyApplication.SHARE_INSTANCE.getContentResolver().openInputStream(uri);
                FileExtension.writeDataToFile(pictureFile, stream, true);
                Bitmap thumbnail = decodeBitmap(pictureFile, newDefaultScaleFactor(thumbnailSize.width(), thumbnailSize.height()));
                outThumbnail[0] = thumbnail;
                return pictureFile.length() > 0 && thumbnail != null && thumbnail.getWidth() > 0 && thumbnail.getHeight() > 0;
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return false;
    }

    public static void clearPublicDir() {
        safeCall(() -> {
            File file = new File(MyApplication.SHARE_INSTANCE.getCacheDir().getAbsolutePath() + File.separator + "public" + File.separator);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File item : files) {
                    item.delete();
                }
            }
        });
    }

    @Nullable
    private static File createTempImageFile(String name) {
        name = ObjectExtension.opt(name).or("tmp.jpg");
        File file = FileExtension.createFile(MyApplication.SHARE_INSTANCE.getCacheDir().getAbsolutePath() + File.separator + "public" + File.separator + name, true);
        if (file != null) {
            file.setWritable(true, false);
            opt(file.getParentFile()).consume(it -> {
                it.setExecutable(true, false);
                it.setWritable(true, false);
            });
        }
        return file;
    }

}
