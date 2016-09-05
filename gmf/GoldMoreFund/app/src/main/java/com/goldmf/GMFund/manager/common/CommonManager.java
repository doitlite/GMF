package com.goldmf.GMFund.manager.common;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.request.ImageRequest;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.discover.FocusInfo;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.manager.stock.SimulationStockManager;
import com.goldmf.GMFund.manager.trader.TraderManager;
import com.goldmf.GMFund.model.LoadingInfo;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.goldmf.GMFund.util.AppUtil;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.message.SessionManager;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.manager.stock.SimulationStockManager;
import com.goldmf.GMFund.protocol.ContactUsProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.goldmf.GMFund.util.AppUtil;
import com.goldmf.GMFund.util.FrescoHelper;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.util.VersionUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action1;
import yale.extension.common.Optional;

/**
 * Created by cupide on 15/9/7.
 */
public class CommonManager {

    public final static String Avator = "avator";    //qiniu_token Key 用于用户头像上传的（公共信息）String
    public final static String Forcert = "forcert";  //forcert key 用于用户凭证上传的。(私有信息)String
    public final static String Logger = "logger";  //logger key 用于用户日志上传的。(私有信息)String
    public final static String ChatImg = "chat_imgs";//ChatImg token 用于用户聊天的时候上传图片

    public final static String Config_Key_HK_Bank = "hk_bank";   //公司的hk银行信息，内容为BankCard

    private static String sCommonManagerCitysKey = "CommonManagerCitysKey";
    private static String sCommonManagerConfigsKey = "CommonManagerConfigsKey";
    private static String sCommonManagerQuestionsKey = "CommonManagerQuestionsKey";
    private static String sCommonManagerWhiteList = "CommonManagerWhiteList";

    private static String sCommonMessageManagerLoadingData = "CommonMessageManagerLoadingData";

    private List<String> provinces = new ArrayList<>();
    private Map<String, List<String>> citys = new HashMap<>();
    private ArrayList<String> whiteList = new ArrayList<>();

    private Map<String, Object> configs = new HashMap<>();     //配置信息

    private String currentVersion;
    private UpdateInfo updateInfo;     //升级信息

    private JsonArray questions;     //常见问题

    public String bountyCardFirst = "";//佣金页面的介绍图

    private List<LoadingInfo> loadingInfos = new ArrayList<>();

    /**
     * 静态方法
     **/
    private static CommonManager manager = new CommonManager();

    public static CommonManager getInstance() {
        return manager;
    }

    public UpdateInfo getUpdateInfo() {
        return this.updateInfo;
    }


    private CommonManager() {

        //获取version
        this.currentVersion = AppUtil.getVersionName(MyApplication.SHARE_INSTANCE);

        this.loadLocalData();

        NotificationCenter.loginSubject.subscribe(new Action1<Void>() {
            @Override
            public void call(Void nil) {
                //刷新数据
                CommonManager.this.freshRedPoint(null);
            }
        });
    }


    private void loadLocalData() {
        //读取本地数据

        {
            JsonArray array = ModelSerialization.loadByKey(sCommonManagerCitysKey, JsonArray.class);
            if (array != null) {
                CommonManager.this.loadCity(array);
            }
        }


        {
            JsonElement ret = ModelSerialization.loadByKey(sCommonManagerConfigsKey, JsonElement.class);
            if (ret != null) {
                CommonManager.this.loadConfig(ret);
            }
        }

        //UpdateInfo 判断是否需要继续保存，有可能程序已经升级了
        {
            this.updateInfo = UpdateInfo.loadData();
            if (this.updateInfo != null) {
                if (VersionUtil.isBigger(this.updateInfo.updateVersion, this.currentVersion)) {
                    //可以提示了
                } else {
                    this.updateInfo.remove();
                    this.updateInfo = null;
                }
            }
        }

        //常见问题
        {
            this.questions = ModelSerialization.loadByKey(sCommonManagerQuestionsKey, JsonArray.class);
        }

        //WhiteList
        {
            String[] dataSet = ModelSerialization.loadByKey(sCommonManagerWhiteList, String[].class);
            if (dataSet != null) {
                whiteList.addAll(Arrays.asList(dataSet));
            }
        }

        //闪屏
        {

            JsonElement data = ModelSerialization.loadJsonByKey(sCommonMessageManagerLoadingData, false);
            if (data != null && data.isJsonObject()) {
                loadLoaidngData(data);
            }
        }
    }

