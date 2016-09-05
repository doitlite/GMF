package com.goldmf.GMFund.manager.trader;

import android.util.Pair;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.model.Feed;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.Order.OrderV2;
import com.goldmf.GMFund.model.StockInfo;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.BookFundProtocol;
import com.goldmf.GMFund.protocol.FundProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;

import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;

/**
 * Created by cupide on 15/7/27.
 */
public class TraderManager {

    private static String sTraderManagerFundlistJsonKey2 = "TraderManagerFundlistJsonKey2";


    private List<FundBrief> mFundList = new ArrayList<>();
    public List<FundBrief> getFundList() {
        return mFundList;
    }


    /**
     * 静态方法
     */
    private static TraderManager manager = new TraderManager();

    public static TraderManager getInstance() {
        return manager;
    }

    private TraderManager() {

        //获取本地数据
        loadLocalData();

        NotificationCenter.logoutSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    //删除本地数据
                    ModelSerialization.removeJsonByKey(sTraderManagerFundlistJsonKey2, true);

                    TraderManager.this.mFundList.clear();
                });

        NotificationCenter.loginSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {

                    //刷新数据
                    TraderManager.this.getFundList(false, null);
                });
    }


    private void loadLocalData() {

        JsonElement list = ModelSerialization.loadJsonByKey(sTraderManagerFundlistJsonKey2, true);
        if (list != null) {
            mFundList.clear();
            mFundList.addAll(FundBrief.translate(list.getAsJsonArray()));
        }
    }

    public final void getFundList(boolean allowCache,
                                  final MResults<List<FundBrief>> results) {

        //获取本地数据
        if (allowCache && mFundList != null) {
            MResults.MResultsInfo<List<FundBrief>> info = new MResults.MResultsInfo<>();
            info.setHasNext(true);
            info.data = mFundList;
            info.setHasNext(true);
            MResults.MResultsInfo.SafeOnResult(results, info);
        }

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 +"product/operable-list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<List<FundBrief>>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        if (ret != null && ret.isJsonArray()) {

                            mFundList.clear();
                            mFundList.addAll(FundBrief.translate(ret.getAsJsonArray()));

                            for (FundBrief brief :mFundList){
                                if(brief != null)
                                    brief.editRealTimeTradeInfo(getLocalFundTradeInfo(brief.index));
                            }

                            //保存数据
                            ModelSerialization.saveJsonByKey(ret, sTraderManagerFundlistJsonKey2, true);
                        }

                        MResults.MResultsInfo<List<FundBrief>> info = protocol.buildRet();
                        info.data = mFundList;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }


    public final void getPortfolio(boolean local,
                                   int fundID,
                                   final MResults<Fund> results) {
        assert (fundID != 0);

        if (local) {
//        由于 2016年的 加息需求，这里不需要本地访问，直接从server获取。
        }

        FundProtocol p = new FundProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Fund>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                FundProtocol pro = (FundProtocol) protocol;

                MResults.MResultsInfo<Fund> info = protocol.buildRet();
                info.data = pro.fund;
                MResults.MResultsInfo.SafeOnResult(results, info);
            }
        });
        p.fundID = fundID;

        p.startWork();
    }


    /**
     * 获取一个产品的投资用户列表
     *
     * @param sFund
     * @param results
     */
    public void getInvestors(FundBrief sFund,
                             final MResults<List<FundInvestor>> results) {
        assert (sFund != null);

            new ComonProtocol.Builder()
                    .url(CHostName.HOST1 +"product/investors")
                    .params(ComonProtocol.buildParams("product_id", Integer.toString(sFund.index)))
                    .callback(new ComonProtocol.ComonCallback() {
                        @Override
                        public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                            MResults.MResultsInfo.SafeOnResult(results, protocol.<List<FundInvestor>>buildRet());
                        }

                        @Override
                        public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                            List<FundInvestor> investors = new ArrayList<>();

                            JsonArray array = GsonUtil.getAsJsonArray(ret);
                            if (array != null && array.size() > 0) {
                                for (JsonElement element : array) {
                                    if (element.isJsonObject()) {
                                        FundInvestor investor = FundInvestor.translateFromJsonData(element.getAsJsonObject());
                                        investors.add(investor);
                                    }
                                }
                            }

                            MResults.MResultsInfo<List<FundInvestor>> info = protocol.buildRet();
                            info.data = investors;
                            MResults.MResultsInfo.SafeOnResult(results, info);
                        }
                    })
                    .build()
                    .startWork();
    }


    /**
     * 预约
     * @param sFund 组合
     * @param results
     */
    public void booking(FundBrief sFund,
                       final MResults<Void> results) {
        assert (sFund != null);

        BookFundProtocol p = new BookFundProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResults.MResultsInfo<Void> info = protocol.buildRet();
                MResults.MResultsInfo.SafeOnResult(results, info);
            }
        });
        p.sFund = sFund;
        p.startWork();
    }



    private Map<Integer, FundTradeInfo> fundTradeInfos = new Hashtable<>();

    //以下为组合相关的交易

    /**
     * 返回某个组合的本地缓存当日详情，可能为null
     * @param FundID 组合id
     * @return 组合当日详情
     */
    public FundTradeInfo getLocalFundTradeInfo(int FundID){
        return fundTradeInfos.get(FundID);
    }

    private FundTradeInfo getFundTradeInfo(int FundID){
        FundTradeInfo info = fundTradeInfos.get(FundID);
        if(info == null){
            info = new FundTradeInfo();
            this.fundTradeInfos.put(FundID, info);
        }
        return info;
    }

    /**
     * 获取某个fund的当日详情
     * @param fundID 组合id
     * @param results
     */
    public final void freshTradeInfo(int fundID,
                                     final MResults<FundTradeInfo> results){
        getFundTradeInfo(fundID);

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 +"/omsv2/oms/api/get_settlement_info")
                .params(ComonProtocol.buildParams("product_id", String.valueOf(fundID)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        MResults.MResultsInfo.SafeOnResult(results, protocol.<FundTradeInfo>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        FundTradeInfo data = getFundTradeInfo(fundID);
                        for (FundBrief brief :mFundList){
                            if(brief.index == fundID)
                                brief.editRealTimeTradeInfo(data);
                        }

                        if(ret.isJsonObject()){
                            data.readFromeJsonData(GsonUtil.getAsJsonObject(ret));
                        }

                        MResults.MResultsInfo<FundTradeInfo> info = protocol.buildRet();
                        info.data = data;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 获取某个fund的持仓详情，返回：FundTradeInfo 里面的 StockPositionV2
     * @param fundID 组合ID
     * @param results 返回
     */
    public final void listStockPositionList(int fundID,
                                            final MResults<FundTradeInfo> results){

        getFundTradeInfo(fundID);

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 +"omsv2/oms/api/position_realtime")
                .params(ComonProtocol.buildParams("product_id", String.valueOf(fundID)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        MResults.MResultsInfo.SafeOnResult(results, protocol.<FundTradeInfo>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        FundTradeInfo data = getFundTradeInfo(fundID);
                        data.holdStocks.clear();
                        if(ret.isJsonArray()){
                            data.holdStocks.addAll(StockPosition.StockPositionV2.translate(GsonUtil.getAsJsonArray(ret)));
                        }

                        MResults.MResultsInfo<FundTradeInfo> info = protocol.buildRet();
                        info.data = data;
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 查询股票 de 最大可买 最大可卖
     *
     * @param sstock
     * @param results Pair 最大可买 最大可卖
     */
    public final void qualifyStock(int fundID,
                                   StockSimple sstock,
                                   final MResults<Pair<Double, Double>> results) {

        String url = String.format("omsv2/oms/helper/risk_pre/%d", fundID);

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + url)
                .params(ComonProtocol.buildParams("stock_id", sstock.index))
                .callback(new ComonProtocol.ComonCallback() {
                              @Override
                              public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }

                              @Override
                              public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                                  MResults.MResultsInfo<Pair<Double, Double>> info = protocol.buildRet();

                                  if (ret.isJsonObject()) {
                                      double buyMaxAmount = GsonUtil.getAsDouble(ret, "buy_max_volume");
                                      double sellMaxAmount = GsonUtil.getAsDouble(ret, "sell_max_volume");
                                      info.data = new Pair<>(buyMaxAmount, sellMaxAmount);
                                  }

                                  MResults.MResultsInfo.SafeOnResult(results, info);
                              }
                          }
                )
                .build()
                .startWork();
    }

    /**
     * 提交某个fund的订单前风控检测
     * @param fundID 组合ID
     * @param order 订单
     * @param results   结果
     */
    public final void riskOrder(int fundID,
                                Order order,
                                final MResults<Void> results){

        if (order == null || order.stock == null || order.stock.index == null)
            return;

        String url = String.format("omsv2/oms/helper/risk_check/%d", fundID);

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST1 + url)
                .postParams(ComonProtocol.buildParams("stock_id", order.stock.index,
                        "price", String.valueOf(order.orderPrice),
                        "volume", String.valueOf(order.orderAmount),
                        "trade_direction", String.valueOf(order.buy ? 1 : 2),
                        "trade_mode", String.valueOf(order.orderModel==Order.Model_Market?2:1)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 某个fund的下单协议
     * @param fundID    组合ID
     * @param order     订单
     * @param results   返回
     */
    public final void orderOrder(int fundID,
                                Order order,
                                final MResults<Void> results){

        if (order == null || order.stock == null || order.stock.index == null)
            return;

        String url = String.format("omsv2/oms/workflow/%d/add_hand_order", fundID);

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST1 + url)
                .postParams(ComonProtocol.buildParams("stock_id", order.stock.index,
                        "price", String.valueOf(order.orderPrice),
                        "volume", String.valueOf(order.orderAmount),
                        "trade_direction", String.valueOf(order.buy ? 1 : 2),
                        "trade_mode", String.valueOf(order.orderModel==Order.Model_Market?2:1)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 撤销某个订单（如果可以撤销的话）
     * @param fundID    组合ID
     * @param orderID   订单ID
     * @param results   结果
     */
    public final void cancelOrder(int fundID,
                                 String orderID,
                                 final MResults<Void> results){

        String url = String.format("omsv2/oms/workflow/%d/%s/do_cancel", fundID, orderID);

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST1 + url)
                .postParams(ComonProtocol.buildParams("order_id", orderID))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 查询今日撤单委托列表
     * @param fundID
     * @param results
     */
    public final void listCancelOrderList(int fundID,
                                          final MResults<CommandPageArray<OrderV2>> results){
        listOrderList(fundID, ORDER_LIST_Cancel, results);
    }

    /**
     * 查询今日委托列表
     * @param fundID
     * @param results
     */
    public final void listTodayOrderList(int fundID,
                                          final MResults<CommandPageArray<OrderV2>> results){
        listOrderList(fundID, ORDER_LIST_Today, results);
    }


    /**
     * 查询今日成交列表
     * @param fundID
     * @param results
     */
    public final void listTodayDealOrderList(int fundID,
                                         final MResults<CommandPageArray<OrderV2>> results){
        listOrderList(fundID, ORDER_LIST_TodayDeal, results);
    }

    /**
     * 查询历史成交列表
     * @param fundID
     * @param results
     */
    public final void listHistoryDealOrderList(int fundID,
                                             final MResults<CommandPageArray<OrderV2>> results){
        listOrderList(fundID, ORDER_LIST_History, results);
    }


    private static final int ORDER_LIST_Cancel = 1; //撤单订单
    private static final int ORDER_LIST_Today = 2;  //今日订单
    private static final int ORDER_LIST_TodayDeal = 3;  //今日成交
    private static final int ORDER_LIST_History = 4;    //历史订单

    private CommandPageArray<OrderV2> orderList = null;
    private void listOrderList(int fundID,
                                    int type,
                                    final MResults<CommandPageArray<OrderV2>> results) {

        String url = "";
        switch (type){
            case ORDER_LIST_Cancel:
            case ORDER_LIST_Today:
                url = "omsv2/oms/order/get_entrust_list?permission_type=product&type=all";
                break;
            case ORDER_LIST_TodayDeal:
                url = "omsv2/oms/order/get_deal_list?permission_type=product";
                break;
            case ORDER_LIST_History:
                url = "omsv2/oms/order/get_deal_list?permission_type=product&start=2015-01-01";
                break;
            default:
                break;
        }

        orderList = new CommandPageArray.Builder<OrderV2>()
                .cgiUrl(CHostName.HOST1 + url)
                .cgiParam(buildParams("product_id", String.valueOf(fundID)))
                .translateItem(data -> {
                    OrderV2 order = OrderV2.translateFromJsonData(data);

                    if(type == ORDER_LIST_Cancel) {
                        if(order.status != Order.NODeal
                                && Order.PartDeal != order.status
                                && Order.Waiting != order.status
                                && Order.WaitingCancel != order.status){
                            return null;
                        }
                    }
                    return order;
                })
                .commandPage(40)
                .build();

        orderList.getPrePage(results);

    }


}
