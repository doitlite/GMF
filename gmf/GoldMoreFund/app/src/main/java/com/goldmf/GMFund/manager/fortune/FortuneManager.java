package com.goldmf.GMFund.manager.fortune;

import android.util.Pair;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.common.RedPoint;
import com.goldmf.GMFund.manager.stock.RealUserIncomeChart;
import com.goldmf.GMFund.manager.stock.SimulationUserIncomeChart;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.manager.fortune.BountyAccount.BountyInfo;
import com.goldmf.GMFund.protocol.ExchangeProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action1;

import static com.goldmf.GMFund.base.MResults.MResultsInfo.SafeOnResult;
import static com.goldmf.GMFund.extension.ObjectExtension.*;
import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;

/**
 * Created by cupide on 15/8/3.
 */
public class FortuneManager {
    private static String sCNAccountKey = "CNAccountKey";
    private static String sHKAccountKey = "HKAccountKey";

    private static String sCNBountyAccountKey = "CNBountyAccountKey";
    private static String sHKBountyAccountKey = "HKBountyAccountKey";

    public FundFamily cnAccount = null;
    public FundFamily hkAccount = null;

    private FortuneInfo cnHoldFunds = new FortuneInfo();//我的沪深组合
    private FortuneInfo hkHoldFunds = new FortuneInfo();//我的港股组合

    /**
     * 奖励金账户
     */
    public BountyAccount cnBountyAccount = new BountyAccount(FundBrief.Money_Type.CN);
    public BountyAccount hkBountyAccount = new BountyAccount(FundBrief.Money_Type.HK);
    public String bountyTopMsg;          //奖励金界面顶部string
    public String bountyBottomMsg;       //奖励金界面底部string

    public List<BountyAccount.BountyInfo> bountyList = new ArrayList<>();             //奖励金详情List
    public TarLinkButton shareButton;     //分享按钮详情
    public final RedPoint bountyRedPoint = new RedPoint("bounty", false); //奖励金小红点

    /**
     * 奖励金规则List
     */
//    public final List<BountyRuleInfo> bountyRuleInfos = new ArrayList<>();
//    public String bountyRuleInfoMsg;      //奖励金规则提示信息

    /**
     * 2.4 红包相关需求
     */
    public final RedPoint couponRedPoint = new RedPoint("coupon", false);   //红包小红点
    public List<Coupon> couponList = new ArrayList<>();     //用户的红包list

    /**
     * 2.6 资金明细相关
     */
    private CommandPageArray<AccountTradeInfo> page;
    public List<Pair<Integer, String>> traderListValue = new ArrayList<>();

    /**
     * 静态方法
     */
    private static FortuneManager manager = new FortuneManager();

    public static FortuneManager getInstance() {
        return manager;
    }

    private FortuneManager() {

        //读取本地数据
        loadLocalData();

        NotificationCenter.logoutSubject.subscribe(aVoid -> {

            //清除Account
            if (cnAccount != null)
                cnAccount.remove(sCNAccountKey);

            if (hkAccount != null)
                hkAccount.remove(sHKAccountKey);

            FortuneManager.this.cnAccount = null;
            FortuneManager.this.hkAccount = null;

            FortuneManager.this.cnHoldFunds = new FortuneInfo();
            FortuneManager.this.hkHoldFunds = new FortuneInfo();

            //清除奖励金账户
            if (cnBountyAccount != null) {
                cnBountyAccount.remove(sCNBountyAccountKey);
            }
            if (hkBountyAccount != null) {
                hkBountyAccount.remove(sHKBountyAccountKey);
            }

            cnBountyAccount = new BountyAccount(FundBrief.Money_Type.CN);
            hkBountyAccount = new BountyAccount(FundBrief.Money_Type.HK);

            //删除本地数据
            if (bountyList != null)
                bountyList.clear();

            if(couponList != null)
                couponList.clear();
        });

        NotificationCenter.loginSubject.subscribe(new Action1<Void>() {
            @Override
            public void call(Void nil) {
                //刷新数据
                FortuneManager.this.freshAccount(null);

                FortuneManager.this.freshCouponList(null);

//                FortuneManager.this.freshBountyRuleInfos(null);
            }
        });
    }

    private void loadLocalData() {

        {
            this.cnAccount = FundFamily.loadData(sCNAccountKey);
            this.hkAccount = FundFamily.loadData(sHKAccountKey);
        }

        {
            BountyAccount account = BountyAccount.loadData(sCNBountyAccountKey);
            if (account != null) {
                this.cnBountyAccount = account;
            }
        }

        {
            BountyAccount account = BountyAccount.loadData(sHKBountyAccountKey);
            if (account != null) {
                this.hkBountyAccount = account;
            }
        }
    }

