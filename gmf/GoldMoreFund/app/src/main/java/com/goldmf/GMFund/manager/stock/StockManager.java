package com.goldmf.GMFund.manager.stock;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.stock.GMFLiveItem.GMFLiveItemMore;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.LineData;
import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.Rank;
import com.goldmf.GMFund.model.Stock;
import com.goldmf.GMFund.model.StockData;
import com.goldmf.GMFund.model.StockData.StockKline;
import com.goldmf.GMFund.model.StockData.StockTline;
import com.goldmf.GMFund.model.StockInfo.StockBrief;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol.ParamParse;
import com.goldmf.GMFund.protocol.base.ComonProtocol.ParamParse.ParamBuilder;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.util.GMutableArrayUtil;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.goldmf.GMFund.base.MResults.MResultsInfo.SafeOnResult;

/**
 * Created by cupide on 15/7/29.
 */
public class StockManager extends BaseManager {

    private SearchHistoryStockStore searchHistoryStockStore = null;

    public synchronized SearchHistoryStockStore getSearchHistoryStockStore() {

        if (searchHistoryStockStore == null) {

            searchHistoryStockStore = new SearchHistoryStockStore();
        }
        return searchHistoryStockStore;
    }

    /**
     * 一些内存级别的存储
     */
    private GMutableArrayUtil<String, StockData> stockDataBackup = new GMutableArrayUtil<>(5);  //内存级别的存储
    private GMutableArrayUtil<String, Stock> stockDetailBackup = new GMutableArrayUtil<>(10);  //内存级别的存储

    private TopTrader topTrader = new TopTrader();

    private List<CommandPageArray<FeedOrder>> orderFeedBackup = new ArrayList<>();

    private CommandPageArray<Order> orderList = null;
    private CommandPageArray<GMFLiveItemMore> liveData = null;


    /**
     * 静态方法
     */
    private static StockManager manager = new StockManager();

    public static StockManager getInstance() {
        return manager;
    }

    private StockManager() {

        NotificationCenter.logoutSubject.subscribe(aVoid -> {
            stockDetailBackup.clear();
            stockDataBackup.clear();
            topTrader = new TopTrader();
        });
    }

