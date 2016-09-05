package com.goldmf.GMFund.model;

import android.graphics.Paint;
import android.support.annotation.Nullable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.generic.WrappingUtils;
import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.extension.IntExtension;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.trader.FundTradeInfo;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.locks.Lock;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.IntExtension.notMatch;

/**
 * Created by cupide on 15/8/3.
 */
public class FundBrief extends PageArray.PageItemIndex {

    public static class Fund_Type {
        public final static int Porfolio = 1;    //进取型操盘乐
        public final static int Bonus = 2;      //分红乐
        public final static int Charitable = 3;    //公益乐

        public final static int WuYo = 4;    //无忧型操盘乐
        public final static int WenJian = 5;    //稳健型操盘乐

        public final static int NoDefine = 6;    //未定义的组合

        public static int toFundType(int type) {
            if (type >= NoDefine)
                return NoDefine;
            else
                return type;
        }

        public static String toString(int type) {
            if (type == Porfolio) {
                return "进取型";
            } else if (type == WuYo) {
                return "无忧型";
            } else if (type == WenJian) {
                return "稳盈型";
            }

            return "";
        }

        public static boolean isUnknown(int type) {
            return notMatch(type, Porfolio, Bonus, Charitable, WuYo, WenJian);
        }

        /**
         * 是否有募集金额上限
         */
        public static boolean hasInvestLimit(int type) {
            return anyMatch(type, Bonus, WuYo, WenJian);
        }
    }

    public static class Fund_Status {

        public final static int Stop = 0;    //0 结束
        public final static int Review = 1;    //1 审核期（内部含义，准备期）
        public final static int Capital = 2;    //2 募集期间（内部含义，募集期）
        public final static int LockIn = 3;    //3 封闭运行中
        public final static int Booking = 4;    //4 预约ing的组合
        public final static int NoDefine = 5;    //不支持的状态

        public static int toFundSatus(int status) {
            if (status >= NoDefine)
                return NoDefine;
            else
                return status;
        }

        public static boolean afterReview(int status) {
            return status == Booking || status == Capital || status == LockIn || status == Stop;
        }

        public static boolean beforeCapital(int status) {
            return status == Review || status == Booking;
        }

        public static boolean afterCapital(int status) {
            return status == LockIn || status == Stop;
        }

        public static boolean beforeLockIn(int status) {
            return status == Review || status == Booking || status == Capital;
        }

        public static boolean isUnknown(int status) {
            return notMatch(status, Stop, Review, Capital, LockIn, Booking);
        }
    }

    public static class Money_Type {

        public final static int CN = 1;    //人民币
        public final static int HK = 2;   // 港币
        public final static int US = 3;    // 美元

        public static String getSymbol(int type) {
            switch (type) {
                case CN:
                    return "￥";
                case HK:
                    return "HK$";
                case US:
                    return "US$";
                default:
                    return "";
            }
        }

        public static String getUnit(int type) {
            switch (type) {
                case CN:
                    return "元";
                case HK:
                    return "港元";
                case US:
                    return "美元";
                default:
                    return "";
            }
        }

        public static String getStockType(int type) {
            switch (type) {
                case CN:
                    return "沪深";
                case HK:
                    return "港股";
                case US:
                    return "美股";
                default:
                    return "";
            }
        }

        public static int getInstance(int value) {
            if (value == 1) return CN;
            else if (value == 2) return HK;
            else if (value == 3) return US;
            else return CN;
        }
    }

    @Override
    public Object getKey() {
        return this.index;
    }

    @Override
    public long getTime() {
        return 0;
    }

    //以下为基本信息
    public int index;               //server索引信息
    public String name;            //全称
    public int innerType;           //底层产品类型
    public int type;                //fund类型
    public int status;             //当前状态
    private int subStatus;          //小状态

    public String fundIcon;         //组合图标
    public String statusTitle;      //当前状态标题
    public String statusDetail;     //当前状态信息

    public int moneyType;           //货币种类

    public double traderInvest;     //操盘手出资
    public int traderID;            //操盘手id
    public User traderUser;            //操盘手

    //投资ing相关
    public double targetCapital;    //需要募集资金数量
    public double raisedCapital;    //募集到的资金数量

