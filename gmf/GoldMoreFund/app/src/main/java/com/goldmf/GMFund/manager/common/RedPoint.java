package com.goldmf.GMFund.manager.common;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Map;

import static com.goldmf.GMFund.base.MResults.MResultsInfo.SafeOnResult;
import static com.goldmf.GMFund.base.MResults.MResultsInfo.SuccessComRet;
import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;

/**
 * Created by cupide on 15/10/29.
 */
public class RedPoint implements Serializable {
    private String location;//位置

    public String text;
    public int number;

    private String updateTime;             //更新时间戳
    private boolean bSnsServer;             //是否是sns Server

    public String getLocation() {
        return location;
    }

    public RedPoint(String location, boolean bSnsServer) {
        this.location = location;
        this.bSnsServer = bSnsServer;
    }

    public void readFromeJsonData(JsonObject dic) {
        this.text = GsonUtil.getAsString(dic, "msg");
        this.number = GsonUtil.getAsInt(dic, "num");
        this.updateTime = GsonUtil.getAsString(dic, "update_time");
        NotificationCenter.redDotCountChangedSubject.onNext(null);
    }

    /**
     * 清除小红点,并发送小红点清除协议
     */
    public final void clear() {
        if (this.number > 0) {
            clear(null);
        }
        this.number = 0;
    }

    /**
     * 增加某些参数的方式来清除小红点,并发送小红点清除协议
     *
     * @param param
     */
    public final void clear(Map<String, String> param) {

        new CommonPostProtocol.Builder()
                .url(CHostName.formatUrl(bSnsServer ? CHostName.HOST2 : CHostName.HOST1, "clear-tips"))
                .postParams(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("type", this.location)
                        .add("update_time", this.updateTime)
                        .add(param))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                    }
                })
                .build()
                .startWork();

        NotificationCenter.redDotCountChangedSubject.onNext(null);
    }

    /**
     * 刷新小红点
     *
     * @param results
     */
    public final void freshRedPoint(final MResults<RedPoint> results) {

        new ComonProtocol.Builder()
                .url((this.bSnsServer ? CHostName.HOST2 : CHostName.HOST1) + "new-tips")
                .params(buildParams("type", this.getLocation()))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        RedPoint.this.readFromeJsonData(GsonUtil.getAsJsonObject(ret, location));

                        MResults.MResultsInfo.SafeOnResult(results, protocol.<RedPoint>buildRet().setData(RedPoint.this));
                    }
                })
                .build()
                .startWork();
    }
}