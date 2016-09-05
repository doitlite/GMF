package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;
import static com.goldmf.GMFund.extension.ObjectExtension.*;

import static com.goldmf.GMFund.util.FormatUtil.ESCAPE_EMPTY_STRING;

/**
 * Created by cupide on 15/11/28.
 */
public class GMFMessage extends PageItemIndex {

    public static class CacheInt {

        int serverInt;
        int localInt;

        CacheInt(int sInt, int lInt) {
            serverInt = sInt;
            localInt = lInt;
        }

        private void addLocal(int value) {
            localInt += value;
        }

        private void setServerInt(int sInt) {
            serverInt = sInt;
            localInt = Math.max(serverInt, localInt);
        }

        private int get() {
            return Math.max(serverInt, localInt);
        }
    }

    public static int Message_Text = 1;     //纯文本 content生效。
    public static int Message_TarLink = 2;  //纯链接 tarLinkText\tarLink
    public static int Message_Image = 3;    //纯图片 imageUrl、imageWidth、imageHeigth 生效。
    public static int Message_Card = 201;     //自行处理没有文字和没有图片，or 没有tarlink的情况。
    public static int Message_Topic = 202;     //其实赶脚和标准的Message_Card在卡层面处理一样的。

    public static int Message_Text_2 = 401;      //文本 content生效。
    public static int Message_TarLink_2 = 402;    //链接 attachInfo
    public static int Message_Image_2 = 403;      //图片 imageList 图片列表


    private static char SEP_IMAGE_SIZE = 'x';

    public String messageID;            //server下发id
    public String content;           //内容

    //以下为2.2之前的新协议不再使用的。
    public String title;             //标题
    public String imageUrl;          //图片url地址
    public int imageWidth;           //图片长度
    public int imageHeight;          //图片高度
    public String tarLinkText;       //tar link 的文字
    public String tarLink;           //tar link 操作

    public int templateType;         //message的主题样式
    public int messagType;           //
    public long createTime;          //创建时间（server）

    protected User user;             //发送用户信息
    public String localID;

    //2.2 新增

    public int commentCount;     //评论个数
    public List<RemaindFeed> commentBrief;  //评论摘要
    private CacheInt donateCount = new CacheInt(0, 0);     //打赏个数
    private CacheInt donateScore = new CacheInt(0, 0);     //打赏积分

    public ShareInfo attachInfo;       //附件
    public List<ShortcutImagePair> imageList;          //多图

    public int myCommentCount;     //我的评论个数
    private CacheInt myDonateCount = new CacheInt(0, 0);     //我的打赏个数
    private CacheInt myDonateScore = new CacheInt(0, 0);     //我的打赏积分

    public int myDonateCount() {
        return myDonateCount.get();
    }

    public int myDonateScore() {
        return myDonateScore.get();
    }

    public int donateCount() {
        return donateCount.get();
    }

    public int donateScore() {
        return donateScore.get();
    }

    public Intelligence intelligence;

    //more
    public ShareInfo shareInfo;            //消息的分享信息
    public List<RemaindFeed> donateList = new ArrayList<>();      //打赏用户list <RemaindFeed>
    public CommandPageArray<RemaindFeed> commentList;             //评价用户列表，支持下拉刷新 <GMFRemaindFeed>, page

    public JsonObject jsonData;

    private CommandPageArray<RemaindFeed> donateListPage;


    public Optional<User> getUser() {
        return new Optional<>(this.user);
    }

    @Override
    public Object getKey() {
        return this.messageID;
    }

    @Override
    public long getTime() {
        return createTime;
        //Long.valueOf(Optional.of(this.messageID).or("0"));
    }

    public static boolean isValid(GMFMessage message) {
        return message != null && message.messageID != null;
    }

    public static boolean isValidIntelligence(GMFMessage message) {
        return message != null && message.messageID != null && message.intelligence != null && message.intelligence.unlockScore != 0;
    }

    public static boolean isSendByMe(GMFMessage message) {
        return safeGet(() -> message.getUser().get().index == MineManager.getInstance().getmMe().index, false);
    }

    /**
     * 获取 打赏用户列表 协议 donate-list
     *
     * @param results
     */
    public void getDonateUserListPage(final MResults<CommandPageArray<RemaindFeed>> results) {
        this.donateListPage = new CommandPageArray.Builder<RemaindFeed>()
                .cgiUrl(CHostName.HOST2 + "message/donate-list")
                .cgiParam(buildParams("message_id", this.messageID))
                .classOfT(RemaindFeed.class)
                .commandPage(20)
                .build();
        this.donateListPage.getPrePage(results);
    }

    /**
     * 创建一个空message
     *
     * @param messageID
     * @return
     */
    public static GMFMessage buildEmptyMessage(String messageID) {
        GMFMessage message = new GMFMessage();
        message.messageID = messageID;
        return message;
    }

