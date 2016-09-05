package com.goldmf.GMFund.manager.score;

import android.util.Pair;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo.PayAction;
import com.goldmf.GMFund.manager.common.RedPoint;
import com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAccountInfo;
import com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction;
import com.goldmf.GMFund.model.GMFCommResult;
import com.goldmf.GMFund.model.StockInfo;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 16/3/12.
 */
public class ScoreManager extends BaseManager {

    private final String sScoreManagerAccount = "ScoreManagerAccount";
    private final String RED_POINT_LOCATION_SCORE = "gcoin";
    private final String SCORE_ID_CHECKIN = "3";
    /**
     * 一些内存级别的存储
     */
    public ScoreAccount account = new ScoreAccount();
    public DayActionManager checkin = new DayActionManager(SCORE_ID_CHECKIN);  //每日签到
    public RedPoint scoreRedPoint = new RedPoint(RED_POINT_LOCATION_SCORE, false);          //小红点

    /**
     * 静态方法
     */
    private static ScoreManager manager = new ScoreManager();
    public static ScoreManager getInstance() {
        return manager;
    }

    private ScoreManager() {

        //读取本地数据
        loadLocalData();

        NotificationCenter.logoutSubject.subscribe(aVoid -> {
            //删除本地数据
            ModelSerialization.removeJsonByKey(sScoreManagerAccount, true);
            account = new ScoreAccount();
            checkin = new DayActionManager(SCORE_ID_CHECKIN);
            scoreRedPoint = new RedPoint(RED_POINT_LOCATION_SCORE, false);
        });

        NotificationCenter.loginSubject.subscribe(aVoid -> {

            ScoreManager.this.freshAccount(null);
            ScoreManager.this.checkin.fresh(null);
        });
    }

    private void loadLocalData() {
        {
            JsonElement ret = ModelSerialization.loadJsonByKey(sScoreManagerAccount);
            if (ret != null && ret.isJsonObject()) {
                account.readFromeJsonData(ret.getAsJsonObject());
            }
        }
    }

    /**
     * 刷新 账户
     * @param results
     */
    public final void freshAccount(final MResults<ScoreAccount> results){

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "gcoin/balance")
                .callback(new ComonProtocol.ComonCallback() {
                              @Override
                              public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }

                              @Override
                              public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                                  MResults.MResultsInfo<ScoreAccount> info = protocol.buildRet();

                                  if (ret.isJsonObject()) {
                                      account.readFromeJsonData(ret.getAsJsonObject());
                                      info.data = account;

                                      ModelSerialization.saveJsonByKey(ret, sScoreManagerAccount, true);
                                  }

                                  MResults.MResultsInfo.SafeOnResult(results, info);
                              }
                          }
                )
                .build()
                .startWork();
    }

    /**
     * 刷新用户侠币账户记录
     * @param results
     */
    public final void freshAccountRecord(final MResults<List<ScoreAccountInfo>> results){

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/activity-log")
                .callback(new ComonProtocol.ComonCallback() {
                              @Override
                              public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }

                              @Override
                              public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                                  MResults.MResultsInfo<List<ScoreAccountInfo>> info = protocol.buildRet();

                                  if (ret.isJsonObject()) {
                                      info.data = new ArrayList<>(ScoreAccountInfo.translate(GsonUtil.getAsJsonArray(ret, "list")));
                                  }

                                  MResults.MResultsInfo.SafeOnResult(results, info);
                              }
                          }
                )
                .build()
                .startWork();
    }


    /**
     * 刷新用户的所有可以领取侠币的活动（如何赚）
     * @param results
     */
    public final void freshActions(final MResults<List<ScoreAction>> results){

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/activity-earn")
                .callback(new ComonProtocol.ComonCallback() {
                              @Override
                              public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }

                              @Override
                              public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                                  MResults.MResultsInfo<List<ScoreAction>> info = protocol.buildRet();

                                  if (ret.isJsonObject()) {
                                      info.data = new ArrayList<>(ScoreAction.translate(GsonUtil.getAsJsonArray(ret, "list")));
                                  }

                                  MResults.MResultsInfo.SafeOnResult(results, info);
                              }
                          }
                )
                .build()
                .startWork();
    }

    /**
     * 领取某个活动的侠币
     * @param aID
     * @param results
     */
    public final void gainActionScore(String aID,
                                      final MResults<GMFCommResult> results){

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "activity/update")
                .postParams(ComonProtocol.buildParams("id", aID))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo<GMFCommResult> info = protocol.buildRet();

                        if (ret.isJsonObject()) {
                            info.data = GMFCommResult.translateFromJsonData(ret.getAsJsonObject());
                        }

                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 购买积分接口
     * @param amount 购买多少人民币的积分
     * @param results
     */
    public final void buyScore(double amount, final MResults<PayAction> results){

        CashierManager.getInstance().cost(amount, 1, result -> {
            if(result.isSuccess && result.data != null) {

                final PayAction.SuccessCallBack oldSuccess = result.data.success;
                result.data.success = callback -> {
                    if (oldSuccess != null) {
                        oldSuccess.onRun(null);
                    }
                    ScoreManager.getInstance().freshAccount(result1 -> {
                        MResults.MResultsInfo.SafeOnResult(callback, MResults.MResultsInfo.COPY(result1));
                    });
                };

                MResults.MResultsInfo.SafeOnResult(results, result);
            }else{

                MResults.MResultsInfo.SafeOnResult(results, result);

            }
        });
    }

    /**
     * 分享
     * @param shareType
     * @param results
     */
    public final void reportShareIncomeShow(int shareType,
                                            final MResults<Void> results){
        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "report-share-info")
                .postParams(ComonProtocol.buildParams("share_cont", String.format("%d", shareType)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }
}
