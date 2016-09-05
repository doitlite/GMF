package com.goldmf.GMFund.manager.mine;

import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cupide on 15/11/2.
 */
public class PhoneState {

    public static final int LOGINRESULT_NEED_INVITED = 1; // 未被邀请
    public static final int LOGINRESULT_NEED_REG = 2; // 已经被邀请，可以注册
    public static final int LOGINRESULT_NEED_LOGIN = 3; // 已注册，可以登录

    public int phoneState;

    @SerializedName("nick_name")
    public String name;
    public User invitedUser;


    public static PhoneState translateFromJsonData(JsonElement dic) {

        Gson gson = new Gson();
        try {
            PhoneState state = gson.fromJson(dic, PhoneState.class);
            state.invitedUser = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "invited_info"));
            int registered = GsonUtil.getAsInt(dic, "registered");
            int allowRegister = GsonUtil.getAsInt(dic, "allow_register");
            if(registered == 1){
                state.phoneState = LOGINRESULT_NEED_LOGIN;
            } else{
                if(allowRegister == 1) {
                    state.phoneState = LOGINRESULT_NEED_REG;
                }else {
                    state.phoneState = LOGINRESULT_NEED_INVITED;
                }
            }

            return state;
        } catch (Exception ignored) {
            return null;
        }
    }
}