    private void readBriefFromJsonData(JsonObject dic) {
        if (dic == null || !dic.has("updated_time"))
            return;

        this.messageID = GsonUtil.getAsString(dic, "id");
        this.title = GsonUtil.getAsString(dic, "title");
        this.content = GsonUtil.getAsString(dic, "content");
        this.imageUrl = GsonUtil.getAsString(dic, "image_url");
        String imageSize = GsonUtil.getAsString(dic, "image_size");
        if (imageSize.length() > 0 && imageSize.indexOf(SEP_IMAGE_SIZE) >= 0) {
            this.imageWidth = Integer.valueOf(imageSize.substring(0, imageSize.indexOf(SEP_IMAGE_SIZE)));
            this.imageHeight = Integer.valueOf(imageSize.substring(imageSize.indexOf(SEP_IMAGE_SIZE) + 1));
        }

        this.tarLinkText = GsonUtil.getAsString(dic, "tar_link_text");
        this.tarLink = GsonUtil.getAsString(dic, "tar_link");
        this.templateType = GsonUtil.getAsInt(dic, "template_type");
        this.messagType = GsonUtil.getAsInt(dic, "message_type");
        this.createTime = GsonUtil.getAsLong(dic, "created_time");

        this.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user_info"));
        this.localID = GsonUtil.getAsString(dic, "local_id");

        //2.2
        this.commentCount = GsonUtil.getAsInt(dic, "comment", "count");

        {
            ArrayList<RemaindFeed> list = new ArrayList<>();
            for (JsonElement temp : GsonUtil.getAsJsonArray(dic, "comment", "brief")) {
                list.add(RemaindFeed.translateFromJsonData(temp.getAsJsonObject(), RemaindFeed.MessageAction_Comment));
            }
            this.commentBrief = list;
        }

        this.donateCount.setServerInt(GsonUtil.getAsInt(dic, "donate", "count"));
        this.donateScore.setServerInt(GsonUtil.getAsInt(dic, "donate", "score"));

        this.attachInfo = ShareInfo.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "attach_info"));
        {
            ArrayList<ShortcutImagePair> list = new ArrayList<>();
            for (JsonElement temp : GsonUtil.getAsJsonArray(dic, "image_list")) {
                list.add(ShortcutImagePair.translateFromJsonData(temp.getAsJsonObject()));
            }
            this.imageList = list;
        }

        this.myCommentCount = GsonUtil.getAsInt(dic, "mine", "comment_count");
        this.myDonateCount.setServerInt(GsonUtil.getAsInt(dic, "mine", "donate_count"));
        this.myDonateScore.setServerInt(GsonUtil.getAsInt(dic, "mine", "donate_score"));

        if (GsonUtil.has(dic, "intelligence_info")) {
            if (this.intelligence == null) {
                this.intelligence = new Intelligence();
                this.intelligence.messageID = this.messageID;
            }
            this.intelligence.readFromJsonData(GsonUtil.getAsJsonObject(dic, "intelligence_info"));
        }

        this.jsonData = dic;
    }

    public void readFromJsonData(JsonObject dic) {
        if (dic == null)
            return;

        if (dic.has("message_brief")) {

            JsonObject brief = GsonUtil.getAsJsonObject(dic, "message_brief");
            this.readBriefFromJsonData(GsonUtil.getAsJsonObject(brief));

            if (this.intelligence != null) {
                this.intelligence.readUserListFromJsonData(dic);
            }

            //more
            this.shareInfo = ShareInfo.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "share_info"));

            {
                donateList.clear();
                for (JsonElement temp : GsonUtil.getAsJsonArray(dic, "donate_list")) {
                    donateList.add(RemaindFeed.translateFromJsonData(temp.getAsJsonObject(), RemaindFeed.MessageAction_Donate));
                }

                if (this.myDonateCount.localInt > this.myDonateCount.serverInt) {
                    if (this.myDonateCount.serverInt == 0) {
                        donateList.add(0, RemaindFeed.buildMineRemaindFeedWithDonate(this.myDonateScore()));
                    } else {
                        for (RemaindFeed donate : donateList) {
                            if (MineManager.isMe(donate.user.index)) {
                                donate.score = this.myDonateScore();
                                break;
                            }
                        }
                    }
                }
            }

            //commentList 的读取在MessageDetailProtocol 里面
        } else {
            this.readBriefFromJsonData(dic);
        }
    }

    public static GMFMessage translateFromJsonData(JsonObject dic) {
        try {
            GMFMessage message = new GMFMessage();
            message.readFromJsonData(dic);
            return message;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<? extends GMFMessage> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public final String imageSize() {
        return "" + imageWidth + SEP_IMAGE_SIZE + imageHeight;
    }

    public final String getBrief() {

        if (this.templateType == Message_Text) {
            return this.content;
        } else if (this.templateType == Message_TarLink) {
            return "[链接]" + this.tarLinkText;
        } else if (this.templateType == Message_Image) {
            return "[图片]";
        } else if (this.templateType == Message_Card) {
            return ESCAPE_EMPTY_STRING(this.title, this.content);
        } else if (this.templateType == Message_Topic) {
            return "[话题]" + ESCAPE_EMPTY_STRING(this.title, this.content);
        } else {
            return ESCAPE_EMPTY_STRING(this.content, this.title);
        }
    }


    public static class ShortcutImagePair implements Serializable {

        public String shortcutUrl; // 小图
        public String sourceUrl; // 大图

        public static ShortcutImagePair translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull())return null;
            try {
                ShortcutImagePair info = new ShortcutImagePair();
                info.shortcutUrl = GsonUtil.getAsString(dic, "shortcut_url");
                info.sourceUrl = GsonUtil.getAsString(dic, "source_url");
                return info;
            } catch (Exception ignored) {
                return null;
            }
        }
    }


    public final void localProcessDonate(int score) {

        if (this.myDonateCount() == 0) {
            this.donateCount.addLocal(1);
        }

        this.myDonateCount.addLocal(1);
        this.myDonateScore.addLocal(score);
        this.donateScore.addLocal(score);

        for (RemaindFeed user : this.donateList) {
            if (user.user.index == MineManager.getInstance().getmMe().index) {
                this.donateList.remove(user);
                break;
            }
        }

        RemaindFeed mineFeed = RemaindFeed.buildMineRemaindFeedWithDonate(this.myDonateScore());
        this.donateList.add(0, mineFeed);

        ScoreManager.getInstance().account.localSubtractScore(score);
    }

    public final void localProcessAddComment() {
        if (this.myCommentCount == 0) {
            this.commentCount += 1;
            this.myCommentCount = 1;
        }

        this.commentCount += 1;
    }

    public final void localProcessDelComment() {
        this.commentCount -= 1;
    }

    public static class Intelligence {

        private String messageID;

        private CommandPageArray<RemaindFeed> likeUserListPage;
        private CommandPageArray<RemaindFeed> unlikeUserListPage;

        public static final int Intelligence_Action_Waiting = 1;    //等待评价
        public static final int Intelligence_Action_Like = 2;       //用户主动评价为值
        public static final int Intelligence_Action_Unlike = 3;     //用户主动评价为坑
        public static final int Intelligence_Action_Timeout = 4;    //系统自动评价为值

        public int unlockScore;      //解锁积分;
        public int likeCount;        //觉得值的用户数
        public int unlikeCount;      //觉得坑的用户数

        public boolean bMyUnlock;     //是否已经解锁
        public int myAction;
        public int gainScore;         //我获取的积分

        public List<RemaindFeed> likeUserList;      //觉得值的用户列表-freshmore
        public List<RemaindFeed> unlikeUserList;    //觉得坑的用户列表-freshmore

        private void readFromJsonData(JsonObject dic) {

            this.unlockScore = GsonUtil.getAsInt(dic, "unlock_score");
            this.likeCount = GsonUtil.getAsInt(dic, "like_count");
            this.unlikeCount = GsonUtil.getAsInt(dic, "unlike_count");

            this.bMyUnlock = GsonUtil.getAsBoolean(dic, "mine", "unlock");
            this.myAction = GsonUtil.getAsInt(dic, "mine", "action");
            this.gainScore = GsonUtil.getAsInt(dic, "mine", "gain_score");
        }

        /**
         * 通过解锁积分创建一个情报
         *
         * @param unlockScore
         * @return
         */
        public static Intelligence build(int unlockScore) {
            Intelligence info = new Intelligence();
            info.unlockScore = unlockScore;
            return info;
        }

        /**
         * 获取觉得值的用户列表-协议层evaluate-list
         *
         * @param results
         */
        public final void getLikeUserListPage(final MResults<CommandPageArray<RemaindFeed>> results) {
            this.likeUserListPage = new CommandPageArray.Builder<RemaindFeed>()
                    .cgiUrl(CHostName.HOST2 + "message/evaluate-list?like=1")
                    .cgiParam(buildParams("message_id", this.messageID))
                    .classOfT(RemaindFeed.class)
                    .commandPage(20)
                    .parseMoreData(data -> {
                        Intelligence.this.likeCount = GsonUtil.getAsInt(data, "total_count");
                    })
                    .build();
            this.likeUserListPage.getPrePage(results);
        }

        /**
         * 获取觉得坑的用户列表-协议层evaluate-list
         *
         * @param results
         */
        public final void getUnlikeUserListPage(final MResults<CommandPageArray<RemaindFeed>> results) {
            this.unlikeUserListPage = new CommandPageArray.Builder<RemaindFeed>()
                    .cgiUrl(CHostName.HOST2 + "message/evaluate-list?like=0")
                    .cgiParam(buildParams("message_id", this.messageID))
                    .classOfT(RemaindFeed.class)
                    .commandPage(20)
                    .parseMoreData(data -> {
                        Intelligence.this.unlikeCount = GsonUtil.getAsInt(data, "total_count");
                    })
                    .build();
            this.unlikeUserListPage.getPrePage(results);
        }

        private void readUserListFromJsonData(JsonObject dic) {
            this.likeUserList = new ArrayList<>(RemaindFeed.translate(GsonUtil.getAsJsonArray(dic, "like_list")));
            this.unlikeUserList = new ArrayList<>(RemaindFeed.translate(GsonUtil.getAsJsonArray(dic, "unlike_list")));
        }
    }
}
