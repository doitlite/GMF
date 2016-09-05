package com.goldmf.GMFund.controller.chat;

import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.controller.chat.ChatFragments.ConversationDetailAdapter;
import com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM;
import com.goldmf.GMFund.controller.chat.ChatViewModels.PlainImageMsgCellVM;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.message.UpImageMessage;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.util.SecondUtil;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;


/**
 * Created by yale on 15/11/25.
 */
public class ChatMessageEventHandler {
    private static final long SHOW_TIME_INTERVAL_IN_SECOND = 60 * 60;

    private Delegate mDelegate = Delegate.NULL;
    public MessageManager mCurrentMessageManagerOrNil;
    private boolean mIsFetchingOlderMessages = false;

    public void setDelegate(Delegate delegate, boolean weakRetain) {
        if (delegate == null) {
            mDelegate = Delegate.NULL;
            return;
        }

        if (!weakRetain) {
            mDelegate = delegate;
        } else {
            WeakReference<Delegate> ref = new WeakReference<>(delegate);
            mDelegate = new Delegate() {
                @Override
                public ConversationDetailAdapter getAdapter() {
                    if (ref.get() != null) {
                        return ref.get().getAdapter();
                    }
                    return NULL.getAdapter();
                }

                @Override
                public RecyclerView getRecyclerView() {
                    if (ref.get() != null) {
                        return ref.get().getRecyclerView();
                    }
                    return NULL.getRecyclerView();
                }
            };
        }
    }

    private BaseManager.OnKeyListener mListenerOrNil = null;
    private boolean mForceScrollToBottom = true;
    private Long mEnterTime;
    private String mWelcomeTips;

