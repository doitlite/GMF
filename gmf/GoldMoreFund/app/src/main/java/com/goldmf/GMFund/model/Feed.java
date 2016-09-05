package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/12.
 */
public class Feed extends PageItemIndex{

    public String fid;
    public int feedType;
    public long createTime;
    public long updateTime;

    @Override
    public Object getKey() {
        return this.fid;
    }


    @Override
    public long getTime() {
        return this.createTime;
    }

    public void readFromeJsonData(JsonObject dic) {
        this.fid = GsonUtil.getAsString(dic, "fid");
        this.feedType = GsonUtil.getAsInt(dic, "feed_type");
        this.createTime = GsonUtil.getAsLong(dic, "created_at");
        this.updateTime = GsonUtil.getAsLong(dic, "updated_at");
    }

}
