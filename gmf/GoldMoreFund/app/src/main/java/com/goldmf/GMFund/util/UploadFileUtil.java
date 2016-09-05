package com.goldmf.GMFund.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.BitmapExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.QiniuToken;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Action2;

import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;

/**
 * Created by yale on 15/12/1.
 */
public class UploadFileUtil {
    public static void uploadFile(@NonNull String tokenType, @NonNull File file, @Nullable Action1<Double> progressListener, @Nullable Action2<Boolean, String> completeListener) {
        getQiniuTokenAsync(tokenType)
                .doOnNext(token -> uploadFileImpl(token, file, progressListener, completeListener))
                .subscribe();
    }

    public static void uploadFile(@NonNull String tokenType, @NonNull List<File> files, @Nullable Action2<Boolean, List<String>> completeListener) {
        List<File> copyFileList = new ArrayList<>(files);
        getQiniuTokenAsync(tokenType)
                .doOnNext(token -> {
                    uploadFileImpl(token, copyFileList, new ArrayList<>(files.size()), completeListener);
                })
                .subscribe();
    }

    public static Observable<MResultsInfo<String>> uploadFile(@NonNull String tokenType, @NonNull File file) {
        return getQiniuTokenAsync(tokenType)
                .flatMap(token -> {
                    return Observable.create(sub -> {
                        uploadFileImpl(token, file, null, ((isSuccess, imageURL) -> {
                            if (!sub.isUnsubscribed()) {
                                MResultsInfo<String> result = new MResultsInfo<>();
                                result.isSuccess = isSuccess;
                                result.data = imageURL;
                                sub.onNext(result);
                            }
                            sub.onCompleted();
                        }));
                    });
                });
    }


    public static Observable<MResultsInfo<List<String>>> uploadFile(@NonNull String tokenType, @NonNull List<File> files) {
        List<File> copyFileList = new ArrayList<>(files);
        return getQiniuTokenAsync(tokenType)
                .flatMap(token -> {
                    return Observable.create(sub -> {
                        uploadFileImpl(token, copyFileList, new ArrayList<>(files.size()), ((isSuccess, imageList) -> {
                            if (!sub.isUnsubscribed()) {
                                MResultsInfo<List<String>> result = new MResultsInfo<>();
                                result.isSuccess = isSuccess;
                                result.data = imageList;
                                sub.onNext(result);
                            }
                            sub.onCompleted();
                        }));
                    });
                });
    }

    private static Observable<QiniuToken> getQiniuTokenAsync(String tokenType) {
        return Observable.create(sub -> {
            CommonManager.getInstance().getQiniuAppToken(tokenType, callback -> {
                if (!sub.isUnsubscribed()) {
                    if (isSuccess(callback) && callback.data != null && callback.data.get(tokenType) != null) {
                        sub.onNext(callback.data.get(tokenType));
                    } else {
                        sub.onNext(null);
                    }
                    sub.onCompleted();
                }
            });
        });
    }

    private static void uploadFileImpl(QiniuToken tokenOrNil, List<File> files, List<String> imageURLs, Action2<Boolean, List<String>> completeListener) {
        if (files.isEmpty()) {
            completeListener.call(true, imageURLs);
        } else {
            uploadFileImpl(tokenOrNil, files.get(0), null, (isSuccess, imageURL) -> {
                if (isSuccess) {
                    imageURLs.add(imageURL);
                    if (files.isEmpty()) {
                        completeListener.call(true, imageURLs);
                    } else {
                        uploadFileImpl(tokenOrNil, files.subList(1, files.size()), imageURLs, completeListener);
                    }
                } else {
                    completeListener.call(false, imageURLs);
                }
            });
        }
    }

    private static void uploadFileImpl(QiniuToken tokenOrNil, File file, Action1<Double> progressListener, Action2<Boolean, String> completeListener) {
        Action1<Double> hookedProgressListener = hookProgressListener(progressListener);
        UpProgressHandler progressHandler = progressListener == null ? null : (key, percent) -> hookedProgressListener.call(percent);

        if (tokenOrNil == null) {
            completeListener.call(false, "");
            return;
        }

        UpCompletionHandler completionHandler = (key, info, response) -> {
            if (completeListener != null) {
                try {
                    String remoteFileUrl = tokenOrNil.domain + response.getString("key");
                    completeListener.call(info.isOK(), remoteFileUrl);
                } catch (Exception ignored) {
                    completeListener.call(false, "");
                }
            }
        };

        Configuration configuration = new Configuration.Builder()
                .connectTimeout(10)
                .responseTimeout(10)
                .retryMax(3)
                .build();
        UploadManager uploadManager = new UploadManager(configuration);
        UploadOptions options = new UploadOptions(null, null, false, progressHandler, null);
        String filePath = file.getAbsolutePath();
        if (filePath.endsWith("jpg")) {
            File resizeBitmapFile = new File(file.getParentFile().getAbsolutePath() + File.separator + "tmp-" + System.currentTimeMillis() + ".jpg");
            boolean isSuccess = BitmapExtension.compressBitmap(file, resizeBitmapFile);
            if (!isSuccess) {
                resizeBitmapFile.delete();
                completeListener.call(false, "");
            } else {
                uploadManager.put(resizeBitmapFile, null, tokenOrNil.token, (key, info, response) -> {
                    resizeBitmapFile.delete();
                    completionHandler.complete(key, info, response);
                }, options);
            }
        } else {
            uploadManager.put(file, null, tokenOrNil.token, completionHandler, options);
        }
    }

    private static Action1<Double> hookProgressListener(Action1<Double> progressListener) {
        if (progressListener == null) return null;

        return new Action1<Double>() {
            private Double mLastValue;

            @Override
            public void call(Double value) {
                if (mLastValue == null || mLastValue < value) {
                    mLastValue = value;
                    progressListener.call(value);
                }
            }
        };
    }
}