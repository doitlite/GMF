package com.goldmf.GMFund.manager.fortune;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.extension.IntExtension;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.IntExtension.notMatch;

/**
 * Created by cupide on 16/7/13.
 */
public class Coupon implements Serializable{

    private static final int Coupon_Status_begin = -1;
    public static final int Coupon_Status_over = 0; //过期红包
    public static final int Coupon_Status_normal = 1;  //正常红包
    public static final int Coupon_Status_used = 2;    //使用过的红包
    public static final int Coupon_Status_invalid = 3; //未生效的红包
    public static final int Coupon_Status_noDefine = 4; //未定义的红包

    private static final int Coupon_Type_begin = 0;
    public static final int Coupon_Type_single_amount = 1;  //单次投资满**元可用**元的红包
    public static final int Coupon_Type_noDefine = 2;       //未定义的红包类型

    public static boolean isUnknownStatus(int status) {
        return notMatch(status, Coupon_Status_begin, Coupon_Status_over, Coupon_Status_normal, Coupon_Status_used, Coupon_Status_invalid);
    }

    public static boolean isUnknownType(int type) {
        return notMatch(type, Coupon_Type_begin, Coupon_Type_single_amount);
    }

    public String sId;   //红包id
    public int moneyType;     //红包币种
    public int status;     //红包状态
    public int type;         //红包类型

    public long startTime;    //红包有效期开始时间
    public long stopTime;     //红包有效期结束时间
    public long usedTime;     //红包使用时间

    public String title;     //红包标题
    public String content;   //红包描述

    public double amount;            //红包价值
    public double validAmount;       //红包满**元可用，0表示任意金额可用

    public ArrayList<Integer> validFundType;   //红包可用组合类型（GMFFund_Type，[]表示所有类型可用）
    public ArrayList<Integer> validFundID;     //红包可用组合id， []表示所有组合可用


    public void readFromeJsonData(JsonObject dic) {
        this.sId = GsonUtil.getAsString(dic, "id");
        this.moneyType = GsonUtil.getAsInt(dic, "market");

        this.status = GsonUtil.getAsInt(dic, "status");
        if (this.status >= Coupon_Status_noDefine && this.status <= Coupon_Status_begin)
            this.status = Coupon_Status_noDefine;

        this.type = GsonUtil.getAsInt(dic, "type");
        if (this.type >= Coupon_Type_noDefine && this.type <= Coupon_Type_begin)
            this.type = Coupon_Type_noDefine;

        this.startTime = GsonUtil.getAsLong(dic, "start_time");
        this.stopTime = GsonUtil.getAsLong(dic, "stop_time");
        this.usedTime = GsonUtil.getAsLong(dic, "used_time");

        this.title = GsonUtil.getAsString(dic, "title");
        this.content = GsonUtil.getAsString(dic, "content");

        this.amount = GsonUtil.getAsDouble(dic, "amount");
        this.validAmount = GsonUtil.getAsDouble(dic, "valid_amount");

        this.validFundType = GsonUtil.getAsIntList(GsonUtil.getAsJsonArray(dic, "valid_fund_type"));
        this.validFundID = GsonUtil.getAsIntList(GsonUtil.getAsJsonArray(dic, "valid_fund_id"));
    }

    /**
     * 判断某个组合的投资金额是否满足本红包的条件
     *
     * @param amount
     * @param brief
     * @return
     */
    public boolean isValid(double amount, FundBrief brief) {

        if (brief == null)
            return false;

        if(this.status != Coupon_Status_normal)
            return false;

        if(this.type == Coupon_Type_noDefine)
            return false;

        if (amount > 0 && this.validAmount > 0 && !(this.validAmount <= amount))
            return false;

        if (this.validFundID != null && this.validFundID.size() > 0 && !(this.validFundID.contains(brief.index)))
            return false;

        if (this.validFundType != null && this.validFundType.size() > 0 && !(this.validFundType.contains(brief.innerType)))
            return false;

        return true;
    }


    public static Coupon translateFromJsonData(JsonObject dic) {
        if (dic == null || dic.isJsonNull()) return null;
        try {
            Coupon info = new Coupon();
            info.readFromeJsonData(dic);
            return info;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static List<? extends Coupon> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coupon coupon = (Coupon) o;

        return sId.equals(coupon.sId);

    }

    @Override
    public int hashCode() {
        return sId.hashCode();
    }
}
