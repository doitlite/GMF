package com.goldmf.GMFund.manager.message;

import android.os.Handler;
import android.os.Looper;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.common.RedPoint;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.Feed;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action1;

import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;

/**
 * Created by cupide on 15/11/28.
 */
public class SessionManager extends BaseManager {

    private static String sGMFSessionManagerListJsonData = "GMFSessionManagerListJsonData";   //json
    private static String sGMFSnsSessionManagerListJsonData = "GMFSnsSessionManagerListJsonData";   //json

    public static String Key_SessionManager_SessionList = "SessionManager_SessionList";
    public static String Key_SessionManager_RedPoint = "SessionManager_RedPoint";

    private class SessionList {

        private int redPoint;
        public RedPoint sessionNew;

        private HashMap<String, MessageSession> data = new HashMap<>();         //data
        private ArrayList<MessageSession> groupList = new ArrayList<>();        //groupList
        boolean bSnsServer = false;


        SessionList(boolean snsServer) {
            this.bSnsServer = snsServer;
            this.sessionNew = new RedPoint("message", snsServer);

            //读取本地数据
            this.loadLocalData();
        }

        List<MessageSession> list() {
            return this.groupList;
        }

        private String getGMFSessionManagerListDataKey() {
            if (this.bSnsServer)
                return sGMFSnsSessionManagerListJsonData;
            else
                return sGMFSessionManagerListJsonData;
        }

        private void loadLocalData() {

            {
                JsonElement ret = ModelSerialization.loadJsonByKey(getGMFSessionManagerListDataKey(), true);
                if (ret != null && ret.isJsonObject()) {
                    this.loadSessionListData(ret);
                }
            }
        }

        private void loadSessionListData(JsonElement ret) {
            {
                JsonArray groupList = GsonUtil.getAsJsonArray(ret, "group_list");
                if (groupList != null && groupList.size() > 0) {

                    for (JsonElement element : groupList) {
                        if (element.isJsonObject()) {
                            JsonObject dic = element.getAsJsonObject();

                            String groupID = GsonUtil.getAsString(dic, "id");
                            SessionGroup group = SessionList.this.getSessionGroup(groupID);
                            if (group == null) {
                                group = new SessionGroup(groupID);
                                data.put(groupID, group);
                            }

                            group.readFromJsonData(dic);
                            group.removeAll();
                        }
                    }

                }
            }
            JsonArray array = GsonUtil.getAsJsonArray(ret, "session_list");
            if (array != null) {

                ArrayList<MessageSession> tmpGroupList = new ArrayList<>();
                if (array.size() > 0) {
                    for (JsonElement element : array) {
                        if (element.isJsonObject()) {
                            JsonObject dic = element.getAsJsonObject();

                            String sessionID = GsonUtil.getAsString(dic, "id");
                            String groupID = GsonUtil.getAsString(dic, "group_id");
                            MessageSession session = SessionList.this.getSession(sessionID);
                            if (session == null) {
                                session = new MessageSession(sessionID);
                                data.put(sessionID, session);
                            }
                            session.readFromJsonData(dic);

                            //groupID
                            SessionGroup group = SessionList.this.getSessionGroup(groupID);
                            if (group != null) {
                                if (!tmpGroupList.contains(group)) {
                                    tmpGroupList.add(group);
                                }
                                group.add(session);
                            } else {
                                tmpGroupList.add(session);
                            }
                        }
                    }
                }

                SessionList.this.groupList.clear();
                SessionList.this.groupList.addAll(tmpGroupList);

                tmpGroupList.clear();
                tmpGroupList = null;
            }

            //统计小红点
            countRedPoint();
        }


        final void countRedPoint() {

            int redPointCount = 0;
            for (MessageSession session : this.groupList) {

                if(!(session instanceof SessionGroup)) {
                    redPointCount += session.number;
                    redPointCount += session.toReadNumber;
                }
            }

            if ((redPointCount > 0) != (this.redPoint > 0)) {
                this.redPoint = redPointCount;

                handler.post(() -> {
                    SessionManager.this.fire(Key_SessionManager_RedPoint, null);
                });
            }

            this.redPoint = redPointCount;
            Logger.d("redPoint:{%d}\n", this.redPoint);
        }


