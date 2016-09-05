package com.goldmf.GMFund.controller.circle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.CommonProxyActivity;
import com.goldmf.GMFund.controller.RecyclerViewFragment;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.extension.RecyclerViewExtension;
import com.goldmf.GMFund.manager.message.BarMessageManager;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.RemaindFeed;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.widget.UserAvatarView;

import java.io.CharConversionException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.*;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.*;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.*;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.*;

/**
 * Created by yale on 16/5/31.
 */

public class CircleRateUserListFragment extends RecyclerViewFragment {
    private int mMessageType;
    private String mLinkID;
    private String mSessionID;
    private String mMessageID;
    private int mRateType;

    private boolean mIsFetchingMore = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMessageType = getArguments().getInt(CommonProxyActivity.KEY_MESSAGE_TYPE_INT);
        mLinkID = getArguments().getString(CommonProxyActivity.KEY_LINK_ID_STRING);
        mSessionID = getArguments().getString(CommonProxyActivity.KEY_SESSION_ID_STRING);
        mMessageID = getArguments().getString(CommonProxyActivity.KEY_MESSAGE_ID_STRING);
        mRateType = getArguments().getInt(CommonProxyActivity.KEY_RATE_TYPE_INT);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButton(this, findToolbar(this));
        setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
        v_setClick(mReloadSection, v -> fetchData(true));
        setOnSwipeRefreshListener(() -> fetchData(false));

        changeVisibleSection(TYPE_LOADING);

