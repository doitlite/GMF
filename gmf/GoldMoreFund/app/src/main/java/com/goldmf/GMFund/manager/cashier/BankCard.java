package com.goldmf.GMFund.manager.cashier;

import android.support.annotation.Nullable;

import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/9/3.
 */
public class BankCard {

    @Nullable
    private String cid;   //卡id
    public BankInfo bank;           //开户银行
    public String province;         //开户省
    public String city;             //开户城市
    public String cardNO;          //绑定后server返回的卡的id
    public String cardPhone;       //开户银行的手机号码
    public String cardUserName;     //开户的个人姓名

    public int status;              //Card_Status
    public String cardMsg;             //**** **** **** 8375
    public double todayDeposit;        //当日剩余限额
    public String payChannel = Pay_Channel_No;          //支付类型

    public double withdrawSingleLimit;           //提现单笔限额
    public double withdrawDayLimit;              //提现单日限额

    public static int Card_Status_Normal = 1;       //server：1：正常
    public static int Card_Status_Fail = 2;         //server：2：绑卡失败
    public static int Card_Status_Empty = 3;        //server：3：没绑卡
    public static int Card_Status_Binking = 4;      //server：4; //新浪绑卡中

    public static String Pay_Channel_No = "";             //未知的支付类型
    public static String Pay_Channel_Sina = "sina";       //sina支付
    public static String Pay_Channel_Fuyou = "fuyou";     //富友支付

    public String cardPhone(){
        return this.cardPhone;
    }
    public String cardNO(){
        return this.cardNO;
    }

    private JsonObject jsonData;

    public void readFromJsonData(JsonObject dic) {

        this.status = GsonUtil.getAsInt(dic, "base_info", "status");
        this.todayDeposit = GsonUtil.getAsDouble(dic, "base_info", "today_deposit");

        this.bank = BankInfo.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "bank_info"));

        JsonObject bind_card_info = GsonUtil.getAsJsonObject(dic, "bind_card_info");

        this.cid = GsonUtil.getAsString(bind_card_info, "bank_card_id");
        this.cardUserName = GsonUtil.getAsString(bind_card_info, "bank_username");
        this.cardNO = GsonUtil.getAsString(bind_card_info, "bank_account_no");
        this.province = GsonUtil.getAsString(bind_card_info, "province");
        this.city = GsonUtil.getAsString(bind_card_info, "city");
        this.cardMsg = GsonUtil.getAsString(bind_card_info, "bank_card_msg");
        this.cardPhone = GsonUtil.getAsString(bind_card_info, "phone_no");
        this.payChannel = GsonUtil.getAsString(bind_card_info, "payChannel");

        this.withdrawSingleLimit = GsonUtil.getAsDouble(dic, "bank_info", "withdraw_single_limit");
        this.withdrawDayLimit = GsonUtil.getAsDouble(dic, "bank_info", "withdraw_day_limit");

        this.jsonData = dic;
    }

    public static BankCard translateFromJsonData(JsonObject dic) {
        BankCard card = buildEmptyCard();
        card.readFromJsonData(dic);
        return card;
    }

    public static final BankCard buildEmptyCard() {

        BankCard card = BankCard.buildBankCard("", "", "");
        card.status = Card_Status_Empty;
        return card;
    }

    public static final BankCard buildWatingCard(BankCard bindBankCard) {

        bindBankCard.status = Card_Status_Binking;
        return bindBankCard;
    }

    public static final BankCard buildHKBankCard(JsonObject dic) {

        String name = GsonUtil.getAsString(dic, "bank_name");

        BankCard bankCard = buildBankCard(name, null, null);

        int status = GsonUtil.getAsInt(dic, "status");
        if(status == 1)
            bankCard.status = Card_Status_Binking;
        else if(status == 2)
            bankCard.status = Card_Status_Empty;
        else
            bankCard.status = Card_Status_Normal;

        bankCard.cardNO = GsonUtil.getAsNullableString(dic, "bank_card_id");
        bankCard.cardUserName = GsonUtil.getAsNullableString(dic, "bank_username");
        bankCard.cardPhone = GsonUtil.getAsNullableString(dic, "phone_no");

        return bankCard;
    }

    public static final BankCard buildBankCard(String bankName, String bankUserName, String cardID) {

        BankInfo bank = new BankInfo();
        bank.bankName = bankName;
        BankCard card = new BankCard();
        card.bank = bank;
        card.cardUserName = bankUserName;
        card. cardNO = cardID;
        return card;
    }

    //vc层不可以用
    public Map<String,Object> formatPostData(){
        HashMap<String, Object> params = new HashMap<>();
        params.put("bank_account_no", this.cardNO);
        if(this.bank != null)
            params.put("bank_code", this.bank.bankCode);
        params.put("phone_no", this.cardPhone);
        params.put("province", this.province);
        params.put("city", this.city);

        if(this.payChannel != null) {
            if (this.payChannel.equals(Pay_Channel_No)) {
                params.put("pay_channel", this.bank.payChannel);
            } else {
                params.put("pay_channel", this.payChannel);
            }
        }
        return params;
    }

    public static class Builder {
        private BankCard card = new BankCard();

        public Builder bank(BankInfo info){
            if (info == null) throw new IllegalArgumentException("info == null");
            card.bank = info;
            return this;
        }

        public Builder province(String province){
            if (province == null) throw new IllegalArgumentException("mUrl == null");
            card.province = province;
            return this;
        }

        public Builder city(String city){
            if (city == null) throw new IllegalArgumentException("city == null");
            card.city = city;
            return this;
        }
        public Builder cardNO(String cardNO){
            if (cardNO == null) throw new IllegalArgumentException("cardNO == null");
            card.cardNO = cardNO;
            return this;
        }
        public Builder cardUserName(String cardUserName){
            if (cardUserName == null) throw new IllegalArgumentException("cardUserName == null");
            card.cardUserName = cardUserName;
            return this;
        }
        public Builder cardPhone(String cardPhone){
            if (cardPhone == null) throw new IllegalArgumentException("cardPhone == null");
            card.cardPhone = cardPhone;
            return this;
        }

        public BankCard build(){
            return this.card;
        }
    }

    //保存相关
    transient ModelSerialization<BankCard> mSerialization = new ModelSerialization<BankCard>(this);

    public static BankCard loadData(String key) {
        return ModelSerialization.loadByKey(key, BankCard.class, true);
    }
    public void save(String key) {
        mSerialization.saveByKey(key, true);
    }
    public void remove(String key) {
        mSerialization.removeByKey(key, true);
    }
}