        private void freshSessionList(Map<String, String> params, final MResults<JsonElement> results) {
            new ComonProtocol.Builder()
                    .url((bSnsServer ? CHostName.HOST2 : CHostName.HOST1) + "message/session-list")
                    .params(params)
                    .callback(new ComonProtocol.ComonCallback() {
                        @Override
                        public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                            MResults.MResultsInfo.SafeOnResult(results, protocol.<JsonElement>buildRet());
                        }

                        @Override
                        public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                            if (ret != null && ret.isJsonObject()) {

                                loadSessionListData(ret);

                                //查看下是否需要刷新MessageManager
                                for (MessageSession temp : SessionList.this.data.values()) {
                                    MessageManager messageManager = SessionManager.this.messageManagerList.get(temp.sessionID);
                                    if (messageManager != null) {
                                        if (messageManager.getStarted() && (messageManager.getLastUpdateTime() < temp.updateTime)) {
                                            messageManager.timerFreshMessageList();
                                        }
                                    }
                                }

                                SessionManager.this.fire(Key_SessionManager_SessionList, null);

                                //saveData
                                ModelSerialization.saveJsonByKey(ret, getGMFSessionManagerListDataKey(), true);
                            }


                            MResults.MResultsInfo<JsonElement> info = protocol.buildRet();
                            info.data = ret;
                            MResults.MResultsInfo.SafeOnResult(results, info);
                        }
                    })
                    .build()
                    .startWork();
        }

        private SessionGroup getSessionGroup(String groupID) {

            MessageSession session = data.get(groupID);
            if (session instanceof SessionGroup) {
                return (SessionGroup) session;
            }
            return null;
        }

        private SessionGroup getSessionGroup(String linkID, String groupID) {

            for (MessageSession temp : this.data.values()) {
                if (temp.sessionID.equals(groupID) || temp.linkID.equals(linkID)) {

                    if (temp instanceof SessionGroup) {
                        return (SessionGroup) temp;
                    }
                }
            }
            return null;
        }

        private MessageSession getSession(String sessionID) {

            return data.get(sessionID);
        }

        private MessageSession getSession(int messageType, String linkID, String sessionID) {

            for (MessageSession temp : this.data.values()) {
                if ((temp.messageType == messageType) &&
                        (temp.sessionID.equals(sessionID) || temp.linkID.equals(linkID))) {

                    return temp;
                }
            }
            return null;
        }

        private void timerFreshNewTips() {

            sessionNew.freshRedPoint(result -> {
                if (result.isSuccess) {

                    if (sessionNew.number > 0) {
                        freshSessionList(null, result1 -> {
                            if (result1.isSuccess) {
                                SessionList.this.sessionNew.clear();

                                SessionManager.getInstance().handler.postDelayed(() -> {
                                    //发现有新消息的时候,2s后重新调用一次新消息
                                    timerFreshNewTips();

                                }, 2000L);
                            }
                        });
                    }
                }
            });
        }

        private void clear() {
            sessionNew = new RedPoint("message", bSnsServer);
            data.clear();
            groupList.clear();
            redPoint = 0;

            //删除本地数据
            ModelSerialization.removeJsonByKey(this.getGMFSessionManagerListDataKey(), true);
        }

        private void clearRedPoint(String sessionID) {
//            if (this.redPoint > 0) {
                this.sessionNew.clear(buildParams("session_id", sessionID).getParams());

                this.redPoint = 0;
//            }
        }

        private void clearGroupRedPoint(String groupID) {
//            if (this.redPoint > 0) {
                this.sessionNew.clear(buildParams("group_id", groupID).getParams());

                this.redPoint = 0;
//            }
        }
    }


    private static int SESSIONMANAGER_TIME = 20 * 1000;
    private static int MESSAGEMANAGER_TIME = 5 * 1000;

    private HashMap<String, MessageManager> messageManagerList = new HashMap<>();   //消息managerlist

    private SessionList snsSessionListData; //sns server上的sessionlist（BarMessageSession）
    private SessionList sessionListData;    //sessionlist（MessageSession）

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            timerFreshNewTips();
        }
    };
    private CommandPageArray<Feed> personalPage;//backup


    /**
     * 获取小红点的个数（ > 0，则展示消息icon上的小红点）
     *
     * @return
     */
    public final int getRedPoint() {
        return snsSessionListData.redPoint + sessionListData.redPoint;
    }

    /**
     * 获取sessionList
     *
     * @return
     */
    public final List<MessageSession> getSnsSessionList() {
        return snsSessionListData.list();
    }

    /**
     * 获取sessionList
     *
     * @return
     */
    public final List<MessageSession> getSessionList() {
        return sessionListData.list();
    }

    /**
     * 静态方法
     */
    private static SessionManager manager = new SessionManager();

    public static SessionManager getInstance() {
        return manager;
    }

    private SessionManager() {

        snsSessionListData = new SessionList(true);
        sessionListData = new SessionList(false);

        if (MineManager.getInstance().isLoginOK()) {

            handler.post(runnable);
        }


        NotificationCenter.logoutSubject.subscribe(aVoid -> {

            //logout 停止定时器
            handler.removeCallbacks(runnable);

            SessionManager.this.sessionListData.clear();
            SessionManager.this.snsSessionListData.clear();
            SessionManager.this.messageManagerList.clear();

            SessionManager.this.fire(Key_SessionManager_SessionList, null);
            SessionManager.this.fire(Key_SessionManager_RedPoint, null);
        });

        NotificationCenter.loginSubject
                .subscribe(nil -> {
                    //login启动定时器
                    handler.removeCallbacks(runnable);
                    handler.post(runnable);
                });
    }

    private int delayTime = SESSIONMANAGER_TIME;

    public final void smartClearRedPoint(MessageSession session) {
        if (session != null) {
            if (session instanceof SessionGroup) {
                clearGroupRedPoint((SessionGroup) session);
            } else {
                clearRedPoint(session);
            }

        }
    }

    /**
     * 清除某个session的小红点
     *
     * @param session
     */
    public final void clearRedPoint(MessageSession session) {

        if (MessageSession.isBarSession(session.linkID)) {
            this.snsSessionListData.clearRedPoint(session.sessionID);
        } else {
            this.sessionListData.clearRedPoint(session.sessionID);
        }
        if (session.number > 0) {
            session.clearNumber();
        } else {
            session.clearToReadNumber();
        }
    }

    /**
     * 清除某个sessionGroup的小红点
     *
     * @param group Message组
     */
    public final void clearGroupRedPoint(SessionGroup group) {

        if (MessageSession.isBarSession(group.linkID)) {
            this.snsSessionListData.clearGroupRedPoint(group.sessionID);
        } else {
            this.sessionListData.clearGroupRedPoint(group.sessionID);
        }

        if (group.number > 0) {
            group.clearNumber();
        }
    }


    /**
     * 异步获取 SessionGroup
     *
     * @param linkID  匹配linkID
     * @param groupID or 匹配groupID
     * @param results data中为返回的 SessionGroup, 可能为nil
     */
    public final void getSessionGroup(String linkID, String groupID, final MResults<SessionGroup> results) {

        boolean bSnsServer = MessageSession.isBarSession(linkID);
        final SessionList tempList = bSnsServer ? snsSessionListData : sessionListData;
        {
            SessionGroup group = tempList.getSessionGroup(linkID, groupID);
            if (group != null) {
                MResults.MResultsInfo.SafeOnResult(results,
                        MResults.MResultsInfo.<SessionGroup>SuccessComRet().setData(group));
                return;
            }
        }

        tempList.freshSessionList(null, ret -> {
            if (ret.isSuccess) {

                SessionGroup group = tempList.getSessionGroup(linkID, groupID);
                if (group != null) {
                    MResults.MResultsInfo.SafeOnResult(results,
                            MResults.MResultsInfo.<SessionGroup>SuccessComRet().setData(group));
                    return;
                }
            }

            //最后的返回
            MResults.MResultsInfo.SafeOnResult(results, MResults.MResultsInfo.<SessionGroup>FailureComRet().setData(null));
        });
    }


    /**
     * 异步获取MessageManager，
     *
     * @param messageType 匹配 messageType
     * @param linkID      匹配linkID
     * @param sessionID   or 匹配sessionID
     * @param results     data中为返回的 MessageManager 可能为空
     */
    public final void getMessageManager(int messageType, String linkID, String sessionID, final MResults<MessageManager> results) {

        MessageSession session = sessionListData.getSession(messageType, linkID, sessionID);
        if (session != null) {
            MResults.MResultsInfo.SafeOnResult(results,
                    MResults.MResultsInfo.<MessageManager>SuccessComRet().setData(this.getMessageManager(session)));
            return;
        }

        session = snsSessionListData.getSession(messageType, linkID, sessionID);
        if (session != null) {
            MResults.MResultsInfo.SafeOnResult(results,
                    MResults.MResultsInfo.<MessageManager>SuccessComRet().setData(this.getMessageManager(session)));
            return;
        }

        Map<String, String> params = null;
        if (linkID != null && linkID.length() > 0) {

            params = buildParams("link_id", linkID, "message_type", messageType).getParams();
        }

        boolean bSnsServer = MessageSession.isBarSession(linkID);
        final SessionList tempList = bSnsServer ? snsSessionListData : sessionListData;

        tempList.freshSessionList(params, ret -> {

            if (ret.isSuccess) {

                if (ret.data != null && ret.data.isJsonObject()) {
                    String tarSessionID = GsonUtil.getAsString(ret.data, "tar_session_id");
                    MessageSession temp = tempList.getSession(tarSessionID);
                    if (temp == null && sessionID != null && sessionID.length() > 0) {
                        temp = tempList.getSession(sessionID);
                    }

                    if (temp != null) {
                        MResults.MResultsInfo.SafeOnResult(results,
                                MResults.MResultsInfo.<MessageManager>SuccessComRet().setData(this.getMessageManager(temp)));
                        return;
                    }
                }
            }

            //最后的返回
            MResults.MResultsInfo.SafeOnResult(results, MResults.MResultsInfo.<MessageManager>FailureComRet().setData(null));
        });
    }

    /**
     * 外部方法：调用刷新SessionList
     *
     * @param results
     */
    public final void freshSessionList(final MResults<Void> results) {

        final boolean[] bRet = new boolean[1];
        final boolean[] bSNSRet = new boolean[1];

        MResults<JsonElement> call = result1 -> {

            if (bRet[0] && bSNSRet[0]) {
                MResults.MResultsInfo.SafeOnResult(results, MResults.MResultsInfo.COPY(result1));
            }
        };

        sessionListData.freshSessionList(null, result1 -> {
            bRet[0] = true;

            MResults.MResultsInfo.SafeOnResult(call, result1);
        });

        snsSessionListData.freshSessionList(null, result1 -> {
            bSNSRet[0] = true;

            MResults.MResultsInfo.SafeOnResult(call, result1);
        });
    }

    /**
     * 获取user的个人动态
     *
     * @param userID
     * @param results
     */
    public void getUserPersonalPage(int userID, final MResults<CommandPageArray<Feed>> results) {

        this.personalPage = new PersonalFeed.PersonalCommand(userID);

        this.personalPage.getNextPage(results);
    }


    protected void beginSubTimer() {

        this.delayTime = MESSAGEMANAGER_TIME;
    }

    protected void endSubTimer() {

        this.delayTime = SESSIONMANAGER_TIME;
    }

    void countRedPoint() {
        this.sessionListData.countRedPoint();
        this.snsSessionListData.countRedPoint();
    }

    private void timerFreshNewTips() {

        if (MineManager.getInstance().isLoginOK()) {

            sessionListData.timerFreshNewTips();
            snsSessionListData.timerFreshNewTips();

            if (MineManager.getInstance().getmMe().isLoginOk()) {
                //下一个定时器
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, this.delayTime);
            }
        }
    }

    private MessageManager getMessageManager(MessageSession session) {
        if (session == null)
            return null;

        MessageManager messageManager = null;

        if (this.messageManagerList.containsKey(session.sessionID)) {
            messageManager = this.messageManagerList.get(session.sessionID);
        } else {
            if (MessageSession.isBarSession(session.linkID)) {
                messageManager = new BarMessageManager(session);
            } else {
                messageManager = new MessageManager(session);
            }
            this.messageManagerList.put(session.sessionID, messageManager);
        }

        return messageManager;
    }
}