    /**
     * @return 所有的省
     */
    public final List<String> getProvinces() {
        return provinces;
    }

    /**
     * 获取某个省的城市列表
     *
     * @param province 省
     * @return
     */
    public final List<String> getCity(String province) {
        if (citys.containsKey(province)) {
            return citys.get(province);
        }
        return null;
    }

    /**
     * 返回config的数据
     *
     * @param key
     * @return
     */
    public final Object getConfig(String key) {
        if (this.configs.containsKey(key)) {
            return this.configs.get(key);
        }
        return null;
    }

    /**
     * 返回网站白名单列表
     *
     * @return 网站白名单列表
     */
    public final List<String> getWhiteList() {
        return this.whiteList;
    }

    /**
     * 获取上传用户港股的充值凭证 七牛qiniu
     *
     * @param results
     */
    public final void getQiniuAppToken(String type,
                                       final MResults<Map<String, QiniuToken>> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "qiniu-token/all")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<Map<String, QiniuToken>>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        Map<String, QiniuToken> map = new HashMap<>();
                        JsonObject obj = GsonUtil.getAsJsonObject(ret, type);

                        if (obj != null) {
                            QiniuToken token = QiniuToken.translateFromJsonData(obj);
                            map.put(type, token);
                        }

                        MResults.MResultsInfo<Map<String, QiniuToken>> info = protocol.buildRet();
                        info.data = map;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();

    }

    private void test() {

    }

    /**
     * 刷新一些常规的数据,需要在启动的界面运行
     */
    public void freshCommInfo() {

        this.freshConfig();
        SessionManager.getInstance();
        this.getNewVersion(null);

        MyApplication.SHARE_INSTANCE.mHandler.post(() -> {

            if (MineManager.getInstance().isLoginOK()) {

                MineManager.getInstance().getPro(null);

                if (ProtocolManager.getInstance().getSNSToken() == null) {
                    MineManager.getInstance().freshSNSKey();
                }
            }
        });

        MyApplication.SHARE_INSTANCE.mHandler.postDelayed(() -> {

            CashierManager.getInstance().freshBankTips(null);

            if (MineManager.getInstance().isLoginOK()) {

                CashierManager.getInstance().freshMyBankCard();
                FortuneManager.getInstance().freshAccount(null);
                CommonManager.this.freshRedPoint(null);
                FortuneManager.getInstance().freshCouponList(null);

                if (ProtocolManager.getInstance().getSNSToken() != null) {

                    ScoreManager.getInstance().freshAccount(null);  //积分信息

                    MyApplication.SHARE_INSTANCE.mHandler.postDelayed(() -> {

                        SimulationStockManager.getInstance().freshFollowStock(null);//自选股
                        SimulationStockManager.getInstance().listStockPosition(null);//持仓

                    }, 2000);
                }

                //操盘手额外处理
                if (MineManager.getInstance().getmMe().type == User.User_Type.Trader ||
                        MineManager.getInstance().getmMe().type == User.User_Type.Talent) {
                    MyApplication.SHARE_INSTANCE.mHandler.postDelayed(() -> {
                        TraderManager.getInstance().getFundList(false, null);
                    }, 2000);
                }
            }

        }, 1000);

        boolean isFirstLand = (this.provinces == null);
        MyApplication.SHARE_INSTANCE.mHandler.postDelayed(() -> {
            if (this.provinces.size() == 0 || this.citys.size() == 0) {
                this.freshCity();
            }
            CashierManager.getInstance().freshBanks();
            this.freshWhiteList(null);
            this.freshNewLoadingAnimate();

        }, isFirstLand ? 100 : 5000);


        if (MyConfig.IS_DEBUG_MODE) {
            test();
        }
    }

    private void loadConfig(JsonElement ret) {

        if (ret.isJsonObject()) {
            JsonObject hkconfig = GsonUtil.getAsJsonObject(ret, "foreign_bank", "cmb_hk");
            if (hkconfig != null) {

                BankCard card = BankCard.buildHKBankCard(hkconfig);
                CommonManager.this.configs.put(Config_Key_HK_Bank, card);
            }

            CommonManager.this.bountyCardFirst = GsonUtil.getAsString(ret, "bounty_card_first");
        }

    }

    private void loadCity(JsonArray array) {

        provinces.clear();
        citys.clear();

        for (JsonElement temp : array) {
            JsonArray obj = GsonUtil.getAsJsonArray(temp);
            if (obj != null && obj.size() == 4) {
                int level = GsonUtil.getChildAsInt(obj, 2);
                if (level == 1) {
                    provinces.add(GsonUtil.getChildAsString(obj, 1));
                } else if (level == 2) {
                    String city = GsonUtil.getChildAsString(obj, 1);
                    int provinceIndex = GsonUtil.getChildAsInt(obj, 3) - 1;
                    if (provinceIndex < provinces.size()) {
                        String key = provinces.get(provinceIndex);
                        if (!citys.containsKey(key)) {
                            citys.put(key, new ArrayList<String>());
                        }
                        citys.get(key).add(city);
                    }
                }
            }
        }
    }

    private void freshConfig() {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "init-config")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        CommonManager.this.loadConfig(ret);

                        long serverTime = GsonUtil.getAsLong(ret, "server_time");
                        if (serverTime != 0) {
                            SecondUtil.setServerTime(serverTime);
                        }


                        //存数据
                        ModelSerialization<JsonElement> mSerialization = new ModelSerialization<>(ret);
                        mSerialization.saveByKey(sCommonManagerConfigsKey);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 刷新city
     */
    public void freshCity() {

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "js/select-city/dis_common_district.json")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        JsonArray array = GsonUtil.getAsJsonArray(ret);
                        if (array != null) {

                            CommonManager.this.loadCity(array);

                            //存数据
                            ModelSerialization<JsonElement> mSerialization = new ModelSerialization<>(ret);
                            mSerialization.saveByKey(sCommonManagerCitysKey);
                        }
                    }
                })
                .build()
                .startWork();
    }


    /**
     * 联网获取新版本信息
     *
     * @param results
     */
    public void getNewVersion(final MResults<UpdateInfo> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "mobile-boot?cmd=update")
                .params(ComonProtocol.buildParams("os", "android", "version", this.currentVersion))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<UpdateInfo>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        if (ret.isJsonObject()) {
                            UpdateInfo newUpdateInfo = UpdateInfo.translateFromJsonData(ret.getAsJsonObject());

                            String localVersion = (CommonManager.this.updateInfo == null) ? "" : updateInfo.updateVersion;

                            if (newUpdateInfo != null
                                    && VersionUtil.isBigger(newUpdateInfo.updateVersion, CommonManager.this.currentVersion)) {
                                if (VersionUtil.isBigger(newUpdateInfo.updateVersion, localVersion)) {
                                    //新的比本地存的还要高？
                                    CommonManager.this.updateInfo = newUpdateInfo;
                                } else if (CommonManager.this.updateInfo != null) {
                                    CommonManager.this.updateInfo.needForceUpdate = newUpdateInfo.needForceUpdate;
                                }
                            }

                            if (CommonManager.this.updateInfo != null) {
                                //存数据
                                CommonManager.this.updateInfo.save();
                            }
                        }
                        MResults.MResultsInfo<UpdateInfo> info = protocol.buildRet();
                        info.data = CommonManager.this.updateInfo;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 获取本次 这一次的闪屏
     *
     * @return
     */
    public LoadingInfo getLoadingAnimate() {
        if (this.loadingInfos != null && this.loadingInfos.size() > 0) {
            //获取的策略是：每次获取当天需要显示的闪屏，
            //如果都没有显示过，则获取第一只，如果显示过了，则获取一只最早显示的。
            LoadingInfo lastOne = null;
            for (LoadingInfo info : this.loadingInfos) {
                boolean inDiskCache = FrescoHelper.isImageInCache(info.imageUrl);
                if (info.isValidTime() && inDiskCache) {
                    if (lastOne == null || lastOne.lastShowTime > info.lastShowTime) {
                        lastOne = info;
                    }
                }
            }
            if(lastOne != null){
                lastOne.lastShowTime = SecondUtil.currentSecond();
                SaveLoadingInfo();
            }
            return lastOne;
        }
        return null;
    }

    private void SaveLoadingInfo(){
        //存数据
        JsonObject data = new JsonObject();
        JsonArray infoIDList = new JsonArray();
        for (LoadingInfo info : CommonManager.this.loadingInfos) {
            infoIDList.add(info.getJsonData());
        }
        data.add("list", infoIDList);
        ModelSerialization.saveJsonByKey(data, sCommonMessageManagerLoadingData, false);

    }

    private void loadLoaidngData(JsonElement data) {
        if (data.isJsonObject()) {
            ArrayList<LoadingInfo> array = new ArrayList<>();

            try {
                for (JsonElement temp : GsonUtil.getAsJsonArray(data, "list")) {
                    LoadingInfo info = LoadingInfo.translateFromJsonData(GsonUtil.getAsJsonObject(temp));
                    array.add(info);
                    for (LoadingInfo info1 : this.loadingInfos) {
                        if (info1.InfoID.equals(info.InfoID)) {
                            info1.readFromeJsonData(GsonUtil.getAsJsonObject(temp));
                            array.remove(info);
                            array.add(info1);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }

            this.loadingInfos = array;
        }
    }

    /**
     * 获取启动屏 闪屏
     */
    public void freshNewLoadingAnimate() {
        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "flash-list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        loadLoaidngData(ret);

                        SaveLoadingInfo();

                        //启动下载逻辑
                        for (LoadingInfo info : CommonManager.this.loadingInfos) {
                            Fresco.getImagePipeline().prefetchToDiskCache(ImageRequest.fromUri(info.imageUrl), null);
                        }
                    }
                })
                .build()
                .startWork();
    }


    public final void delayUpdateAlert() {

        if (this.updateInfo != null) {
            this.updateInfo.delayUpdateAlert();

            //存数据
            this.updateInfo.save();
        }
    }

    /**
     * 获取常见问题接口
     */
    public final void getNormalQuestion(boolean bLocal,
                                        final MResults<JsonArray> results) {
        if (bLocal && this.questions != null && questions.size() > 0) {

            MResults.MResultsInfo<JsonArray> info = new MResults.MResultsInfo<>();
            info.data = questions;
            info.hasNext = true;
            MResults.MResultsInfo.SafeOnResult(results, info);
        }
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "help/qa")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<JsonArray>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        if (ret.isJsonObject()) {
                            questions = GsonUtil.getAsJsonArray(ret, "list");

                            if (questions != null) {
                                //存数据
                                ModelSerialization<JsonArray> mSerialization = new ModelSerialization<>(questions);
                                mSerialization.saveByKey(sCommonManagerQuestionsKey);
                            }
                        }

                        MResults.MResultsInfo<JsonArray> info = protocol.buildRet();
                        info.data = CommonManager.this.questions;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }


    /**
     * 意见反馈
     *
     * @param phone
     * @param msg
     */
    public final void contactUs(String phone, String msg) {
        ContactUsProtocol p = new ContactUsProtocol(null);
        p.phone = phone;
        p.msg = msg;
        p.startWork();
    }

    /**
     * 获取小红点
     */
    public final void freshRedPoint(final MResults<Void> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "new-tips")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        {
                            String location = FortuneManager.getInstance().bountyRedPoint.getLocation();
                            JsonObject obj = GsonUtil.getAsJsonObject(ret, location);
                            if (obj != null) {
                                FortuneManager.getInstance().bountyRedPoint.readFromeJsonData(obj);
                            }
                        }
                        {
                            String location = FortuneManager.getInstance().couponRedPoint.getLocation();
                            JsonObject obj = GsonUtil.getAsJsonObject(ret, location);
                            if (obj != null) {
                                FortuneManager.getInstance().couponRedPoint.readFromeJsonData(obj);
                            }
                        }

                        {
                            String location = ScoreManager.getInstance().scoreRedPoint.getLocation();
                            JsonObject obj = GsonUtil.getAsJsonObject(ret, location);
                            if (obj != null) {
                                ScoreManager.getInstance().scoreRedPoint.readFromeJsonData(obj);
                            }
                        }

                        MResults.MResultsInfo<Void> info = protocol.buildRet();
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 获取白名单信息
     */
    final void freshWhiteList(final MResults<List<String>> results) {

        new ComonProtocol.Builder()
                .url("http://rel.wechat.caopanman.com/config/whitelist")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<List<String>>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        CommonManager.this.whiteList.clear();
                        JsonArray array = GsonUtil.getAsJsonArray(ret, "list");
                        GsonUtil.getAsStringList(CommonManager.this.whiteList, array);

                        if (whiteList != null) {
                            //存数据
                            ModelSerialization.saveByKey(whiteList, String.class, sCommonManagerWhiteList);
                        }

                        MResults.MResultsInfo<List<String>> info = protocol.buildRet();
                        info.data = CommonManager.this.whiteList;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();

    }
}