    public void onEnterConversation(int messageType, String linkId, String sessionId) {
        if (mCurrentMessageManagerOrNil == null || !mCurrentMessageManagerOrNil.getSessionID().equals(sessionId)) {
            if (mCurrentMessageManagerOrNil != null) {
                mCurrentMessageManagerOrNil.stop();
                if (mListenerOrNil != null) {
                    mCurrentMessageManagerOrNil.removeListener(mListenerOrNil);
                }
            }

            ChatService.getSessionManager(messageType, linkId, sessionId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(manager -> {

                        if (manager == null) {
                            return;
                        }

                        mEnterTime = SecondUtil.currentSecond();
                        resetWelcomeTips(manager);
                        mCurrentMessageManagerOrNil = manager;
                        mListenerOrNil = (key, data) -> {
                            if (key.equals(MessageManager.Key_MesssageManager_List)) {
                                handleMessageListChangedEvent(mCurrentMessageManagerOrNil.messageList());
                            } else if (key.equals(MessageManager.Key_MesssageManager_UploadImage)) {
                                handleUploadImageProgressChangedEvent((UpImageMessage) data.get(MessageManager.Obj_MesssageManager_Message));
                            } else if (key.equals(MessageManager.Key_MesssageManager_SendMessage)) {
                                handleSendMessageResultEvent((GMFMessage) data.get(MessageManager.Obj_MesssageManager_Message));
                            }
                        };
                        mCurrentMessageManagerOrNil.addListener(MessageManager.Key_MesssageManager_List, mListenerOrNil);
                        mCurrentMessageManagerOrNil.addListener(MessageManager.Key_MesssageManager_UploadImage, mListenerOrNil);
                        mCurrentMessageManagerOrNil.addListener(MessageManager.Key_MesssageManager_SendMessage, mListenerOrNil);
                        mCurrentMessageManagerOrNil.start();

                        if (!Optional.of(mCurrentMessageManagerOrNil.messageList()).let(List::isEmpty).or(Boolean.TRUE)) {
                            mListenerOrNil.onKey(MessageManager.Key_MesssageManager_List, Collections.singletonMap(MessageManager.Obj_MesssageManager_Message, mCurrentMessageManagerOrNil.messageList()));
                        }
                    });
        }
    }

    private void resetWelcomeTips(MessageManager manager) {
        mWelcomeTips = "";
        safeCall(() -> {
            MessageSession session = manager.getSession();
            if (session.linkID.equals("10000") && session.enablePost) {
                mWelcomeTips = "我是操盘侠客服小辣椒，有什么疑问或建议，请点击下方输入框给我留言哦！";
            }
        });
    }

    private boolean hasWelcomeTips() {
        return mEnterTime != null && !TextUtils.isEmpty(mWelcomeTips);
    }

    private void handleMessageListChangedEvent(List<GMFMessage> messages) {
        if (messages != null && !messages.isEmpty()) {
            onResetMessageList(messages, mForceScrollToBottom);
            mForceScrollToBottom = false;
        }
    }

    private void handleSendMessageResultEvent(GMFMessage message) {
        if (message == null)
            return;
        RecyclerView recyclerView = mDelegate.getRecyclerView();
        ConversationDetailAdapter adapter = mDelegate.getAdapter();
        if (recyclerView != null && adapter != null) {
            List<ChatViewModels.BaseMsgCellVM> items = adapter.getItems();
            BaseMsgCellVM tmp;
            for (int i = items.size() - 1; i >= 0; i--) {
                tmp = items.get(i);
                if (tmp.raw == message) {
                    setIsShowTimeOfIndex(items, i);
                    tmp.updateStateWithRawMessage(message);
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    private void handleUploadImageProgressChangedEvent(UpImageMessage message) {
        if (message != null) {
            ConversationDetailAdapter adapter = mDelegate.getAdapter();
            if (adapter != null) {
                List<BaseMsgCellVM> items = adapter.getItems();
                BaseMsgCellVM tmp;
                for (int i = items.size() - 1; i >= 0; i--) {
                    tmp = items.get(i);
                    if (tmp.raw == message) {
                        if (tmp instanceof PlainImageMsgCellVM) {
                            ChatViewModels.PlainImageMsgCellVM cast = (ChatViewModels.PlainImageMsgCellVM) tmp;
                            cast.uploadProgress = (int) (message.percent * 100);
                            adapter.notifyItemChanged(i);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void onExitConversation() {
        if (mCurrentMessageManagerOrNil != null) {
            if (mListenerOrNil != null) {
                mCurrentMessageManagerOrNil.removeListener(mListenerOrNil);
            }
            mCurrentMessageManagerOrNil.stop();
        }
    }

    public void onRequestOlderMessages() {
        if (!mIsFetchingOlderMessages && mCurrentMessageManagerOrNil != null) {
            mIsFetchingOlderMessages = true;
            if (mDelegate.getAdapter().getItemCount() > 0 && mCurrentMessageManagerOrNil.isMore()) {
                mCurrentMessageManagerOrNil.freshOld(callback -> {
                    mIsFetchingOlderMessages = false;
                });
            } else {
                mIsFetchingOlderMessages = false;
            }
        }
    }

    public void onResetMessageList(List<GMFMessage> rawMessages, boolean forceScrollToBottom) {
        if (rawMessages == null || rawMessages.isEmpty())
            return;

        RecyclerView recyclerView = mDelegate.getRecyclerView();
        ConversationDetailAdapter adapter = mDelegate.getAdapter();
        if (adapter != null && recyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            List<BaseMsgCellVM> previousItems = adapter.getItems();
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            boolean shouldScrollToBottom = lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition >= previousItems.size() - 2;

            if (previousItems.isEmpty()) {
                ChatService.transformMessageList(rawMessages)
                        .doOnNext(it -> {
                            if (hasWelcomeTips()) {
                                insertItem(it, new ChatViewModels.WelcomeTipsMsgCellVM(mWelcomeTips, mEnterTime));
                            }
                            setIsShowTime(it);
                            adapter.resetItems(it);
                        })
                        .subscribe();
            } else {
                GMFMessage previousTopRaw = previousItems.get(0).raw;
                GMFMessage topRaw = rawMessages.get(0);
                boolean isInsertedOlderData = topRaw != previousTopRaw;
                boolean isDataSetChanged = false;
                if (isInsertedOlderData) {
                    int previousTopRawIndex = Stream.of(rawMessages).filter(it -> it == previousTopRaw).findFirst().map(it -> rawMessages.indexOf(it)).orElse(-1);
                    if (previousTopRawIndex > 0) {
                        ChatService.transformMessageList(rawMessages.subList(0, previousTopRawIndex)).subscribe(newItems -> {
                            previousItems.addAll(0, newItems);
                            setIsShowTime(previousItems);
                            adapter.notifyItemRangeInserted(0, newItems.size());
                        });
                        isDataSetChanged = true;
                    }
                }

                if (!isDataSetChanged) {
                    ChatService.transformMessageList(rawMessages).subscribe(newItems -> {
                        if (hasWelcomeTips()) {
                            insertItem(newItems, new ChatViewModels.WelcomeTipsMsgCellVM(mWelcomeTips, mEnterTime));
                        }
                        setIsShowTime(newItems);
                        adapter.resetItems(newItems);
                    });
                }
            }

            if (forceScrollToBottom || shouldScrollToBottom) {
                if (previousItems.isEmpty()) {
                    recyclerView.scrollToPosition(Math.max(adapter.getItemCount() - 1, 0));
                } else {
//                    recyclerView.smoothScrollToPosition(Math.max(adapter.getItemCount() - 1, 0));
                    recyclerView.scrollToPosition(Math.max(adapter.getItemCount() - 1, 0));
                }
            }
        }
    }
    //    public void onResetMessageList(List<GMFMessage> rawMessages, boolean scrollToBottom) {
    //        if (rawMessages == null || rawMessages.isEmpty()) return;
    //
    //        RecyclerView recyclerView = mDelegate.getRecyclerView();
    //        ConversationDetailAdapter adapter = mDelegate.getAdapter();
    //        if (adapter != null && recyclerView != null) {
    //            List<BaseMsgCellVM> items = adapter.getItems();
    //            if (items.isEmpty()) {
    //                adapter.resetItems(items);
    //            } else {
    //
    //            }
    //
    //            if (scrollToBottom) {
    //                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    //            }
    //        }
    //    }

    public void onReceiveNewMessage(BaseMsgCellVM message) {
        if (message == null)
            return;
        RecyclerView recyclerView = mDelegate.getRecyclerView();
        ConversationDetailAdapter adapter = mDelegate.getAdapter();
        if (recyclerView != null && adapter != null) {
            setIsShowTimeOfInsert(adapter.getItems(), message, adapter.getItemCount());
            adapter.addItem(adapter.getItemCount(), message);
            recyclerView.smoothScrollToPosition(Math.max(adapter.getItemCount() - 1, 0));
        }
    }

    public void onResendPlainTextMessage(ChatViewModels.PlainTextMsgCellVM message) {
        message.state = PlainImageMsgCellVM.STATE_SENDING;

        ChatService.sendPlainTextMessage(mCurrentMessageManagerOrNil, (SendMessage) message.raw)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void onSendPlainTextMessage(String text) {
        mForceScrollToBottom = true;
        ChatService.sendPlainTextMessage(mCurrentMessageManagerOrNil, text)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void onDeleteMessage(BaseMsgCellVM message) {
        if (message.raw instanceof SendMessage) {
            mCurrentMessageManagerOrNil.cancelMessage((SendMessage) message.raw);
        }
    }

    public void onResendPlainImageMessage(PlainImageMsgCellVM message) {
        ChatService.sendPlainImageMessage(mCurrentMessageManagerOrNil, (UpImageMessage) message.raw)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void onSendPlainImageMessage(Uri uri, int width, int height) {
        mForceScrollToBottom = true;
        ChatService.sendPlainImageMessage(mCurrentMessageManagerOrNil, uri.getPath(), width, height)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private static void setIsShowTime(List<BaseMsgCellVM> items) {
        ChatViewModels.BaseMsgCellVM prevItem = null;
        for (ChatViewModels.BaseMsgCellVM item : items) {
            item.showTime = item.raw.createTime != 0 && (prevItem == null || item.raw.createTime - prevItem.raw.createTime >= SHOW_TIME_INTERVAL_IN_SECOND);
            prevItem = item.raw.createTime > 0 ? item : prevItem;
        }
    }

    private static void setIsShowTimeOfIndex(List<BaseMsgCellVM> items, int index) {
        ChatViewModels.BaseMsgCellVM prevItem = null;
        int prevPosition = index - 1;
        while (prevPosition >= 0) {
            prevItem = items.get(prevPosition);
            if (prevItem.raw.createTime > 0) {
                break;
            }
            prevPosition--;
        }

        BaseMsgCellVM item = items.get(index);
        item.showTime = item.raw.createTime != 0 && (prevItem == null || item.raw.createTime - prevItem.raw.createTime >= SHOW_TIME_INTERVAL_IN_SECOND);
    }

    private static void setIsShowTimeOfInsert(List<BaseMsgCellVM> items, BaseMsgCellVM insertItem, int insertPosition) {
        int prevPosition = insertPosition - 1;
        BaseMsgCellVM prevItem = prevPosition >= 0 && prevPosition < items.size() ? items.get(prevPosition) : null;
        insertItem.showTime = prevItem == null || insertItem.raw.createTime - prevItem.raw.createTime > SHOW_TIME_INTERVAL_IN_SECOND;

        BaseMsgCellVM nextItem = insertPosition >= 0 && insertPosition < items.size() ? items.get(insertPosition) : null;
        if (nextItem != null) {
            nextItem.showTime = nextItem.raw.createTime - insertItem.raw.createTime >= SHOW_TIME_INTERVAL_IN_SECOND;
        }
    }

    private static void setIsShowTimeOfRemove(List<BaseMsgCellVM> items, int removePosition) {
        int prevPosition = removePosition - 1;
        int nextPosition = removePosition + 1;
        ChatViewModels.BaseMsgCellVM prevItem = prevPosition >= 0 && prevPosition < items.size() ? items.get(prevPosition) : null;
        BaseMsgCellVM nextItem = nextPosition >= 0 && nextPosition < items.size() ? items.get(nextPosition) : null;

        if (nextItem != null) {
            nextItem.showTime = prevItem == null || nextItem.raw.createTime - prevItem.raw.createTime > SHOW_TIME_INTERVAL_IN_SECOND;
        }
    }

    private static void insertItem(List<BaseMsgCellVM> items, BaseMsgCellVM newItem) {
        if (items != null && newItem != null) {
            int index = 0;
            for (BaseMsgCellVM item : items) {
                if (item.raw.createTime > newItem.raw.createTime || (item.raw.createTime <= 0L && item.source == BaseMsgCellVM.SOURCE_CLIENT)) {
                    break;
                }
                index++;
            }
            items.add(index, newItem);
        }
    }

    public interface Delegate {
        ConversationDetailAdapter getAdapter();

        RecyclerView getRecyclerView();

        Delegate NULL = new Delegate() {
            @Override
            public ConversationDetailAdapter getAdapter() {
                return null;
            }

            @Override
            public RecyclerView getRecyclerView() {
                return null;
            }
        };
    }
}
