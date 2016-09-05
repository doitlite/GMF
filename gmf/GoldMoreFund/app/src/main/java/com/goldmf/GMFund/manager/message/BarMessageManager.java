package com.goldmf.GMFund.manager.message;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.RemaindFeed;
import com.goldmf.GMFund.protocol.MessageDetailProtocol;
import com.goldmf.GMFund.protocol.PostMessageProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.CommandPageArray.CommandPage;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.goldmf.GMFund.base.MResults.*;
import static com.goldmf.GMFund.base.MResults.MResultsInfo.SafeOnResult;
import static com.goldmf.GMFund.base.MResults.MResultsInfo.SuccessComRet;
import static com.goldmf.GMFund.manager.message.MessageSession.*;

/**
 * Created by cupide on 16/5/10.
 */
public class BarMessageManager extends MessageManager {

    public static String Key_BarMesssageManager_Donate = "BarMesssageManager_Donate";   //BarMessage Donate
    public static String Obj_BarMesssageManager_Score = "BarMesssageManager_Score";   //BarMessage Score

    private static int SESSION_HEAD_BASE_COMMAND = 0; //

    private HashMap<Integer, CommandPageArray<GMFMessage>> commands = new HashMap<>();
    private CommandPageArray<GMFMessage> baseCommand;

    private CommandPageArray<RemaindFeed> remaindFeedPage;//backup

    private HashMap<GMFMessage, Integer> donateDic = new HashMap<>();

    private Boolean bTimer = false;
    private Runnable runnable = () -> BarMessageManager.this.timerSendDonate();

    BarMessageManager(MessageSession session) {
        super(session);

        for (SessionHead head : session.headList) {
            if (head.headID == SESSION_HEAD_BASE_COMMAND) {
                baseCommand = this.buildSubCommand(head);
                break;
            }
        }

        if (baseCommand != null)
            baseCommand.getPrePage(null);
    }

    @Override
    void timerFreshMessageList() {
        return;
    }

    @Override
    public void sendMessage(SendMessage message) {
        throw new UnsupportedOperationException("不支持【回调方式】发送message！");
    }

    @Override
    public void sendMessage(SendMessage message, MResults<Void> results) {

        if (message == null || message.loading) {
            MResultsInfo.SafeOnResult(results, MResultsInfo.<Void>FailureComRet());
            return;
        }

        this.post(message, ret -> {

            if (ret.isSuccess && baseCommand != null) {
                this.baseCommand.addFromeTop(message);
                //fire out
                BarMessageManager.this.fire(Key_MesssageManager_List, null);

                HashMap<String, Object> param = new HashMap<>();
                param.put(Obj_MesssageManager_Message, message);
                param.put(Obj_SendMessage_Ret, true);
                BarMessageManager.this.fire(Key_MesssageManager_SendMessage, param);

                this.timerFreshMessageList();
            }

            MResultsInfo.SafeOnResult(results, ret);
        });
    }

    @Override
    public void cancelMessage(SendMessage message) {
        throw new IllegalArgumentException("不支持【撤回】message！，请用【删除】message");
    }

    @Override
    public void stop() {
        super.started = false;
        SessionManager.getInstance().endSubTimer();
    }

    @Override
    public void start() {
        if (!super.started) {
            super.started = true;

            this.timerFreshMessageList();
        }

        SessionManager.getInstance().beginSubTimer();
    }

    /**
     * 通过head获取page的数据
     *
     * @param head
     * @param results
     */
    public final void getCommandPage(SessionHead head, final MResults<CommandPageArray<GMFMessage>> results) {
        CommandPageArray<GMFMessage> command = this.commands.get(head.headID);

        if (command != null
                && command.data() != null
                && command.data().size() > 0) {
            MResultsInfo.SafeOnResult(results,
                    MResultsInfo.<CommandPageArray<GMFMessage>>SuccessComRet().setHasNext(true).setData(command));
        }

        if (command == null) {
            command = this.buildSubCommand(head);
        }

        command.getPrePage(results);
    }

