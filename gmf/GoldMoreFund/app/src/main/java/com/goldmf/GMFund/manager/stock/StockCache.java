package com.goldmf.GMFund.manager.stock;


import android.os.Handler;
import android.os.Looper;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.goldmf.GMFund.extension.UIControllerExtension.runOnMain;
import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;

/**
 * Created by cupide on 16/8/8.
 */
public class StockCache {

    private Map<String, StockSimple> stockCache = new HashMap<>();

    private List<String> timerStockIDs = new ArrayList<>();


    /**
     * 静态方法
     */
    private static StockCache cache = new StockCache();

    public static StockCache getInstance() {
        return cache;
    }

    private Runnable runnable = () -> StockCache.this.freshStockInfo();
    protected Handler handler = new Handler(Looper.getMainLooper());

    public StockSimple getStockSimple(String stockID){
        StockSimple simple = stockCache.get(stockID);
        if(simple == null){
            simple = new StockSimple();
            simple.index = stockID;

            stockCache.put(stockID, simple);
            timerStockIDs.add(stockID);

            handler.postDelayed(runnable, 1000);
        }
        return simple;
    }

    private void freshStockInfo(){
        if(this.timerStockIDs.size() == 0)return;

        final List<String> stockIDTemp = new ArrayList<>(this.timerStockIDs);
        this.timerStockIDs.clear();

        String stockIDBuf = new String();
        for (String stockID :stockIDTemp){
            stockIDBuf = stockIDBuf + stockID + ",";
        }

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "omsv2/oms/helper/stock_brief")
                .params(buildParams("stock_id", stockIDBuf))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        StockCache.this.timerStockIDs.addAll(stockIDTemp);
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        List<String> stockIDTemp2 = new ArrayList<>(stockIDTemp);
                        if(ret.isJsonArray()){
                            for (JsonElement element :GsonUtil.getAsJsonArray(ret)){
                                String stockID = GsonUtil.getAsString(element, "stock_id");
                                StockSimple simple = StockCache.this.stockCache.get(stockID);
                                if(simple != null){
                                    simple.readFromeJsonData(GsonUtil.getAsJsonObject(element));
                                    stockIDTemp2.remove(stockID);
                                }
                            }
                        }

                        StockCache.this.timerStockIDs.addAll(stockIDTemp2);

                        runOnMain(() -> NotificationCenter.freshStockInfoSubject.onNext(null));
                    }
                })
                .build()
                .startWork();
    }
}
