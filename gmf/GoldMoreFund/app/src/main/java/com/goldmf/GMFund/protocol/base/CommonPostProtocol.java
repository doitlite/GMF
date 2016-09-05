package com.goldmf.GMFund.protocol.base;

import java.security.spec.PSSParameterSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 16/3/11.
 */
public class CommonPostProtocol extends ComonProtocol {

    HashMap<String, Object> mPostParams;

    private CommonPostProtocol(Builder builder) {
        super.mCallback = mCallback2;
        this.mUrl = builder.mUrl;
        this.mPostParams = builder.mPostParams;
        this.mComonCallback = builder.mCallback;
    }


    @Override
    protected Map<String, Object> getPostData() {
        Map<String, Object> postData = new HashMap<>();
        if(this.mPostParams == null)
            return postData;

        return mPostParams;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }

    public static class Builder {
        private String mUrl;
        private HashMap<String, Object> mPostParams = new HashMap<>();
        private ComonCallback mCallback;

        public Builder() {
        }

        public Builder url(String url) {
            if (url == null) throw new IllegalArgumentException("mUrl == null");
            mUrl = url;
            return this;
        }

        public Builder postParams(Map<String, Object> params) {
            mPostParams.putAll(params);
            return this;
        }

        public Builder postParams(ParamParse params) {
            if (params != null) {
                for (Map.Entry<String, String> entry : params.params.entrySet()) {
                    mPostParams.put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder postParams(ParamParse.ParamBuilder builder) {
            return postParams(new ParamParse(builder));
        }

        public Builder callback(ComonCallback callback) {
            if (callback != null)
                mCallback = callback;
            return this;
        }

        public ComonProtocol build() {
            if (mUrl == null) throw new IllegalStateException("mUrl == null");
            return new CommonPostProtocol(this);
        }
    }
}
