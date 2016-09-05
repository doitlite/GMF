package com.goldmf.GMFund.controller.business;

import android.text.TextUtils;
import android.util.Pair;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.stock.GMFLiveItem;
import com.goldmf.GMFund.manager.stock.SimulationAccountMore;
import com.goldmf.GMFund.manager.stock.SimulationStockManager;
import com.goldmf.GMFund.manager.stock.StockManager;
import com.goldmf.GMFund.manager.stock.TopTrader;
import com.goldmf.GMFund.manager.trader.FundTradeInfo;
import com.goldmf.GMFund.manager.trader.TraderManager;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.Order.OrderV2;
import com.goldmf.GMFund.model.Rank;
import com.goldmf.GMFund.model.Stock;
import com.goldmf.GMFund.model.StockData.StockKline;
import com.goldmf.GMFund.model.StockData.StockTline;
import com.goldmf.GMFund.model.StockInfo.StockBrief;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.protocol.base.CommandPageArray;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;

import rx.Observable;

import static com.goldmf.GMFund.extension.MResultExtension.*;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableListMResult;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by yale on 16/2/25.
 */
public class StockController {
    private StockController() {
    }

    public static Observable<MResultsInfo<Void>> fetchMarketStatsInfo() {
        return Observable.just(new MResultsInfo<Void>())
                .delay(1, TimeUnit.SECONDS);
    }

    public static final int FEED_TYPE_BEGIN = FeedOrder.Feed_Type_begin;
    public static final int FEED_TYPE_BUY = FeedOrder.Feed_Type_buy;
    public static final int FEED_TYPE_SELL = FeedOrder.Feed_Type_sell;

    /**
     * 获取全部牛人圈信息
     */
    public static Observable<MResultsInfo<TopTrader>> fetchTopTraderAll() {
        return Observable.create(sub -> StockManager.getInstance().getTopTrader(FeedOrder.Feed_Type_begin, createObservableMResult(sub)));
    }

    /**
     * 获取其他类型的牛人圈信息
     */
    public static Observable<MResultsInfo<CommandPageArray<FeedOrder>>> fetchTopTraderOther(int feedType) {
        return Observable.create(sub -> StockManager.getInstance().getStockFeedOrderList(null, feedType, createObservableMResult(sub)));
    }

    /**
     * 获取某只股票的牛人圈信息
     */
    public static Observable<MResultsInfo<CommandPageArray<FeedOrder>>> fetchTopTraderOther(String stockId, int feedType) {
        return Observable.create(sub -> StockManager.getInstance().getStockFeedOrderList(stockId, feedType, createObservableMResult(sub)));
    }

    /**
     * 获取自选股列表
     */
    public static Observable<MResultsInfo<Void>> fetchFollowStock(boolean bLocal) {
        if (bLocal && SimulationStockManager.getInstance().followStockList != null) {
            Observable.just(new MResults.MResultsInfo<>());
        }
        return Observable.create(sub -> SimulationStockManager.getInstance().freshFollowStock(createObservableMResult(sub)));
    }

    /**
     * 删除自选股
     */
    public static Observable<MResultsInfo<Void>> deleteFollowStock(List<StockBrief> stocks) {
        return Observable.create(sub -> SimulationStockManager.getInstance().delFollowStock(stocks, createObservableMResult(sub)));
    }

    /**
     * 对自选股重新排序
     */
    public static Observable<MResultsInfo<Void>> resortFollowStock(List<StockBrief> stocks) {
        return Observable.create(sub -> SimulationStockManager.getInstance().sequenceFollowStocks(stocks, createObservableMResult(sub)));
    }

    /**
     * 获取直播标题
     */
    public static Observable<MResultsInfo<List<GMFLiveItem>>> fetchLiveHeadLineList(boolean bLocal) {

        if (bLocal) {

            MResultsInfo info = new MResultsInfo<>();
            return Observable.just(info.setData(Collections.<GMFLiveItem>emptyList()));
        }
        return Observable.create(sub -> StockManager.getInstance().getLiveArray(createObservableListMResult(sub)));
    }

    /**
     * 获取直播数据
     */
    public static Observable<MResultsInfo<CommandPageArray<GMFLiveItem.GMFLiveItemMore>>> fetchLiveDataList() {
        return Observable.create(sub -> StockManager.getInstance().getLiveData(createObservableMResult(sub)));
    }

