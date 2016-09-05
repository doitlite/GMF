package com.goldmf.GMFund.manager.message;

import android.text.TextUtils;

import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.util.NumberUtil;
import com.goldmf.GMFund.util.SecondUtil;

/**
 * Created by cupide on 15/11/30.
 */
public class SendMessage extends GMFMessage {

    public boolean loading;
    public boolean local = true;

    public boolean isSuccess() {
        return !TextUtils.isEmpty(messageID);
    }

//    @Override
//    public Optional<User> getUser() {
//        return new Optional<>(MineManager.getInstance().getmMe());
//    }

    public SendMessage(String content) {

        super.templateType = Message_Text;
        super.content = content;
        this.loading = false;
        this.localID = SendMessage.createLocalID();
        super.user = MineManager.getInstance().getmMe();
    }

    static int count = 1;
    static String currentTimeStr = null;

    private static String createLocalID() {

        if (currentTimeStr == null) {
            currentTimeStr = String.valueOf(SecondUtil.currentSecond());
        }
        count++;

        String mineIndex = "0";
        if (MineManager.getInstance().getmMe() != null) {
            mineIndex = String.valueOf(MineManager.getInstance().getmMe().index);
        }

        return mineIndex + "_" + currentTimeStr + "_" + count;
    }

    public boolean same(PageItemIndex index) {

        boolean same = super.same(index);

        if (!same) {
            if (index instanceof GMFMessage
                    && ((GMFMessage) index).localID != null
                    &&  ((GMFMessage) index).localID.length() > 0
                    && this.localID != null
                    && this.localID.length() > 0) {
                GMFMessage message = (GMFMessage) index;
                return this.localID.equals(message.localID);
            }
        }
        return same;
    }
}
