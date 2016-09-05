package com.goldmf.GMFund.manager.cashier;

import android.text.TextUtils;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo.PayAction;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.protocol.BindBankCardProtocol;
import com.goldmf.GMFund.protocol.DepositProtocol;
import com.goldmf.GMFund.protocol.InvestProtocol;
import com.goldmf.GMFund.protocol.RechargeProtocol;
import com.goldmf.GMFund.protocol.WithdrawProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 15/9/3.
 */
public class CashierManager {

    private static String sCashierManagerBanksJsonKey = "CashierManagerBanksJsonKey";

    private static String sBankCardKey = "BankCardKey";
    private static String sHKBankCardKey = "HKBankCardKey";

    public final List<BankInfo> banks = new ArrayList<>();

    private BankCard card = BankCard.buildEmptyCard();      //绑定的银行卡(只有一个)
    private BankCard hkCard = BankCard.buildEmptyCard();

    private BindBankCardProtocol bindBankCardProtocol = null;
    private RechargeProtocol rechargeProtocol = null;

    private TarLinkButton.TarLinkText bankTips = null;

    /**
     * 静态方法
     */
    private static CashierManager manager = new CashierManager();

    public static CashierManager getInstance() {
        return manager;
    }

    private CashierManager() {

        loadLocalData();

        NotificationCenter.logoutSubject.subscribe(aVoid -> {
            //删除本地数据
            if (CashierManager.this.card != null) {
                card.remove(sBankCardKey);
            }

            if (CashierManager.this.hkCard != null) {
                hkCard.remove(sHKBankCardKey);
            }

            CashierManager.this.card = BankCard.buildEmptyCard();
            CashierManager.this.hkCard = BankCard.buildEmptyCard();

        });

        NotificationCenter.loginSubject.subscribe(aVoid -> {
            //刷新数据
            CashierManager.this.freshBanks(result ->
                    MyApplication.SHARE_INSTANCE.mHandler.post(() -> CashierManager.this.freshMyBankCard()));
        });
    }

    private void loadLocalData() {
        {
            JsonElement ret = ModelSerialization.loadJsonByKey(sCashierManagerBanksJsonKey);
            if (ret != null) {
                this.loadBanks(ret);
            }
        }

        {
            BankCard temp = BankCard.loadData(sBankCardKey);
            if (temp != null)
                this.card = temp;
        }

        {
            BankCard temp = BankCard.loadData(sHKBankCardKey);
            if (temp != null)
                this.hkCard = temp;
        }
    }

    /**
     * @return 绑定的人民币账户
     */
    public BankCard getCard() {
        return card;
    }

    /**
     * @return 绑定的港币账户
     */
    public BankCard getHkCard() {
        return hkCard;
    }


    /**
     * @return 人民币的现金余额
     */
    public double getCnCashBalance() {
        if (FortuneManager.getInstance().cnAccount != null)
            return FortuneManager.getInstance().cnAccount.cashBalance;
        return 0;
    }

    /**
     * @return 人民币的现金余额
     */
    public double getCnCashBalanceWithoutDecimal() {
        if (FortuneManager.getInstance().cnAccount != null)
            return new BigDecimal(FortuneManager.getInstance().cnAccount.cashBalance).setScale(0, RoundingMode.DOWN).doubleValue();
        return 0;
    }

    /**
     * @return 港币的现金余额
     */
    public double getHkCashBalance() {

        if (FortuneManager.getInstance().hkAccount != null)
            return FortuneManager.getInstance().hkAccount.cashBalance;
        return 0;

    }

    /**
     * @return 港币的现金余额
     */
    public double getHkCashBalanceWithoutDecimal() {

        if (FortuneManager.getInstance().hkAccount != null)
            return new BigDecimal(FortuneManager.getInstance().hkAccount.cashBalance).setScale(0, RoundingMode.DOWN).doubleValue();
        return 0;

    }


    public void freshBanks() {
        this.freshBanks(null);
    }

    private void loadBanks(JsonElement ret) {
        JsonArray array = GsonUtil.getAsJsonArray(ret);
        if (array != null) {
            banks.clear();

            for (JsonElement temp : array) {
                JsonObject obj = GsonUtil.getAsJsonObject(temp);
                if (obj != null) {
                    banks.add(BankInfo.translateFromJsonData(obj));
                }
            }
        }
    }