    /**
     * 获取用户排行榜信息
     */
    public static Observable<MResultsInfo<Rank>> fetchUserLeaderBoard(String rankId) {
        return Observable.create(sub -> StockManager.getInstance().getRankDetail(rankId, createObservableMResult(sub)));
    }

    /**
     * 搜索股票
     */
    public static Observable<MResultsInfo<List<StockBrief>>> searchStockByKeyword(String keyword) {
        return Observable.create(sub -> StockManager.getInstance().searchKey(keyword, createObservableListMResult(sub)));
    }

    /**
     * 获取用户持仓列表
     */
    public static Observable<MResultsInfo<TradeUserPosition>> fetchUserPositionSimulation(int userID) {
        Observable<MResultsInfo<SimulationAccountMore>> observable = Observable.create(sub -> StockManager.getInstance().listStockPosition(String.valueOf(userID), createObservableMResult(sub)));
        return observable.map(it -> {
            return isSuccess(it) ? map(it, TradeUserPosition.create(it.data)) : cast(it, TradeUserPosition.class);
        });
    }

    /**
     * 获取用户持仓列表
     */
    public static Observable<MResultsInfo<TradeUserPosition>> fetchUserPositionReal(int fundID) {
        Observable<MResultsInfo<FundTradeInfo>> observable1 = Observable.create(sub -> TraderManager.getInstance().freshTradeInfo(fundID, createObservableMResult(sub)));
        Observable<MResultsInfo<FundTradeInfo>> observable2 = Observable.create(sub -> TraderManager.getInstance().listStockPositionList(fundID, createObservableMResult(sub)));
        return Observable.merge(observable1, observable2).skip(1).map(it -> {
            return isSuccess(it) ? map(it, TradeUserPosition.create(it.data)) : cast(it, TradeUserPosition.class);
        });
    }

    /**
     * 获取用户个股持仓详情
     */
    public static Observable<MResultsInfo<StockPosition>> fetchUserPositionDetailSimulation(int userID, String stockID, String range) {
        return Observable.create(sub -> StockManager.getInstance().getPositionDetail(String.valueOf(userID), stockID, range, createObservableMResult(sub)));
    }

    /**
     * 添加自选股
     */
    public static Observable<MResultsInfo<Void>> addFocusStock(StockSimple stock) {
        return Observable.create(sub -> SimulationStockManager.getInstance().addFollowStock(stock, createObservableMResult(sub)));
    }

    /**
     * 删除自选股
     */
    public static Observable<MResultsInfo<Void>> deleteFollowStock(StockBrief stock) {
        return Observable.create(sub -> SimulationStockManager.getInstance().delFollowStock(stock, createObservableMResult(sub)));
    }

    /**
     * 获取撤单记录
     */
    public static Observable<MResultsInfo<CommandPageArray<Order>>> fetchPendingOrderListSimulation() {
        return Observable.create(sub -> SimulationStockManager.getInstance().listLivingOrderList(createObservableMResult(sub)));
    }

    /**
     * 获取组合撤单记录
     */
    public static Observable<MResultsInfo<CommandPageArray<OrderV2>>> fetchPendingOrderListReal(int fundID) {
        return Observable.create(sub -> TraderManager.getInstance().listCancelOrderList(fundID, createObservableMResult(sub)));
    }

    /**
     * 获取用户成交记录
     */
    public static Observable<MResultsInfo<CommandPageArray<Order>>> fetchStockOrderListSimulation(int userID) {
        return Observable.create(sub -> StockManager.getInstance().listStockOrderList(String.valueOf(userID), createObservableMResult(sub)));
    }

    /**
     * 获取当日成交
     */
    public static Observable<MResultsInfo<CommandPageArray<OrderV2>>> fetchTodayOrderListReal(int fundID) {
        return Observable.create(sub -> TraderManager.getInstance().listTodayOrderList(fundID, createObservableMResult(sub)));
    }

    /**
     * 获取当日委托
     */
    public static Observable<MResultsInfo<CommandPageArray<OrderV2>>> fetchTodayDealOrderListReal(int fundID) {
        return Observable.create(sub -> TraderManager.getInstance().listTodayDealOrderList(fundID, createObservableMResult(sub)));
    }

    /**
     * 获取历史成交
     */
    public static Observable<MResultsInfo<CommandPageArray<OrderV2>>> fetchHistoryOrderListReal(int fundID) {
        return Observable.create(sub -> TraderManager.getInstance().listHistoryDealOrderList(fundID, createObservableMResult(sub)));
    }

