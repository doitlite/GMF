package com.goldmf.GMFund.protocol;


import com.goldmf.GMFund.model.LineData.KLineData;
import com.goldmf.GMFund.model.StockInfo;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/12.
 */
public class KLineProtocol extends PageProtocol<KLineData> {

    public StockInfo.StockSimple stock;

    public int type;   //k线类型
    public int authority;   //复权
    public int specType;
    public int count;


    public KLineProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected String getUrl() {
        return CHostName.formatUrl(CHostName.HOST2, "page/k-lines");
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if (super.returnCode == 0) {
            if (data.isJsonObject()) {

                JsonElement metadata = GsonUtil.getAsJsonElement(data, "metadata");

                long firstTS = GsonUtil.getAsLong(metadata, "data_begin");
                long lastTS = GsonUtil.getAsLong(metadata, "data_end");

                long min = GsonUtil.getAsLong(metadata, "available_data_begin");
                long max = GsonUtil.getAsLong(metadata, "available_data_end");

                Boolean more = !(Double.valueOf(min).equals(Double.valueOf(firstTS)));

                JsonArray array = GsonUtil.getAsJsonArray(data.getAsJsonObject(), "lines");
                List<KLineData> orders = array != null ? getListArray(array, KLineData.class) : new ArrayList<>();
                this.page = new PageArray<>(orders, firstTS, lastTS, more);
            }
        }
        return (super.returnCode == 0);
    }

    @Override
    protected Map<String, String> getParam() {
        HashMap<String, String> param = new HashMap<>();

        param.putAll(Optional.of(this.pageCgiParam).or(Collections.emptyMap()));
        param.put("stock_id", this.stock.index);

        param.put("first_ts", String.valueOf(this.firstTS));
        param.put("last_ts", String.valueOf(this.lastTS));

        param.put("accum_adj_factor_type", String.valueOf(authority));
        param.put("freq", String.valueOf(type));
        param.put("spec_name", "ma," + KLineData.getSpecString(this.specType));

        param.put("count", String.valueOf(count));

        return param;
    }
}
