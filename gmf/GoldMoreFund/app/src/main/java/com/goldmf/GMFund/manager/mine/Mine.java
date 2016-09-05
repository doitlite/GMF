package com.goldmf.GMFund.manager.mine;

import android.text.TextUtils;

import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.ModifyValue;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import rx.internal.operators.OperatorUnsubscribeOn;

import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by cupide on 15/7/25.
 */
public class Mine extends User {
    //私有访问
    public boolean isLogin;

    private static String sMineKeyDeserted = "MineKey";

    private static String sMineJsonKey = "MineJsonKey";

    public static String sMinePhoneKey = "MineKey";
    public static String sBackMinePhone = "BackMinePhone";

    public String token;            //key

    transient ModifyValue<String> modifyName = new ModifyValue<>();                   //昵称
    transient ModifyValue<String> modifyPhotoUrl = new ModifyValue<>();               //头像地址
    transient ModifyValue<Boolean> modifyHideVtcProfile = new ModifyValue<>();        //隐藏模拟业绩

    //包外访问
    public String phone;            //手机号码
    public String realName;        //真名
    transient ModifyValue<CLocation> modifyLocation = new ModifyValue<>();      //所在地
    public boolean isBindWX;        //是否已经绑定微信
    public String wxNickName;       //微信昵称

    private long createdTime;
    private long updatedTime;

    private int status;
    private String homePage;
    private String intruduction;


    transient public ModifyValue<ShippingAddress> address = new ModifyValue<>(); //server返回的收货地址

    public int riskAssessmentGrade;     //风险测评  结果为0：没有进行过风险测评
    public String riskAssessmentGradeMsg;  //风险测评 msg
    public boolean setAuthenticate;     //是否做过实名认证 NO：需要实名认证，YES：已经做过实名认证
    public boolean setTraderPassword;   //是否设置过交易密码 NO：没有设置交易密码，YES：已经设置交易密码

    public CAuthenticate authenticate;  //实名认证

    public boolean uploadLogger;    //是否上传日志

    private JsonObject jsonData;

    public String getName() {
        if (this.modifyName == null || this.modifyName.getValue() == null)
            return "";
        return this.modifyName.getValue();
    }

    public String getPhotoUrl() {
        if (this.modifyPhotoUrl == null || this.modifyPhotoUrl.getValue() == null)
            return "";
        return this.modifyPhotoUrl.getValue();
    }

    public String getCity() {
        try {
            return this.modifyLocation.getValue().getCity();
        } catch (Exception ignored) {
        }

        return "";
    }

    public String getProvince() {
        try {
            return this.modifyLocation.getValue().getProvince();
        } catch (Exception ignored) {
        }

        return "";
    }

    /**
     * 用户是否主动隐藏了自己的模拟业绩
     * @return
     */
    public boolean isHideVtcProfile(){
        try {
            return this.modifyHideVtcProfile.getValue();
        } catch (Exception ignored) {
        }

        return false;
    }

    public boolean isFilledShippingAddress() {
        ShippingAddress address = getAddress();
        return !TextUtils.isEmpty(address.name);
    }

    public ShippingAddress getAddress() {
        return safeGet(() -> this.address.getValue(), null);
    }

    transient ModelSerialization<Mine> mSerialization = new ModelSerialization<Mine>(this);

    public static Mine loadData() {
        String phoneTemp = ModelSerialization.getString(sMinePhoneKey);

        JsonElement data = ModelSerialization.loadJsonByKey(sMineJsonKey, true);
        if(data != null && data.isJsonObject()){

            Mine mine = new Mine();
            mine.readFromJsonData(GsonUtil.getAsJsonObject(data));
            mine.isLogin = true;

            //删除掉之前第一版的key
            mine.mSerialization.removeByKey(sMineKeyDeserted, true);

            return mine;
        }
        else {
            Mine mine = ModelSerialization.loadByKey(sMineKeyDeserted, Mine.class, true);
            if (mine != null && phoneTemp != null && phoneTemp.equals(mine.phone)) {
                return mine;
            } else {
                return null;
            }
        }
    }

