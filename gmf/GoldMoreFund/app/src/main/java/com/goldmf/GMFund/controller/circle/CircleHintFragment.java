package com.goldmf.GMFund.controller.circle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.SimpleFragment;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.message.BarMessageManager;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.GMFMessage.Intelligence;
import com.goldmf.GMFund.model.RemaindFeed;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.FormatUtil;

import java.util.List;

import rx.Observable;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_LINK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MESSAGE_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SESSION_ID_STRING;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.SpannableStringExtension.ImageTextParams;
import static com.goldmf.GMFund.extension.SpannableStringExtension.appendImageText;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static java.util.Collections.emptyList;

/**
 * Created by yale on 16/5/25.
 */
public class CircleHintFragment extends SimpleFragment {

    private int mMessageType;
    private String mLinkID;
    private String mSessionID;
    private boolean mIsFetchingMore = false;

    public CircleHintFragment init(int messageType, String linkID, String sessionID) {
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_MESSAGE_TYPE_INT, messageType);
        arguments.putString(KEY_LINK_ID_STRING, linkID);
        arguments.putString(KEY_SESSION_ID_STRING, sessionID);
        setArguments(arguments);
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMessageType = getArguments().getInt(KEY_MESSAGE_TYPE_INT);
        mLinkID = getArguments().getString(KEY_LINK_ID_STRING);
        mSessionID = getArguments().getString(KEY_SESSION_ID_STRING);
        return inflater.inflate(R.layout.frag_circle_hint, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
        setupBackButton(this, findToolbar(this));

        v_setClick(mReloadSection, v -> reloadData());

        changeVisibleSection(TYPE_LOADING);

        reloadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsFetchingMore = false;
    }

    private void reloadData() {
        Observable<MResults.MResultsInfo<MessageManager>> observable = ChatController.getMessageManager(mMessageType, mLinkID, mSessionID)
                .map(response -> {
                    response.isSuccess = response.isSuccess && response.data != null && response.data instanceof BarMessageManager;
                    return response;
                });
        consumeEventMRUpdateUI(observable, true)
                .setTag("get_message_manager")
                .onNextSuccess(managerResponse -> {
                    BarMessageManager manager = (BarMessageManager) managerResponse.data;
                    consumeEventMRUpdateUI(ChatController.getHintCommandPageArray(manager), true)
                            .setTag("get_hint_command_page_array")
                            .onNextSuccess(response -> {
                                CommandPageArray<RemaindFeed> pageArray = response.data;
                                resetContentView(manager, pageArray);
                            })
                            .done();
                })
                .done();

    }

    private void resetContentView(BarMessageManager manager, CommandPageArray<RemaindFeed> pageArray) {
        resetSwipeRefreshLayout(manager, pageArray);
        resetRecyclerView(manager, pageArray);
    }

    private void resetSwipeRefreshLayout(BarMessageManager manager, CommandPageArray<RemaindFeed> pageArray) {
        setOnSwipeRefreshListener(() -> {
            consumeEventMRUpdateUI(PageArrayHelper.getPreviousPage(pageArray), false)
                    .setTag("fetch_previous")
                    .onNextSuccess(response -> {
                        resetRecyclerView(manager, pageArray);
                    })
                    .done();
        });
    }

