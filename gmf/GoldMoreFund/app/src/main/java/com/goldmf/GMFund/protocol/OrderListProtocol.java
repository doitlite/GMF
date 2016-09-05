package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/8/29.
 */
public class OrderListProtocol extends ProtocolBase {

    public String orderStatuss;
    public long beginTime;
    public long endTime;
    public String fundIDs;
    public int type;
    public int pageNumber;
    public int pageSize;
    public StockSimple stock;

    public PageArray<Order> orderList;//返回值


    public OrderListProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if(super.returnCode == 0){
            if(data.isJsonObject()){
                //有page的
                int pageNumber = GsonUtil.getAsInt(data.getAsJsonObject(), "cur_page_no");
                int pageCount = GsonUtil.getAsInt(data.getAsJsonObject(), "total_page_count");

                JsonArray order_list = GsonUtil.getAsJsonArray(data.getAsJsonObject(), "list");
                if(order_list != null){

                    this.orderList = new PageArray<>(getOrderArray(order_list), pageNumber, pageCount);
                }
            }
            else if(data.isJsonArray()){
                this.orderList = new PageArray<>(getOrderArray(data.getAsJsonArray()), 1, 1);
            }
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {

            return CHostName.HOST1 + "trade/history-list";
    }

    @Override
    protected Map<String, Object> getPostData() {
        return null;
    }

    @Override
    protected Map<String, String> getParam() {
        if(this.fundIDs == null || this.fundIDs.length() == 0){
           this.fundIDs = "";
        }

        HashMap<String,String> param = new HashMap<>();
        if(this.beginTime != 0)
            param.put("ts_start", String.valueOf(this.beginTime));
        if(this.beginTime != 0)
            param.put("ts_end", String.valueOf(this.endTime));

        param.put("product_id", this.fundIDs);

        if(this.pageSize != 0){
            param.put("page_no", String.valueOf(this.pageNumber));
            param.put("page_size", String.valueOf(this.pageSize));
        }

        if(this.stock != null){
            param.put("stock_id", this.stock.index);
        }

        return param;
    }

    private ArrayList<Order> getOrderArray(JsonArray orderList){
        if(orderList == null)
            return null;

        ArrayList<Order> orders = new ArrayList<>();
        for (JsonElement element : orderList){
            if(element.isJsonObject()){
                Order order = Order.translateFromJsonData(element.getAsJsonObject());
                orders.add(order);
            }
        }
        return orders;
    }
}
