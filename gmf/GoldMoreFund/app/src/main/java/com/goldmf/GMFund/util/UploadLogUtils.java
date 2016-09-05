package com.goldmf.GMFund.util;

import com.goldmf.GMFund.manager.common.QiniuToken;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;
import rx.functions.Action1;
import yale.extension.common.Optional;

/**
 * Created by yale on 15/10/28.
 */
public class UploadLogUtils {
    private UploadLogUtils() {
    }

    public static void uploadLog_sync(QiniuToken uploadToken, String logFilePath, Action1<Boolean> finishHandlerOrNil) {
        finishHandlerOrNil = finishHandlerOrNil == null ? isSuccess -> {
        } : finishHandlerOrNil;

        if (uploadToken == null) {
            finishHandlerOrNil.call(false);
            return;
        }

        File logFile = new File(logFilePath);
        String logFileName = logFile.getName();
        Matcher fullMatcher = Pattern.compile("^[0-9a-zA-Z]+_[0-9\\-]+(_build)[0-9]+(\\.log)$").matcher(logFileName);
        if (fullMatcher.find()) {
            Matcher indexMatcher = Pattern.compile("^[0-9a-zA-Z]+").matcher(logFileName);
            Matcher dateMatcher = Pattern.compile("[0-9]+\\-[0-9]+\\-[0-9]+").matcher(logFileName);
            Matcher buildCountMatcher = Pattern.compile("(build)[0-9]+").matcher(logFileName);
            if (indexMatcher.find() && dateMatcher.find() && buildCountMatcher.find()) {
                String index = indexMatcher.group();
                String date = dateMatcher.group();
                String buildCount = buildCountMatcher.group();
                String key = "android/" + index + "/access_log/" + date + "-" + buildCount + ".log";
                uploadLogFile(logFile, key, uploadToken, finishHandlerOrNil);
                return;
            }
        }

        finishHandlerOrNil.call(false);
    }

    private static Optional<String> getRemoteLogHash(String callback) {
        try {
            JSONObject jsonObject = new JSONObject(callback);
            if (jsonObject.has("hash")) {
                return Optional.of(jsonObject.getString("hash"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Optional.of(null);
    }

    private static String calculateHash(String filePath) {
        try {
            return QETag.calcETag(filePath);
        } catch (Exception e) {
            return "";
        }
    }

    private static void uploadLogFile(File logFile, String key, QiniuToken uploadToken, Action1<Boolean> finishHandler) {
        UploadManager manager = new UploadManager();
        HashMap<String, String> params = new HashMap<>();
        params.put("insertOnly", "0");
        manager.put(logFile, key, uploadToken.token, (key1, info, response) -> {
            finishHandler.call(info.isOK() || "file exists".equalsIgnoreCase(info.error));
        }, null);
    }

    private static Optional<String> viewRemoteFileInfo(String key, QiniuToken accessToken) {
        try {
            String encodedEntryURI = QETag.urlSafeBase64Encode((accessToken.name + ":" + key).getBytes());
            Request request = new Request.Builder()
                    .url("http://rs.qiniu.com/stat/" + encodedEntryURI)
                    .method("GET", null)
                    .addHeader("Authorization", "QBox " + accessToken.token)
                    .build();
            Response response = OKHttpUtil.enqueue_sync(request);
            if (response.code() == 200) {
                return Optional.of(response.body().string());
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return Optional.of(null);
    }
}
