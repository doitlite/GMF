package com.goldmf.GMFund.manager.dev;

import android.text.TextUtils;

import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.util.GzipUtil;

import java.util.LinkedList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;

/**
 * Created by yale on 15/10/26.
 */
public class RequestLogManager {
    private static LinkedList<RequestLog> mDataSet = new LinkedList<>();

    private RequestLogManager() {
    }

    public static void addLog(Request request, Response response, String responseMessage) {

        if (!MyConfig.isDevModeEnable()) {
            return;
        }


        if (request != null && response != null) {
            safeCall(() -> addLog(request.url(), request.method(), buildRequestMessage(request), responseMessage, response.code()));
        }
    }

    public static void addLog(Request request, Exception exception) {
        if (!MyConfig.isDevModeEnable()) {
            return;
        }

        if (request != null && exception != null) {
            safeCall(() -> addLog(request.url(), request.method(), buildRequestMessage(request), buildResponseMessage(exception), -1));
        }
    }

    public static void addLog(HttpUrl url, String method, String requestMessage, String responseMessage, int responseCode) {
        if (!MyConfig.isDevModeEnable()) {
            return;
        }

        mDataSet.add(0, new RequestLog(url, method, requestMessage, responseMessage, responseCode));
    }

    private static String buildRequestMessage(Request request) {
        HttpUrl url = request.url();
        StringBuilder requestMessage = new StringBuilder("");
        if (!TextUtils.isEmpty(url.query())) {
            requestMessage.append(request.method()).append(" ").append(url.encodedPath()).append("?").append(url.query()).append("\n");
        } else {
            requestMessage.append(request.method()).append(" ").append(url.encodedPath()).append("\n");
        }
        requestMessage.append("Host: ").append(url.host());
        if (url.port() != -1) {
            requestMessage.append(":").append(url.port());
        }
        requestMessage.append("\n");
        if (request.headers() != null && request.headers().size() > 0) {
            requestMessage.append("Headers:\n");
            requestMessage.append(request.headers().toString());
        }
        if (request.body() != null) {
            requestMessage.append("Body:\n");
            requestMessage.append(request.body().toString());
        }
        return requestMessage.toString();
    }

    private static String buildResponseMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        if (e.getStackTrace() != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append(element.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public static List<RequestLog> getLogs() {
        return mDataSet;
    }

    public static void clear() {
        mDataSet.clear();
    }

    public static class RequestLog {
        public final HttpUrl mURL;
        public final String mMethod;
        private final byte[] mRequestMessage;
        private final byte[] mResponseMessage;
        public final int mResponseCode;
        public final long mResponseTimeMillis;

        public RequestLog(HttpUrl URL, String method, String requestMessage, String responseMessage, int responseCode) {
            mURL = URL;
            mMethod = method;
            mRequestMessage = GzipUtil.compress(requestMessage);
            mResponseMessage = GzipUtil.compress(responseMessage);
            mResponseCode = responseCode;
            mResponseTimeMillis = System.currentTimeMillis();
        }

        public String requestMessage() {
            return GzipUtil.decompress(mRequestMessage);
        }

        public String responseMessage() {
            return GzipUtil.decompress(mResponseMessage);
        }

    }
}
