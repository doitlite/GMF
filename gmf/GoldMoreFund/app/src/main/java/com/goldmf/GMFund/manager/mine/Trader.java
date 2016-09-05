package com.goldmf.GMFund.manager.mine;

import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.RichTextUrl;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 15/7/20.
 */
public class Trader implements Serializable{

    public double historicalReturn;     //历史平均收益
    public int portfolioNum;            //发布的组合个数
    public double managerAmount;        //管理的资金总量

    public int investedNum;             //多少人投资
    public String bigPhotoUrl;          //大头像(操盘手专用的)
    public String name;                 //真名


    public String iconUrl;     //icon地址，比如：“新人”“人气”

    public static final int Trader_Show_Vertical  = 0;
    public static final int Trader_Show_Horizontal  = 1;
    public int showType;       //1:表示横版显示，0表示竖版显示。
    public String brief;       //简单简介，3行33字内
    public String secondText;  //第二行的管理信息文本。如：没有管理组合


    public TraderMoreInfo more;         //更多信息


    private void readBriefFromJsonData(JsonObject dic){

        this.historicalReturn = GsonUtil.getAsDouble(dic, "historical_return");
        this.portfolioNum =  GsonUtil.getAsInt(dic, "manage_product_num");
        this.managerAmount = GsonUtil.getAsDouble(dic, "manage_amount");

        this.investedNum =  GsonUtil.getAsInt(dic, "invested_persons_num");
        this.name = GsonUtil.getAsString(dic, "real_name");

        this.bigPhotoUrl = GsonUtil.getAsString(dic, "avatar_big_url");

        this.iconUrl = GsonUtil.getAsString(dic, "icon_url");
        this.showType = GsonUtil.getAsInt(dic, "show_type");
        this.brief = GsonUtil.getAsString(dic, "brief_text");
        this.secondText = GsonUtil.getAsString(dic, "manager_text");
    }

    public void readFromJsonData(JsonObject dic) {

        this.readBriefFromJsonData(dic);
        this.more = TraderMoreInfo.translateFromJsonData(dic);
    }

    public static Trader translateFromJsonData(JsonObject dic){
        if(dic == null || dic.isJsonNull())return null;

        try {
            Trader trader = new Trader();
            trader.readFromJsonData(dic);
            return trader;
        }
        catch (Exception ignored){
            return null;
        }
    }

    public static class TraderMoreInfo{
        public double manageAmount;     //管理的资金总额
        public String introduction; //个人简介
        public String managementConcept; //管理理念
        public String messageUrl;   //留言的url
        public List<FundBrief> holdFunds;//发布的组合
        public List<RichTextUrl> introductionUrls; //介绍url list

        public TraderPerformance performance;   //操盘手业绩 柱状图(可以为null)

        public static TraderMoreInfo translateFromJsonData(JsonObject dic) {

            if(dic.has("hold_products")){
                TraderMoreInfo more = new TraderMoreInfo();
                more.readFromJsonData(dic);
                return more;
            }
            return null;
        }

        public void readFromJsonData(JsonObject dic) {

            this.manageAmount = GsonUtil.getAsDouble(dic, "manage_amount");
            this.introduction = GsonUtil.getAsString(dic, "introduction");
            this.managementConcept = GsonUtil.getAsString(dic, "trader_style");
            this.messageUrl = GsonUtil.getAsString(dic, "message_url");

            {
                this.introductionUrls = new ArrayList<>();

                JsonArray array = GsonUtil.getAsJsonArray(dic, "urls");
                if(array != null) {
                    for (JsonElement temp : array) {
                        JsonObject obj = GsonUtil.getAsJsonObject(temp);
                        RichTextUrl rUrl = RichTextUrl.translateFromJsonData(obj);
                        if (rUrl != null)
                            this.introductionUrls.add(rUrl);
                    }
                }
            }

            {
                JsonArray array = GsonUtil.getAsJsonArray(dic, "hold_products");
                this.holdFunds = new ArrayList<>(FundBrief.translate(array));
            }

            this.performance = TraderPerformance.translateFromJsonData(GsonUtil.getAsJsonArray(dic, "history_incomes"));

        }
    }

    public static class TraderPerformance{
        public List<Double> performanceInfo;        //走势图上的具体数值
        public List<String> timeInfo;               //走势图上时间的具体数值

        public static TraderPerformance translateFromJsonData(JsonArray dic){
            if(dic == null)
                return null;

            JsonArray array = GsonUtil.getAsJsonArray(dic);
            if(array == null || array.size() == 0)
                return  null;

            TraderPerformance performance = new TraderPerformance();
            performance.performanceInfo = new ArrayList<>();
            performance.timeInfo = new ArrayList<>();
            for (JsonElement element : array) {

                performance.performanceInfo.add(GsonUtil.getAsDouble(element, "value"));
                performance.timeInfo.add(GsonUtil.getAsString(element, "year"));
            }

            return  performance;
        }
    }

}
