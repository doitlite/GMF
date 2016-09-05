package com.goldmf.GMFund.manager.message;

import android.os.Handler;
import android.os.Looper;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.message.MessageSession.SessionText;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.protocol.PostMessageProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by cupide on 15/11/28.
 */
public class MessageManager extends BaseManager {

    public static String Key_MesssageManager_List = "MesssageManager_List";
    public static String Key_MesssageManager_UploadImage = "MesssageManager_UploadImage";
    public static String Key_MesssageManager_SendMessage = "MesssageManager_SendMessage";

    public static String Obj_MesssageManager_Message = "MesssageManager_Message";   //SendMessage
    public static String Obj_UploadImage_Percent = "UploadImage_Percent";           //Double
    public static String Obj_SendMessage_Ret = "SendMessage_Ret";                   //Boolean


    private String getMessageManagerDataKey() {
        String sGMFMessageManagerData = "GMFMessageManagerData";   //list
        return sGMFMessageManagerData + this.getSessionID();
    }

    protected MessageSession session;
    protected boolean started = false;
    protected Handler handler = new Handler(Looper.getMainLooper());
    private long lastUpdateTime;
    private SendMessage localMessage;

    private CommandPageArray<GMFMessage> command;


    public final SendMessage getLocalMessage(){
        return localMessage;
    }

    public final String getSessionID() {
        return this.session.sessionID;
    }

    public final boolean isMore() {
        return this.command.getMore();
    }

    public final long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    boolean getStarted() {
        return this.started;
    }

    /**
     * 按照类型修改本地数据（内存缓存）草稿箱
     *
     * @param sendMessage
     */
    public final void saveLocalMessage(SendMessage sendMessage) {
        this.localMessage = sendMessage;

        if (sendMessage == null) {
            if (this.messageList() != null && this.messageList().size() > 0) {
                GMFMessage last = this.messageList().get(this.messageList().size() - 1);
                if (last != null) {
                    if (last instanceof SendMessage) {
                        SendMessage message = (SendMessage) last;

                        getSession().editLocalText(SessionText.buildBriefText(message));
                    } else {
                        getSession().editLocalText(null);
                    }
                }
            }
        } else {
            getSession().editLocalText(SessionText.buildBriefText(sendMessage));
        }
    }


    MessageManager(MessageSession session) {

        if (session == null) throw new IllegalArgumentException("session == null");

        this.session = session;
        this.loadLocalData();
        NotificationCenter.logoutSubject.subscribe(aVoid -> {
            //删除本地数据
            ModelSerialization.removeByKey(this.getMessageManagerDataKey(), GMFMessage[].class, true);

            this.stop();
        });

        this.command = new CommandPageArray.Builder<GMFMessage>()
                .classOfT(GMFMessage.class)
                .cgiUrl(CHostName.HOST1 + "message/message-list")
                .cgiParam(new ComonProtocol.ParamParse.ParamBuilder()
                        .add("session_id", this.session.sessionID))
                .commandTS()
                .build();
    }


    /**
     * 开启定时刷新
     */
    public void start() {

        if (!this.started) {
            this.started = true;

            this.timerFreshMessageList();
        }

        SessionManager.getInstance().beginSubTimer();
    }

    /**
     * 关闭定时刷新
     */
    public void stop() {

        this.started = false;

        SessionManager.getInstance().endSubTimer();

        //saveData
        if(this.command.data().size() > 0){
            JsonObject data = new JsonObject();
            JsonArray messageList = new JsonArray();
            for (GMFMessage message : this.command.data()){
                messageList.add(message.jsonData);
            }
            data.add("message_list", messageList);

            JsonObject time = new JsonObject();
            time.add("first_ts", new JsonPrimitive(this.command.getPage().getFirstTS()));
            time.add("last_ts", new JsonPrimitive(this.command.getPage().getLastTS()));
            time.add("more", new JsonPrimitive(this.command.getPage().getMore()));
            data.add("meta", time);

            ModelSerialization.saveJsonByKey(data, getMessageManagerDataKey());
        }
    }

    /**
     * 获取message列表
     *
     * @return
     */
    public List<GMFMessage> messageList() {
        return this.command.data();
    }

    /**
     * 获取绑定的session
     *
     * @return
     */
    public MessageSession getSession() {

        return this.session;
    }