    public void editAccount(int moneyType, FundFamily account) {
        if (moneyType == FundBrief.Money_Type.CN) {
            this.cnAccount = account;
            this.cnAccount.save(sCNAccountKey);
        } else if (moneyType == FundBrief.Money_Type.HK) {
            this.hkAccount = account;
        }
    }

    /**
     * 刷新我的资产的第一页的数据（我的港股组合、我的沪深组合，我的证券账号，我的委托账号等）
     *
     * @param results
     */
    public void freshAccount(final MResults<Void> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "mine-gmf/account-list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.<Void>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        JsonObject cnObj = GsonUtil.getAsJsonObject(ret, "cn_account_info");
                        if (cnObj != null) {
                            FortuneManager.this.cnAccount = FundFamily.translateFromJsonData(cnObj);
                        }

                        JsonObject hkObj = GsonUtil.getAsJsonObject(ret, "hk_account_info");
                        if (hkObj != null) {
                            FortuneManager.this.hkAccount = FundFamily.translateFromJsonData(hkObj);
                        }

                        //存数据
                        FortuneManager.this.cnAccount.save(sCNAccountKey);
                        FortuneManager.this.hkAccount.save(sHKAccountKey);

                        MResults.MResultsInfo<Void> info = protocol.buildRet();
                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    public void freshHoldFunds(final int moneyType,
                               final MResults<FortuneInfo> results) {


        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "mine-gmf/product-list")
                .params(buildParams("market", String.valueOf(moneyType)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.<FortuneInfo>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        FortuneInfo desInfo = hkHoldFunds;
                        if (moneyType == FundBrief.Money_Type.CN) {
                            desInfo = cnHoldFunds;
                        }

                        if (ret != null && ret.isJsonObject()) {

                            JsonArray products = GsonUtil.getAsJsonArray(ret.getAsJsonObject(), "products");
                            if (products != null) {
                                desInfo.investFunds = new ArrayList<>(FundBrief.translate(products));
                            }

                            JsonArray profit_sharing = GsonUtil.getAsJsonArray(ret.getAsJsonObject(), "profit_sharing_data");
                            if (profit_sharing != null) {
                                desInfo.sharingFunds = new ArrayList<>(FundBrief.translate(profit_sharing));
                            }

                            JsonObject fundFamily = GsonUtil.getAsJsonObject(ret.getAsJsonObject(), "products_statistics");
                            if (fundFamily != null) {

                                FundFamily family = FundFamily.translateFromJsonData(fundFamily);

                                desInfo.family = family;
                            }
                        }

                        MResults.MResultsInfo<FortuneInfo> info = protocol.buildRet();
                        info.data = desInfo;
                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    public void getRegisterBounty(MResults<RegisterBounty> callback) {
        new ComonProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST1, "user/register/bounty"))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(callback, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        RegisterBounty data = RegisterBounty.translateFromJsonData(GsonUtil.getAsJsonObject(ret));
                        SafeOnResult(callback, new MResults.MResultsInfo<RegisterBounty>().setData(data));
                    }
                })
                .build().startWork();
    }
    /**
     * 返回 用户操作明细（资金明细）
     */
    public final void freshAccountTraderList(int transactType,
                                             final MResults<CommandPageArray<AccountTradeInfo>> results) {
        page = new CommandPageArray.Builder<AccountTradeInfo>()
                .classOfT(AccountTradeInfo.class)
                .cgiParam(buildParams("transact_type", String.valueOf(transactType)))
                .cgiUrl(CHostName.HOST1 + "history/trade-list")
                .commandPage(20)
                .parseMoreData(data -> {
                    try {
                        traderListValue.clear();
                        traderListValue.add(new Pair<>(0, "所有"));
                        JsonArray array = GsonUtil.getAsJsonArray(data, "transact_value");
                        if (array != null) {
                            for (JsonElement temp : array) {
                                JsonObject map = GsonUtil.getAsJsonObject(temp);
                                for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                                    traderListValue.add(new Pair<>(Integer.valueOf(entry.getKey()), GsonUtil.getAsString(entry.getValue())));
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                })
                .build();

        page.getPrePage(result -> SafeOnResult(results, result));
    }

    /**
     * 获取奖励金账户
     */
    public final void freshBountyAccount(final MResults<Void> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "bounty/profile")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.<Void>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        JsonObject cnObj = GsonUtil.getAsJsonObject(ret, "cn");
                        if (cnObj != null) {
                            cnBountyAccount.readFromeJsonData(cnObj);
                        }

                        JsonObject hkObj = GsonUtil.getAsJsonObject(ret, "hk");
                        if (hkObj != null) {
                            hkBountyAccount.readFromeJsonData(hkObj);
                        }

                        FortuneManager.this.bountyTopMsg = GsonUtil.getAsString(ret, "top_tips");
                        FortuneManager.this.bountyBottomMsg = GsonUtil.getAsString(ret, "bottom_reason");
                        FortuneManager.this.shareButton = TarLinkButton.translateFromJsonData(GsonUtil.getAsJsonObject(ret, "share_button"));

                        //存数据
                        FortuneManager.this.cnBountyAccount.save(sCNBountyAccountKey);
                        FortuneManager.this.hkBountyAccount.save(sHKBountyAccountKey);

                        //刷新BountyList
                        FortuneManager.this.freshBountyList(info2 -> {
                            SafeOnResult(results, info2);
                        });
                    }
                })
                .build()
                .startWork();
    }

    public void freshBountyList(final MResults<Void> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "bounty/event-all-list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.<Void>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        bountyList = new ArrayList<>(BountyInfo.translate(GsonUtil.getAsJsonArray(ret, "all_lists")));

                        MResults.MResultsInfo<Void> info = protocol.buildRet();
                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 刷新我的红包list
     * @param results
     */
    public void  freshCouponList(final MResults<List<Coupon>> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "coupon/coupon-list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        couponList = new ArrayList<>(Coupon.translate(GsonUtil.getAsJsonArray(ret, "list")));

                        MResults.MResultsInfo<List<Coupon>> info = protocol.buildRet();
                        info.data = couponList;
                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 刷新奖励金规则
     public final void freshBountyRuleInfos(final MResults<Void> results) {
     new ComonProtocol.Builder()
     .url(CHostName.HOST1 + "bounty/event-list")
     .callback(new ComonProtocol.ComonCallback() {
    @Override public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
    MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
    }

    @Override public void onSuccess(ComonProtocol protocol, JsonElement ret) {

    JsonArray array = GsonUtil.getAsJsonArray(ret, "list");
    if (array != null) {
    FortuneManager.this.bountyRuleInfos.clear();
    for (JsonElement temp : array) {
    JsonObject obj = GsonUtil.getAsJsonObject(temp);
    if (obj != null) {
    BountyRuleInfo info = BountyRuleInfo.translateFromJsonData(obj);
    if (info != null)
    FortuneManager.this.bountyRuleInfos.add(info);
    }
    }
    }

    FortuneManager.this.bountyRuleInfoMsg = GsonUtil.getAsString(ret, "msg");

    MResults.MResultsInfo<Void> info = protocol.buildRet();
    MResults.MResultsInfo.SafeOnResult(results, info);
    }
    })
     .build()
     .startWork();
     }*/


    /**
     * 提取奖励金余额到用户的账户中
     */
    public final void exchange(int moneyType,
                               double amount,
                               final MResults<Void> results) {
        ExchangeProtocol p = new ExchangeProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                //刷新资产页
                FortuneManager.getInstance().freshAccount(result -> {

                    //直接返成功
                    MResults.MResultsInfo<Void> info = protocol.buildRet();
                    SafeOnResult(results, info);
                });
            }
        });
        p.moneyType = moneyType;
        p.amount = amount;
        p.startWork();
    }


    /**
     * 获取某个用户的个人主页 的 投资业绩
     *
     * @param userID
     * @param results
     */
    public final void freshIncomeChart(int userID,
                                       final MResults<List<RealUserIncomeChart>> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/user-invest-profile")
                .params(buildParams("user_id", String.valueOf(userID)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo<List<RealUserIncomeChart>> info = protocol.buildRet();

                        if (ret.isJsonObject()) {
                            List<RealUserIncomeChart> array = new ArrayList<>();

                            {
                                JsonObject object = GsonUtil.getAsJsonObject(ret, "cn");
                                if (object != null) {
                                    array.add(RealUserIncomeChart.translateFromJsonData(object));
                                }
                            }
                            {
                                JsonObject object = GsonUtil.getAsJsonObject(ret, "hk");
                                if (object != null) {
                                    array.add(RealUserIncomeChart.translateFromJsonData(object));
                                }
                            }

                            info.data = array;
                        }

                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }
}
