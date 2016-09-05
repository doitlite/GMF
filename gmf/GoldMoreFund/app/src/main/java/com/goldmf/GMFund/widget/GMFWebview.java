package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.goldmf.GMFund.util.OKHttpUtil;
import com.orhanobut.logger.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yale on 15/7/23.
 */
public class GMFWebview extends WebView {

    private static String USER_AGENT;

    private HashMap<String, InternalCallback> mCallbackMap = new HashMap<>();

    public GMFWebview(Context context) {
        this(context, null);
    }

    public GMFWebview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GMFWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setFocusableInTouchMode(true);
        if (!isInEditMode()) {
            WebSettings settings = getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setUserAgentString(getUserAgent(context));
            settings.setUseWideViewPort(true);
        }
    }

    private String getUserAgent(Context context) {
        if (USER_AGENT == null) {
            USER_AGENT = getSettings().getUserAgentString() + " " + OKHttpUtil.generateUserAgent(context);
        }

        return USER_AGENT;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCallbackMap.clear();
    }

    @Override
    public int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(decorateRequestURL(url));
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(decorateRequestURL(url), additionalHttpHeaders);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED && getSuggestedMinimumHeight() > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(getSuggestedMinimumHeight(), MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void callJS(String methodName, Object... params) {
        callJS(null, methodName, params);
    }

    public void callJS(ResultCallback callback, String methodName, Object... params) {
        JSFunctionBuilder builder = new JSFunctionBuilder(methodName, callback != null);
        for (Object param : params)
            builder.addParam(param);
        JSFunction function = builder.build();
        if (callback != null) {
            mCallbackMap.put("" + function.id, callback);
        }
        loadUrl(function.script);
    }

    private static abstract class InternalCallback {
        abstract void onCallback(String callerId, String receivedValueOrNil);

        protected abstract void onCallback(String receivedValueOrNil);
    }

    public static abstract class ResultCallback extends InternalCallback {
        @Override
        void onCallback(String callerId, String receivedValueOrNil) {
            onCallback(receivedValueOrNil);
        }
    }

    private static class JSFunctionBuilder {

        private static int COUNTER = 1;
        private int mParamCount = 0;
        private StringBuilder mContent;
        private int mFunctionId = -1;

        public JSFunctionBuilder(String name, boolean hasReturnValue) {
            mContent = new StringBuilder("javascript: " + name + "(");
            if (hasReturnValue) {
                mFunctionId = COUNTER++;
                addParam(mFunctionId);
            }
        }

        public JSFunctionBuilder addParam(String value) {
            if (mParamCount > 0) {
                mContent.append(",");
            }

            mContent.append(value);
            mParamCount++;
            return this;
        }

        public JSFunctionBuilder addParam(Object value) {
            if (mParamCount > 0) {
                mContent.append(",");
            }
            if (value instanceof String) {
                mContent.append("'").append(value).append("'");
            } else {
                mContent.append(value);
            }
            mParamCount++;
            return this;
        }

        public JSFunction build() {
            return new JSFunction(mFunctionId, mContent.toString() + ")");
        }
    }

    private static class JSFunction {
        public final int id;
        public final String script;

        JSFunction(int id, String script) {
            this.id = id;
            this.script = script;
        }
    }

    private static String decorateRequestURL(String requestURL) {
        try {
            URL url = new URL(requestURL);
            if (url.getQuery() == null) {
                return requestURL + "?platform=android&os=android&protocol_ver=" + ProtocolManager.getInstance().versionProtocol;
            } else {
                return requestURL + "&platform=android&os=android&protocol_ver=" + ProtocolManager.getInstance().versionProtocol;
            }
        } catch (MalformedURLException e) {
            return requestURL;
        }
    }
}
