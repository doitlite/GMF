package com.goldmf.GMFund.controller.business;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.message.BarMessageManager;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.message.MessageSession.SessionHead;
import com.goldmf.GMFund.manager.message.PersonalFeed;
import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.message.SessionManager;
import com.goldmf.GMFund.model.Feed;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.RemaindFeed;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;

import java.util.Collections;

import rx.Observable;

import static com.goldmf.GMFund.extension.MResultExtension.cast;
import static com.goldmf.GMFund.extension.MResultExtension.castToCommandPageArray;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;
import static com.goldmf.GMFund.extension.MResultExtension.createObservablePageArrayMResult;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;


/**
 * Created by yale on 16/2/17.
 */
public class ChatController {
    private ChatController() {
    }

    public static Observable<MResultsInfo<Void>> refreshSessionList() {
        return Observable.create(sub -> SessionManager.getInstance().freshSessionList(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<MessageManager>> getMessageManager(MessageSession session) {
        if (session == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }
        return getMessageManager(session.messageType, session.linkID, session.sessionID);
    }

    public static Observable<MResultsInfo<MessageManager>> getMessageManager(int messageType, String linkID, String sessionID) {
        Observable<MResultsInfo<MessageManager>> observable = Observable.create(sub -> SessionManager.getInstance().getMessageManager(messageType, linkID, sessionID, createObservableMResult(sub)));
        return observable.map(manager -> ObjectExtension.apply(manager, it -> it.isSuccess = it.isSuccess && it.data != null));
    }

    public static Observable<MResultsInfo<SessionHeadHolder>> getSessionHead(int messageType, String linkID, String sessionID, int headID) {
        return getMessageManager(messageType, linkID, sessionID)
                .map(response -> {
                    SessionHead head = null;
                    if (isSuccess(response) && response.data != null && response.data.getSession() != null) {
                        MessageSession session = response.data.getSession();
                        for (SessionHead item : session.headList) {
                            if (item.headID == headID) {
                                head = item;
                                return cast(response, SessionHeadHolder.class).setData(new SessionHeadHolder(session, head));
                            }
                        }
                    }


                    return cast(response, SessionHeadHolder.class).setIsSuccess(false);
                });
    }

    public static Observable<MResultsInfo<SessionHeadHolder>> getSessionHead(MessageManager manager, int headID) {
        if (manager == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return getSessionHead(manager.getSession(), headID);
    }

    public static Observable<MResultsInfo<SessionHeadHolder>> getSessionHead(MessageSession session, int headID) {
        if (session == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.defer(() -> {
            SessionHead head = null;
            for (SessionHead item : session.headList) {
                if (item.headID == headID) {
                    head = item;
                    MResultsInfo<SessionHeadHolder> ret = new MResultsInfo<>();
                    ret.isSuccess = true;
                    ret.data = new SessionHeadHolder(session, head);
                    return Observable.just(ret);
                }
            }


            MResultsInfo<SessionHeadHolder> ret = new MResultsInfo<>();
            ret.isSuccess = false;
            return Observable.just(ret);
        });
    }

    public static Observable<MResultsInfo<CommandPageArray<GMFMessage>>> getBarCommandPageArray(BarMessageManager manager, int headID) {
        if (manager == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return getSessionHead(manager, headID)
                .flatMap(response -> {
                    if (isSuccess(response) && response.data != null) {
                        SessionHead head = response.data.head;
                        return Observable.create(sub -> {
                            manager.getCommandPage(head, createObservablePageArrayMResult(sub));
                        });
                    }

                    MResultsInfo<CommandPageArray<GMFMessage>> ret = new MResultsInfo<>();
                    ret.isSuccess = false;
                    return Observable.just(ret);
                });
    }

    public static Observable<MResultsInfo<CommandPageArray<GMFMessage>>> getBarCommandPageArray(MessageSession session, int headID) {
        if (session == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return getBarCommandPageArray(session.messageType, session.linkID, session.sessionID, headID);
    }

    @SuppressWarnings("unchecked")
    public static Observable<MResultsInfo<CommandPageArray<GMFMessage>>> getBarCommandPageArray(int messageType, String linkID, String sessionID, int headID) {
        return getSessionHead(messageType, linkID, sessionID, headID)
                .flatMap(response -> {
                    if (isSuccess(response) && response.data != null) {
                        MessageSession session = response.data.session;
                        SessionHead head = response.data.head;

                        return getMessageManager(session)
                                .flatMap(it -> {
                                    if (isSuccess(it) && it.data instanceof BarMessageManager) {
                                        BarMessageManager manager = (BarMessageManager) it.data;
                                        return Observable.create(sub -> manager.getCommandPage(head, createObservableMResult(sub)));
                                    }
                                    return Observable.just(castToCommandPageArray(it, GMFMessage.class));
                                });
                    }

                    MResultsInfo<CommandPageArray<GMFMessage>> ret = new MResultsInfo<>();
                    ret.isSuccess = false;
                    return Observable.just(ret);
                });
    }


    public static Observable<MResultsInfo<GMFMessage>> fetchMessageDetail(BarMessageManager manager, GMFMessage message) {
        if (manager == null || message == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.freshMore(message, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<GMFMessage>> getBarMessage(BarMessageManager manager, int headID, String messageID) {
        if (manager == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return getBarMessage(manager.getSession(), headID, messageID);
    }

    public static Observable<MResultsInfo<GMFMessage>> getBarMessage(MessageSession session, int headID, String messageID) {
        if (session == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        if (headID < 0) {
            GMFMessage message = GMFMessage.buildEmptyMessage(messageID);
            return Observable.just(MResultsInfo.<GMFMessage>SuccessComRet().setData(message));
        }

        return getBarCommandPageArray(session, headID)
                .map(response -> {
                    if (isSuccess(response)) {
                        GMFMessage message = Stream.of(opt(response.data.data()).or(Collections.emptyList()))
                                .filter(it -> it.messageID.equals(messageID))
                                .findFirst()
                                .orElse(null);
                        if (message != null) {
                            return cast(response, GMFMessage.class).setData(message);
                        }
                    }

                    return cast(response, GMFMessage.class).setIsSuccess(false);
                });
    }

    public static Observable<MResultsInfo<GMFMessage>> getBarMessage(int messageType, String linkID, String sessionID, int headID, String messageID) {
        if (headID < 0) {
            GMFMessage message = GMFMessage.buildEmptyMessage(messageID);
            return Observable.just(MResultsInfo.<GMFMessage>SuccessComRet().setData(message));
        }

        return getBarCommandPageArray(messageType, linkID, sessionID, headID)
                .map(response -> {
                    if (isSuccess(response)) {
                        GMFMessage message = Stream.of(opt(response.data.data()).or(Collections.emptyList()))
                                .filter(it -> it.messageID.equals(messageID))
                                .findFirst()
                                .orElse(null);
                        if (message != null) {
                            cast(response, GMFMessage.class).setData(message);
                        }
                    }

                    return cast(response, GMFMessage.class).setIsSuccess(false);
                });
    }

    public static Observable<MResultsInfo<Void>> sendBarMessage(BarMessageManager manager, SendMessage message) {
        if (manager == null || message == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.sendMessage(message, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> sendBarMessage(int messageType, String linkID, String sessionID, SendMessage message) {
        return getMessageManager(messageType, linkID, sessionID).flatMap(response -> {
            if (response.data == null || !(response.data instanceof BarMessageManager)) {
                return Observable.just(new MResultsInfo<Void>().setIsSuccess(false));
            }

            BarMessageManager manager = (BarMessageManager) response.data;
            return sendBarMessage(manager, message);
        });
    }

    public static Observable<MResultsInfo<Void>> sendComment(BarMessageManager manager, GMFMessage message, RemaindFeed comment, int targetUserID) {
        if (manager == null || message == null || comment == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.comment(message, comment, targetUserID, createObservableMResult(sub)));
    }

    public static class SessionHeadHolder {
        public MessageSession session;
        public SessionHead head;

        public SessionHeadHolder(MessageSession session, SessionHead head) {
            this.session = session;
            this.head = head;
        }
    }

    public static Observable<MResultsInfo<Void>> unlockBarMessage(BarMessageManager manager, GMFMessage message) {
        if (manager == null || message == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.unlock(message, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> likeBarMessage(BarMessageManager manager, GMFMessage message) {
        if (manager == null || message == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.judge(message, true, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> dislikeBarMessage(BarMessageManager manager, GMFMessage message) {
        if (manager == null || message == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.judge(message, false, createObservableMResult(sub)));
    }

    public static void donate(BarMessageManager manager, GMFMessage message, int score) {
        if (manager == null || message == null) {
            return;
        }

        manager.donate(message, score);
    }

    public static void donate(MessageSession session, GMFMessage message, int score) {
        if (session == null || message == null) {
            return;
        }

        getMessageManager(session)
                .doOnNext(response -> {
                    if (response.data != null && response.data instanceof BarMessageManager) {
                        donate((BarMessageManager) response.data, message, score);
                    }
                })
                .subscribe();
    }

    public static Observable<MResultsInfo<Void>> commitDonateImmediately(BarMessageManager manager, GMFMessage message) {
        return Observable.create(sub -> manager.commitDonateMessage(message, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<CommandPageArray<RemaindFeed>>> getHintCommandPageArray(BarMessageManager manager) {
        if (manager == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.getMyRemainPage(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> deleteBarMessage(BarMessageManager manager, GMFMessage message) {
        if (manager == null || message == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.delMessage(message, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> deleteBarComment(BarMessageManager manager, GMFMessage message, RemaindFeed comment) {
        if (manager == null || message == null || comment == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> manager.delComment(message, comment, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<CommandPageArray<Feed>>> fetchUserCirclePageArray(int userID) {
        return Observable.create(sub -> SessionManager.getInstance().getUserPersonalPage(userID, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<CommandPageArray<RemaindFeed>>> fetchLikeUserList(GMFMessage message) {
        if (message == null || message.intelligence == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> {
            message.intelligence.getLikeUserListPage(createObservablePageArrayMResult(sub));
        });
    }

    public static Observable<MResultsInfo<CommandPageArray<RemaindFeed>>> fetchDislikeUserList(GMFMessage message) {
        if (message == null || message.intelligence == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> {
            message.intelligence.getUnlikeUserListPage(createObservablePageArrayMResult(sub));
        });
    }

    public static Observable<MResultsInfo<CommandPageArray<RemaindFeed>>> fetchDonateUserList(GMFMessage message) {
        if (message == null || message.intelligence == null) {
            return Observable.just(MResultsInfo.FailureComRet());
        }

        return Observable.create(sub -> {
            message.getDonateUserListPage(createObservablePageArrayMResult(sub));
        });
    }
}
