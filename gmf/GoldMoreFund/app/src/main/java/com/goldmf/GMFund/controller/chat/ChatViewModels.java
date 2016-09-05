package com.goldmf.GMFund.controller.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.internal.RegexPatternHolder;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.message.MessageSession.SessionText;
import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.message.SessionGroup;
import com.goldmf.GMFund.manager.message.UpImageMessage;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.FormatUtil;

import java.io.File;
import java.util.regex.Matcher;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ConversationDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ConversationGroupPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREEN_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;

/**
 * Created by yale on 15/11/20.
 */
public class ChatViewModels {

    public static class ConversationListCellVM {
        public MessageSession raw;
        public String iconURL;
        public String title;
        public CharSequence content;
        public String time;
        public CharSequence todayNewCount;
        public int unreadCount;
        public int hintCount;
        public long updateTimeInSecond;
        public boolean isBarSession;
        public boolean isGroupSession;

        public ConversationListCellVM(MessageSession session) {
            this.raw = session;
            this.isBarSession = safeGet(() -> MessageSession.isBarSession(raw), false);
            this.isGroupSession = safeGet(() -> raw instanceof SessionGroup, false);
            this.iconURL = session.icon;
            this.title = session.title;
            boolean hasTodayNewCount = safeGet(() -> session.todayNumber > 0, false);
            this.todayNewCount = hasTodayNewCount ? safeGet(() -> "今日+" + session.todayNumber, "") : "";
            this.unreadCount = session.number;
            this.hintCount = session.toReadNumber;

            if (session.subText() == null) {
                //显示content
                this.content = "";

                //显示时间
                this.updateTimeInSecond = 0;
                this.time = "";
            } else {
                SessionText text = session.subText();

                //显示content
                if (text.type == SessionText.Normal) {
                    this.content = text.str;
                } else if (text.type == SessionText.SendErr) {
                    this.content = concatNoBreak(setColor("[发送失败]", TEXT_RED_COLOR), text.str);
                } else if (text.type == SessionText.Local) {
                    this.content = concatNoBreak(setColor("[草稿]", TEXT_GREEN_COLOR), text.str);
                } else if (text.type == SessionText.Sending) {
                    this.content = concatNoBreak(setColor("[发送中]", TEXT_GREEN_COLOR), text.str);
                } else {
                    this.content = text.str;
                }

                //显示时间
                this.updateTimeInSecond = text.time;
                this.time = FormatUtil.formatTimeByNow(updateTimeInSecond);
            }
        }

        public void openDetailPage(Fragment fragment) {
            if (raw != null) {
                if (isGroupSession) {
                    showActivity(fragment, an_ConversationGroupPage(raw.messageType, raw.linkID, raw.sessionID));
                } else if (isBarSession) {
                    showActivity(fragment, an_CircleListPage(raw.messageType, raw.linkID, raw.sessionID));
                } else {
                    showActivity(fragment, an_ConversationDetailPage(raw.messageType, raw.linkID, raw.sessionID));
                }
            }
        }
    }

    public static class BaseMsgCellVM {
        public static final int SOURCE_CLIENT = 1;
        public static final int SOURCE_SERVER = 2;

        public static final int TYPE_PLAIN_TEXT_LEFT = 1;
        public static final int TYPE_PLAIN_TEXT_RIGHT = 2;
        public static final int TYPE_PLAIN_IMAGE_LEFT = 3;
        public static final int TYPE_PLAIN_IMAGE_RIGHT = 4;
        public static final int TYPE_IMAGE_NOTIFICATION = 6;
        public static final int TYPE_WELCOME_TIPS = 7;

        public static final int STATE_OK = 0;
        public static final int STATE_SENDING = 1;
        public static final int STATE_FAIL = 2;

        public int source = SOURCE_SERVER;
        public int state = STATE_OK;

        public int type;
        public boolean showTime = false;
        public GMFMessage raw;

        public BaseMsgCellVM() {
        }

        public BaseMsgCellVM(int type, GMFMessage message) {
            this.type = type;
            this.raw = message;
            this.source = (message instanceof SendMessage) ? SOURCE_CLIENT : SOURCE_SERVER;
            updateStateWithRawMessage(message);
        }

        public void updateStateWithRawMessage(GMFMessage message) {
            if (message instanceof SendMessage) {
                SendMessage cast = (SendMessage) message;
                if (cast.loading || cast.createTime <= 0L)
                    state = STATE_SENDING;
                else {
                    state = cast.isSuccess() ? STATE_OK : STATE_FAIL;
                }
            } else {
                this.state = STATE_OK;
            }
        }
    }

    public static class WelcomeTipsMsgCellVM extends BaseMsgCellVM {
        public String content;

        public WelcomeTipsMsgCellVM(String content, long createTime) {
            this.type = TYPE_WELCOME_TIPS;
            this.raw = new GMFMessage();
            this.showTime = false;
            this.content = content;
            this.raw.createTime = createTime;
        }
    }

