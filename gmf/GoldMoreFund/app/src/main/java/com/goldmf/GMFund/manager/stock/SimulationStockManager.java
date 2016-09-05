package com.goldmf.GMFund.manager.stock;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.SimulationAccount;
import com.goldmf.GMFund.model.Stock;
import com.goldmf.GMFund.model.StockInfo;
import com.goldmf.GMFund.model.StockInfo.StockBrief;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol.ParamParse.ParamBuilder;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by cupide on 16/3/12.
 */
public class SimulationStockManager extends BaseManager {

    private final String sSimulationStockManagerStockJsonKey = "SimulationStockManagerStockJsonKey";
    private final String sSimulationStockManagerStockPositionJsonKey = "SimulationStockManagerStockPositionJsonKey";

    public static final int ACCOUNT_STATE_NORMAL = 1;      //已开户
    public static final int ACCOUNT_STATE_NOTCREATED = 2; //未开户
    public static final int ACCOUNT_STATE_UNKNOW = 0;     //未知是否开户

    /**
     * 一些内存级别的存储
     */
    public int accountStatus = ACCOUNT_STATE_UNKNOW;                  //是否创建了虚拟户
    public SimulationAccountMore mineAccountMore = new SimulationAccountMore();   //用户Account详情

    public List<StockBrief> followStockList;            //自选股
    public List<StockBrief> focusCompareIndexs;         //【自选】页面上的指数列表


    /**
     * 静态方法
     */
    private static SimulationStockManager manager = new SimulationStockManager();

    public static SimulationStockManager getInstance() {
        return manager;
    }

    private SimulationStockManager() {

        //读取本地数据
        loadLocalData();

        NotificationCenter.logoutSubject.subscribe(aVoid -> {
            //删除本地数据
            ModelSerialization.removeJsonByKey(sSimulationStockManagerStockJsonKey, true);
            ModelSerialization.removeJsonByKey(sSimulationStockManagerStockPositionJsonKey, true);

            SimulationStockManager.this.accountStatus = ACCOUNT_STATE_UNKNOW;
            this.mineAccountMore = new SimulationAccountMore();
            if (this.followStockList != null) {
                this.followStockList.clear();
            }
        });

        NotificationCenter.loginSubject.subscribe(aVoid -> {

            SimulationStockManager.this.accountStatus = ACCOUNT_STATE_UNKNOW;
            SimulationStockManager.this.freshFollowStock(null);
            SimulationStockManager.this.listStockPosition(null);
        });
    }

    private void loadFollowStock(JsonElement dic) {

        if (dic != null && dic.isJsonObject()) {

            followStockList = new ArrayList<>(StockBrief.translate(GsonUtil.getAsJsonArray(dic, "list")));
            focusCompareIndexs = new ArrayList<>(StockBrief.translate(GsonUtil.getAsJsonArray(dic, "compare_index")));
        }

    }

    private void loadLocalData() {
        {
            JsonElement ret = ModelSerialization.loadJsonByKey(sSimulationStockManagerStockJsonKey, true);
            loadFollowStock(ret);
        }

        {
            JsonElement ret = ModelSerialization.loadJsonByKey(sSimulationStockManagerStockPositionJsonKey, true);
            if (ret != null && ret.isJsonObject()) {
                mineAccountMore.readFromeJsonData(ret.getAsJsonObject());
            }
        }

        this.accountStatus = ACCOUNT_STATE_UNKNOW;
    }


    /**
     * 获取新增加的虚拟股票账户
     *
     * @return
     */
    public final SimulationAccount getAccount() {
        return mineAccountMore.account;
    }

    /**
     * 获取新增加的虚拟股票账户
     *
     * @return
     */
    public final SimulationAccountMore getAccountMore() {
        return mineAccountMore;
    }