    /**
     * 刷新上一页
     *
     * @param results
     */
    public final void freshOld(final MResults<CommandPageArray<GMFMessage>> results) {

        int count = this.command.data().size();

        this.command.getPrePage(result -> {

            if (MResults.MResultsInfo.isSuccess(result)) {
                if (count != this.command.data().size()) {
                    MessageManager.this.fire(Key_MesssageManager_List, null);
                }
            }

            MResults.MResultsInfo.SafeOnResult(results, result);

        });
    }

    void timerFreshMessageList() {

        int count = this.command.data().size();

        this.command.getNextPage(result -> {

            if (MResults.MResultsInfo.isSuccess(result)) {
                if (count != this.command.data().size()) {
                    MessageManager.this.fire(Key_MesssageManager_List, null);
                }

                this.session.clearNumber();
            }
        });
    }

    /**
     * 发送消息 with 回调
     *
     * @param message
     */
    public void sendMessage(SendMessage message, final MResults<Void> results) {

        if (message == null || message.loading)
            return;

        this.sendSynMessage(message, ret -> {

            if (ret.isSuccess) {
                MessageManager.this.timerFreshMessageList();
            }

            MResults.MResultsInfo.SafeOnResult(results, ret);
        });
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void sendMessage(SendMessage message) {

        if (message == null || message.loading)
            return;

        //处理重发机制
        if (this.command.data().contains(message)) {
            //重发
            assert (message.loading == false);
        } else {
            this.command.addToBottom(message);
        }

        this.editLoadingMessage(message);

        super.fire(Key_MesssageManager_List, null);

        this.sendSynMessage(message, ret -> {

            this.saveLocalMessage(null);

            MessageManager.this.fireMessageRet(message, ret.isSuccess);
        });

    }

    /**
     * 取消发送，只对message是 非loading and 非success 生效
     *
     * @param message
     */
    public void cancelMessage(SendMessage message) {

        assert (!message.loading && message.isSuccess() == false);

        if (!message.loading && message.isSuccess() == false) {
            this.command.del(message);

            super.fire(Key_MesssageManager_List, null);
        }
    }


    private void sendSynMessage(SendMessage message, final MResults<Void> results) {

        message.local = false;

        if (message instanceof UpImageMessage) {
            UpImageMessage imageMessage = (UpImageMessage) message;

            Action1<Double> progressAction = (percent) -> MessageManager.this.editMessagePercent(imageMessage, percent);

            Action1<Boolean> completionAction = (success) -> {
                if (success) {

                    MessageManager.this.post(message, results);

                } else {

                    MResults.MResultsInfo.SafeOnResult(results, ProtocolBase.buildErr(10, "系统繁忙，请稍后重试"));

                }
            };

            imageMessage.uploadFile(progressAction, completionAction);

        } else {

            MessageManager.this.post(message, results);
        }
    }

    private void editLoadingMessage(SendMessage message) {

        message.loading = false;

        MessageManager.this.saveLocalMessage(null);
    }

    private void editMessagePercent(UpImageMessage message, double percent) {


        HashMap<String, Object> param = new HashMap<>();
        param.put(Obj_MesssageManager_Message, message);
        param.put(Obj_UploadImage_Percent, message.percent);
        MessageManager.this.fire(Key_MesssageManager_UploadImage, param);
    }

    private void fireMessageRet(SendMessage message, boolean success) {
        message.loading = false;

        this.saveLocalMessage(null);

        HashMap<String, Object> param = new HashMap<>();
        param.put(Obj_MesssageManager_Message, message);
        param.put(Obj_SendMessage_Ret, success);
        MessageManager.this.fire(Key_MesssageManager_SendMessage, param);
    }

    private void loadLocalData() {
        {
            JsonElement data = ModelSerialization.loadJsonByKey(getMessageManagerDataKey(), true);
            if (data != null && data.isJsonObject()) {

                this.command.addArrayFromTop(PageProtocol.ParsePageArray(data, GMFMessage.class, false, null, null));
            }
        }
    }

    private void post(SendMessage message, final MResults<Void> results) {

        handler.post(() -> {
                    PostMessageProtocol p = new PostMessageProtocol(new ProtocolCallback() {
                        @Override
                        public void onFailure(ProtocolBase protocol, int errCode) {
                            MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                        }

                        @Override
                        public void onSuccess(ProtocolBase protocol) {
                            MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
                        }
                    });
                    p.message = message;
                    p.sessionID = MessageManager.this.getSessionID();
                    p.startWork();
                }
        );
    }


}
