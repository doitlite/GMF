package com.goldmf.GMFund.util;

import android.text.TextUtils;

import com.goldmf.GMFund.extension.FileExtension;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.functions.Action1;
import rx.functions.Action2;
import yale.extension.common.Optional;

/**
 * Created by yale on 15/9/23.
 */
public class DownloadUtil {
    private DownloadUtil() {
    }

    public static DownloadRequest createDownloadRequest(String downloadURL, File saveFile, Optional<String> md5) {
        return new DownloadRequest(downloadURL, saveFile, md5);
    }

    private static void disconnect(HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
        }
    }

    private static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean isNeedToDownload(File localFile, String remoteMD5) {
        if (localFile.exists() && !localFile.isDirectory() && !TextUtils.isEmpty(remoteMD5)) {
            String localMD5 = FileExtension.md5FromFile(localFile);
            return !remoteMD5.equals(localMD5);

        }
        return true;
    }

    public static final class DownloadRequest {
        private String mDownloadURL;
        private File mSaveFile;
        private Action2<Integer, Integer> mProgressListener;
        private Action1<Boolean> mCompleteListener;
        private Optional<String> mRemoteMD5;
        private boolean mIsExecuting = false;
        private boolean mHasCancel = false;

        private DownloadRequest(String downloadURL, File saveFile, Optional<String> md5) {
            mDownloadURL = downloadURL;
            mSaveFile = saveFile;
            mRemoteMD5 = md5;
            mProgressListener = (arg1, arg2) -> {
            };
            mCompleteListener = arg -> {
            };
        }

        public DownloadRequest setProgressListener(Action2<Integer, Integer> progressListener) {
            if (progressListener != null) mProgressListener = progressListener;
            return this;
        }

        public DownloadRequest setCompleteListener(Action1<Boolean> completeListener) {
            if (completeListener != null) mCompleteListener = completeListener;
            return this;
        }

        public boolean isExecuting() {
            return mIsExecuting;
        }

        public void cancel() {
            mHasCancel = true;
        }

        public void execute() {
            if (mIsExecuting) return;

            mHasCancel = false;
            mIsExecuting = true;
            String localMD5OrNil = FileExtension.md5FromFile(mSaveFile);
            String remoteMD5OrNil = mRemoteMD5.or("");
            if (!TextUtils.isEmpty(localMD5OrNil) && !TextUtils.isEmpty(remoteMD5OrNil) && localMD5OrNil.equals(remoteMD5OrNil)) {
                mCompleteListener.call(true);
                mIsExecuting = false;
                return;
            }

            HttpURLConnection conn = null;
            BufferedInputStream input = null;
            FileOutputStream output = null;
            try {
                URL url = new URL(mDownloadURL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.connect();

                final long localFileLength = mSaveFile.length();
                final long remoteFileLength = conn.getContentLength();
                if (localFileLength > 0 && localFileLength == remoteFileLength) {
                    mCompleteListener.call(true);
                    return;
                }

                final int contentLength = conn.getContentLength();
                input = new BufferedInputStream(conn.getInputStream());
                output = new FileOutputStream(mSaveFile);
                byte[] buffer = new byte[1024 * 4];
                int read_count;
                int downloaded_count = 0;
                while (!mHasCancel && (read_count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read_count);
                    output.flush();
                    downloaded_count += read_count;
                    mProgressListener.call(downloaded_count, contentLength);
                }
                mCompleteListener.call(!mHasCancel);
            } catch (Exception ignored) {
                mCompleteListener.call(false);
            } finally {
                mIsExecuting = false;
                close(input);
                close(output);
                disconnect(conn);
            }
        }
    }
}