    public void save() {
        ModelSerialization.saveString(sMinePhoneKey, this.phone);
        ModelSerialization.saveString(sBackMinePhone, this.phone);//同时保存backPhone，不删除
//        mSerialization.saveByKey(sMineKeyDeserted, true);//-old

        ModelSerialization.saveJsonByKey(this.jsonData, sMineJsonKey, true);
    }

    public void remove() {
        mSerialization.removeByKey(sMineKeyDeserted, true);//-old

        ModelSerialization.saveString(sMinePhoneKey, "");
        ModelSerialization.removeJsonByKey(sMineJsonKey);
    }

    public Mine() {
        isLogin = false;
    }

    public boolean isLoginOk() {
        return isLogin;
    }

    public void readFromJsonData(JsonObject dic) {
        if(!dic.has("cellphone"))
            return;

        super.readFromJsonData(dic);

        this.modifyName.modify(super.getName());
        this.modifyPhotoUrl.modify(super.getPhotoUrl());

        this.isLogin = true;
        this.phone = GsonUtil.getAsString(dic, "cellphone");
        this.realName = GsonUtil.getAsString(dic, "real_name");

        String city = GsonUtil.getAsString(dic, "city");
        String province = GsonUtil.getAsString(dic, "province");
        this.modifyLocation.modify(CLocation.BuildLocation(province, city));

        this.createdTime = GsonUtil.getAsLong(dic, "created_at");
        this.updatedTime = GsonUtil.getAsLong(dic, "updated_at");

        this.status = GsonUtil.getAsInt(dic, "status");
        this.homePage = GsonUtil.getAsString(dic, "home_page");
        this.intruduction = GsonUtil.getAsString(dic, "intruduction");

        this.riskAssessmentGrade = GsonUtil.getAsInt(dic, "evaluation_level");
        this.riskAssessmentGradeMsg = GsonUtil.getAsString(dic, "evaluation_msg");
        this.setTraderPassword = (GsonUtil.getAsInt(dic, "set_trade_passwd") != 0);
        this.setAuthenticate = (GsonUtil.getAsInt(dic, "check_real_name_pass") != 0);

        if (this.setAuthenticate) {
            this.authenticate = new CAuthenticate();
            this.authenticate.name = GsonUtil.getAsString(dic, "hide_real_name");
            this.authenticate.card = GsonUtil.getAsString(dic, "hide_identity_card");
        }
        this.uploadLogger = (GsonUtil.getAsInt(dic, "upload_logger") == 1);


        ShippingAddress address = ShippingAddress.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "delivery_address"));
        this.address = new ModifyValue<>(address);

        this.isBindWX = GsonUtil.getAsInt(dic, "wxbind") == 1;
        this.wxNickName = GsonUtil.getAsString(dic, "wxname");


        this.jsonData = dic;
    }

    public static class CAuthenticate {
        public String name;//实名认证 真名
        public String card;//实名认证 身份证
    }

    public static class CLocation {

        @SerializedName("country")
        private String country;
        @SerializedName("city")
        private String city;
        @SerializedName("province")
        private String province;

        public String getCountry() {
            return country;
        }

        public String getCity() {
            return city;
        }

        public String getProvince() {
            return province;
        }

        static public CLocation BuildLocation(String province, String city) {
            CLocation location = new CLocation();
            location.country = "中国";
            location.province = province;
            location.city = city;
            return location;
        }

        public static CLocation translateFromJsonData(JsonObject dic) {
            Gson gson = new Gson();
            try {
                CLocation location = gson.fromJson(dic, CLocation.class);
                return location;
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public static class ShippingAddress {
        public String name;
        public String cellphone;
        public CLocation city;
        public String address; //详细地址

        public static ShippingAddress translateFromJsonData(JsonObject dic) {
            try {
                ShippingAddress address = new ShippingAddress();
                address.name = GsonUtil.getAsString(dic, "address_nick_name");
                address.cellphone = GsonUtil.getAsString(dic, "address_cellphone");
                address.address = GsonUtil.getAsString(dic, "address_detail");

                String province = GsonUtil.getAsString(dic, "address_province");
                String city = GsonUtil.getAsString(dic, "address_city");
                address.city = CLocation.BuildLocation(province, city);
                return address;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
