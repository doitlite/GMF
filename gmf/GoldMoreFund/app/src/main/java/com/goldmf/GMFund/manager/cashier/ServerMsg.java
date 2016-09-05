package com.goldmf.GMFund.manager.cashier;

import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 16/5/26.
 */
public class ServerMsg<T>  {

    public String title;    //标题
    public String tip;      //信息
    private List<TarLinkButton> buttonData = new ArrayList<>();   //按钮群（TarLinkButton）

    private T data;

    public ServerMsg<T> setData(T d){
        data = d;
        return this;
    }

    /**
     * 有效的msg 需要显示在界面上
     * @param msg 消息
     * @return 返回是否有效
     */
    public static boolean isValid(ServerMsg msg){
        return (msg != null)&&(msg.tip != null)&&(msg.tip.length() > 0);
    }

    public T getData(){
        return data;
    }

    public List<TarLinkButton> buttons(){
        return this.buttonData;
    }

    public  static<T> ServerMsg<T> translateFromJsonData(JsonObject dic) {
        try {
            ServerMsg<T> msg = new ServerMsg<T>();
            msg.title = GsonUtil.getAsString(dic, "title");
            msg.tip = GsonUtil.getAsString(dic, "tips");
            msg.buttonData.clear();
            JsonArray buttons = GsonUtil.getAsJsonArray(dic, "buttons");
            if (buttons != null) {
                for (JsonElement temp : buttons) {
                    msg.buttonData.add(TarLinkButton.translateFromJsonData(temp.getAsJsonObject()));
                }
            }
            return msg;
        } catch (Exception ignored) {
            return null;
        }
    }

}
