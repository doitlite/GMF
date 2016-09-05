package com.goldmf.GMFund.controller.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.util.FormatUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import rx.functions.Action3;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.util.DownloadUtil.*;

/**
 * Created by yale on 15/9/25.
 */
public class DownloadDialog extends ProgressDialog {

    private static HashMap<String, DownloadRequest> mBackgroundRequestMap = new HashMap<>();
    private boolean mIsDownloading = false;
    private String mDownloadURL;
    private File mSavePath;
    private Optional<String> mMd5;
    private Handler mHandler = new Handler();
    private Action3<Dialog, Boolean, File> mFinishDownloadListener = (dialog, isSuccess, file) -> {
    };

    public DownloadDialog(Context context, String downloadURL, File savePath, Optional<String> md5) {
        super(context);
        setCancelable(false);
        setMessage("初始化中，请稍候");

        mDownloadURL = downloadURL;
        mSavePath = savePath;
        mMd5 = md5;
    }

    @Override
    public void show() {
        super.show();
        MyApplication.setTopDialog(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        MyApplication.setTopDialog(null);
    }

    public static void downloadOnBackground(String downloadURL, File savePath, String md5, boolean onlyWifi) {
        Context ctx = MyApplication.SHARE_INSTANCE;
        if (onlyWifi) {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected()) {
                downloadOnBackgroundImpl(downloadURL, savePath, Optional.of(md5));
            }
            return;
        }

        downloadOnBackgroundImpl(downloadURL, savePath, Optional.of(md5));
    }

    public void setFinishDownloadListener(Action3<Dialog, Boolean, File> listener) {
        if (listener != null)
            mFinishDownloadListener = listener;
    }

    public void startDownload() {
        if (!mIsDownloading) {
            mIsDownloading = true;
            cancelBackgroundDownloadImpl(mDownloadURL);
            new Thread(() -> {
                DownloadRequest request = createDownloadRequest(mDownloadURL, mSavePath, mMd5);
                request.setProgressListener((currentSize, totalSize) -> mHandler.post(() -> onDownloadProgressChanged(currentSize, totalSize)));
                request.setCompleteListener(isSuccess -> mHandler.post(() -> onFinishDownload(isSuccess)));
                request.execute();
            }).start();
        }
    }

    private void onDownloadProgressChanged(int currentSize, int totalSize) {
        setMessage("已下载 " + FormatUtil.formatRatio((double) currentSize / totalSize, false, 2));
    }

    private void onFinishDownload(boolean isSuccess) {
        mIsDownloading = false;
        mFinishDownloadListener.call(this, isSuccess, mSavePath);
    }

    private static void downloadOnBackgroundImpl(String downloadURL, File savePath, Optional<String> md5) {
        DownloadRequest historyDownloadRequestOrNil = mBackgroundRequestMap.get(downloadURL);
        if (historyDownloadRequestOrNil != null && historyDownloadRequestOrNil.isExecuting()) {
            return;
        }
        mBackgroundRequestMap.remove(downloadURL);

        new Thread(() -> {
            DownloadRequest request = createDownloadRequest(downloadURL, savePath, md5);
            request.setCompleteListener(isSuccess -> mBackgroundRequestMap.remove(downloadURL));
            mBackgroundRequestMap.put(downloadURL, request);
            request.execute();
        }).start();
    }

    private static void cancelBackgroundDownloadImpl(String downloadURL) {
        DownloadRequest historyDownloadRequestOrNil = mBackgroundRequestMap.get(downloadURL);
        if (historyDownloadRequestOrNil != null) {
            historyDownloadRequestOrNil.cancel();
        }
        mBackgroundRequestMap.remove(downloadURL);
    }
}
