package com.goldmf.GMFund.manager.stock;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.protocol.base.PageArray.*;

/**
 * Created by yale on 16/3/14.
 */
public class GMFLiveItem extends PageItemIndex {
    public long createTime;
    public String title;

    @Override
    public Object getKey() {
        return this.createTime;
    }


    @Override
    public long getTime() {
        return this.createTime;
    }

    public static GMFLiveItem translateFromJsonData(JsonObject dic) {
        if(dic == null || dic.isJsonNull())return null;
        try {
            GMFLiveItem data = new GMFLiveItem();
            data.createTime = GsonUtil.getAsLong(dic, "created_at");
            data.title = GsonUtil.getAsString(dic, "title");
            return data;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<? extends GMFLiveItem> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    /**
     * Created by yale on 16/3/14.
     */
    public static class GMFLiveItemMore extends GMFLiveItem {
        public String content;

        public static GMFLiveItemMore translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull())return null;
            try {
                GMFLiveItemMore data = new GMFLiveItemMore();
                data.content = GsonUtil.getAsString(dic, "content");
                return data;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
