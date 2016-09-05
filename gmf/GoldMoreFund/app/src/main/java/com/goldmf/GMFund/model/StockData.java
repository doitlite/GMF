package com.goldmf.GMFund.model;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.model.LineData.KLineData;
import com.goldmf.GMFund.model.LineData.TLineData;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.protocol.KLineProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import static com.goldmf.GMFund.base.MResults.MResultsInfo.SafeOnResult;

/**
 * Created by cupide on 16/3/11.
 */
public class StockData {

    public static class StockTline {
        public StockSimple stock;
        public int authority;            //复权
        public int type;                //类型 Tline_Type_Day 等
        public List<TLineData> data = null;        //数据

        public StockTline(StockSimple s, int t, int a) {
            this.stock = s;
            this.type = t;
            this.authority = a;
        }
    }

    public static class StockKline extends CommandPageArray<KLineData> {
        public StockSimple stock;
        public int authority;           //复权
        public int type;                //类型 Tline_Type_Day 等
        public int specType;            //指标线类型

        public final List<KLineData> data() {
            return super.data();
        }

        public StockKline(StockSimple stock, int type, int authority, int specType) {
            super();

            this.stock = stock;
            this.type = type;
            this.authority = authority;
            this.specType = specType;

            this.build();
        }

        @Override
        public void build(){

            KLineProtocol p = new KLineProtocol(null);
            p.authority = this.authority;
            p.type = this.type;
            p.stock = this.stock;
            p.specType = this.specType;
            p.count = COMMON_COUNT;
            this.protocol = p;

            this.command = new CommandPageArray.CommandPage<KLineData>() {
                @Override
                public void onNext(PageProtocol<KLineData> p, PageArray<KLineData> array) {
                    KLineProtocol protocol = (KLineProtocol) p;

                    protocol.firstTS = 0;
                    if (array.isEmpty()) {
                        // 第一次
                        protocol.lastTS = 0;
                    } else {
                        protocol.lastTS = array.getLastTS();
                    }
                }

                @Override
                public void onPre(PageProtocol<KLineData> p, PageArray<KLineData> array) {
                    KLineProtocol protocol = (KLineProtocol) p;

                    if (array.isEmpty()) {
                        // 第一次
                        protocol.firstTS = 0;
                    } else {
                        protocol.firstTS = array.getFirstTS();
                    }
                    protocol.lastTS = 0;
                }
            };
        }


        /**
         * 外部不要调用
         */
        public final void getSpecData(int specType, final MResults<StockKline> results) {

            if(this.specDataValid(specType)){
                SafeOnResult(results, new MResults.MResultsInfo<StockKline>().setData(this));
                return;
            }
            else {
                long beginTime = this.pageArray.getFirstTS();
                long endTime = this.pageArray.getLastTS();

                //从server返回
                ComonProtocol.ComonCallback callback = new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        MyApplication.post(()->{
                            MResults.MResultsInfo<StockKline> info = protocol.buildRet();

                            if (ret.isJsonObject()) {

                                specData.add(specType);

                                JsonArray array = GsonUtil.getAsJsonArray(ret, "lines");
                                for (JsonElement temp :array){

                                    double traderTime = GsonUtil.getAsDouble(temp, "trade_time");
                                    for (int i = 0; i< data().size(); i++){
                                        KLineData kline = data().get(i);
                                        if(traderTime == kline.traderTime){
                                            kline.readFromJsonData(temp, specType);
                                            break;
                                        }
                                    }
                                }
                            }

                            MResults.MResultsInfo.SafeOnResult(results, info.setData(StockKline.this));
                        });
                    }
                };

                new ComonProtocol.Builder()
                        .url(CHostName.HOST2 + "quote/spec")
                        .params(ComonProtocol.buildParams(
                                "stock_id", this.stock.index,
                                "freq", String.valueOf(this.type),
                                "accum_adj_factor_type", String.valueOf(this.authority),
                                "begin", String.valueOf(beginTime),
                                "end", String.valueOf(endTime),
                                "spec_name", KLineData.getSpecString(specType)))
                        .callback(callback)
                        .build()
                        .startWork();
            }
        }

        private List<Integer> specData = new ArrayList<>();

        /**
         *
         * @param specType
         * @return
         */
        public final Boolean specDataValid(int specType){
            return this.specData.contains(specType);
        }

        private boolean lock = false;

        @Override
        public void getPrePage(final MResults<CommandPageArray<KLineData>> results){

            if(lock){
                SafeOnResult(results, new MResults.MResultsInfo<CommandPageArray<KLineData>>().setData(this));
            }
            else {
                lock = true;
                KLineProtocol p = (KLineProtocol) this.protocol;
                p.specType = this.specType;

                super.getPrePage(result -> {
                    if (result.isSuccess) {
                        this.specData.clear();
                        this.specData.add(specType);
                    }
                    SafeOnResult(results, result);

                    lock = false;
                });
            }
        }

        @Override
        public void getNextPage(final MResults<CommandPageArray<KLineData>> results){
            if(lock){
                SafeOnResult(results, new MResults.MResultsInfo<CommandPageArray<KLineData>>().setData(this));
            }else {
                lock = true;
                KLineProtocol p = (KLineProtocol) this.protocol;
                p.specType = this.specType;

                super.getNextPage(result -> {
                    if (result.isSuccess) {
                        this.specData.clear();
                        this.specData.add(specType);
                    }
                    SafeOnResult(results, result);

                    lock = false;
                });
            }
        }
    }

    private StockSimple stock;
    public int authority; //复权

    private StockKline dayKLine;     //日K线数据
    private StockKline weekKLine;    //周K线数据
    private StockKline monthKLine;   //月K线数据

    public StockData(StockSimple stock, int authority) {
        this.stock = stock;
        this.authority = authority;
    }

    private static final int COMMON_COUNT = 150;


    public final StockKline get(int kLineType) {

        switch (kLineType) {
            case KLineData.Kline_Type_Day:
                return this.dayKLine;
            case KLineData.Kline_Type_Week:
                return this.weekKLine;
            case KLineData.Kline_Type_Month:
                return this.monthKLine;
        }
        return null;
    }

    public final void put(StockKline stockKline) {

        if (stockKline == null)
            return;

        switch (stockKline.type) {
            case KLineData.Kline_Type_Day:
                this.dayKLine = stockKline;
                break;
            case KLineData.Kline_Type_Week:
                this.weekKLine = stockKline;
                break;
            case KLineData.Kline_Type_Month:
                this.monthKLine = stockKline;
                break;
        }
    }


}
