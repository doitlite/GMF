package com.goldmf.GMFund.manager.stock;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.discover.FocusInfo;
import com.goldmf.GMFund.model.Feed;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 16/3/12.
 */
public class TopTrader extends CommandPageArray<FeedOrder> {

    public int feedType = FeedOrder.Feed_Type_begin;
    public List<FocusInfo> focusList = new ArrayList<>();

    public TopTrader() {
        super(new CommandPageArray.Builder<FeedOrder>()
                .classOfT(FeedOrder.class)
                .cgiUrl(CHostName.HOST2 + "page/top-trader")
                .commandTS());
    }

    @Override
    public  void getNextPage(final MResults<CommandPageArray<FeedOrder>> results){
        super.protocol.pageCgiParam = ComonProtocol.buildParams("type", String.valueOf(this.feedType)).getParams();

        super.getNextPage(result -> {
            if (result.isSuccess && result.ret != null) {

                if (result.ret.isJsonObject()) {

                    TopTrader.this.focusList.clear();
                    TopTrader.this.focusList.addAll(
                            FocusInfo.translate(
                                    GsonUtil.getAsJsonArray(result.ret, "ranking_profile_info")));
                }
            }

            //返回
            MResults.MResultsInfo.SafeOnResult(results, result);
        });
    }

    @Override
    public void getPrePage(final MResults<CommandPageArray<FeedOrder>> results){

        super.protocol.pageCgiParam = ComonProtocol.buildParams("type", String.valueOf(this.feedType)).getParams();

        super.getPrePage(result -> {
            if (result.isSuccess && result.ret != null) {

                if (result.ret.isJsonObject()) {

                    TopTrader.this.focusList.clear();
                    TopTrader.this.focusList.addAll(
                            FocusInfo.translate(
                                    GsonUtil.getAsJsonArray(result.ret, "ranking_profile_info")));
                }
            }

            //返回
            MResults.MResultsInfo.SafeOnResult(results, result);
        });
    }
}