    public double minInvestLimit;       //最小投资额度
    public double maxInvestLimit;       //最大投资额度
    public int investTimes;             //投资成员个数

    //预约态的变量
    public int bookingTimes;             //预约人次

    //我的相关
    public FundPreferential preferential;   //加息详情

    public UserInvest investOrNull;                 //用户投资收益详情，如果有的话
    public TraderInvest traderInvestMementOrNull;   //操盘手收益详情，如果有的话

    //以下为收益相关
    public boolean incomeVisible = false;          //是否 非投资人可见
    private Double currentIncomeOrNull;      //最近一次总收益
    public Double currentIncomeRatioOrNull; //最近一次总收益率
    public Double currentIncomeAnnualRatioOrNull; //最近一次年化总收益率
    public Double currentNetValueOrNull;    //最近一次净值
    public Double currentPositionOrNull;    //最近一次仓位

    public Double recentYearIncomeRatio;    //最近一年收益率
    public double expectedMinAnnualYield;    //预期年化收益(保本) （10%）
    public double expectedMaxAnnualYield;    //预期年化收益(浮动最高)（浮动最高 16%）
    public
    @Nullable
    Double floatingAnnualYieldOrNull;       //当前浮动年化收益 （16% 算）

    public Double userProfitSharingRatio;   //用户分成比例
    public Double traderProfitSharingRatio; //操盘手分成比例

    public double stopLossRatio;      //止损阈值、清仓线
    public double earlyWarningRatio;     //预警线
    public double capitalEnsureRatio;   //本金保障率

    private Double dayIncome; //当日盈亏
    public Double dayIncomeRatio; //当日盈亏率
    private double totalCapital;          //总资产

    public Double dayIncome(){
        if(tradeInfo != null)
            return tradeInfo.dayIncome;
        return dayIncome;
    }

    public Double currentIncomeOrNull(){
        if(tradeInfo != null)
            return tradeInfo.income;
        return currentIncomeOrNull;
    }

    public double totalCapital(){
        if(tradeInfo != null)
            return tradeInfo.totalCapital;
        return totalCapital;
    }

    private  FundTradeInfo tradeInfo = null;
    public void editRealTimeTradeInfo(FundTradeInfo tradeInfo)
    {
        this.tradeInfo = tradeInfo;
    }


