package com.goldmf.GMFund.manager.dev;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.QiniuToken;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.util.UploadLogUtils;
import com.umeng.socialize.utils.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.CalendarExtension.addDay;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by yale on 15/10/28.
 */
public class AccessLogManager {
    private static final long MAX_CACHE_SIZE_OF_DIR_IN_BYTE = 1024 * 1024 * 10;
    private static final int MAX_LOG_FILE_COUNT_OF_DIR = 50;
    private static SharedPreferences mAccessLogPref = MyApplication.SHARE_INSTANCE.getSharedPreferences("meta_access_log", Context.MODE_APPEND);
    private static SharedPreferences mBuildCountPref = MyApplication.SHARE_INSTANCE.getSharedPreferences("build_count_date", Context.MODE_APPEND);
    private static int mBuildCountToday = -1;

    static {
        String date = formatSecond("yyyy-MM-dd");
        String buildCountStr = getKeyValue(mBuildCountPref, date, "build_count", "0");
        int historyBuildCountToday = !TextUtils.isEmpty(buildCountStr) && TextUtils.isDigitsOnly(buildCountStr) ? Integer.valueOf(buildCountStr) : 0;
        mBuildCountToday = historyBuildCountToday + 1;
        setKeyValue(mBuildCountPref, date, "build_count", "" + mBuildCountToday);
    }

    private AccessLogManager() {
    }

    public static String generateDefaultLogFileName() {
        if (MineManager.getInstance().isLoginOK()) {
            return MineManager.getInstance().getmMe().index + "_" + FormatUtil.formatSecond("yyyy-MM-dd") + "_build" + mBuildCountToday + ".log";
        }
        return "device_" + FormatUtil.formatSecond("yyyy-MM-dd-HH") + "_build" + mBuildCountToday + ".log";
    }

    public static void increaseBuildCount() {
        mBuildCountToday++;
        String date = formatSecond("yyyy-MM-dd");
        setKeyValue(mBuildCountPref, date, "build_count", "" + mBuildCountToday);
    }

    public static File getDefaultLogFileParentDir() {
        return new File(MyApplication.SHARE_INSTANCE.getCacheDir().getPath() + File.separator + "access_log");
    }

    public static Optional<List<File>> getAccessLogs(Optional<String> logDirPath, Optional<Func1<File, Boolean>> filter) {
        File[] files = logDirPath.isPresent() ? new File(logDirPath.get()).listFiles() : getDefaultLogFileParentDir().listFiles();

        if (files != null && files.length > 0) {
            Stream<File> stream = Stream.of(files).filter(it -> !it.isDirectory() && it.getName().endsWith(".log"));

            if (filter.isPresent()) {
                stream = stream.filter(it -> filter.get().call(it));
            }
            return opt(stream.collect(Collectors.toList()));
        }

        return Optional.empty();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void sweepDepricatedFile(Optional<String> logDirPath, Optional<Long> maxCacheSize, Optional<Integer> maxLogCount) {
        Optional<List<File>> filesRef = getAccessLogs(logDirPath, Optional.of(null));
        final long MAX_CACHE_SIZE = maxCacheSize.isPresent() ? maxCacheSize.get() : MAX_CACHE_SIZE_OF_DIR_IN_BYTE;
        final int MAX_LOG_COUNT = maxLogCount.isPresent() ? maxLogCount.get() : MAX_LOG_FILE_COUNT_OF_DIR;

        if (filesRef.isPresent()) {
            List<File> files = filesRef.get();
            Collections.sort(files, (lhs, rhs) -> (int) (rhs.lastModified() - lhs.lastModified()));
            long recordedFileSize = 0;
            int recordedFileCount = 0;
            for (File file : files) {
                if (recordedFileSize > MAX_CACHE_SIZE || recordedFileCount > MAX_LOG_COUNT) {
                    file.delete();
                } else {
                    recordedFileSize += file.length();
                    recordedFileCount++;
                }
            }
        }
    }

    public static void uploadLogSince(int dayBefore) {
        CommonManager.getInstance().getQiniuAppToken(CommonManager.Logger, callback -> {
            if (callback.isSuccess && callback.data != null && callback.data.get(CommonManager.Logger) != null) {
                QiniuToken uploadToken = callback.data.get(CommonManager.Logger);
                Calendar calendar = Calendar.getInstance();
                String[] dates = new String[1 + dayBefore];
                for (int i = 0; i < dates.length; i++) {
                    dates[i] = formatSecond(calendar.getTimeInMillis() / 1000, "yyyy-MM-dd");
                    addDay(calendar, -1);
                }

                StringBuilder allDatePatternStr = new StringBuilder();
                for (String date : dates) {
                    allDatePatternStr.append("|(").append(date).append(")");
                }
                Pattern currentBuildPattern = Pattern.compile("(" + dates[0] + "_build" + mBuildCountToday + ")");
                Func1<File, Boolean> reserveCondition = f -> {
                    boolean isValidFile = f.length() > 0;
                    boolean isMatch = Pattern.compile(allDatePatternStr.toString().substring(1)).matcher(f.getName()).find();
                    boolean isFileNameFormatValid = !currentBuildPattern.matcher(f.getName()).find();
                    boolean notUploadBefore = getKeyValue(mAccessLogPref, f.getName(), "is_uploaded", "no").equalsIgnoreCase("no");
                    return isValidFile && isMatch && isFileNameFormatValid && notUploadBefore;
                };
                Optional<List<File>> files = AccessLogManager.getAccessLogs(Optional.empty(), opt(reserveCondition));
                if (files.isPresent() && !files.get().isEmpty()) {
                    Observable.from(files.get())
                            .map(File::getPath)
                            .subscribeOn(Schedulers.io())
                            .subscribe(path -> {
                                UploadLogUtils.uploadLog_sync(uploadToken, path, isSuccess -> {
                                    if (isSuccess)
                                        setKeyValue(mAccessLogPref, new File(path).getName(), "is_uploaded", "yes");
                                });
                            });
                }
            }
        });
    }

    private static void setKeyValue(SharedPreferences preferences, String scope, String key, String value) {
        if (TextUtils.isEmpty(scope) || TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) return;
        preferences.edit().putString(scope + "___" + key, value).apply();
    }

    private static String getKeyValue(SharedPreferences preferences, String scope, String key, String optionalOrNil) {
        if (TextUtils.isEmpty(scope) || TextUtils.isEmpty(key)) return optionalOrNil;

        String ret = preferences.getString(scope + "___" + key, optionalOrNil);
        return ret;
    }
}