    /**
     * 创建虚拟股票账号
     *
     * @param results
     */
    public final void createAccount(final MResults<Void> results) {

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "vtc/account/create")
                .postParams(ComonProtocol.buildParams("market", String.valueOf(FundBrief.Money_Type.CN)))
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
     * 查询股票 de 最大可买 最大可卖
     *
     * @param sstock
     * @param price
     * @param results Pair 最大可买 最大可卖
     */
    public final void qualifyStock(StockSimple sstock,
                                   double price,
                                   final MResults<Pair<Double, Double>> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "vtc/order/qualify")
                .params(ComonProtocol.buildParams("stock_id", sstock.index,
                        "entrust_price", String.valueOf(price)))
                .callback(new ComonProtocol.ComonCallback() {
                              @Override
                              public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }

                              @Override
                              public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                                  MResults.MResultsInfo<Pair<Double, Double>> info = protocol.buildRet();

                                  if (ret.isJsonObject()) {
                                      double buyMaxAmount = GsonUtil.getAsDouble(ret, "buy_maximum");
                                      double sellMaxAmount = GsonUtil.getAsDouble(ret, "sell_maximun");
                                      info.data = new Pair<>(buyMaxAmount, sellMaxAmount);
                                  }

                                  MResults.MResultsInfo.SafeOnResult(results, info);
                              }
                          }
                )
                .build()
                .startWork();
    }


    Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 下单协议
     *
     * @param order
     * @param results
     */
    public final void orderOrder(Order order,
                                 final MResults<Order> results) {

        if (order == null || order.stock == null || order.stock.index == null)
            return;

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "vtc/order/add")
                .postParams(ComonProtocol.buildParams("stock_id", order.stock.index,
                        "entrust_price", String.valueOf(order.orderPrice),
                        "entrust_amount", String.valueOf(order.orderAmount),
                        "order_type", String.valueOf(order.buy ? 1 : 2),
                        "order_model", String.valueOf(order.orderModel)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        String orderID = GsonUtil.getAsString(ret, "order_id");

                        int second = 2; //2s

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SimulationStockManager.this.queryOrder(orderID,
                                        results);
                            }
                        }, second * 1000L);

                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    private void queryOrder(String orderID,
                            final MResults<Order> results) {
        StockManager.getInstance().listOrderList("" + MineManager.getInstance().getmMe().index,
                null, 0, orderID, result -> {

                    MResults.MResultsInfo<Order> info = new MResults.MResultsInfo<>();
                    if (result.isSuccess && result.data != null
                            && result.data.data() != null
                            && result.data.data().size() > 0) {
                        info.data = result.data.data().get(0);
                    }
                    MResults.MResultsInfo.SafeOnResult(results, info);
                });
    }

    /**
     * 撤销订单协议
     *
     * @param order
     * @param results
     */
    public final void cancelOrder(Order order,
                                  final MResults<Void> results) {

        if (order == null || order.orderId == null)
            return;

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "vtc/order/cancel")
                .postParams(ComonProtocol.buildParams("order_id", order.orderId))
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
     * 获取living订单list - 还未执行结束的单 可以撤单的OrderList
     *
     * @param results
     */
    public final void listLivingOrderList(final MResults<CommandPageArray<Order>> results) {
        StockManager.getInstance().listOrderList("" + MineManager.getInstance().getmMe().index,
                null,
                206,
                null,
                result -> {
                    MResults.MResultsInfo.SafeOnResult(results, result);
                });
    }

    /**
     * 获取当前用户持仓详情，返回：SimulationAccountMore
     *
     * @param results
     */
    public final void listStockPosition(final MResults<SimulationAccountMore> results) {
        StockManager.getInstance().listStockPosition("" + MineManager.getInstance().getmMe().index,
                result -> {
                    if (result.isSuccess
                            && result.data != null
                            && result.ret != null
                            && result.ret.isJsonObject()) {

                        SimulationStockManager.this.accountStatus = ACCOUNT_STATE_NORMAL;
                        SimulationStockManager.this.mineAccountMore.readFromeJsonData(result.ret.getAsJsonObject());

                        ModelSerialization.saveJsonByKey(result.ret, sSimulationStockManagerStockPositionJsonKey, true);

                    } else {
                        if (result.errCode == CommonDefine.No_Simulation_account)
                            SimulationStockManager.this.accountStatus = ACCOUNT_STATE_NOTCREATED;
                    }

                    MResults.MResultsInfo.SafeOnResult(results, result);
                });
    }

    /**
     * 获取当前用户的调仓记录，返回：CommandPageArray<Order>
     *
     * @param results
     */
    public final void listStockOrderList(final MResults<CommandPageArray<Order>> results) {
        StockManager.getInstance().listStockOrderList("" + MineManager.getInstance().getmMe().index,
                result -> {
                    MResults.MResultsInfo.SafeOnResult(results, result);
                });
    }

    /**
     * 刷新自选股，详细数据请 拿 followStockList focusCompareIndexs
     *
     * @param results
     */
    public final void freshFollowStock(final MResults<Void> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/stock-follow")
                .callback(new ComonProtocol.ComonCallback() {
                              @Override
                              public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }

                              @Override
                              public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                                  loadFollowStock(ret);

                                  ModelSerialization.saveJsonByKey(ret, sSimulationStockManagerStockJsonKey, true);

                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }
                          }
                )
                .build()
                .startWork();
    }

    /**
     * 添加自选股
     *
     * @param stock
     * @param results
     */
    public final void addFollowStock(StockSimple stock,
                                     final MResults<Void> results) {

        if (stock == null)
            return;

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "stock-follow/add")
                .postParams(ComonProtocol.buildParams("stock_id", stock.index))
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
     * 删除自选股
     *
     * @param stock
     * @param results
     */
    public final void delFollowStock(StockBrief stock,
                                     final MResults<Void> results) {

        if (stock == null)
            return;

        this.delFollowStock(Collections.singletonList(stock), results);
    }

    /**
     * 删除自选股
     *
     * @param stocks  批量删除的stocklist
     * @param results
     */
    public final void delFollowStock(List<StockBrief> stocks,
                                     final MResults<Void> results) {
        if (stocks == null || stocks.size() == 0) return;

        HashMap<String, Object> params = new HashMap<>();
        if (stocks.size() == 1) {
            params.put("stock_id", stocks.get(0).index);
        } else {
            List<String> stockIDList = new ArrayList<>();
            for (StockSimple stock : stocks) {
                stockIDList.add(stock.index);
            }
            params.put("stock_list[]", stockIDList);
        }

        //本地预删除
        this.followStockList.removeAll(stocks);


        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "stock-follow/delete")
                .postParams(params)
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        SimulationStockManager.this.freshFollowStock(null);
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        SimulationStockManager.this.freshFollowStock(null);
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 自选股排序
     *
     * @param stocks  新股票list
     * @param results
     */
    public final void sequenceFollowStocks(List<StockBrief> stocks,
                                           final MResults<Void> results) {
        if (stocks == null || stocks.size() == 0) return;

        //合并 stocks 和 this.followStockList
        List<StockBrief> tempStockList = new ArrayList<>();
        for (StockBrief stock : stocks) {
            if (this.followStockList.contains(stock)) {
                tempStockList.add(stock);
            }
        }
        if (tempStockList.size() != this.followStockList.size()) {
            for (StockBrief stock : this.followStockList) {
                if (!tempStockList.contains(stock)) {
                    tempStockList.add(stock);
                }
            }
        }
        this.followStockList.clear();
        this.followStockList.addAll(tempStockList);//本地先处理这个


        HashMap<String, Object> params = new HashMap<>();
        List<String> stockIDList = new ArrayList<>();
        for (StockBrief stock : this.followStockList) {
            stockIDList.add(stock.index);
        }
        params.put("stock_list[]", stockIDList);

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "stock-follow/sequence")
                .postParams(params)
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SimulationStockManager.this.freshFollowStock(null);
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        SimulationStockManager.this.freshFollowStock(null);

                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }
}
