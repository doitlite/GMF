package com.goldmf.GMFund.controller.chat;

import android.content.Context;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.extension.FileExtension;

import java.io.File;
import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by yale on 15/11/26.
 */
public class ChatCacheManager {
    private static final long IMAGE_CACHE_MAX_LIMIT = 1024 * 1024 * 20;     // 20MB
    private static final long IMAGE_CACHE_BASELINE = 1024 * 1024 * 10;      // 10MB

    private static ChatCacheManager sInstance;

    private long mCurrentCacheImageSize = 0;
    private String mCacheImageDirPath;

    public static ChatCacheManager shareManager() {
        if (sInstance == null) {
            sInstance = new ChatCacheManager();
        }
        return sInstance;
    }

    private ChatCacheManager() {
    }

    /**
     * 获取缓存图片的地址
     */
    public String getNewCacheImagePath() {
        String dirPath = getCacheImageDirPath();
        String newImagePath = dirPath + File.separator + System.currentTimeMillis() + ".jpg";
        File file = FileExtension.createFile(newImagePath, true);
        if (file != null) {
            file.setWritable(true, false);
            file.setReadable(true, false);
        }
        return newImagePath;
    }

    /**
     * 清除历史缓存
     */
    void cleanOutOfLimitCache() {
        mCurrentCacheImageSize = computeCacheSize(getCacheImageDirPath());
        if (mCurrentCacheImageSize > IMAGE_CACHE_MAX_LIMIT) {
            long remainingCacheSize = cleanOutOfLimitCacheImpl(getCacheImageDirPath(), IMAGE_CACHE_BASELINE);
            mCurrentCacheImageSize = max(min(remainingCacheSize, IMAGE_CACHE_BASELINE), 0);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String getCacheImageDirPath() {
        if (mCacheImageDirPath == null) {
            Context ctx = MyApplication.SHARE_INSTANCE;
            mCacheImageDirPath = ctx.getCacheDir().getAbsoluteFile() + File.separator + "chat_image_cache";
            File dir = new File(mCacheImageDirPath);
            dir.mkdirs();
            dir.setExecutable(true, false);
            dir.setReadable(true, false);
            dir.setWritable(true, false);
        }
        return mCacheImageDirPath;
    }

    private long computeCacheSize(String cacheDirPath) {
        long cacheSize = 0;
        if (cacheDirPath != null) {
            File dir = new File(cacheDirPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        cacheSize += f.length();
                    }
                }
            }
        }
        return cacheSize;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private long cleanOutOfLimitCacheImpl(String cacheDirPath, long maxCacheSize) {
        if (cacheDirPath == null) return 0;

        long remainingCacheSize = 0;
        File dir = new File(cacheDirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                Arrays.sort(files, (lhs, rhs) -> (int) (lhs.lastModified() - rhs.lastModified()));
                for (File f : files) {
                    if (remainingCacheSize < maxCacheSize) {
                        remainingCacheSize += f.length();
                    } else {
                        f.delete();
                    }
                }
            }
        }

        return remainingCacheSize;
    }
}