    /**
     * 搜索关键词
     */
    public final void searchKey(String stockCodeKey,
                                final MResults<List<StockBrief>> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "quote/quick-query")
                .params(ComonProtocol.buildParams("code", stockCodeKey))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        SafeOnResult(results, protocol.<List<StockBrief>>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<List<StockBrief>> info = protocol.buildRet();

                        if (ret.isJsonObject()) {
                            info.data = new ArrayList<>(StockBrief.translate(GsonUtil.getAsJsonArray(ret, "list")));
                        }

                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 返回 股票详情
     *
     * @param results
     */
    public final void getStockInfo(Boolean local,
                                   String stockID,
                                   final MResults<Stock> results) {
        if (local) {
            Stock stock = this.stockDetailBackup.get(stockID);

            if (stock != null) {
                MResultsInfo<Stock> info = new MResults.MResultsInfo<>();
                info.data = stock;
                SafeOnResult(results, info);
            }
        }

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/stock-detail")
                .params(ComonProtocol.buildParams("stock_id", stockID))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        Stock stock = StockManager.this.stockDetailBackup.get(stockID);

                        if (ret.isJsonObject()) {
                            if (stock == null) {
                                stock = new Stock();
                            }

                            stock.readFromeJsonData(GsonUtil.getAsJsonObject(ret, "stock_detail"));
                            stock.stockPosition = StockPosition.translateFromJsonData(GsonUtil.getAsJsonObject(ret, "portfolio"));

                            StockManager.this.stockDetailBackup.put(stockID, stock);
                        }

                        SafeOnResult(results, protocol.<Stock>buildRet().setData(stock));
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 获取 T线
     *
     * @param sstock
     * @param authority
     * @param tLineType
     * @param results
     */
    public final void getStockTLine(StockSimple sstock,
                                    int authority,
                                    int tLineType,
                                    final MResults<StockTline> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "quote/t-lines")
                .params(ComonProtocol.buildParams("stock_id", sstock.index,
                        "range", String.valueOf(tLineType),
                        "accum_adj_factor_type", String.valueOf(authority)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        SafeOnResult(results, protocol.<StockTline>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<StockTline> info = protocol.buildRet();
                        StockTline tline = new StockTline(sstock, tLineType, authority);
                        tline.type = tLineType;
                        tline.authority = authority;
                        tline.data = new LinkedList<>();

                        if (ret.isJsonObject()) {
                            JsonObject dic = GsonUtil.getAsJsonObject(ret);
                            JsonObject metadataDic = GsonUtil.getAsJsonObject(dic, "metadata");
                            double preClose = GsonUtil.getAsDouble(metadataDic, "prev_close_price");
                            JsonArray array = GsonUtil.getAsJsonArray(dic, "lines");
                            for (JsonElement temp : array) {
                                LineData.TLineData data = LineData.TLineData.translateFromJsonData(temp);
                                if (data != null) {
                                    data.prevClose = preClose;
                                    tline.data.add(data);
                                }
                            }

                            info.data = tline;
                        }

                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();

    }


    /**
     * 获取 K线
     *
     * @param stockSimple
     * @param authority
     * @param kLineType
     * @param specType
     * @param results
     */
    public final void getStockKLine(Boolean local,
                                    StockSimple stockSimple,
                                    int authority,
                                    int kLineType,
                                    int specType,
                                    final MResults<StockKline> results) {

        if (stockSimple == null)
            return;

        StockData data = this.stockDataBackup.get(stockSimple.index);
        StockKline kline = data != null ? data.get(kLineType) : null;


        if (data != null && kline != null && data.authority == authority && kline.data().size() > 0) {

            if (kline.specDataValid(specType)) {

                if(local) {
                    SafeOnResult(results, new MResultsInfo<StockKline>().setData(kline));
                }

                kline.getNextPage(result -> {
                    MResultsInfo<StockKline> info = new MResultsInfo<>();
                    MResultsInfo.COPY_ALL(info, result);
                    info.data = (StockKline) result.data;
                    SafeOnResult(results, info);
                });
                return;
            } else {
                kline.getSpecData(specType, results);
            }

        } else {
            if (data == null || data.authority != authority) {
                data = new StockData(stockSimple, authority);

                this.stockDataBackup.put(stockSimple.index, data);
            }

            kline = new StockKline(stockSimple, kLineType, authority, specType);
            data.put(kline);

            kline.getNextPage(result -> {
                MResultsInfo<StockKline> info = new MResultsInfo<>();
                MResultsInfo.COPY_ALL(info, result);
                info.data = (StockKline) result.data;
                SafeOnResult(results, info);
            });
        }
    }


    /**
     * 获取牛人圈首页
     */
    public final void getTopTrader(int feedType,
                                   final MResults<TopTrader> results) {

        this.topTrader.feedType = feedType;

        this.topTrader.getNextPage(result -> {
            MResultsInfo<TopTrader> info = new MResultsInfo<>();
            MResultsInfo.COPY_ALL(info, result);
            info.data = (TopTrader) result.data;
            SafeOnResult(results, info);
        });
    }

    /**
     * 获取排行榜详情
     *
     * @param rankID
     * @param results
     */
    public final void getRankDetail(String rankID,
                                    final MResults<Rank> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/gmf-leaderboard")
                .params(ComonProtocol.buildParams("leaderboard_id", rankID))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<Rank> info = protocol.buildRet();

                        if (ret.isJsonObject()) {
                            info.data = Rank.translateFromJsonData(ret.getAsJsonObject());
                        }

                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    public void getLiveArray(MResults<List<GMFLiveItem>> callback) {
        new ComonProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, "quote/news/latest-headline"))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(callback, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        JsonArray jsonArr = GsonUtil.getAsJsonArray(ret, "list");
                        List<GMFLiveItem> data = new ArrayList<>(GMFLiveItem.translate(jsonArr));
                        SafeOnResult(callback, new MResultsInfo<List<GMFLiveItem>>().setData(data));
                    }
                })
                .build().startWork();
    }

    public void getLiveData(MResults<CommandPageArray<GMFLiveItemMore>> callback) {
        if (this.liveData == null) {
            CommandPageArray<GMFLiveItemMore> liveData = new CommandPageArray.Builder<GMFLiveItemMore>()
                    .classOfT(GMFLiveItemMore.class)
                    .cgiUrl(CHostName.HOST2 + "quote/news/get-list")
                    .commandTS()
                    .build();

            this.liveData = liveData;
        }

        this.liveData.getNextPage(result -> {
            MResultsInfo.SafeOnResult(callback, result);
        });
    }

    /**
     * 获取某个用户的个人主页 的 模拟业绩
     *
     * @param userID
     * @param results
     */
    public final void freshIncomeChart(int userID,
                                       final MResults<SimulationUserIncomeChart> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/user-vtc-profile")
                .params(ComonProtocol.buildParams("user_id", String.valueOf(userID)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {

                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<SimulationUserIncomeChart> info = protocol.buildRet();

                        if (ret.isJsonObject()) {
                            info.data = SimulationUserIncomeChart.translateFromJsonData(ret.getAsJsonObject());
                        }

                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 获取某只股票的全部FeedOrder
     *
     * @param feedType
     * @param results
     */
    public final void getStockFeedOrderList(String stockID,
                                            int feedType,
                                            final MResults<CommandPageArray<FeedOrder>> results) {

        ParamBuilder param = new ParamBuilder()
                .add("type", feedType);
        if(stockID != null){
            param.add("stock_id", stockID);
        }

        CommandPageArray<FeedOrder> orderFeedList = new CommandPageArray.Builder<FeedOrder>()
                .classOfT(FeedOrder.class)
                .cgiUrl(CHostName.HOST2 + "feed/get-list")
                .cgiParam(param)
                .commandTS()
                .build();

        this.orderFeedBackup.add(orderFeedList);

        orderFeedList.getNextPage(result -> {
            SafeOnResult(results, result);
            this.orderFeedBackup.remove(orderFeedList);
        });
    }

    /**
     * 获取某个用户的持仓全部信息
     *
     * @param userID
     * @param results
     */
    public final void listStockPosition(String userID,
                                        final MResults<SimulationAccountMore> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/user-position/")
                .params(ComonProtocol.buildParams("user_id", String.valueOf(userID)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<SimulationAccountMore> info = protocol.buildRet();

                        if (ret.isJsonObject()) {
                            SimulationAccountMore data = new SimulationAccountMore();
                            data.readFromeJsonData(ret.getAsJsonObject());
                            info.data = data;
                        }

                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 返回某个用户的成交记录
     *
     * @param userID
     * @param results
     */
    public final void listStockOrderList(String userID,
                                         final MResults<CommandPageArray<Order>> results) {

        listOrderList(userID, null, 200, null, result -> {
            SafeOnResult(results, result);
        });
    }

    /**
     * 获取 某个用户的某个持仓的具体详情
     *
     * @param userId
     * @param stockID
     * @param range
     * @param results
     */
    public final void getPositionDetail(String userId,
                                        String stockID,
                                        String range,
                                        final MResults<StockPosition> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "vtc/portfolio/detail")
                .params(ComonProtocol.buildParams("user_id", userId,
                        "stock_id", stockID,
                        "range", range))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<StockPosition> info = protocol.buildRet();

                        if (ret != null && ret.isJsonObject()) {
                            StockPosition position = StockPosition.translateFromJsonData(GsonUtil.getAsJsonObject(ret, "position"));
                            if(position!= null)
                                position.orderList = new ArrayList<>(Order.translate(GsonUtil.getAsJsonArray(ret, "list")));
                            info.data = position;
                        }

                        SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    //给其他类用的
    final void listOrderList(String userID,
                                    String stockID,
                                    int listType,
                                    String orderID,
                                    final MResults<CommandPageArray<Order>> results) {

        if (userID == null)
            return;

        ParamBuilder param = new ParamBuilder().add("user_id", userID);
        if (stockID != null) {
            param.add("stock_id", stockID);
        }
        if (listType != 0) {
            param.add("list_type", String.valueOf(listType));
        }
        if (orderID != null) {
            param.add("order_id", orderID);// getAsString
        }

        orderList = new CommandPageArray.Builder<Order>()
                .classOfT(Order.class)
                .cgiUrl(CHostName.HOST2 + "vtc/order/get-list")
                .cgiParam(param)
                .commandPage(20)
                .build();

        orderList.getNextPage(result -> {
            SafeOnResult(results, result);
        });
    }


//    public void qualifyStock(final StockBrief sStock,
//                             List<FundBrief> funds,
//                             double price,
//                             final MResults<Map<Integer, StockTradeQualify>> results) {
//        assert (sStock != null);
//        assert (funds != null && funds.size() > 0);
//
//
//        new ComonProtocol.Builder()
//                .url("trade/order-qualify")
//                .params(ComonProtocol.buildParams("product_id", toString(funds),
//                        "stock_id", sStock.index,
//                        "current_price", String.valueOf(price)))
//                .callback(new ComonProtocol.ComonCallback() {
//                    @Override
//                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
//                        MResults.MResultsInfo.SafeOnResult(results, protocol.<Map<Integer, StockTradeQualify>>buildRet());
//                    }
//
//                    @Override
//                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
//
//                        Map<Integer, StockTradeQualify> orderQualify = new HashMap<>();
//                        if (ret.isJsonObject()) {
//
//                            for (Map.Entry<String, JsonElement> element : ret.getAsJsonObject().entrySet()) {
//
//                                int fundId = Integer.valueOf(element.getKey());
//                                if (element.getValue().isJsonObject()) {
//                                    StockTradeQualify qualify = new StockTradeQualify(sStock, fundId);
//                                    qualify.readFromeJsonData(element.getValue().getAsJsonObject());
//
//                                    orderQualify.put(fundId, qualify);
//                                }
//                            }
//                        }
//                        MResults.MResultsInfo<Map<Integer, StockTradeQualify>> info = protocol.buildRet();
//                        info.data = orderQualify;
//
//                        MResults.MResultsInfo.SafeOnResult(results, info);
//                    }
//                })
//                .build()
//                .startWork();
//    }


}