    public void readFromeJsonData(JsonObject dic) {

        this.index = GsonUtil.getAsInt(dic, "id");
        this.name = GsonUtil.getAsString(dic, "name");
        this.status = Fund_Status.toFundSatus(GsonUtil.getAsInt(dic, "fund_status"));

        this.fundIcon = GsonUtil.getAsString(dic, "fund_icon");
        this.subStatus = GsonUtil.getAsInt(dic, "status");
        this.moneyType = GsonUtil.getAsInt(dic, "market");

        this.type = Fund_Type.toFundType(GsonUtil.getAsInt(dic, "fund_type"));
        this.innerType = Fund_Type.toFundType(GsonUtil.getAsInt(dic, "fund_inner_type"));

        this.statusTitle = GsonUtil.getAsString(dic, "fund_status_title");
        this.statusDetail = GsonUtil.getAsString(dic, "fund_status_detail");

        this.totalCapital = GsonUtil.getAsDouble(dic, "total_capital");
        this.raisedCapital = GsonUtil.getAsDouble(dic, "collect_capital");
        this.targetCapital = GsonUtil.getAsDouble(dic, "target_capital");

        this.createTime = GsonUtil.getAsLong(dic, "create_at");

        this.startTime = GsonUtil.getAsLong(dic, "start_time");
        this.stopTime = GsonUtil.getAsLong(dic, "stop_time");
        this.beginFundraisingTime = GsonUtil.getAsLong(dic, "begin_fundraising_time");
        this.endFundraisingTime = GsonUtil.getAsLong(dic, "end_fundraising_time");

        this.currentNetValueOrNull = GsonUtil.getAsNullableDouble(dic, "net_value");
        this.currentPositionOrNull = GsonUtil.getAsNullableDouble(dic, "position");

        this.currentIncomeOrNull = GsonUtil.getAsNullableDouble(dic, "income");
        this.currentIncomeRatioOrNull = GsonUtil.getAsNullableDouble(dic, "income_ratio");
        this.currentIncomeAnnualRatioOrNull = GsonUtil.getAsNullableDouble(dic, "annual_income_ratio");

        this.traderID = GsonUtil.getAsInt(dic, "operator_uid");
        this.traderInvest = GsonUtil.getAsDouble(dic, "operator_invest");
        if (dic.has("operator")) {
            JsonObject operatorDic = GsonUtil.getAsJsonObject(dic, "operator");
            this.traderUser = User.translateFromJsonData(operatorDic);
        }
        else {
            if(MineManager.isMe(this.traderID)){
                this.traderUser = MineManager.getInstance().getmMe();
            }
        }

        this.minInvestLimit = GsonUtil.getAsDouble(dic, "invest_min_limit");
        this.maxInvestLimit = GsonUtil.getAsDouble(dic, "invest_max_limit");

        {
            this.expectedMinAnnualYield = GsonUtil.getAsDouble(dic, "bonus_more", "expected_min_annual_yield");
            this.expectedMaxAnnualYield = GsonUtil.getAsDouble(dic, "bonus_more", "expected_max_annual_yield");
            this.floatingAnnualYieldOrNull = GsonUtil.getAsNullableDouble(dic, "bonus_more", "floating_annual_yield");

            this.recentYearIncomeRatio = GsonUtil.getAsNullableDouble(dic, "recent_year_income_ratio");
        }

        this.investTimes = GsonUtil.getAsInt(dic, "invest_times");
        this.bookingTimes = GsonUtil.getAsInt(dic, "booking_times");

        this.preferential = FundPreferential.translateFromJsonData(dic);

        if (GsonUtil.has(dic, "investment")) {
            JsonObject investDic = GsonUtil.getAsJsonObject(dic, "investment");
            this.investOrNull = UserInvest.translateFromJsonData(investDic);
        }

        if (GsonUtil.has(dic, "trader_investment")) {
            JsonObject traderInvestDic = GsonUtil.getAsJsonObject(dic, "trader_investment");
            this.traderInvestMementOrNull = TraderInvest.translateFromJsonData(traderInvestDic);
        }

        this.tradingDay = GsonUtil.getAsInt(dic, "trading_day");
        this.leftRunningDay = GsonUtil.getAsInt(dic, "left_running_day");
        this.incomeVisible = GsonUtil.getAsBoolean(dic, "income_visible");
        this.userProfitSharingRatio = GsonUtil.getAsDouble(dic, "user_profit_sharing_ratio");
        this.traderProfitSharingRatio = GsonUtil.getAsDouble(dic, "profit_sharing_ratio");

        this.stopLossRatio = GsonUtil.getAsDouble(dic, "stop_loss");
        this.earlyWarningRatio = GsonUtil.getAsDouble(dic, "early_warning");
        this.capitalEnsureRatio = GsonUtil.getAsDouble(dic, "capital_ensure");

        this.dayIncome = GsonUtil.getAsDouble(dic, "day_income");
        this.dayIncomeRatio = GsonUtil.getAsDouble(dic, "day_income_ratio");
    }

    public static FundBrief translateFromJsonData(JsonObject dic) {
        FundBrief sFund = new FundBrief();
        sFund.readFromeJsonData(dic);

        //sever不应该下发这个！
        if (sFund.subStatus == 7)
            return null;

        return sFund;
    }

