package com.goldmf.GMFund.manager.fortune;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 15/10/28.
 */
public class BountyAccount implements Serializable {

    public int moneyType;
    public double validAmount;       //奖励金-额度
    public double totalAmount;       //累计提现金额

    public double minLimit;          //最小提现额度
    public boolean canExchange;      //本次可否提现
    public String reason;            //不能提现的理由

    public int inviteNumber;        //邀请好友数量

//    public int number;               //tips数字

//    public final List<BountyInfo> bountyListNew = new ArrayList<>();
//    public final List<BountyInfo> bountyListUnconfirmed = new ArrayList<>();
//    public final List<BountyInfo> bountyListExchanged = new ArrayList<>();

    public BountyAccount() {
    }

    public BountyAccount(int moneyType) {
        this.moneyType = moneyType;
    }


    public synchronized void readFromeJsonData(JsonObject dic) {

        this.validAmount = GsonUtil.getAsDouble(dic, "valid_amount");
        this.minLimit = GsonUtil.getAsDouble(dic, "min_limit_amount");
        this.canExchange = (GsonUtil.getAsInt(dic, "can_exchange") == 1);
        this.reason = GsonUtil.getAsString(dic, "reason");
//        this.number = GsonUtil.getAsInt(dic, "tip_number");

        this.inviteNumber = GsonUtil.getAsInt(dic, "invite_num");
        this.totalAmount = GsonUtil.getAsDouble(dic, "total_amount");
    }

    transient ModelSerialization<BountyAccount> mSerialization = new ModelSerialization<>(this);

    public static BountyAccount loadData(String key) {
        return ModelSerialization.loadByKey(key, BountyAccount.class, true);
    }

    public synchronized void save(String key) {
        mSerialization.saveByKey(key, true);
    }

    public void remove(String key) {
        mSerialization.removeByKey(key, true);
    }


    public static class BountyInfo implements Serializable {

        public long bountyTime;     //奖励金获取时间
        public String msg;          //奖励金获取原因
        public String userName;     //邀请用户名
        public double amount;       //获取的奖励金
        public int moneyType;       //
        public String statusText;   //状态文本
        public String color;

        public static BountyInfo translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull()) return null;

            try {
                BountyInfo info = new BountyInfo();
                info.bountyTime = GsonUtil.getAsLong(dic, "dateline");
                info.msg = GsonUtil.getAsString(dic, "ac_desc");
                info.amount = GsonUtil.getAsDouble(dic, "amount");
                info.moneyType = GsonUtil.getAsInt(dic, "market");
                info.statusText = GsonUtil.getAsString(dic, "status_text");
                info.color = GsonUtil.getAsString(dic, "color");

                JsonObject buttonObj = GsonUtil.getAsJsonObject(dic, "user_info");
                info.userName = GsonUtil.getAsString(buttonObj, "nick_name");
                return info;
            } catch (Exception ignored) {
                return null;
            }
        }

        public static List<? extends BountyInfo> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }
}
