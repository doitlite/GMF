package com.goldmf.GMFund.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.util.StreamUtil;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yalez on 2016/8/3.
 */
public class FrescoHelper {
    private FrescoHelper() {
    }

    public static Bitmap getBitmapFromDisk(String url) {
        CacheKey key = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(url), null);
        FileCache cache = Fresco.getImagePipelineFactory().getMainFileCache();
        if (cache.hasKey(key)) {
            InputStream input = null;
            try {
                input = cache.getResource(key).openStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(input);
            }
        }

        return null;
    }


    public static void prefetchToDisk(String url) {
        ImageRequest request = ImageRequest.fromUri(url);
        Fresco.getImagePipeline().prefetchToDiskCache(request, null);
    }

    public static void prefetchToMemory(String url) {
        ImageRequest request = ImageRequest.fromUri(url);
        Fresco.getImagePipeline().prefetchToBitmapCache(request, null);
    }

    public static boolean isImageInCache(String url) {
        return isImageInMemoryCache(url) || isImageInDiskCache(url);
    }

    public static boolean isImageInMemoryCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        return Fresco.getImagePipeline().isInBitmapMemoryCache(ImageRequest.fromUri(url));
    }

    public static boolean isImageInDiskCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }


        CacheKeyFactory factory = DefaultCacheKeyFactory.getInstance();
        FileCache mainFileCache = Fresco.getImagePipelineFactory().getMainFileCache();
        FileCache smallFileCache = Fresco.getImagePipelineFactory().getSmallImageFileCache();

        ImageRequest request = ImageRequest.fromUri(url);
        CacheKey key = factory.getEncodedCacheKey(request, null);

        return mainFileCache.hasKey(key) && smallFileCache.hasKey(key);

    }

    private static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }
}
