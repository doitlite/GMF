package com.goldmf.GMFund.controller.chat;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.message.SessionManager;
import com.goldmf.GMFund.manager.message.UpImageMessage;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.GMFMessage;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_IMAGE_LEFT;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_IMAGE_RIGHT;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_TEXT_LEFT;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_TEXT_RIGHT;

/**
 * Created by yale on 15/11/25.
 */
public class ChatService {
    private ChatService() {
    }

    public static class UploadProgress {
        boolean isFinish;
        boolean isSuccess;
        double percent;
    }

    public static Observable<MessageManager> getSessionManager(int messageType, String linkId, String sessionId) {
        Observable<MResults.MResultsInfo<MessageManager>> observable = ChatController.getMessageManager(messageType, linkId, sessionId);
        return observable.map(callback -> callback.data);
    }

    public static Observable<List<ChatViewModels.BaseMsgCellVM>> transformMessageList(List<GMFMessage> messages) {
        long loginUserIndex = MineManager.getInstance().getmMe() == null ? 0 : MineManager.getInstance().getmMe().index;
        Func1<GMFMessage, Boolean> isSendByMe = message -> GMFMessage.isSendByMe(message);
        return Observable.from(messages)
                .map(message -> toMessageViewModel(message, isSendByMe))
                .filter(model -> model != null)
                .toList();
    }

    private static BaseMsgCellVM toMessageViewModel(GMFMessage message, Func1<GMFMessage, Boolean> isSendByMe) {
        int type = message.templateType;
        if (type == GMFMessage.Message_Text || type == GMFMessage.Message_TarLink)
            return new ChatViewModels.PlainTextMsgCellVM(isSendByMe.call(message) ? TYPE_PLAIN_TEXT_RIGHT : TYPE_PLAIN_TEXT_LEFT, message);
        else if (type == GMFMessage.Message_Image)
            return new ChatViewModels.PlainImageMsgCellVM(isSendByMe.call(message) ? TYPE_PLAIN_IMAGE_RIGHT : TYPE_PLAIN_IMAGE_LEFT, message);
        else if (type == GMFMessage.Message_Card)
            return new ChatViewModels.SystemImageMsgCellVM(message);
        else if (type == GMFMessage.Message_Topic) {
            return new ChatViewModels.SystemImageMsgCellVM(message);
        } else {
            return new ChatViewModels.UnSupportMessage(isSendByMe.call(message) ? TYPE_PLAIN_TEXT_RIGHT : TYPE_PLAIN_TEXT_LEFT, message);
        }
    }


    public static Observable<Void> sendPlainTextMessage(MessageManager messageManager, String content) {
        return sendPlainTextMessage(messageManager, new SendMessage(content));
    }

    public static Observable<Void> sendPlainTextMessage(MessageManager messageManager, SendMessage message) {
        return Observable.create((Observable.OnSubscribe<Void>) sub -> {
            if (messageManager != null) {
                messageManager.sendMessage(message);
            }
            if (!sub.isUnsubscribed()) {
                sub.onNext(null);
            }
            sub.onCompleted();
        });
    }

    public static Observable<Void> sendPlainImageMessage(MessageManager messageManager, String imagePath, int imageWidth, int imageHeight) {
        UpImageMessage message = new UpImageMessage(imagePath, imageWidth, imageHeight);
        return sendPlainImageMessage(messageManager, message);
    }

    public static Observable<Void> sendPlainImageMessage(MessageManager messageManager, UpImageMessage message) {
        return Observable.create(sub -> {
            if (messageManager != null)
                messageManager.sendMessage(message);
            if (!sub.isUnsubscribed()) {
                sub.onNext(null);
            }
            sub.onCompleted();
        });
    }

    public static UpImageMessage buildTopicMessage(String imagePath, int imageWidth, int imageHeight, String content, String title, List<String> imageFileList, List<String> contentList) {
        UpImageMessage message = new UpImageMessage(imagePath, imageWidth, imageHeight, content, title);
        message.imageFileList = imageFileList;
        message.contentList = contentList;
        return message;
    }

    public static void saveLocalMessage(MessageManager messageManager, UpImageMessage message) {
        if (messageManager != null)
            messageManager.saveLocalMessage(message);
    }

//    public static Observable<MResults.MResultsInfo> sendPlainTopicMessage(MessageManager messageManager, String imagePath, int imageWidth, int imageHeight, String content, String title, List<String> imageFileList, List<String> contentList) {
//        UpImageMessage message = new UpImageMessage(imagePath, imageWidth, imageHeight, content, title);
//        message.imageFileList = imageFileList;
//        message.contentList = contentList;
//        return sendPlainTopicMessage(messageManager, message);
//    }

    public static Observable<MResults.MResultsInfo> sendPlainTopicMessage(MessageManager messageManager, UpImageMessage message) {
        return Observable.create(sub -> {
            if (messageManager != null) {
                messageManager.sendMessage(message, new MResults<Void>() {
                    @Override
                    public void onResult(MResultsInfo<Void> result) {
                        if (!sub.isUnsubscribed()) {
                            sub.onNext(result);
                        }
                        sub.onCompleted();
                    }
                });
            } else {
                if (!sub.isUnsubscribed()) {
                    sub.onNext(null);
                }
                sub.onCompleted();
            }
        });
    }

}