    /**
     * 添加委托
     */
    public static Observable<MResultsInfo<Order>> addSimulationOrder(Order order) {
        return Observable.create(sub -> SimulationStockManager.getInstance().orderOrder(order, createObservableMResult(sub)));
    }

    /**
     * 添加委托
     */
    public static Observable<MResultsInfo<Void>> addRealOrderWithFundID(int fundID, Order order) {
        return Observable.create(sub -> TraderManager.getInstance().orderOrder(fundID, order, createObservableMResult(sub)));
    }

    /**
     * 进行下单风险监测
     */
    public static Observable<MResultsInfo<Void>> checkRealOrderWithFundID(int fundID, Order order) {
        return Observable.create(sub -> TraderManager.getInstance().riskOrder(fundID, order, createObservableMResult(sub)));
    }

    /**
     * 撤销虚拟盘委托
     */
    public static Observable<MResultsInfo<Void>> cancelSimulationOrder(Order order) {
        return Observable.create(sub -> SimulationStockManager.getInstance().cancelOrder(order, createObservableMResult(sub)));
    }

    /**
     * 查询虚拟盘的股票最大可买/可卖
     */
    public static Observable<MResultsInfo<Pair<Double, Double>>> queryTradeCountSimualtion(StockSimple stock, double price) {
        return Observable.create(sub -> SimulationStockManager.getInstance().qualifyStock(stock, price, createObservableMResult(sub)));
    }

    /**
     * 查询实盘的股票最大可买/可卖
     */
    public static Observable<MResultsInfo<Pair<Double, Double>>> queryTradeCountReal(int fundID, StockSimple stock, double price) {
        return Observable.create(sub -> TraderManager.getInstance().qualifyStock(fundID, stock, createObservableMResult(sub)));
    }

