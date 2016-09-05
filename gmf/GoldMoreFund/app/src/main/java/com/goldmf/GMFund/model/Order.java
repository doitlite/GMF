package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.goldmf.GMFund.model.StockInfo.StockSimple;

import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 15/8/22.
 */
public class Order extends PageItemIndex {

    public static final int NODeal = 30;   //未成交（可撤单）（server定义:ORDER_STATUS_ENTRUST    = 30; // 委托成功等待交易所回报）（case '2': return '已委托';）
    public static final int PartDeal = 41; //部分成交（可撤单）（server定义:ORDER_STATUS_DEAL_SOME  = 41; // 部分成交）（case '4': return '部分成交';）
    public static final int Deal = 42;     //全部成交（server定义：ORDER_STATUS_DEAL_WHOLE = 42; // 全部成交）（case '5': return '全部成交';）
    public static final int Cancel = 50;   //已撤单（server定义：ORDER_STATUS_CANCEL     = 50; // 已撤单）（case '7': return '已撤单';）

    public static final int PartDeal_Cancel = 99;//部分成交（剩余已经撤单）。（case '4': return '部分成交';）

    public static final int Waiting = 101; //等待执行（可撤单）  （case '1': return '等待执行'）
    public static final int Back = 103; //已退回   （case '3': return '已退回';）
    public static final int WaitingCancel = 106; //等待撤单   （case '6': return '等待撤单';）
    public static final int Bad = 108; //废单   （case '8': return '废单';）
    public static final int Del = 109; //废单   （case '9': return '已删除';）

    public static final int NoDefine = 200;//client不认识的单，状态text显示 “--”，无任何操作。


    public static final int Model_Market = 1;       //1：市价委托
    public static final int Model_Limit = 2;        //2:限价委托

    protected int cancelStatus = -1;   //撤单状态； -1:未设置； 0:未撤单； 1:请求撤单ing； 2:已撤单。

    public StockSimple stock;     //操作的股票
    public String orderId;         //订单号
    public Boolean buy;             //true:买入
    public int status;             //订单状态
    public int orderModel;         //订单Model  1 市价委托 2 限价委托

    public long orderAmount;       //下单数量
    public double orderPrice;      //下单价格
    public long orderTime;         //下单时间

    public double transactionPrice;    //成交价格
    public long transactionAmount;   //成交数量
    public long transactionTime;       //成交时间

    public double beforePosition;       //操作前仓位
    public double afterPosition;       //操作后仓位

    public double income;              //总收益
    public double incomeRatio;       //总收益 百分比

    public String range;            //range

    @Override
    public Object getKey() {
        return orderId;
    }

    @Override
    public long getTime() {
        return orderTime;
    }

    /**
     * 该订单是否能撤单？
     * @return
     */
    public Boolean canCancel(){
        if(this.status == PartDeal){
            return (this.cancelStatus == 0);
        }
        else{
            return (this.status == NODeal || Waiting == this.status);
        }
    }

    public CharSequence getStatusText() {
        if (status == NODeal) {
            return "未成交";
        } else if (status == PartDeal) {
            switch (this.cancelStatus){
                case -1:
                case 0:
                    return "部分成交";
                case 1:
                    return "部分成交，等待撤单";
                default:
                    return "部分成交";
            }
        } else if (status == PartDeal_Cancel) {
            return "部分成交，剩余已撤单";
        } else if (status == Deal) {
            return "全部成交";
        } else if (status == Cancel) {
            return "已撤单";
        } else if (status == Waiting) {
            return "等待执行";
        } else if (status == Back) {
            return "已退回";
        } else if (status == WaitingCancel) {
            return "等待撤单";
        } else if (status == Bad) {
            return "废单";
        } else if (status == Del) {
            return "已删除";
        } else {
            return "--";
        }
    }

    public static Order translateFromJsonData(JsonObject dic) {
        if(dic == null ||dic.isJsonNull())return null;
        try {
            Order order = new Order();
            order.orderId = GsonUtil.getAsString(dic, "order_id");
            order.buy = GsonUtil.getAsBoolean(dic, "order_type");
            order.status = GsonUtil.getAsInt(dic, "order_status");
            order.orderModel = GsonUtil.getAsInt(dic, "order_model");
            order.orderAmount = GsonUtil.getAsLong(dic, "entrust_amount");
            order.orderPrice = GsonUtil.getAsDouble(dic, "entrust_price");
            order.orderTime = GsonUtil.getAsLong(dic, "entrust_time");
            order.transactionPrice = GsonUtil.getAsDouble(dic, "business_price");
            order.transactionAmount = GsonUtil.getAsLong(dic, "business_amount");
            order.transactionTime = GsonUtil.getAsLong(dic, "business_time");
            order.beforePosition = GsonUtil.getAsDouble(dic, "before_position");
            order.afterPosition = GsonUtil.getAsDouble(dic, "after_position");
            order.income = GsonUtil.getAsDouble(dic, "earning");
            order.incomeRatio = GsonUtil.getAsDouble(dic, "earning_ratio");
            order.range = GsonUtil.getAsString(dic, "range");

            order.stock = StockSimple.translateFromJsonData(dic);
            return order;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static List<? extends Order> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public static class OrderV2 extends Order{


        private static int traslateStatus(int status, int cancelStatus){

            switch(status){
                case 1: return Waiting;
                case 2: return NODeal;
                case 3: return Back;
                case 4: {
                    if(cancelStatus == 2)
                        return PartDeal_Cancel;
                    return PartDeal;
                }
                case 5: return Deal;
                case 6: return WaitingCancel;
                case 7: return Cancel;
                case 8: return Bad;
                case 9: return Del;
                default: return NoDefine;
            }
        }

        public static OrderV2 translateFromJsonData(JsonObject dic) {
            if(dic == null ||dic.isJsonNull())return null;
            try {
                OrderV2 order = new OrderV2();
                order.orderId = GsonUtil.getAsString(dic, "id");
                order.buy = GsonUtil.getAsBoolean(dic, "entrust", "type");

                order.orderModel = GsonUtil.getAsInt(dic, "entrust", "model")==1?Model_Limit:Model_Market;

                order.orderAmount = GsonUtil.getAsLong(dic, "entrust", "amount");
                order.orderPrice = GsonUtil.getAsDouble(dic, "entrust", "price");
                order.orderTime = GsonUtil.getAsLong(dic, "created_at");

                order.transactionPrice = GsonUtil.getAsDouble(dic, "deal", "price");
                order.transactionAmount = GsonUtil.getAsLong(dic, "deal", "amount");
                order.transactionTime = GsonUtil.getAsLong(dic, "updated_at");

                order.stock = StockSimple.getStockSimpleByIndex(GsonUtil.getAsString(dic, "stock", "code"));
                order.cancelStatus = GsonUtil.getAsInt(dic, "cancel_status");
                int status = GsonUtil.getAsInt(dic, "status");
                order.status = traslateStatus(status, order.cancelStatus);

                return order;
            } catch (Exception ignored) {
                ignored.printStackTrace();
                return null;
            }
        }

        public static List<? extends Order> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }
}
