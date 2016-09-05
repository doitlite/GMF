package com.goldmf.GMFund.manager.discover;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.message.PersonalFeed;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.model.TarLinkButton.TarLinkText;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cupide on 15/8/21.
 */
public class DiscoverManager {

    private static String sDiscoverManagerPromotion = "DiscoverManagerPromotion";

    private static String sDiscoverManagerRecommandFundListJsonKey = "DiscoverManagerRecommandFundListJsonKey";
    private static String sDiscoverManagerInvestFocusListKey = "DiscoverManagerInvestFocusListKey2";

    public static class PromotionArray<T extends PageItemIndex> {
        private Class<T> classType;
        public String title;
        public List<T> list;
        public TarLinkText more;

        PromotionArray(Class<T> type) {
            this.classType = type;
            this.list = new ArrayList<>();
        }

        private void readFromJsonData(JsonObject dic) {
            this.title = GsonUtil.getAsString(dic, "title");
            this.more = TarLinkText.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "more"));

            this.list = PageProtocol.getListArray(GsonUtil.getAsJsonArray(dic, "list"), this.classType);
        }
    }

    //首页相关
    public final PromotionArray<FocusInfo> promotionFocusList = new PromotionArray<>(FocusInfo.class); //焦点图list
    public final PromotionArray<FocusInfo> promotionList = new PromotionArray<>(FocusInfo.class); //8宫格list
    public final PromotionArray<FundBrief> promotionFundList = new PromotionArray<>(FundBrief.class); //推荐组合
    public final PromotionArray<User> promotionTraderList = new PromotionArray<>(User.class); //推荐操盘手
    public final PromotionArray<User> promotionTalentList = new PromotionArray<>(User.class); //推荐牛人
    public final PromotionArray<GMFMatch> promotionMatchList = new PromotionArray<>(GMFMatch.class); //推荐比赛
    public final PromotionArray<PersonalFeed> promotionMessageList = new PromotionArray<>(PersonalFeed.class); //推荐话题


    //投资首页相关
    public final List<FundBrief> recommandFundList = new ArrayList<>();
    public final List<FocusInfo> investFocusList = new ArrayList<>();

    public TotalInfo totalInfo;             //平台总收益

    public int totalTrader;                 //已上线操盘手
    public User focusTrader;                //平台的主推trader

    public int totalTalent;                 //已上线牛人个数
    public User focusTalent;                //平台的主推牛人

    /**
     * 静态方法
     */
    private static DiscoverManager manager = new DiscoverManager();

    public static DiscoverManager getInstance() {
        return manager;
    }

    private DiscoverManager() {

        //读取本地数据
        loadLocalData();
    }

    private void loadLocalData() {
        {
            JsonElement ret = ModelSerialization.loadJsonByKey(sDiscoverManagerPromotion);
            loadPromotion(ret);
        }

        {
            JsonElement ret = ModelSerialization.loadJsonByKey(sDiscoverManagerRecommandFundListJsonKey);
            loadRecommandFundList(ret);
        }

        {
            JsonElement ret = ModelSerialization.loadJsonByKey(sDiscoverManagerInvestFocusListKey);
            loadInvestFocusList(ret);
        }
    }

    /**
     * 刷新2.6版首页
     */
    public void freshPromotion(boolean bLocal,
                               final MResults<Void> results) {
        if (bLocal && this.promotionList != null && promotionList.list != null && promotionList.list.size() > 0) {
            MResults.MResultsInfo<Void> result = MResults.MResultsInfo.SuccessComRet();
            result.hasNext = true;
            MResults.MResultsInfo.SafeOnResult(results, result);
        }

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/home-page")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        if (ret != null && ret.isJsonObject()) {

                            DiscoverManager.this.loadPromotion(ret);

                            //saveData
                            ModelSerialization.saveJsonByKey(ret, sDiscoverManagerPromotion);
                        }

                        MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 获取推荐组合list
     */
    public void freshRecommandFundList(boolean bLocal,
                                       final MResults<List<FundBrief>> results) {
        if (bLocal && this.recommandFundList != null && recommandFundList.size() > 0) {

            MResults.MResultsInfo<List<FundBrief>> info = new MResults.MResultsInfo<>();
            info.data = recommandFundList;
            info.hasNext = true;
            MResults.MResultsInfo.SafeOnResult(results, info);
        }

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "product/list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<List<FundBrief>>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        loadRecommandFundList(ret);

                        //saveData
                        ModelSerialization.saveJsonByKey(ret, sDiscoverManagerRecommandFundListJsonKey);

                        MResults.MResultsInfo<List<FundBrief>> info = protocol.buildRet();
                        info.data = recommandFundList;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 刷新投资首页list
     *
     * @param results
     */
    public void freshInvestFocusList(boolean bLocal,
                                     final MResults<List<FocusInfo>> results) {
        if (bLocal && this.investFocusList != null && investFocusList.size() > 0) {

            MResults.MResultsInfo<List<FocusInfo>> info = new MResults.MResultsInfo<>();
            info.data = investFocusList;
            info.hasNext = true;
            MResults.MResultsInfo.SafeOnResult(results, info);
        }
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "mobile-boot?cmd=invest_focus")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        loadInvestFocusList(ret);

                        //saveData
                        ModelSerialization.saveJsonByKey(ret, sDiscoverManagerInvestFocusListKey);

                        MResults.MResultsInfo.SafeOnResult(results,
                                protocol.<List<FocusInfo>>buildRet().setData(investFocusList));
                    }
                })
                .build()
                .startWork();

    }

    private void loadPromotion(JsonElement dic) {

        if (dic != null && dic.isJsonObject()) {

            this.promotionFocusList.readFromJsonData(GsonUtil.getAsJsonObject(dic, "focus"));
            this.promotionList.readFromJsonData(GsonUtil.getAsJsonObject(dic, "promotion"));
            this.promotionFundList.readFromJsonData(GsonUtil.getAsJsonObject(dic, "product"));
            this.promotionTraderList.readFromJsonData(GsonUtil.getAsJsonObject(dic, "trader"));
            this.promotionTalentList.readFromJsonData(GsonUtil.getAsJsonObject(dic, "talent"));
            this.promotionMatchList.readFromJsonData(GsonUtil.getAsJsonObject(dic, "match"));
            this.promotionMessageList.readFromJsonData(GsonUtil.getAsJsonObject(dic, "message"));
        }
    }

    private void loadRecommandFundList(JsonElement ret) {

        JsonArray list = null;
        if (ret != null && ret.isJsonArray()) {
            list = GsonUtil.getAsJsonArray(ret);
        } else if (ret != null && ret.isJsonObject()) {
            list = GsonUtil.getAsJsonArray(ret, "list");
        }
        if (list != null && recommandFundList != null) {

            recommandFundList.clear();
            recommandFundList.addAll(FundBrief.translate(list));
        }

        if (ret != null && ret.isJsonObject()) {

            totalTrader = GsonUtil.getAsInt(ret, "traders_list_brief", "traders_num");
            focusTrader = User.translateFromJsonData(GsonUtil.getAsJsonObject(ret, "traders_list_brief", "focus_trader"));

            totalTalent = GsonUtil.getAsInt(ret, "talent_list_brief", "traders_num");
            focusTalent = User.translateFromJsonData(GsonUtil.getAsJsonObject(ret, "talent_list_brief", "focus_trader"));
        }
    }


    private void loadInvestFocusList(JsonElement ret) {

        if (ret != null && ret.isJsonObject() && this.investFocusList != null) {
            this.investFocusList.clear();

            investFocusList.addAll(FocusInfo.translate(GsonUtil.getAsJsonArray(ret, "list")));

            if (GsonUtil.getAsJsonObject(ret, "total_info") != null) {
                totalInfo = TotalInfo.translateFromJsonData(GsonUtil.getAsJsonObject(ret, "total_info"));
            }
        }
    }
}