    /**
     * 撤销实盘委托
     */
    public static Observable<MResultsInfo<Void>> cancelRealOrderWithFundID(int fundID, Order order) {
        return Observable.create(sub -> TraderManager.getInstance().cancelOrder(fundID, order.orderId, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Stock>> fetchStockDetailInfo(boolean local, String stockID) {
        if (TextUtils.isEmpty(stockID)) {
            MResults.MResultsInfo<Stock> info = new MResults.MResultsInfo<>();
            info.setIsSuccess(false);
            info.msg = "股票ID不能为空";
            return Observable.just(info);
        }

        Observable<MResultsInfo<Stock>> observable = Observable.create(sub -> StockManager.getInstance().getStockInfo(local, stockID, createObservableMResult(sub)));
        observable = observable.map(it -> {
            it.isSuccess = it.data != null;
            return it;
        });
        return observable;
    }

    public static Observable<MResultsInfo<StockTline>> fetchStockTLineData(StockSimple stock, int authority, int tLineType) {
        Observable<MResultsInfo<StockTline>> observable = Observable.create(sub -> StockManager.getInstance().getStockTLine(stock, authority, tLineType, createObservableMResult(sub)));
        observable = observable.map(it -> {
            it.isSuccess = it.data != null;
            return it;
        });
        return observable;
    }

    public static Observable<MResultsInfo<StockKline>> fetchStockKLineData(Boolean local, StockSimple stock, int authority, int kLineType, int specType) {
        Observable<MResultsInfo<StockKline>> observable = Observable.create(sub -> StockManager.getInstance().getStockKLine(local, stock, authority, kLineType, specType, createObservableMResult(sub)));
        observable = observable.map(it -> {
            it.isSuccess = it.data != null;
            return it;
        });
        return observable;
    }


    public static Observable<MResultsInfo<Void>> fetchSimulationAccount(boolean bLocal) {
        int accountStatus = SimulationStockManager.getInstance().accountStatus;
        if (bLocal && accountStatus != SimulationStockManager.ACCOUNT_STATE_UNKNOW) {
            MResultsInfo<Void> info = new MResultsInfo<>();
            return Observable.just(info);
        } else {
            if (bLocal && SimulationStockManager.getInstance().mineAccountMore.user != null) {
                return Observable.just(new MResultsInfo<>());
            }
            Observable<MResultsInfo<SimulationAccountMore>> observable = Observable.create(sub -> SimulationStockManager.getInstance().listStockPosition(createObservableMResult(sub)));
            return observable.map(it -> {
                MResultsInfo<Void> ret = new MResultsInfo<>();
                ret.isSuccess = it.isSuccess;
                ret.errCode = it.errCode;
                ret.msg = it.msg;
                return ret;
            });
        }
    }

    public static Observable<MResultsInfo<Void>> createSimulationStockAccount() {
        return Observable.create(sub -> SimulationStockManager.getInstance().createAccount(createObservableMResult(sub)));
    }

    public static class TradeUserPosition {
        public static TradeUserPosition NULL = new TradeUserPosition();

        public List<? extends StockPosition> holdStocks;
        public List<? extends StockPosition> historyStocks;
        public double currentPosition;
        public double dayIncome;
        public double dayIncomeRatio;
        public double totalIncome;
        public double totalIncomeRatio;
        public double totalCapital;
        public double stockCapital;
        public double cashCapital;
        public TradeExtraFundReal extraFundReal = TradeExtraFundReal.NULL;

        public static TradeUserPosition create(SimulationAccountMore raw) {
            if (raw == null) return NULL;
            return new TradeUserPosition(raw);
        }

        public static TradeUserPosition create(FundTradeInfo raw) {
            if (raw == null) return NULL;
            return new TradeUserPosition(raw);
        }

        private TradeUserPosition() {
            this.holdStocks = Collections.emptyList();
            this.historyStocks = Collections.emptyList();
            this.currentPosition = 0D;
            this.dayIncome = 0D;
            this.dayIncomeRatio = 0D;
            this.totalIncome = 0D;
            this.totalIncomeRatio = 0D;
            this.totalCapital = 0D;
            this.stockCapital = 0D;
        }

        private TradeUserPosition(SimulationAccountMore raw) {
            this.holdStocks = opt(raw.holdStocks).or(Collections.emptyList());
            this.historyStocks = opt(raw.historyStocks).or(Collections.emptyList());
            this.currentPosition = safeGet(() -> raw.account.curPosition, 0D);
            this.dayIncome = safeGet(() -> raw.account.todayIncome, 0D);
            this.dayIncomeRatio = safeGet(() -> raw.account.todayIncomeRatio, 0D);
            this.totalIncome = safeGet(() -> raw.account.totalIncome, 0D);
            this.totalIncomeRatio = safeGet(() -> raw.account.totalIncomeRatio, 0D);
            this.totalCapital = safeGet(() -> raw.account.fortuneTurnover, 0D);
            this.stockCapital = safeGet(() -> raw.account.marketTurnover, 0D);
            this.cashCapital = safeGet(() -> raw.account.cashBalance, 0D);
        }

        private TradeUserPosition(FundTradeInfo raw) {
            this.holdStocks = opt(raw.holdStocks).or(Collections.emptyList());
            this.historyStocks = Collections.emptyList();
            this.currentPosition = safeGet(() -> raw.position, 0D);
            this.dayIncome = safeGet(() -> raw.dayIncome, 0D);
            this.dayIncomeRatio = safeGet(() -> raw.dayIncomeRatio, 0D);
            this.totalIncome = safeGet(() -> raw.income, 0D);
            this.totalIncomeRatio = safeGet(() -> raw.incomeRatio, 0D);
            this.totalCapital = safeGet(() -> raw.totalCapital, 0D);
            this.stockCapital = safeGet(() -> raw.capitalValue, 0D);
            this.cashCapital = safeGet(() -> raw.cashBanlance, 0D);
        }

        public boolean isNull() {
            return this == NULL;
        }

        public boolean isNotNull() {
            return !isNull();
        }
    }

    public static class TradeExtraFundReal {

        public static TradeExtraFundReal NULL = new TradeExtraFundReal();

        public double raisedCapital;
        public double traderCapital;
        public int runningDay;
        public int remainingDay;
        public double incomeSharePercent;
        public double warningLine;
        public double clearLing;
        public long startTime;
        public long stopTime;

        public static TradeExtraFundReal create(Fund raw) {
            if (raw == null) {
                return NULL;
            }
            return new TradeExtraFundReal(raw);
        }

        public TradeExtraFundReal() {
        }


        public TradeExtraFundReal(Fund raw) {
            this.raisedCapital = raw.raisedCapital;
            this.traderCapital = raw.traderInvest;
            this.runningDay = (int) raw.tradingDay;
            this.remainingDay = new BigDecimal(raw.remainingDays()).setScale(0, RoundingMode.UP).intValue();
            this.incomeSharePercent = raw.userProfitSharingRatio;

            this.warningLine = raw.earlyWarningRatio;
            this.clearLing = raw.stopLossRatio;
            this.startTime = raw.startTime;
            this.stopTime = raw.stopTime;
        }

        public boolean isNull() {
            return this == NULL;
        }

        public boolean isNotNull() {
            return !isNull();
        }
    }
}
