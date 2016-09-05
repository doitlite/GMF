package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.manager.message.BarMessageManager;
import com.goldmf.GMFund.manager.message.MessageSession.SessionHead;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.protocol.base.CommandPageArray;

import rx.Observable;

/**
 * Created by yale on 16/5/12.
 */
public class CircleController {
    private CircleController() {
    }


    public static Observable<MResults.MResultsInfo<CommandPageArray<GMFMessage>>> getCommandPageArray(BarMessageManager manager, SessionHead head) {
        return Observable.create(sub -> manager.getCommandPage(head, MResultExtension.createObservablePageArrayMResult(sub)));
    }
}
