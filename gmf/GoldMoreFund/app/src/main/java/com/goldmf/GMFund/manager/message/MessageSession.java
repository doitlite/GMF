package com.goldmf.GMFund.manager.message;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by cupide on 15/11/28.
 */
public class MessageSession {
    public static int Session_New_Action_text = 101;      //文本（打码文字）
    public static int Session_New_Action_image = 102;     //图片（打码图片）
    public static int Session_New_Action_stock = 103;     //股票（打码股票）
    public static int Session_New_Action_fund = 104;      //组合


    public static int Session_Add_Type_normal_noAdd = 0;  //2.2 以后的不能新加消息
    public static int Session_Add_Type_normal_2 = 1;      //2.2 之后使用，可以评论和打赏的消息
    public static int Session_Add_Type_Intelligence = 2;  //2.2 之后使用，情报类型的消息
    public static int Session_Add_Type_Prediction = 3;    //2.2 之后使用，预测类型的消息

    public String sessionID;         //server下发id
    private String content;         //副标题sever
    public String title;             //标题
    public String icon;              //图片url地址
    protected long updateTime;          //最新更新时间
    public int number;               //新消息数量

    public String linkID;            //linkID
    public int messageType;          //messageType

    public boolean isOwner;                 //是否显示右上角的 “群发消息”
    public boolean enablePost;              //是否显示下面的“发送消息”
    public boolean enableTopic;             //是否支持 话题

    transient private SessionText localText;      //本地数据

    //2.2新增逻辑
    public int todayNumber;                  //今日新消息数量
    public List<SessionHead> headList = new ArrayList<>();        //下一页的head类型

    public String addButtonText;  //新增 新消息的按钮 文本、
    public int addButtonType;     //新增 新消息 类型
    public String addTitle;      //新消息的时候的标题
    public String addContent;     //新消息的时候的内容
    public int limitWord;           //新消息字数限制
    public List<Integer> actionList;   //新消息的发送类型，见Session_New_Action
    public List<Integer> scoreList;        //花费积分list

    public int toReadNumber;       //未读消息数量

    public MessageSession(String ID) {
        this.sessionID = ID;
    }

    public static Boolean isBarSession(MessageSession session) {
        return safeGet(() -> Integer.valueOf(session.linkID) >= 100000, false);
    }
    public static Boolean isBarSession(String linkID) {
        return safeGet(() -> Integer.valueOf(linkID) >= 100000, false);
    }

    /**
     * 按照类型修改本地数据（内存缓存）
     *
     * @param localText
     */
    void editLocalText(SessionText localText) {
        if (localText != null) {
            this.localText = localText;
            if (this.localText.time == 0) {
                this.localText.time = this.updateTime;
            }
        } else {
            this.localText = null;
        }

        SessionManager.getInstance().fire(SessionManager.Key_SessionManager_SessionList, null);
    }

    /**
     * @return 副标题
     */
    public final SessionText subText() {
        if (this.localText == null || this.localText.time < this.updateTime) {
            if (this.content != null && this.content.length() > 0) {
                return new SessionText(this.content, this.updateTime);
            } else {
                return null;
            }
        } else {
            if (this.localText.time == 0) {
                this.localText.time = this.updateTime;
            }
            return this.localText;
        }
    }

    /**
     * 获取json 的信息
     *
     * @param dic ：json数据
     */
    public void readFromJsonData(JsonObject dic) {

        String sessionID = GsonUtil.getAsString(dic, "id");
        assert (sessionID.equals(this.sessionID));

        this.title = GsonUtil.getAsString(dic, "title");

        this.content = GsonUtil.getAsString(dic, "content");
        this.icon = GsonUtil.getAsString(dic, "icon");
        this.updateTime = GsonUtil.getAsLong(dic, "created_time");

        this.number = GsonUtil.getAsInt(dic, "count");

        this.isOwner = GsonUtil.getAsInt(dic, "is_owner") == 1;
        this.enablePost = GsonUtil.getAsInt(dic, "enable_post") == 1;

        this.linkID = GsonUtil.getAsString(dic, "link_id");
        this.messageType = GsonUtil.getAsInt(dic, "message_type");

        this.enableTopic = GsonUtil.getAsInt(dic, "enable_topic_post") == 1;

//        if (MyConfig.isDevModeEnable()) {
//            this.enableTopic = true;
//        }

        //2.2
        this.todayNumber = GsonUtil.getAsInt(dic, "today_num");

        JsonObject config = GsonUtil.getAsJsonObject(dic, "config");
        {
            this.addButtonText = GsonUtil.getAsString(config, "new_button_text");
            this.addButtonType = GsonUtil.getAsInt(config, "new_button_type");

            {
                this.headList.clear();
                for (JsonElement temp : GsonUtil.getAsJsonArray(config, "tab_list")) {
                    this.headList.add(SessionHead.translateFromJsonData(temp.getAsJsonObject()));
                }
            }
            this.scoreList = GsonUtil.getAsIntList(GsonUtil.getAsJsonElement(config, "score_list"));
            this.actionList = GsonUtil.getAsIntList(GsonUtil.getAsJsonElement(config, "new_action"));
            this.limitWord = GsonUtil.getAsInt(config, "limit_word");
            this.addTitle = GsonUtil.getAsString(config, "new_title");
            this.addContent = GsonUtil.getAsString(config, "new_content");
        }

        this.toReadNumber = GsonUtil.getAsInt(dic, "toread_num");
    }

    static MessageSession translateFromJsonData(JsonObject dic) {
        try {
            MessageSession info = new MessageSession(null);
            info.readFromJsonData(dic);
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 清除number
     */
    public void clearNumber() {

        if (this.number != 0) {
            this.number = 0;

            SessionManager.getInstance().countRedPoint();

            SessionManager.getInstance().fire(SessionManager.Key_SessionManager_SessionList, null);
        }
    }

    /**
     * 清除toReadNumber
     */
    public void clearToReadNumber() {

        if (this.toReadNumber != 0) {
            this.toReadNumber = 0;

            SessionManager.getInstance().countRedPoint();

            SessionManager.getInstance().fire(SessionManager.Key_SessionManager_SessionList, null);
        }
    }


    public static class SessionText {

        public static int Normal = 0;       //常规消息
        public static int Sending = 1;      //发送ing
        public static int Local = 2;        //草稿箱
        public static int SendErr = 3;      //发送失败

        public int type = Normal;
        public String str;
        public long time;

        SessionText(String s, long i) {
            type = Normal;
            str = s;
            time = i;
        }

        SessionText(int t, String s, long i) {
            type = t;
            str = s;
            time = i;
        }

        /**
         * 创建草稿箱文本
         *
         * @param localText
         * @return
         */
        public static SessionText buildLocalText(String localText) {
            return new SessionText(SessionText.Local, localText, 0);
        }

        /**
         * 创建发送文本
         *
         * @param message:发送的message
         * @return
         */
        public static SessionText buildBriefText(SendMessage message) {
            int type = SessionText.Normal;

            if (message.local) type = SessionText.Local;
            else if (message.loading) type = SessionText.Sending;
            else if (!message.isSuccess()) type = SessionText.SendErr;

            return new SessionText(type, message.getBrief(), message.getTime());
        }
    }

    public static class SessionHead implements Serializable {
        public int headID;
        public String title;
        public String cgiUrl;

        public static SessionHead translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull()) return null;
            try {
                SessionHead info = new SessionHead();
                info.headID = GsonUtil.getAsInt(dic, "tab_id");
                info.title = GsonUtil.getAsString(dic, "title");
                info.cgiUrl = GsonUtil.getAsString(dic, "cgi_url");
                return info;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