        fetchData(true);
    }

    private void fetchData(boolean reload) {
        Observable<MResults.MResultsInfo<Pair<BarMessageManager, GMFMessage>>> observable = ChatController.getMessageManager(mMessageType, mLinkID, mSessionID)
                .flatMap(it -> {
                    if (it.isSuccess && it.data != null && it.data instanceof BarMessageManager) {
                        BarMessageManager manager = (BarMessageManager) it.data;
                        Observable<MResults.MResultsInfo<Pair<BarMessageManager, GMFMessage>>> ob = ChatController.getBarMessage(manager, -1, mMessageID)
                                .flatMap(response -> {
                                    if (response.isSuccess && response.data != null) {
                                        MResults.MResultsInfo<Pair<BarMessageManager, GMFMessage>> ret = MResults.MResultsInfo.SuccessComRet();
                                        ret.setData(Pair.create(manager, response.data));
                                        return Observable.just(ret);
                                    }
                                    return Observable.just(MResults.MResultsInfo.FailureComRet());
                                });
                        return ob;
                    }

                    return Observable.just(MResults.MResultsInfo.FailureComRet());
                });

        consumeEventMRUpdateUI(observable, reload)
                .setTag("fetch_message_detail")
                .onNextSuccess(fetchMessageResponse -> {
                    BarMessageManager manager = fetchMessageResponse.data.first;
                    GMFMessage message = fetchMessageResponse.data.second;
                    fetchMessageDetail(manager, message, reload);
                })
                .done();
    }

    private void fetchMessageDetail(BarMessageManager manager, GMFMessage message, boolean reload) {
        consumeEventMRUpdateUI(ChatController.fetchMessageDetail(manager, message), reload)
                .onNextSuccess(response -> {
                    fetchUserList(response.data, reload);
                })
                .done();
    }

    private void fetchUserList(GMFMessage message, boolean reload) {
        boolean isLike = mRateType == 1;
        boolean isDislike = mRateType == 0;
        boolean isDonate = mRateType == 2;
        if (isLike || isDislike || isDonate) {
            Observable<MResults.MResultsInfo<CommandPageArray<RemaindFeed>>> observable;
            if (isLike) {
                observable = ChatController.fetchLikeUserList(message);
            } else if (isDislike) {
                observable = ChatController.fetchDislikeUserList(message);
            } else {
                observable = ChatController.fetchDonateUserList(message);
            }
            consumeEventMRUpdateUI(observable, reload)
                    .setTag("fetch_user_list")
                    .onNextSuccess(response -> {
                        if (isLike) {
                            updateTitle(String.format(Locale.getDefault(), "%d人觉得值", safeGet(() -> message.intelligence.likeCount, 0)));
                        } else if (isDislike) {
                            updateTitle(String.format(Locale.getDefault(), "%d人觉得坑", safeGet(() -> message.intelligence.unlikeCount, 0)));
                        } else if (isDonate) {
                            updateTitle(String.format(Locale.getDefault(), "%d人打赏", safeGet(() -> message.donateCount(), 0)));
                        }
                        resetContentView(response.data);
                    })
                    .done();
        } else {
            createErrorDialog(this, MResultExtension.getErrorMessage((MResults.MResultsInfo) null)).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsFetchingMore = false;
    }

    private void resetContentView(CommandPageArray<RemaindFeed> pageArray) {
        resetSwipeRefreshLayout(pageArray);
        resetRecyclerView(pageArray);
    }

    private void resetSwipeRefreshLayout(CommandPageArray<RemaindFeed> pageArray) {
        setOnSwipeRefreshListener(() -> {
            consumeEventMR(getPreviousPage(pageArray))
                    .setTag("fetch_previous")
                    .onNextSuccess(response -> {
                        resetRecyclerView(pageArray);
                    })
                    .done();
        });
    }

    private void resetRecyclerView(CommandPageArray<RemaindFeed> pageArray) {
        Context ctx = getActivity();
        Resources res = ctx.getResources();

        List<RemaindFeed> users = safeGet(() -> Stream.of(pageArray.data()).collect(Collectors.toList()), Collections.<RemaindFeed>emptyList());
        RecyclerView recyclerView = mRecyclerView;
        if (recyclerView.getAdapter() != null) {
            SimpleRecyclerViewAdapter<RemaindFeed> adapter = getSimpleAdapter(recyclerView);
            adapter.resetItems(users);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
            RecyclerViewExtension.addHorizontalSepLine(recyclerView, new Rect(dp2px(10), 0, 0, 0));
            SimpleRecyclerViewAdapter<RemaindFeed> adapter = new SimpleRecyclerViewAdapter.Builder<>(users)
                    .onCreateItemView((parent, viewType) -> {
                        LinearLayout cell = new LinearLayout(ctx);
                        cell.setOrientation(LinearLayout.HORIZONTAL);
                        cell.setGravity(Gravity.CENTER_VERTICAL);
                        cell.setBackgroundColor(WHITE_COLOR);
                        cell.setPadding(0, 0, dp2px(10), 0);
                        cell.setLayoutParams(new RecyclerView.LayoutParams(-1, dp2px(50)));

                        {

                            UserAvatarView avatarView = new UserAvatarView(ctx);
                            avatarView.setId(R.id.img_avatar);
                            cell.addView(avatarView, new LinearLayout.LayoutParams(dp2px(50), dp2px(50)));

                            TextView nameLabel = new TextView(ctx);
                            nameLabel.setId(R.id.label_name);
                            nameLabel.setPadding(dp2px(10), 0, dp2px(10), 0);
                            nameLabel.setTextSize(16);
                            nameLabel.setTextColor(TEXT_BLACK_COLOR);
                            cell.addView(nameLabel, apply(new LinearLayout.LayoutParams(0, -2), params -> {
                                params.weight = 1;
                            }));

                            TextView countLabel = new TextView(ctx);
                            countLabel.setId(R.id.label_count);
                            cell.addView(countLabel, new LinearLayout.LayoutParams(-2, -2));

                            ImageView arrowImage = new ImageView(ctx);
                            arrowImage.setImageResource(R.drawable.ic_arrow_right_grey);
                            arrowImage.setId(R.id.img_arrow);
                            arrowImage.setScaleType(ImageView.ScaleType.CENTER);
                            cell.addView(arrowImage, new LinearLayout.LayoutParams(dp2px(40), dp2px(40)));

                        }

                        return cell;
                    })
                    .onCreateViewHolder(builder -> {
                        builder.bindChildWithTag("avatarImage", R.id.img_avatar)
                                .bindChildWithTag("nameLabel", R.id.label_name)
                                .bindChildWithTag("countLabel", R.id.label_count)
                                .configureView((holder, item, pos) -> {
                                    String avatarURL = safeGet(() -> item.user.getPhotoUrl(), "");
                                    int userType = safeGet(() -> item.user.type, User.User_Type.Custom);
                                    UserAvatarView avatarView = builder.getChildWithTag("avatarImage");
                                    avatarView.updateView(avatarURL, userType);

                                    String name = safeGet(() -> item.user.getName(), "");
                                    v_setText(builder.getChildWithTag("nameLabel"), name);

                                    int score = safeGet(() -> item.score, 0);
                                    v_setText(builder.getChildWithTag("countLabel"), score > 0 ? String.valueOf(score) : "");
                                });
                        return builder.create();
                    })
                    .onViewHolderCreated((ad, holder) -> {
                        v_setClick(holder.itemView, v -> {
                            User user = safeGet(() -> ad.getItem(holder.getAdapterPosition()).user, null);
                            if (user != null) {
                                showActivity(ctx, an_UserDetailPage(user));
                            }
                        });
                    })
                    .create();
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!mIsFetchingMore && hasMoreData(pageArray) && isScrollToBottom(recyclerView)) {
                        consumeEventMR(getNextPage(pageArray))
                                .setTag("fetch_next")
                                .onNextSuccess(response -> {
                                    resetRecyclerView(pageArray);
                                })
                                .onNextFinish(response -> {
                                    mIsFetchingMore = false;
                                })
                                .done();
                    }
                }
            });
        }

        if (users.isEmpty()) {
            changeVisibleSection(TYPE_EMPTY);
        } else {
            changeVisibleSection(TYPE_CONTENT);
        }
    }
}