    // pattern (((http|ftp|https):\/{2})?(([0-9a-z_-]+\.)+(com|gov|net|org|edu|int|mil|cn)(:[0-9]+)?((\/([~0-9a-zA-Z\#\+\%@\.\/_-]+))?(\?[0-9a-zA-Z\+\%@\/&\[\];=_-]+)?)?))
    public static class PlainTextMsgCellVM extends BaseMsgCellVM {
        public int senderId;
        public String senderAvatarURL;
        public MsgLink linkOrNil;

        public PlainTextMsgCellVM(int type, GMFMessage message) {
            super(type, message);
            this.senderId = message.getUser().let(user -> user.index).or(-1);
            this.senderAvatarURL = message.getUser().let(User::getPhotoUrl).or("");
            if (TextUtils.isEmpty(message.tarLink) || TextUtils.isEmpty(message.tarLinkText)) {
                this.linkOrNil = null;
            } else {
                this.linkOrNil = new MsgLink();
                this.linkOrNil.name = message.tarLinkText;
                this.linkOrNil.url = message.tarLink;
            }
        }

        public CharSequence parseMessage(Activity activity) {
            if (this.linkOrNil != null) {
                return setColor(setClickEvent(this.linkOrNil.name, view -> CMDParser.parse(this.linkOrNil.url).call(activity)), BLUE_COLOR);
            }
            String ignoreCaseContent = raw.content.toLowerCase();
            Matcher matcher = RegexPatternHolder.MATCH_URL.matcher(ignoreCaseContent);
            if (!matcher.find()) {
                return raw.content;
            } else {
                SpannableString ss = new SpannableString(raw.content);
                do {
                    String ignoreCaseUrl = matcher.group();
                    int start = ignoreCaseContent.indexOf(ignoreCaseUrl);
                    int end = start + ignoreCaseUrl.length();
                    String url = raw.content.substring(start, end);
                    ss.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            if (activity.hasWindowFocus()) {
                                GMFBottomSheet sheet = new GMFBottomSheet.Builder(activity)
                                        .setTitle(url)
                                        .addContentItem(new GMFBottomSheet.BottomSheetItem(0, "复制", R.mipmap.ic_bottomsheet_copy))
                                        .addContentItem(new GMFBottomSheet.BottomSheetItem(1, "打开链接", R.mipmap.ic_bottomsheet_link))
                                        .create();
                                sheet.setOnItemClickListener((bottomSheet, item) -> {
                                    bottomSheet.dismiss();
                                    int position = (int) item.tag;
                                    if (position == 0) {
                                        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                        cm.setPrimaryClip(ClipData.newPlainText(null, url));
                                    } else if (position == 1) {
                                        showActivity(activity, an_WebViewPage(url));
                                    }
                                });
                                sheet.show();
                            }
                        }
                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new ForegroundColorSpan(BLUE_COLOR), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } while (matcher.find());
                return ss;
            }
        }
    }

    public static class UnSupportMessage extends PlainTextMsgCellVM {

        public UnSupportMessage(int type, GMFMessage message) {
            super(type, message);
        }

        @Override
        public CharSequence parseMessage(Activity activity) {
            return "当前版本不支持此格式";
        }
    }

    public static class PlainImageMsgCellVM extends BaseMsgCellVM {
        public int senderId;
        public String senderAvatarURL;
        public Uri imageUri;
        public Rect imageSize;
        public int uploadProgress = 0;

        public PlainImageMsgCellVM(int type, GMFMessage message) {
            super(type, message);
            senderId = message.getUser().let(user -> user.index).or(-1);
            senderAvatarURL = message.getUser().let(User::getPhotoUrl).or("");
            imageSize = new Rect(0, 0, message.imageWidth, message.imageHeight);
            if (message instanceof UpImageMessage) {
                UpImageMessage cast = (UpImageMessage) message;
                imageUri = Uri.fromFile(new File(cast.getImageFile()));
            } else {
                String imageUriText = TextUtils.isEmpty(message.imageUrl) ? "" : message.imageUrl;
                imageUri = Uri.parse(imageUriText);
                source = SOURCE_CLIENT;
            }
        }
    }

    public static class SystemImageMsgCellVM extends BaseMsgCellVM {
        public String title;
        public String coverURL;
        public String content;
        public MsgLink linkOrNil;

        public SystemImageMsgCellVM(GMFMessage message) {
            super(TYPE_IMAGE_NOTIFICATION, message);
            this.title = message.title;
            this.coverURL = message.imageUrl;
            this.content = message.content;
            if (TextUtils.isEmpty(message.tarLink) || TextUtils.isEmpty(message.tarLinkText)) {
                this.linkOrNil = null;
            } else {
                this.linkOrNil = new MsgLink();
                this.linkOrNil.name = message.tarLinkText;
                this.linkOrNil.url = message.tarLink;
            }
        }
    }

    public static class MsgLink {
        public String name;
        public String url;
    }
}