    /**
     * 删除某个message
     *
     * @param message
     */
    public final void delMessage(GMFMessage message, final MResults<Void> results) {

        if (!GMFMessage.isValid(message)
                || this.baseCommand == null
                || message.getUser().isAbsent()
                || !MineManager.isMe(message.getUser().get().index)) {
            MResultsInfo.SafeOnResult(results, MResultsInfo.<Void>FailureComRet());
            return;
        }

        new CommonPostProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, "message/delete"))
                .postParams(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("session_id", this.session.sessionID)
                        .add("message_id", message.messageID))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        BarMessageManager.this.baseCommand.del(message);
                        BarMessageManager.this.fire(Key_MesssageManager_List, null);

                        SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 评论某个message.
     *
     * @param message
     * @param comment
     * @param results
     */
    public final void comment(GMFMessage message, RemaindFeed comment, int targetUserID, final MResults<Void> results) {

        message.localProcessAddComment();

        if (!GMFMessage.isValid(message) || !RemaindFeed.isValid(comment)) {
            MResultsInfo.SafeOnResult(results, MResultsInfo.<Void>FailureComRet());
            return;
        }

        new CommonPostProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, "message/comment"))
                .postParams(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("session_id", this.session.sessionID)
                        .add("message_id", message.messageID)
                        .add("comment", comment.text)
                        .add("target_user_id", targetUserID))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        if (ret != null && ret.isJsonObject()) {
                            comment.readFromJsonData(GsonUtil.getAsJsonObject(ret, "comment_record"));
                            message.readFromJsonData(GsonUtil.getAsJsonObject(ret, "message_record"));
                            BarMessageManager.this.fire(Key_MesssageManager_List, null);
                        }
                        SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 删除某个message的评论
     *
     * @param message
     * @param comment
     * @param results
     */
    public final void delComment(GMFMessage message, RemaindFeed comment, final MResults<Void> results) {

        message.localProcessDelComment();

        if (!GMFMessage.isValid(message)
                || this.baseCommand == null
                || !RemaindFeed.isValid(comment)
                || !MineManager.isMe(comment.user.index)) {
            MResultsInfo.SafeOnResult(results, MResultsInfo.<Void>FailureComRet());
            return;
        }

        new CommonPostProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, "message/comment-delete"))
                .postParams(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("session_id", this.session.sessionID)
                        .add("message_id", message.messageID)
                        .add("comment_id", comment.fid))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        message.commentList.del(comment);

                        if (ret != null && ret.isJsonObject()) {
                            message.readFromJsonData(GsonUtil.getAsJsonObject(ret, "message_record"));
                            BarMessageManager.this.fire(Key_MesssageManager_List, null);
                        }
                        SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 打赏某个message，并等待回复
     *
     * @param message
     * @param score
     * @param results
     */
    public final void donate(GMFMessage message, int score, final MResults<Void> results) {
        message.localProcessDonate(score);
        BarMessageManager.this.fire(Key_MesssageManager_List, null);

        this.actionMessage(message, RemaindFeed.MessageAction_Donate, score, results);
    }

    /**
     * 打赏某个message 1分，不等待回复，可以快速调用
     *
     * @param message
     */
    public final void donate(GMFMessage message) {

        this.donate(message, 1);
    }

    /**
     * 打赏某个message score分，不等待回复，可以快速调用
     *
     * @param message
     */
    public final void donate(GMFMessage message, int score) {

        message.localProcessDonate(score);
        BarMessageManager.this.fire(Key_MesssageManager_List, null);

        Integer donate = this.donateDic.get(message);
        this.donateDic.put(message, (donate == null) ? score : donate + score);

        this.beginSubTimer();
    }

    private void beginSubTimer(){
        if(!bTimer){
            bTimer = true;

            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 5 * 1000);
        }
    }


    /**
     * 强制打赏刷新
     */
    public void commitDonateMessage(GMFMessage message, MResults<Void> results) {
//        NSNumber * number = [self.donateDic objectForKey:message];
//
//        [self.donateDic removeObjectForKey:message];
//
//        if(number.intValue > 0)
//        {
//            [self actionMessage:message actionType:MessageAction_Donate score:number.intValue callback:callback];
//        }
//        else
//        {
//            SafeCallback(callback, SuccessComRet);
//        }

        if (message == null) {
            SafeOnResult(results, MResultsInfo.FailureComRet());
            return;
        }

        Integer number = this.donateDic.get(message);
        this.donateDic.remove(message);

        if (number == null || number <= 0) {
            SafeOnResult(results, MResultsInfo.SuccessComRet());
            return;
        }

        this.actionMessage(message, RemaindFeed.MessageAction_Donate, number, results);
    }

    /**
     * 解锁某个message。
     *
     * @param message
     * @param results
     */
    public final void unlock(GMFMessage message, final MResults<Void> results) {

        if (!GMFMessage.isValidIntelligence(message)
                || message.intelligence.bMyUnlock) {
            MResultsInfo.SafeOnResult(results, MResultsInfo.<Void>FailureComRet());
            return;
        }

        this.actionMessage(message, RemaindFeed.MessageAction_Unlock, 0, results);
    }

    /**
     * 评价某个message
     *
     * @param message
     * @param like
     * @param results
     */
    public final void judge(GMFMessage message, boolean like, final MResults<Void> results) {

        if (!GMFMessage.isValidIntelligence(message)
                || message.intelligence.myAction != GMFMessage.Intelligence.Intelligence_Action_Waiting) {
            MResultsInfo.SafeOnResult(results, MResultsInfo.<Void>FailureComRet());
            return;
        }

        new CommonPostProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, "message/evaluate"))
                .postParams(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("session_id", this.session.sessionID)
                        .add("message_id", message.messageID)
                        .add("like", like))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        if (ret != null && ret.isJsonObject()) {
                            message.readFromJsonData(GsonUtil.getAsJsonObject(ret, "message_record"));
                            BarMessageManager.this.fire(Key_MesssageManager_List, null);
                        }
                        SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 获取某个message的详情页
     *
     * @param message
     * @param results
     */
    public final void freshMore(GMFMessage message, final MResults<GMFMessage> results) {


        if (message.commentList == null) {

            MessageDetailProtocol protocol = new MessageDetailProtocol(null);
            protocol.message = message;

            message.commentList = new CommandPageArray.Builder<RemaindFeed>()
                    .pageProtocl(protocol)
                    .classOfT(RemaindFeed.class)
                    .cgiUrl(CHostName.HOST2 + "message/message-detail")
                    .cgiParam(new ComonProtocol.ParamParse.ParamBuilder()
                            .add("message_id", message.messageID)
                            .add("session_id", this.session.sessionID))
                    .commandPage(20)
                    .build();
        }

        this.sendDonate(message, result1 -> {

            message.commentList.getPrePage(result -> {
                BarMessageManager.this.fire(Key_MesssageManager_List, null);

                MResultsInfo.SafeOnResult(results, MResultsInfo.<GMFMessage>COPY(result).setData(message));
            });
        });
    }

    /**
     * 获取对应的session的提醒page
     *
     * @param results
     */
    public final void getMyRemainPage(final MResults<CommandPageArray<RemaindFeed>> results) {

//        if (this.remaindFeedPage == null)
        {
            this.remaindFeedPage = new CommandPageArray.Builder<RemaindFeed>()
                    .classOfT(RemaindFeed.class)
                    .cgiUrl(CHostName.HOST2 + "feed/remind-list")
                    .cgiParam(new ComonProtocol.ParamParse.ParamBuilder()
                            .add("session_id", this.session.sessionID))
                    .commandTS()
                    .build();
        }

        this.remaindFeedPage.getPrePage(result -> {
            this.session.clearNumber();

            MResultsInfo.SafeOnResult(results,
                    MResultsInfo.<CommandPageArray<RemaindFeed>>COPY(result).setData(result.data));
        });
    }

    private void sendDonate(GMFMessage message, final MResults<Void> results) {

        if (this.donateDic.containsKey(message)) {
            int donate = this.donateDic.get(message);
            this.donateDic.remove(message);

            if (message != null && donate != 0) {
                this.actionMessage(message, RemaindFeed.MessageAction_Donate, donate, results);
                return;
            }
        }

        SafeOnResult(results, SuccessComRet());
    }

    private void timerSendDonate() {
        bTimer = false;

        if (this.donateDic.size() > 0) {
            GMFMessage message = this.donateDic.keySet().iterator().next();
            int donate = this.donateDic.get(message);

            this.donateDic.remove(message);

            if (message != null && donate != 0) {

                this.actionMessage(message, RemaindFeed.MessageAction_Donate, donate, null);
            }

            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 5 * 1000);
        }
    }

    private CommandPageArray<GMFMessage> buildSubCommand(SessionHead head) {
        if (head == null || head.cgiUrl == null || head.cgiUrl.length() == 0)
            return null;

        CommandPageArray.Builder<GMFMessage> builder = new CommandPageArray.Builder<GMFMessage>()
                .classOfT(GMFMessage.class)
                .cgiUrl(CHostName.HOST2 + "message/message-list?" + head.cgiUrl)
                .cgiParam(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("session_id", this.session.sessionID))
                .supportDel(head.headID == SESSION_HEAD_BASE_COMMAND)   //是否支持删除?
                .bSorted(head.headID != SESSION_HEAD_BASE_COMMAND);     //是否顺序的?

        if (head.headID == SESSION_HEAD_BASE_COMMAND) {
            builder.commandTS();
        } else {
            builder.commandPage(50);
        }
        CommandPageArray<GMFMessage> page = builder.build();

        this.commands.put(head.headID, page);

        return page;
    }

    private void actionMessage(GMFMessage message,
                               int messageActionType,
                               int score,
                               final MResults<Void> results) {

        String url;
        if (messageActionType == RemaindFeed.MessageAction_Donate) {
            url = "message/donate";

            HashMap<String, Object> param = new HashMap<>();
            param.put(Obj_BarMesssageManager_Score, Integer.valueOf(score));
            BarMessageManager.this.fire(Key_BarMesssageManager_Donate, param);

        } else if (messageActionType == RemaindFeed.MessageAction_Unlock) {
            url = "message/unlock";
        } else {
            throw new IllegalArgumentException("messageActionType{" + messageActionType + "} is err!");
        }

        new CommonPostProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, url))
                .postParams(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("session_id", this.session.sessionID)
                        .add("message_id", message.messageID)
                        .add("score", score))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        if (ret != null && ret.isJsonObject()) {
                            message.readFromJsonData(GsonUtil.getAsJsonObject(ret, "message_record"));
                            BarMessageManager.this.fire(Key_MesssageManager_List, null);
                        }
                        SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    private void post(SendMessage message, final MResults<Void> results) {

        handler.post(() -> {
                    PostMessageProtocol p = new PostMessageProtocol(new ProtocolCallback() {
                        @Override
                        public void onFailure(ProtocolBase protocol, int errCode) {
                            MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                        }

                        @Override
                        public void onSuccess(ProtocolBase protocol) {
                            MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                        }
                    });
                    p.message = message;
                    p.sessionID = getSessionID();
                    p.bSnsSever = true;
                    p.startWork();
                }
        );
    }
}
