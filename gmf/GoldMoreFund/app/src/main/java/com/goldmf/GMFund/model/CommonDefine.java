package com.goldmf.GMFund.model;

import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolManager;

/**
 * Created by cupide on 15/10/6.
 */
public class CommonDefine {

    public static final int RECHARGERESULT_Lake_of_balance = 5023108;     //余额不足
    public static final int RECHARGERESULT_Error_verification = 5023109;     //验证码错误
    public static final int RECHARGERESULT_Error_other = 5023102;     //其他错误
    public static final int RECHARGERESULT_Fail = 5023107;     //充值失败
    public static final int RECHARGERESULT_Waiting = 5023110;     //充值等待

    public static final int LOGINRESULT_PASSWORD_WRONG = 10003; //密码错误

    public static final int FUNDRESULT_No_Permission = 532231;   //无权限查看该组合

    public static final int SENDCODERESULT_CODE_ERROR = 5023109;    //验证码错误
    public static final int RECHARGE_RESULT_WAITING = 5023110;    //充值结果查询中

    public static final int No_Simulation_account = 20000;      //未开通用户虚拟账号

    public static final int SNS_User_Hide_Vtc_Profile = 100015; //用户隐藏模拟业绩

    public static final String NULL_VALUE_PLACE_HOLDER = "--";

    public static class PlaceHolder {
        public static final String NULL_VALUE = "--";
    }

    //默认使用host1
    public static String H5Format(String url) {
        return H5Format(CHostName.HOST1, url);
    }

    public static String H5Format(String host, String url) {
        return CHostName.formatUrl(host, url);
    }

    //查看指引
    public static String H5URL_Guidance() {
        return H5Format("/mobile-client/deposit/foreign");
    }

    //操盘侠用户协议
    public static String H5URL_UserAgreement() {
        return H5Format("/mobile-client/terms");
    }

    //帮助中心
    public static String H5URL_Help() {
        return H5Format("/mobile-client/help");
    }

    //余额生息服务
    public static String H5URL_Help_Money() {
        return H5Format("/mobile-client/help/yu-e-sheng-xi-fu-wu");
    }

    //余额生息帮助中心
    public static String H5URL_IntrestHelp() {
        return H5Format("/mobile-client/help/yu-e-sheng-xi-fu-wu");
    }

    //风险测评
    public static String H5URL_RiskAssessment() {
        return H5Format("/mobile-client/user/risk-task");
    }

    public static String H5URL_AboutUs() {
        return H5Format("/mobile-client/page/about-us");
    }


    /**
     * 晒收益的页面 h5(带参数)
     */
    public static String H5URL_ShowIncome(int moneyType) {
        return H5Format("/pg/show_incomes?market=" + moneyType);
    }

    /**
     * 奖励金规则页面 h5
     */
    public static String H5URL_BountyRule() {
        return H5Format("/bounty/rules");
    }

    /**
     * 积分怎么花
     */
    public static String H5URL_XiaBiSpend() {
        return H5Format(CHostName.HOST2, "/page/activity-spend");
    }

    /**
     * 什么是积分
     */
    public static String H5URL_GCOIN_EXPLAIN() {
        return H5Format(CHostName.HOST2, "/gcoin/explain");
    }

    /**
     * 模拟股票开户
     */
    public static String H5URL_SIMU_ACOUNT_OPEN() {
        return H5Format(CHostName.HOST2, "/vtc/account-create-rules");
    }

    /**
     * 创建炒股大赛
     */
    public static String H5URL_Create_Game() {
        return H5Format(CHostName.HOST2, "/html/begin_competition.html");
    }


    /**
     * 申请成为操盘手
     */
    public static String H5URL_TRADER_APPLY() {
        return H5Format(CHostName.HOST1, "/trader/apply/");
    }

    /**
     * 申请成为牛人
     */
    public static String H5URL_TALENT_APPLY() {
        return H5Format(CHostName.HOST1, "/talent/apply/");
    }

    /**
     * 创建组合
     */
    public static String H5URL_CREATE_FUND() {
        return H5Format(CHostName.HOST1, "/product/create_product?pay_new=1");
    }

    /**
     * 红包规则
     * @return
     */
    public static String H5URL_COUPON_RULE() {
        return H5Format(CHostName.HOST1, "/coupon/coupon-rule");
    }


    /**
     * 红包兑换
     * @return
     */
    public static String H5URL_COUPON_CODE() {
        return H5Format(CHostName.HOST1, "/coupon/coupon-code");
    }

    public static String URL_FUND_PLACEHOLDER = "https://dn-gmf-product-face.qbox.me/o_1aj19rs5c12681c31ppg31h13d17.png";
}