    /**
     * @param results
     */
    public void freshBanks(MResults<Void> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "payment/bank-list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }


                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        CashierManager.this.loadBanks(ret);

                        //存数据
                        ModelSerialization.saveJsonByKey(ret, sCashierManagerBanksJsonKey);

                        MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }


    /*
    public final void freshCashBalance(final FundBrief.Money_Type type) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "cashier/get_user_cash")
                .params(ComonProtocol.buildParams("market", String.valueOf(type.toInt())))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        if (type == FundBrief.Money_Type.CN) {
                            cnCashBalance = GsonUtil.getAsDouble(ret, "cash_remain");
                        } else if (type == FundBrief.Money_Type.HK) {
                            hkCashBalance = GsonUtil.getAsDouble(ret, "cash_remain");
                        } else {
                            assert (false);
                        }
                    }
                })
                .build()
                .startWork();
    }
    */

    /**
     * 刷新两种绑定的银行卡
     */
    public final void freshMyBankCard() {

        this.freshMyBankCard(FundBrief.Money_Type.CN, null);
        this.freshMyBankCard(FundBrief.Money_Type.HK, null);
    }


    /**
     * 绑定银行卡,发送验证码
     *
     * @param bindBankCard
     * @param results
     */
    public final void bindBankCard(final BankCard bindBankCard,
                                   final MResults<Void> results) {

        this.bindBankCardProtocol = new BindBankCardProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());

            }
        });
        this.bindBankCardProtocol.bindBankCard = bindBankCard;
        this.bindBankCardProtocol.startWork();
    }

    /**
     * 绑定银行卡,输入验证码并验证
     *
     * @param verifyCode
     * @param results
     */
    public final void bindBankCardNext(String verifyCode,
                                       final MResults<Void> results) {
        if (this.bindBankCardProtocol == null || this.bindBankCardProtocol.ticket == null) {
            MResultsInfo.SafeOnResult(results, ProtocolBase.<Void>buildErr(-1, "请先获取验证码。"));
        } else {
            this.bindBankCardProtocol.mCallback = new ProtocolCallback() {
                @Override
                public void onFailure(ProtocolBase protocol, int errCode) {
                    MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                }

                @Override
                public void onSuccess(ProtocolBase protocol) {
                    BankCard bindBankCard = bindBankCardProtocol.bindBankCard;
                    bindBankCardProtocol = null;

                    //伪造一张正在绑定ing的卡
                    CashierManager.this.card = BankCard.buildWatingCard(bindBankCard);

                    MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                }
            };

            this.bindBankCardProtocol.verifyCode = verifyCode;
            this.bindBankCardProtocol.startWork();
        }
    }

    /**
     * 原来的查询充值、投资资格，因为不容易理解，改为“换卡查询”
     *
     * @param results 返回
     */
    public final void changeCard(final MResults<ServerMsg<Boolean>> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + ("payment/card-qualify"))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        if (results != null) {
                            MResultsInfo.SafeOnResult(results, protocol.<ServerMsg<Boolean>>buildRet());
                        }
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        MResultsInfo<ServerMsg<Boolean>> info = protocol.buildRet();

                        if (ret != null && ret.isJsonObject()) {
                            JsonObject dicData = GsonUtil.getAsJsonObject(ret);

                            PayAction action = PayAction.translateFromJsonData(GsonUtil.getAsJsonObject(dicData, "action"));

                            ServerMsg<Boolean> msg = ServerMsg.<Boolean>translateFromJsonData(dicData);
                            if (msg != null && action != null) {
                                msg.setData(true);
                                info.setData(msg);
                            }

                            CashierManager.this.card.readFromJsonData(GsonUtil.getAsJsonObject(dicData, "action", "bank_card"));
                        }
                        MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 充值
     *
     * @param rachargeAmount 充值金额
     * @param results        结果返回
     */
    public final void beginRecharge(double rachargeAmount, final MResults<ServerMsg<RechargeDetailInfo>> results) {

        beginRecharge(false, rachargeAmount, results);
    }

    /**
     * 投资
     *
     * @param fundID       组合id
     * @param investAmount 投资金额
     * @param couponID     红包id
     * @param results      结果返回 Object为（Double类型）rechargeAmount：需要充值的金额，需要上层调用beginRecharge(true,rechargeAmount,callback)，
     *                     否则为（PayAction类型）action:投资url详情
     */
    public final void invest(int fundID,
                             double investAmount,
                             String couponID,
                             final MResults<ServerMsg<Object>> results) {

        InvestProtocol investProtocol = new InvestProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {

                MResultsInfo.SafeOnResult(results, protocol.<ServerMsg<Object>>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase p) {
                InvestProtocol protocol = (InvestProtocol) p;

                MResultsInfo<ServerMsg<Object>> info = p.buildRet();
                info.data = protocol.getMsg();
                MResultsInfo.SafeOnResult(results, info);
            }
        });

        investProtocol.fundID = fundID;
        investProtocol.couponID = couponID;
        investProtocol.investAmount = investAmount;
        investProtocol.startWork();
    }

    /**
     * 同一个orderID的继续充值
     *
     * @param orderID orderID
     * @param results
     */
    public final void continueRecharge(String orderID,
                                       final MResults<ServerMsg<RechargeDetailInfo>> results) {
        boolean isInvest = false;
        if (rechargeProtocol != null) {
            isInvest = rechargeProtocol.isInvest;
        }

        rechargeProtocol = new RechargeProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {

                MResultsInfo.SafeOnResult(results, protocol.<ServerMsg<RechargeDetailInfo>>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                RechargeProtocol p = (RechargeProtocol) protocol;
                ServerMsg<RechargeDetailInfo> msg = p.getMsg();

                MResultsInfo<ServerMsg<RechargeDetailInfo>> info = protocol.buildRet();
                info.data = msg;
                MResultsInfo.SafeOnResult(results, info);
            }
        });

        rechargeProtocol.orderID = orderID;
        rechargeProtocol.isInvest = isInvest;
        rechargeProtocol.startWork();
    }

    /**
     * 投资、充值 结束
     *
     * @param results
     */
    public final void finishRechargeOrInvest(final MResults<Void> results) {

        this.freshMyAccount(result -> {
            FortuneManager.getInstance().couponRedPoint.freshRedPoint(null);
            MResults.MResultsInfo.SafeOnResult(results, result);
        });
    }


    /**
     * 登记港股充值协议
     *
     * @param card
     * @param certificate
     * @param amount
     * @param remark
     * @param results
     */
    public final void depositRecharge(BankCard card,
                                      String certificate,
                                      double amount,
                                      String remark,
                                      final MResults<Void> results) {
        DepositProtocol p = new DepositProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResultsInfo<Void> info = protocol.buildRet();
                MResultsInfo.SafeOnResult(results, info);

                CashierManager.this.freshMyBankCard(FundBrief.Money_Type.HK, null);

            }
        });
        p.card = card;
        p.amount = amount;
        p.certificate = certificate;
        p.remark = remark;
        p.moneyType = FundBrief.Money_Type.HK;
        p.startWork();
    }

    /**
     * 提现
     *
     * @param moneyType
     * @param amount
     * @param results
     */
    public void withdraw(int moneyType,
                         double amount,
                         final MResults<ServerMsg<PayAction>> results) {
        if (!(this.card != null && this.card.bank != null)) {
            return;
        }

        WithdrawProtocol p = new WithdrawProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResultsInfo.SafeOnResult(results, protocol.<ServerMsg<PayAction>>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResultsInfo<ServerMsg<PayAction>> info = protocol.buildRet();

                if (moneyType == FundBrief.Money_Type.CN) {
                    WithdrawProtocol p2 = (WithdrawProtocol) protocol;
                    info.data = p2.msg;

                }
                MResultsInfo.SafeOnResult(results, info);

                //刷新资产页
                FortuneManager.getInstance().freshAccount(null);
            }
        });
        p.amount = amount;
        p.moneyType = moneyType;
        p.startWork();
    }

    /**
     * 提现成功刷新
     *
     * @param results
     */
    public final void withdrawSuccess(final MResults<Void> results) {

        this.freshMyAccount(results);
    }


    /**
     * 刷新银行tips
     *
     * @param results
     */
    public void freshBankTips(final MResults<TarLinkButton.TarLinkText> results) {
        if (bankTips != null && !TextUtils.isEmpty(bankTips.content)) {
            MResultsInfo.SafeOnResult(results, new MResultsInfo<TarLinkButton.TarLinkText>().setData(bankTips));
        }


        new ComonProtocol.Builder()
                .url("public-notice-config?cmd=payment")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<TarLinkButton.TarLinkText> info = protocol.buildRet();
                        info.data = TarLinkButton.TarLinkText.translateFromJsonData(GsonUtil.getAsJsonObject(ret));
                        bankTips = info.data;
                        MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 刷新用户的绑卡信息
     *
     * @param moneyType
     * @param results
     */
    public void freshMyBankCard(int moneyType,
                                final MResults<BankCard> results) {

        String url = null;
        if (moneyType == FundBrief.Money_Type.CN) {
            url = CHostName.HOST1 + "payment/bind-card-list";
        } else {
            url = CHostName.HOST1 + "user/get-foreign-bank";
        }

        new ComonProtocol.Builder()
                .url(url)
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        if (results != null) {
                            MResultsInfo.SafeOnResult(results, protocol.<BankCard>buildRet());
                        }
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        MResultsInfo<BankCard> info = protocol.buildRet();

                        if (moneyType == FundBrief.Money_Type.CN) {
                            JsonArray array = GsonUtil.getAsJsonArray(ret, "list");
                            JsonObject obj = GsonUtil.getChildAsJsonObject(array, 0);

                            if (obj != null) {
                                card = BankCard.translateFromJsonData(obj);
                            }

                            //存数据
                            if (card != null) {
                                card.save(sBankCardKey);
                            }

                            info.data = card;
                        } else {
                            JsonObject obj = GsonUtil.getAsJsonObject(ret);
                            if (obj != null) {
                                hkCard = BankCard.buildHKBankCard(obj);
                            }

                            //存数据
                            if (hkCard != null) {
                                hkCard.save(sHKBankCardKey);
                            }

                            info.data = hkCard;
                        }

                        MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 余额消费接口
     */
    public final void cost(double amount, int action, final MResults<PayAction> results) {

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST1 + "payment/cost2")
                .postParams(ComonProtocol.buildParams("cost_amount", String.valueOf(amount),
                        "action", String.valueOf(action),
                        "market", "1"))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<PayAction> info = protocol.buildRet();
                        info.data = PayAction.translateFromJsonData(GsonUtil.getAsJsonObject(ret, "action"));

                        if (info.data != null) {
                            info.data.success = results1 -> {
                                FortuneManager.getInstance().freshAccount(null);
                            };
                        }

                        MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 余额消耗成功回调
     *
     * @param results
     */
    public final void costSuccess(final MResults<Void> results) {

        this.freshMyAccount(result -> {
            MResults.MResultsInfo.SafeOnResult(results, result);
        });
    }


    private void freshMyAccount(final MResults<Void> results) {
        //顺手刷新下卡
        if (this.card.status == BankCard.Card_Status_Empty) {
            this.freshMyBankCard(FundBrief.Money_Type.CN, null);
        }

        FortuneManager.getInstance().freshAccount(result -> {
            MResults.MResultsInfo.SafeOnResult(results, result);
        });
    }


    /**
     * 充值
     * @param inInvest 是否是投资并充值
     * @param rachargeAmount 充值金额
     * @param results 返回
     */
    public final void beginRecharge(boolean inInvest, double rachargeAmount, final MResults<ServerMsg<RechargeDetailInfo>> results) {

        rechargeProtocol = new RechargeProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {

                MResultsInfo.SafeOnResult(results, protocol.<ServerMsg<RechargeDetailInfo>>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                RechargeProtocol p = (RechargeProtocol) protocol;
                ServerMsg<RechargeDetailInfo> msg = p.getMsg();

                MResultsInfo<ServerMsg<RechargeDetailInfo>> info = protocol.buildRet();
                info.data = msg;
                MResultsInfo.SafeOnResult(results, info);
            }
        });

        rechargeProtocol.amount = rachargeAmount;
        rechargeProtocol.isInvest = inInvest;
        rechargeProtocol.startWork();
    }
}