    public static List<? extends FundBrief> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }


    /**
     * 业绩 h5
     */
    public String fundTradeH5URL() {
        return CommonDefine.H5Format("/product/" + this.index + "/summary");
    }

    /**
     * 组合信息 h5
     */
    public String fundDetailH5URL() {
        return CommonDefine.H5Format("/product/" + this.index + "/more_info");
    }

    /**
     * 用户投资协议(带参数)
     */
    public String investAgreementH5URL() {
        return CommonDefine.H5Format("/mobile-client/product/investment-agreement/" + this.index);
    }

    /**
     * 组合概览 h5
     */
    public String fundInfoH5URL() {
        return CommonDefine.H5Format("/product/" + this.index + "/overview/");
    }

    /**
     * 获取 组合的 状态变化 url fundStatusChangeURL，
     * @return
     */
    public String fundStatusChangeURL(){
        return CommonDefine.H5Format("/omsv2/oms/client/page/notice/?product_id=" + this.index);
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof FundBrief && this.hashCode() == o.hashCode();
    }

    //以下为时间相关变量
    public long createTime;  //创建时间
    public long startTime;  //开始时间(秒)
    public long stopTime;   //结束到期时间(秒)
    public long beginFundraisingTime;  //组合募集开始时间
    public long endFundraisingTime;    //组合募集结束时间

    public int tradingDay;               //运行交易日
    public int leftRunningDay;           //剩余交易日

    public String timeFormatStr() {
        switch (this.status) {
            case Fund_Status.Stop:
                return String.format("共运行%d天", this.expirationDay());
            case Fund_Status.Review:
            case Fund_Status.Booking:
                return "开放投资未开始";
            case Fund_Status.Capital: {
                if (this.remainingDays() > 0) {
                    return "投资期剩余" + FormatUtil.formateRemainingDays(this.remainingDays());
                } else {
                    return "投资期已结束";
                }
            }
            case Fund_Status.LockIn: {
                String str = "已运行" + this.runningDaysStr();
                if (this.remainingDays() <= 0) {
                    return str;
                } else {
                    return str + "，剩余" + FormatUtil.formateRemainingDays(this.remainingDays());
                }
            }
            default:
                break;
        }
        return "";

    }

    public final String runningDaysStr() {

        return FormatUtil.formateRunningDays(this.runningDays());

    }

    public final String stopDayStr() {
        return FormatUtil.formateRemainingDays(((double) this.stopTime - SecondUtil.currentSecond()) / (24 * 60 * 60));
    }

    /**
     * 剩余天数：（会随着state不同而出现不同的值）
     *
     * @return
     */
    public double remainingDays() {

        long current = SecondUtil.currentSecond();
        if (FundBrief.this.status == Fund_Status.LockIn) {
            if (current >= this.startTime) {
                if (current <= this.stopTime) {
                    double difTime = (((double) this.stopTime - current) / (24 * 60 * 60));
                    return difTime;
                }
            }
        } else if (this.status == Fund_Status.Capital) {
            if (current >= this.beginFundraisingTime) {
                if (current <= this.endFundraisingTime) {
                    double difTime = (((double) this.endFundraisingTime - current) / (24 * 60 * 60));
                    return difTime;
                }
            }
        } else if (this.status == Fund_Status.Booking) {
            if (current <= this.beginFundraisingTime) {
                double difTime = (((double) this.beginFundraisingTime - current) / (24 * 60 * 60));
                return difTime;
            }
        }
        return 0;
    }

    /**
     * 已经运行天数：（会随着state不同而出现不同的值）
     */
    public int runningDays() {
        long current = SecondUtil.currentSecond();
        if (this.status == Fund_Status.LockIn) {
            if (current >= this.startTime) {
                if (current <= this.stopTime) {
                    double difTime = (((double) current - this.startTime) / (24 * 60 * 60));
                    return d2i(difTime);
                }
            }
        } else if (this.status == Fund_Status.Capital) {
            if (current >= this.beginFundraisingTime) {
                if (current <= this.endFundraisingTime) {
                    double difTime = (((double) current - this.beginFundraisingTime) / (24 * 60 * 60));
                    return d2i(difTime);
                }
            }
        }
        return 0;
    }

    public int expirationDay() {
        if (this.stopTime != 0 && this.startTime != 0) {
            int difTime = (int) ((this.stopTime - this.startTime) / (24 * 60 * 60));
            return difTime;
        }
        return 0;
    }

    private int d2i(double data) {
        int intValue = (int) data;
        if (data - intValue > 0.5) {
            return intValue + 1;
        }
        return intValue;
    }


    public static class FundPreferential implements Serializable {
        public double preferentialRatio;        //当前加息比例（0.5%）
        public double maxPreferentialRatio;     //最高加息比例（4%）

        public void readFromeJsonData(JsonObject dic) {

            preferentialRatio = GsonUtil.getAsDouble(dic, "preferential", "preferential_rate");
            maxPreferentialRatio = GsonUtil.getAsDouble(dic, "bonus_more", "max_preferential_rate");
        }

        public static FundPreferential translateFromJsonData(JsonObject dic) {
            try {
                FundPreferential preferential = new FundPreferential();
                preferential.readFromeJsonData(dic);
                return preferential;

            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