    private void resetRecyclerView(BarMessageManager manager, CommandPageArray<RemaindFeed> pageArray) {
        List<VM> items = Stream.of(safeGet(() -> pageArray.data(), emptyList())).map(it -> new VM(it)).collect(Collectors.toList());
        RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
        if (recyclerView.getAdapter() != null) {
            SimpleRecyclerViewAdapter<VM> adapter = getSimpleAdapter(recyclerView);
            adapter.resetItems(items);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            addHorizontalSepLine(recyclerView);
            SimpleRecyclerViewAdapter<VM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                    .onCreateItemView(R.layout.cell_circle_hint)
                    .onCreateViewHolder(builder -> {
                        builder.bindChildWithTag("avatarImage", R.id.img_avatar)
                                .bindChildWithTag("nameLabel", R.id.label_name)
                                .bindChildWithTag("previewImage", R.id.img_preview)
                                .bindChildWithTag("messageLabel", R.id.label_message)
                                .bindChildWithTag("contentLabel", R.id.label_content)
                                .bindChildWithTag("rewardImage", R.id.img_reward)
                                .bindChildWithTag("dateLabel", R.id.label_date)
                                .configureView((holder, item, pos) -> {
                                    v_setImageUri(builder.getChildWithTag("avatarImage"), item.avatarURL);
                                    v_setText(builder.getChildWithTag("nameLabel"), item.name);
                                    v_setImageUri(builder.getChildWithTag("previewImage"), item.firstImageURL);
                                    v_setVisibility(builder.getChildWithTag("previewImage"), TextUtils.isEmpty(item.firstImageURL) ? View.GONE : View.VISIBLE);
                                    v_setText(builder.getChildWithTag("contentLabel"), item.content);
                                    v_setVisibility(builder.getChildWithTag("rewardImage"), item.contentIconResID == 0 ? View.GONE : View.VISIBLE);

                                    v_setText(builder.getChildWithTag("messageLabel"), item.message);
                                    v_setVisibility(builder.getChildWithTag("messageLabel"), TextUtils.isEmpty(item.firstImageURL) ? View.VISIBLE : View.GONE);
                                    v_setText(builder.getChildWithTag("dateLabel"), item.createTime);
                                });
                        return builder.create();
                    })
                    .onViewHolderCreated((ad, holder) -> {
                        v_setClick(holder.itemView, v -> {
                            VM item = ad.getItem(holder.getAdapterPosition());
                            MessageSession session = manager.getSession();
                            String messageID = safeGet(() -> item.raw.message.messageID, "");
                            showActivity(this, an_CircleDetailPage(session, -1, messageID));
                        });
                    })
                    .create();
            View footerView = adapter.createFooterView(this, R.layout.footer_circle_hint);
            v_setClick(footerView, v -> {
                if (!mIsFetchingMore) {
                    mIsFetchingMore = true;
                    consumeEventMR(PageArrayHelper.getNextPage(pageArray))
                            .setTag("fetch_more")
                            .onNextStart(response -> mIsFetchingMore = false)
                            .onNextSuccess(response -> {
                                resetRecyclerView(manager, pageArray);
                            })
                            .done();
                }
            });
            adapter.addFooter(footerView);
            recyclerView.setAdapter(adapter);
        }

        SimpleRecyclerViewAdapter<VM> adapter = getSimpleAdapter(recyclerView);
        View footer = adapter.getFooter(0);
        v_setVisibility(footer, hasMoreData(pageArray) ? View.VISIBLE : View.GONE);

        if (items.isEmpty()) {
            changeVisibleSection(TYPE_EMPTY);
        } else {
            changeVisibleSection(TYPE_CONTENT);
        }
    }

    private static class VM {
        private RemaindFeed raw;

        public String avatarURL;
        public CharSequence name;
        public int contentIconResID;
        public CharSequence content;
        public String firstImageURL;
        public CharSequence message;
        public CharSequence createTime;

        public VM(RemaindFeed raw) {
            this.raw = raw;

            this.avatarURL = safeGet(() -> raw.user.getPhotoUrl(), "");
            this.name = safeGet(() -> raw.user.getName(), PlaceHolder.NULL_VALUE);

            this.contentIconResID = anyMatch(raw.feedType, RemaindFeed.MessageAction_Donate) ? R.mipmap.ic_circle_reward : 0;
            this.content = generateContent(raw);
            this.firstImageURL = safeGet(() -> raw.message.imageList.get(0).shortcutUrl, "");
            this.message = generateMessage(raw);
            this.createTime = safeGet(() -> FormatUtil.formatTimeByNow(raw.createTime), PlaceHolder.NULL_VALUE);
        }

        private static CharSequence generateMessage(RemaindFeed raw) {
            ShareInfo attchment = safeGet(() -> raw.message.attachInfo, null);
            boolean hasAttachment = safeGet(() -> attchment != null && !TextUtils.isEmpty(attchment.title), false);
            if (hasAttachment) {
                return safeGet(() -> String.format("%s:\n%s", attchment.title, attchment.msg), "");
            }

            return safeGet(() -> raw.message.content, "");
        }

        private static CharSequence generateContent(RemaindFeed raw) {
            if (anyMatch(raw.feedType, RemaindFeed.MessageAction_Comment)) {
                return safeGet(() -> raw.text, "");
            } else if (anyMatch(raw.feedType, RemaindFeed.MessageAction_Donate)) {
                return safeGet(() -> setColor(String.format("x %d", raw.score), TEXT_RED_COLOR), "");
            } else if (anyMatch(raw.feedType, RemaindFeed.MessageAction_Judge)) {
                return safeGet(() -> {
                    if (anyMatch(raw.action, Intelligence.Intelligence_Action_Unlike)) {
                        ImageTextParams params = new ImageTextParams();
                        params.textColor = TEXT_WHITE_COLOR;
                        params.iconResID = R.mipmap.ic_dislike;
                        params.bgColor = GREY_COLOR;
                        return concatNoBreak(appendImageText(" ", "坑爹", params), setColor(String.format(" %d积分被系统回收", raw.score), TEXT_GREY_COLOR));
                    } else {
                        ImageTextParams params = new ImageTextParams();
                        params.textColor = TEXT_WHITE_COLOR;
                        params.iconResID = R.mipmap.ic_like;
                        params.bgColor = RED_COLOR;
                        return concatNoBreak(appendImageText(" ", "超值", params), setColor(String.format(" 获得%d积分", raw.score), TEXT_RED_COLOR));
                    }
                }, "");
            } else if (anyMatch(raw.feedType, RemaindFeed.MessageAction_Unlock)) {
                return "";
            }

            return "";
        }
    }
}
