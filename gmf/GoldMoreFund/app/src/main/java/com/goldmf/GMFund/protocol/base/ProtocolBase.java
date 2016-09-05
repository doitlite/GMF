package com.goldmf.GMFund.protocol.base;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.fortune.BountyAccount;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.Hourglass;
import com.google.gson.JsonElement;

import java.net.URL;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by cupide on 15/7/23.
 * 基于json的格式解析的cgi协议层。
 */
public abstract class ProtocolBase {

    public interface ErrCode {
        int SUCCESS = 0;    //成功

        int ERR_HTTP = -1;   //okHttp的错误
        int ERR_JSON = -2;   //解析json错误
        int ERR_UNKNOWN = -3;   //未知错误

    }

    public ProtocolCallback mCallback = null;
    final Hourglass mHourglass = new Hourglass();
    boolean reStart = false;

    public int returnCode = 0;
    public String returnMsg = "";
    public JsonElement results = null;

    public ProtocolBase() {
    }

    public ProtocolBase(ProtocolCallback callback) {
        this.mCallback = callback;
    }


    public Call startWork() {
        return ProtocolManager.getInstance().enqueue(this);
    }

    final String url() {

        String subUrl = this.getUrl();
        return CHostName.formatUrl(subUrl);
    }


    final boolean parseJson(JsonElement data) {
        if (data != null && data.isJsonObject()) {

            returnCode = GsonUtil.getAsInt(data, "code");
            returnMsg = GsonUtil.getAsString(data, "msg");

            if (returnCode == 0) {
                this.results = data.getAsJsonObject().get("data");
                if (this.results != null) {
                    return this.parseData(this.results);
                }
            }
            return (returnCode == 0);
        } else if (data != null && data.isJsonArray()) {
            this.returnCode = 0;
            this.returnMsg = "";
            this.results = GsonUtil.getAsJsonArray(data);
            return true;
        } else {
            return this.parseData(data);
        }
    }


    public static <T> MResults.MResultsInfo<T> buildErr(int errCode, String errMsg) {
        MResults.MResultsInfo<T> info = new MResults.MResultsInfo<>();
        info.isSuccess = false;
        info.errCode = errCode;
        info.data = null;
        info.msg = errMsg;
        return info;
    }

    public <T> MResults.MResultsInfo<T> buildRet() {
        MResults.MResultsInfo<T> info = new MResults.MResultsInfo<>();
        info.ret = this.results;
        info.isSuccess = (this.returnCode == 0);
        info.errCode = returnCode;
        info.msg = returnMsg;
        info.data = null;
        return info;
    }

    //需要继承类实现的几个方法
    abstract protected boolean parseData(JsonElement data);

    abstract protected String getUrl();

    abstract protected Map<String, Object> getPostData();

    abstract protected Map<String, String> getParam();
}
