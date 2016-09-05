package com.goldmf.GMFund.protocol.base;

import com.goldmf.GMFund.base.MResults;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/7/24.
 * 通用的协议层，用于简单协议的处理。
 * 建议此类大家不要用。
 */
public class ComonProtocol extends ProtocolBase {

    String mUrl;
    ParamParse mParams;
    ComonCallback mComonCallback;

    ProtocolCallback mCallback2 = new ProtocolCallback() {
        @Override
        public void onFailure(ProtocolBase protocol, int errCode) {

            if (ComonProtocol.this.mComonCallback != null)
                ComonProtocol.this.mComonCallback.onFailure(ComonProtocol.this, errCode, returnMsg);
        }

        @Override
        public void onSuccess(ProtocolBase protocol) {
            //准备callback

            if (ComonProtocol.this.mComonCallback != null)
                ComonProtocol.this.mComonCallback.onSuccess(ComonProtocol.this, results);
        }
    };


    ComonProtocol(ProtocolCallback _callback) {
        super(_callback);
    }

    ComonProtocol() {
        super.mCallback = mCallback2;
    }

    private ComonProtocol(Builder builder) {
        super.mCallback = mCallback2;
        this.mUrl = builder.mUrl;
        this.mParams = builder.mParams;
        this.mComonCallback = builder.mCallback;
    }

    protected boolean parseData(JsonElement data) {
        return ( data != null && (data.isJsonObject() || data.isJsonArray()) && this.returnCode == 0);
    }


    public static ParamParse buildParams(Object... keyOrValue) {

        ParamParse.ParamBuilder builder = new ParamParse.ParamBuilder();

        int i = 0;
        String key = null;
        for (Object param : keyOrValue) {
            if (i % 2 == 0) {
                key = String.valueOf(param);
            } else {
                String value = String.valueOf(param);
                builder.add(key, value);
            }
            i++;
        }

        return new ParamParse(builder);
    }

    @Override
    protected String getUrl() {
        return mUrl;
    }

    @Override
    protected Map<String, Object> getPostData() {
        return null;
    }

    @Override
    protected Map<String, String> getParam() {
        return (mParams==null)?null:mParams.params;
    }

    public static class ParamParse {
        Map<String, String> params = new HashMap<>();

        ParamParse(Map<String, String> _params) {
            params.putAll(_params);
        }

        ParamParse(ParamBuilder builder) {
            params.putAll(builder.params);
        }

        public static class ParamBuilder {
            private Map<String, String> params = new HashMap<>();

            public ParamBuilder add(Map<String,String> p) {
                if(p == null)return this;

                params.putAll(p);
                return this;
            }

            public ParamBuilder add(String key, String value) {
                if(key == null || value == null)return this;

                params.put(key, value);
                return this;
            }
            public ParamBuilder add(String key, int value) {
                if(key == null)return this;

                params.put(key, String.valueOf(value));
                return this;
            }
            public ParamBuilder add(String key, double value) {
                if(key == null)return this;

                params.put(key, String.valueOf(value));
                return this;
            }
            public ParamBuilder add(String key, long value) {
                if(key == null)return this;

                params.put(key, String.valueOf(value));
                return this;
            }
            public ParamBuilder add(String key, boolean value) {
                if(key == null)return this;

                params.put(key, value?"1":"0");
                return this;
            }
        }

        public Map<String, String> getParams(){
            return params;
        }
    }

    public static class Builder {
        private String mUrl;
        private ParamParse mParams;
        private ComonCallback mCallback;

        public Builder() {
        }

        public Builder url(String url) {
            if (url == null) throw new IllegalArgumentException("mUrl == null");
            mUrl = url;
            return this;
        }

        public Builder params(Map<String, String> params) {
            if (params != null)
                mParams = new ParamParse(params);
            return this;
        }

        public Builder params(ParamParse params) {
            if (params != null)
                mParams = params;
            return this;
        }

        public Builder params(ParamParse.ParamBuilder params) {
            if (params != null)
                mParams = new ParamParse(params);
            return this;
        }

        public Builder callback(ComonCallback callback) {
            if (callback != null)
                mCallback = callback;
            return this;
        }

        public ComonProtocol build() {
            if (mUrl == null) throw new IllegalStateException("mUrl == null");
            return new ComonProtocol(this);
        }
    }

    public interface ComonCallback {

        void onFailure(ComonProtocol protocol, int errCode, String errMsg);

        void onSuccess(ComonProtocol protocol, JsonElement ret);

    }

}
