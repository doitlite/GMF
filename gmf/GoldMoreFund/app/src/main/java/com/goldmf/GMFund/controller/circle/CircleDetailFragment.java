package com.goldmf.GMFund.controller.circle;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.BaseActivity;
import com.goldmf.GMFund.controller.BaseFragment;
import com.goldmf.GMFund.controller.SimpleFragment;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.controller.business.CircleController;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.dialog.ShareDialog;
import com.goldmf.GMFund.controller.dialog.ShareDialog.SharePlatform;
import com.goldmf.GMFund.controller.internal.ChildBinder;
import com.goldmf.GMFund.controller.internal.ChildBinders;
import com.goldmf.GMFund.controller.protocol.UMShareHandlerProtocol;
import com.goldmf.GMFund.extension.StringExtension;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.message.BarMessageManager;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.RemaindFeed;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.model.User.User_Type;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.util.IntCounter;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.UserAvatarView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func0;
import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_GLOBAL_OBJ_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_LINK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MESSAGE_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MESSAGE_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SESSION_HEAD_ID_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SESSION_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SHOW_SHARE_DIALOG_BOOLEAN;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleRateUserListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FeedbackPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ScoreHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addContentInset;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.StringExtension.notMatch;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.getScreenSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_globalLayout;
import static com.goldmf.GMFund.extension.ViewExtension.v_reviseTouchArea;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.util.FormatUtil.formatTimeByNow;
import static com.goldmf.GMFund.util.UmengUtil.stat_click_event;
import static java.util.Collections.emptyList;

/**
 * Created by yale on 16/5/23.
 */
public class CircleDetailFragment extends SimpleFragment {

    private int mMessageType;
    private String mLinkID;
    private String mSessionID;
    private String mMessageID;
    private int mHeadID;
    private boolean mShowShareDialogNow;

    private BarMessageManager mManager;
    private GMFMessage mMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMessageType = getArguments().getInt(KEY_MESSAGE_TYPE_INT);
        mLinkID = getArguments().getString(KEY_LINK_ID_STRING);
        mSessionID = getArguments().getString(KEY_SESSION_ID_STRING);
        mHeadID = getArguments().getInt(KEY_SESSION_HEAD_ID_INT);
        mMessageID = getArguments().getString(KEY_MESSAGE_ID_STRING);
        mShowShareDialogNow = getArguments().getBoolean(KEY_SHOW_SHARE_DIALOG_BOOLEAN, false);

        String passMessageObjID = safeGet(() -> getArguments().getString(KEY_GLOBAL_OBJ_ID_STRING), "");
        mMessage = GlobalVariableDic.shareInstance().get(passMessageObjID);
        safeCall(() -> {
            BaseActivity activity = BaseActivity.class.cast(getActivity());
            activity.addRelativeObjectID(passMessageObjID);
        });

        return inflater.inflate(R.layout.frag_circle_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
        setupBackButton(this, findToolbar(this), R.drawable.ic_arrow_left_light);

        {
            HashMap<String, String> params = new HashMap<>();
            params.put("msgid", mMessageID);
            stat_click_event(UmengUtil.eEVENTIDTopicDetailEnter, params);
        }

        changeVisibleSection(TYPE_LOADING);
        v_setClick(mReloadSection, v -> {
            fetchBarMessageManager();
        });

        fetchBarMessageManager();
    }

    @Override
    protected boolean onInterceptGoBack() {
        if (mManager != null && mMessage != null) {
            ChatController.commitDonateImmediately(mManager, mMessage);
        }
        return super.onInterceptGoBack();
    }

    public static GMFMessage getMessage(Fragment childFragment) {
        return safeGet(() -> {
            CircleDetailFragment fragment = (CircleDetailFragment) childFragment.getParentFragment();
            return fragment.mMessage;
        }, null);
    }

    public static BarMessageManager getMessageManager(Fragment childFragment) {
        return safeGet(() -> {
            CircleDetailFragment fragment = (CircleDetailFragment) childFragment.getParentFragment();
            return fragment.mManager;
        }, null);
    }

    private void fetchBarMessageManager() {
        Observable<MResults.MResultsInfo<MessageManager>> observable = ChatController.getMessageManager(mMessageType, mLinkID, mSessionID)
                .map(it -> {
                    it.isSuccess = it.isSuccess && it.data != null && it.data instanceof BarMessageManager;
                    return it;
                });
        consumeEventMRUpdateUI(observable, false)
                .onNextSuccess(response -> {
                    BarMessageManager manager = (BarMessageManager) response.data;
                    MessageSession session = manager.getSession();
                    updateTitle(session.title);
                    fetchGMFMessage(manager, mHeadID, mMessageID, mMessage);
                })
                .done();
    }

    @SuppressWarnings("CodeBlock2Expr")
    private void fetchGMFMessage(BarMessageManager manager, int headID, String messageID, GMFMessage oldMessage) {
        if (oldMessage == null) {
            Observable<MResults.MResultsInfo<GMFMessage>> observable = ChatController.getBarMessage(manager, headID, messageID);
            consumeEventMRUpdateUI(observable, true)
                    .setTag("fetch_message")
                    .onNextSuccess(response -> {
                        consumeEventMRUpdateUI(ChatController.fetchMessageDetail(manager, response.data), true)
                                .setTag("fetch_message_detail")
                                .onNextSuccess(detailResponse -> {
                                    mManager = manager;
                                    mMessage = detailResponse.data;

                                    resetContentView(manager, detailResponse.data);
                                    if (mShowShareDialogNow) {
                                        v_findView(this, R.id.img_share).performClick();
                                        mShowShareDialogNow = false;
                                    }
                                })
                                .done();

                    })
                    .done();
        } else {
            consumeEventMRUpdateUI(ChatController.fetchMessageDetail(manager, oldMessage), true)
                    .setTag("fetch_message_detail")
                    .onNextSuccess(detailResponse -> {
                        mManager = manager;
                        mMessage = detailResponse.data;

                        resetContentView(manager, detailResponse.data);
                        if (mShowShareDialogNow) {
                            v_findView(this, R.id.img_share).performClick();
                            mShowShareDialogNow = false;
                        }
                    })
                    .done();
        }
    }

    private void resetContentView(BarMessageManager manager, GMFMessage message) {

        View shareButton = v_findView(this, R.id.img_share);
        v_setClick(shareButton, v -> {
            ShareInfo shareInfo = message.shareInfo;
            if (shareInfo != null) {
                if (getActivity() instanceof UMShareHandlerProtocol) {
                    boolean isSendByMe = GMFMessage.isSendByMe(mMessage);
                    SharePlatform[] platforms = {SharePlatform.WX, SharePlatform.WX_CIRCLE,
                            SharePlatform.QQ, SharePlatform.SMS,
                            SharePlatform.QZONE, SharePlatform.SINA,
                            SharePlatform.COPY, isSendByMe ? SharePlatform.DELETE : SharePlatform.REPORT};
                    ShareDialog d = new ShareDialog(getActivity(), shareInfo, platforms);
                    d.setShareItemClickEventDelegate((dialog, platform) -> {
                        dialog.dismiss();
                        if (platform == SharePlatform.REPORT) {
                            performReportArticle(shareInfo);
                        } else if (platform == SharePlatform.DELETE) {
                            performDeleteArticle(manager, message);
                        } else {
                            if (getActivity() != null && getActivity() instanceof UMShareHandlerProtocol) {
                                ((UMShareHandlerProtocol) getActivity()).onPerformShare(shareInfo, platform);
                            }
                        }
                    });
                    d.show();
                    stat_click_event(UmengUtil.eEVENTIDTopicDetailShare);
                }
            }
        });
        v_setVisibility(shareButton, message.shareInfo != null ? View.VISIBLE : View.GONE);

        replaceContentFragment(manager);
    }

    private void performReportArticle(ShareInfo shareInfo) {
        stat_click_event(UmengUtil.eEVENTIDTopicDetailReportTopic);
        ChatController.getMessageManager(2, "10000", "")
                .doOnNext(response -> {
                    if (response.data != null) {
                        String userName = mMessage.getUser().let(it -> it.getName()).or("");
                        String url = shareInfo.url;
                        response.data.saveLocalMessage(new SendMessage(String.format("【举报】 %s 来自%s的动态", url, userName)));
                        showActivity(this, an_FeedbackPage(Optional.empty()));
                    }
                })
                .subscribe();
    }

    private void performDeleteArticle(BarMessageManager manager, GMFMessage message) {
        stat_click_event(UmengUtil.eEVENTIDTopicDetailDelete);
        GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity());
        progressDialog.show();
        consumeEventMR(ChatController.deleteBarMessage(manager, message))
                .onNextStart(response -> progressDialog.dismiss())
                .onNextSuccess(response -> {
                    showToast(this, "删除成功");
                    NotificationCenter.onMessageStateChangedSubject.onNext(null);
                    goBack(this);
                })
                .onNextFail(response -> showToast(this, getErrorMessage(response)))
                .done();
    }

    private void replaceContentFragment(BarMessageManager manager) {
        int addButtonType = manager.getSession().addButtonType;
        if (anyMatch(addButtonType, MessageSession.Session_Add_Type_normal_noAdd, MessageSession.Session_Add_Type_normal_2)) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.section_content, new CircleDetailPageMoodFragment().init(new Bundle()));
            transaction.commit();
            changeVisibleSection(TYPE_CONTENT);
        } else if (addButtonType == MessageSession.Session_Add_Type_Intelligence) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.section_content, new CircleDetailPageIntelligenceFragment().init(new Bundle()));
            transaction.commit();
            changeVisibleSection(TYPE_CONTENT);
        } else {
            createErrorDialog(this, getErrorMessage(emptyList())).show();
        }
    }

    public static class CircleDetailPageIntelligenceFragment extends BaseFragment {
        private IntCounter counter = CircleHelper.createRewardCounter();

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_circle_detail_page_inteligence, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            resetContentView(getMessageManager(this), getMessage(this));
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            counter.release();
        }

        private void resetContentView(BarMessageManager manager, GMFMessage message) {
            Context ctx = getActivity();
            Resources res = ctx.getResources();
            CircleHelper.CellVM vm = new CircleHelper.CellVM(manager.getSession(), message).optimize();

            ViewGroup headerSection = v_findView(this, R.id.section_header);
            View cell = headerSection.getChildAt(0);
            ChildBinder binder = ChildBinders.createWithView(cell);
            CircleHelper.bindChildViews(binder);
            Func0<Boolean> ignoreAvatarClickEvent = () -> false;
            Action0 rewardCallback = () -> {
            };
            CircleHelper.afterViewHolderCreated(cell, () -> vm, counter, null, ignoreAvatarClickEvent, rewardCallback);
            v_setClick(cell, v -> {
            });
            int flags = CircleHelper.FLAG_DISALLOW_LIMIT_LINE_COUNT | CircleHelper.FLAG_HIDE_COMMENTS | CircleHelper.FLAG_HIDE_RATE_BAR;
            Func0<Boolean> isContentNotChange = () -> true;
            CircleHelper.configureView(binder, isContentNotChange, vm, flags);

            View likeUserSection = v_findView(this, R.id.section_like_user);
            v_setText(likeUserSection, R.id.label_like_user, setColor(String.format(Locale.getDefault(), "%d人觉得值", vm.likeUsers.size()), vm.isLikeArticle ? TEXT_RED_COLOR : TEXT_GREY_COLOR));
            LinearLayout likeUserGallery = v_findView(likeUserSection, R.id.gallery_like_user);
            likeUserGallery.removeAllViewsInLayout();
            Stream.of(vm.likeUsers)
                    .limit(7)
                    .forEach(user -> {
                        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(res)
                                .setRoundingParams(new RoundingParams().setRoundAsCircle(true))
                                .setPlaceholderImage(res.getDrawable(R.mipmap.ic_avatar_placeholder))
                                .build();
                        SimpleDraweeView draweeView = new SimpleDraweeView(ctx, hierarchy);
                        String avatarURL = safeGet(() -> user.getPhotoUrl(), "");
                        v_setImageUri(draweeView, avatarURL);

                        likeUserGallery.addView(draweeView, apply(new LinearLayout.LayoutParams(dp2px(20), dp2px(20)), params -> {
                            params.leftMargin = dp2px(4);
                        }));
                    });
            if (vm.likeUsers.size() > 7) {
                ImageView moreImage = new ImageView(ctx);
                moreImage.setImageResource(R.mipmap.ic_circle_more);
                likeUserGallery.addView(moreImage, apply(new LinearLayout.LayoutParams(dp2px(20), dp2px(20)), params -> {
                    params.leftMargin = dp2px(4);
                }));
            }
            v_setClick(likeUserSection, v -> {
                if (!vm.likeUsers.isEmpty()) {
                    showActivity(this, an_CircleRateUserListPage(manager.getSession(), message, 1));
                }
            });

            View dislikeUserSection = v_findView(this, R.id.section_dislike_user);
            v_setText(dislikeUserSection, R.id.label_dislike_user, setColor(String.format(Locale.getDefault(), "%d人觉得坑", vm.dislikeUsers.size()), vm.isDislikeArticle ? TEXT_RED_COLOR : TEXT_GREY_COLOR));
            LinearLayout dislikeUserGallery = v_findView(dislikeUserSection, R.id.gallery_dislike_user);
            dislikeUserGallery.removeAllViewsInLayout();
            Stream.of(vm.dislikeUsers)
                    .limit(7)
                    .forEach(user -> {
                        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(res)
                                .setRoundingParams(new RoundingParams().setRoundAsCircle(true))
                                .setPlaceholderImage(res.getDrawable(R.mipmap.ic_avatar_placeholder))
                                .build();
                        SimpleDraweeView draweeView = new SimpleDraweeView(ctx, hierarchy);
                        String avatarURL = safeGet(() -> user.getPhotoUrl(), "");
                        v_setImageUri(draweeView, avatarURL);

                        dislikeUserGallery.addView(draweeView, apply(new LinearLayout.LayoutParams(dp2px(20), dp2px(20)), params -> {
                            params.leftMargin = dp2px(4);
                        }));
                    });
            if (vm.dislikeUsers.size() > 7) {
                ImageView moreImage = new ImageView(ctx);
                moreImage.setImageResource(R.mipmap.ic_circle_more);
                dislikeUserGallery.addView(moreImage, apply(new LinearLayout.LayoutParams(dp2px(20), dp2px(20)), params -> {
                    params.leftMargin = dp2px(4);
                }));
            }
            v_setClick(dislikeUserSection, v -> {
                if (!vm.dislikeUsers.isEmpty()) {
                    showActivity(this, an_CircleRateUserListPage(manager.getSession(), message, 0));
                }
            });


            resetCards(manager, message, vm);
            resetHintLabel(message, vm);
        }

        @SuppressWarnings("deprecation")
        private void resetCards(BarMessageManager manager, GMFMessage message, CircleHelper.CellVM vm) {
            boolean isSendByOther = !GMFMessage.isSendByMe(message);
            boolean isLocked = isSendByOther && anyMatch(vm.mosaicType, CircleHelper.CellVM.MOSAIC_TYPE_LOCKED);
            boolean isUnlocked = isSendByOther && anyMatch(vm.mosaicType, CircleHelper.CellVM.MOSAIC_TYPE_UNLOCKED);
            boolean isLike = isSendByOther && anyMatch(vm.mosaicType, CircleHelper.CellVM.MOSAIC_TYPE_LIKE);
            boolean isDislike = isSendByOther && anyMatch(vm.mosaicType, CircleHelper.CellVM.MOSAIC_TYPE_DISLIKE);

            View payCard = v_findView(this, R.id.card_pay);
            if (isLocked) {
                payCard.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4))));
                Button payBtn = v_findView(payCard, R.id.btn_pay);
                boolean hasEnoughScore = safeGet(() -> ScoreManager.getInstance().account.cashBalance >= vm.intelligenceScore, false);
                if (hasEnoughScore) {
                    payBtn.setText(String.format(Locale.getDefault(), "立即支付%d积分", vm.intelligenceScore));
                    v_setClick(payBtn, v -> {
                        stat_click_event(UmengUtil.eEVENTIDTopicDetailUnlock);
                        GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                        dialog.show();
                        consumeEventMR(ChatController.unlockBarMessage(manager, message).delay(300, TimeUnit.MILLISECONDS))
                                .setTag("unlock_message")
                                .onNextStart(response -> dialog.dismiss())
                                .onNextSuccess(response -> {
                                    NotificationCenter.onMessageStateChangedSubject.onNext(null);
                                    resetContentView(manager, message);
                                })
                                .onNextFail(response -> showToast(this, getErrorMessage(response)))
                                .done();
                    });
                } else {
                    payBtn.setText("积分不足，立即去赚");
                    v_setClick(payBtn, v -> {
                        showActivity(this, an_ScoreHomePage());
                    });
                }
                payBtn.post(() -> {
                    int radius = payBtn.getMeasuredHeight() >> 1;
                    payBtn.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(YELLOW_COLOR, radius)));
                });
                animateToShow(payCard);
            } else {
                v_setGone(payCard);
            }

            View rateCard = v_findView(this, R.id.card_rate);
            if (isUnlocked) {
                rateCard.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4))));
                View likeBtn = v_findView(rateCard, R.id.btn_like);
                likeBtn.post(() -> {
                    int raidus = likeBtn.getMeasuredHeight() >> 1;
                    likeBtn.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(RED_COLOR, raidus)));
                });
                v_setClick(likeBtn, v -> {
                    stat_click_event(UmengUtil.eEVENTIDTopicDetailLike);
                    GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                    dialog.show();
                    consumeEventMR(ChatController.likeBarMessage(manager, message))
                            .setTag("like_message")
                            .onNextStart(response -> dialog.dismiss())
                            .onNextSuccess(response -> {
                                NotificationCenter.onMessageStateChangedSubject.onNext(null);
                                resetContentView(manager, message);
                            })
                            .onNextFail(response -> showToast(this, getErrorMessage(response)))
                            .done();
                });
                View dislikeBtn = v_findView(rateCard, R.id.btn_dislike);
                dislikeBtn.post(() -> {
                    int raidus = dislikeBtn.getMeasuredHeight() >> 1;
                    dislikeBtn.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(GREY_COLOR, raidus)));
                });
                v_setClick(dislikeBtn, v -> {
                    stat_click_event(UmengUtil.eEVENTIDTopicDetailDislike);
                    GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                    dialog.show();
                    consumeEventMR(ChatController.dislikeBarMessage(manager, message))
                            .setTag("dislike_message")
                            .onNextStart(response -> dialog.dismiss())
                            .onNextSuccess(response -> {
                                NotificationCenter.onMessageStateChangedSubject.onNext(null);
                                resetContentView(manager, message);
                            })
                            .onNextFail(response -> showToast(this, getErrorMessage(response)))
                            .done();
                });
                animateToShow(rateCard);
            } else {
                v_setGone(rateCard);
            }

            View afterRateView = v_findView(this, R.id.card_after_rate);
            if (isLike || isDislike) {
                afterRateView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4))));

                int iconResID;
                String text;
                if (isLike) {
                    iconResID = R.mipmap.ic_like_big;
                    text = String.format(Locale.getDefault(), "超值！作者将会收到我的%d积分！", vm.intelligenceScore);
                } else {
                    iconResID = R.mipmap.ic_dislike_big;
                    text = String.format(Locale.getDefault(), "坑爹！%d积分才不给作者！", vm.intelligenceScore);
                }
                v_setImageResource(v_findView(afterRateView, R.id.img_after_rate), iconResID);
                v_setText(v_findView(afterRateView, R.id.label_after_rate), text);

                View sendBtn = v_findView(afterRateView, R.id.btn_send);
                sendBtn.post(() -> {
                    int radius = sendBtn.getMeasuredHeight() >> 1;
                    sendBtn.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(YELLOW_COLOR, radius)));
                });
                v_setClick(sendBtn, v -> {
                    goBack(this);
                    CircleHelper.goToWriteFragment(getActivity(), safeGet(() -> manager.getSession(), null));
                });
                animateToShow(afterRateView);
            } else {
                v_setGone(afterRateView);
            }
        }

        private void animateToShow(View card) {
            card.setVisibility(View.INVISIBLE);
            card.post(() -> {
                card.setVisibility(View.VISIBLE);
                PropertyValuesHolder translationX = PropertyValuesHolder.ofFloat("translationX", card.getMeasuredWidth(), 0f);
                PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0, 1);
                Animator animator = ObjectAnimator.ofPropertyValuesHolder(card, translationX, alpha);
                animator.setInterpolator(new FastOutSlowInInterpolator());
                animator.start();
            });
        }

        @SuppressWarnings("CodeBlock2Expr")
        private void resetHintLabel(GMFMessage message, CircleHelper.CellVM vm) {
            boolean isSendByMe = GMFMessage.isSendByMe(message);
            boolean isUnlocked = anyMatch(vm.mosaicType, CircleHelper.CellVM.MOSAIC_TYPE_UNLOCKED);

            TextView hintLabel = v_findView(this, R.id.label_hint);
            hintLabel.setMovementMethod(new LinkMovementMethod());
            if (isSendByMe) {
                v_setText(hintLabel, String.format(Locale.getDefault(), "你的情报定价%d积分，已获得%d积分", vm.intelligenceScore, vm.gainedScore));
            } else if (isUnlocked) {
                v_setText(hintLabel, "24小时内没有评价将自动选择超值");
            } else {
                v_setText(hintLabel, concatNoBreak("积分可以通过在", setColor(setClickEvent("积分商城", v -> {
                    showActivity(this, an_ScoreHomePage());
                }), TEXT_BLUE_COLOR), "做任务获得"));
            }
        }
    }


    public static class CircleDetailPageMoodFragment extends BaseFragment {

        private boolean mIsFetchingMore = false;
        private SwipeRefreshLayout mRefreshLayout;
        private IntCounter counter = CircleHelper.createRewardCounter();

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_circle_detail_page_mood, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mRefreshLayout = v_findView(this, R.id.refreshLayout);

            if (getMessageManager(this) != null && getMessage(this) != null) {
                resetContentView(getMessageManager(this), getMessage(this), new LinkedList<>(), false);
            } else {
                goBack(this);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mIsFetchingMore = false;
            counter.release();
        }

        private void resetContentView(BarMessageManager manager, GMFMessage message, List<RemaindFeed> localFeeds, boolean scrollToBottom) {
            resetRefreshLayout(manager, message, localFeeds);
            resetRecyclerView(manager, message, localFeeds, scrollToBottom);
            resetBottomBar(manager, message, localFeeds);
        }

        @SuppressWarnings("CodeBlock2Expr")
        private void resetRefreshLayout(BarMessageManager manager, GMFMessage message, List<RemaindFeed> localFeeds) {
            mRefreshLayout.setOnRefreshListener(() -> {
                consumeEventMR(PageArrayHelper.getPreviousPage(message.commentList))
                        .setTag("fetch_previous")
                        .onNextStart(response -> mRefreshLayout.setRefreshing(false))
                        .onNextSuccess(response -> {
                            resetRecyclerView(manager, message, localFeeds, false);
                        })
                        .done();
            });
        }

        @SuppressWarnings("CodeBlock2Expr")
        private void resetRecyclerView(final BarMessageManager manager, final GMFMessage message, final List<RemaindFeed> localFeeds, boolean scrollToBottom) {
            List<CommentVM> items = CommentVM.createFromGMFMessage(message);
            List<CommentVM> localItems = CommentVM.createFromGMFMessage(localFeeds);
            items.addAll(localItems);

            {
                Iterator<CommentVM> iterator = items.iterator();
                LinkedList<CommentVM> distinctItems = new LinkedList<>();
                CommentVM next;
                while (iterator.hasNext()) {
                    next = iterator.next();
                    if (!distinctItems.contains(next)) {
                        distinctItems.add(next);
                    }
                }
                items = distinctItems;
            }

            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<CommentVM> adapter = getSimpleAdapter(recyclerView);
                adapter.resetItems(items);
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                addHorizontalSepLine(recyclerView);
                addContentInset(recyclerView, new Rect(0, 0, 0, dp2px(48)));
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (!mIsFetchingMore && isScrollToBottom(recyclerView) && PageArrayHelper.hasMoreData(message.commentList)) {
                            mIsFetchingMore = true;
                            consumeEventMR(PageArrayHelper.getNextPage(message.commentList))
                                    .setTag("fetch_more")
                                    .onNextStart(response -> mIsFetchingMore = false)
                                    .onNextSuccess(response -> resetContentView(manager, message, localFeeds, false))
                                    .done();
                        }
                    }
                });


                SimpleRecyclerViewAdapter<CommentVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_circle_comment)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("avatarImage", R.id.img_avatar)
                                    .bindChildWithTag("nameLabel", R.id.label_name)
                                    .bindChildWithTag("contentLabel", R.id.label_content)
                                    .bindChildWithTag("dateLabel", R.id.label_date)
                                    .configureView((holder, item, pos) -> {
                                        UserAvatarView avatarView = builder.getChildWithTag("avatarImage");
                                        avatarView.updateView(item.avatarURL, item.userType);

                                        v_setText(builder.getChildWithTag("nameLabel"), item.name);
                                        v_setText(builder.getChildWithTag("contentLabel"), item.content);
                                        v_setText(builder.getChildWithTag("dateLabel"), item.date);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {

                            View avatarImage = v_findView(holder.itemView, R.id.img_avatar);
                            v_reviseTouchArea(avatarImage);
                            v_setClick(avatarImage, v -> {
                                CommentVM item = ad.getItem(holder.getAdapterPosition());
                                User user = safeGet(() -> item.raw.user, null);
                                showActivity(this, an_UserDetailPage(user));
                            });
                            v_setClick(holder.itemView, v -> {
                                CommentVM item = ad.getItem(holder.getAdapterPosition());
                                boolean isSendByOther = safeGet(() -> item.userID != MineManager.getInstance().getmMe().index, false);
                                if (isSendByOther) {
                                    View bottomBar = v_findView(this, R.id.bar_bottom);
                                    EditText contentField = v_findView(bottomBar, R.id.field_content);
                                    contentField.setText(String.format("回复 %s: ", item.name));
                                    contentField.setSelection(contentField.getText().toString().length());
                                    contentField.requestFocus();
                                    runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 500L);
                                }
                            });
                            View moreImage = v_findView(holder.itemView, R.id.img_more);
                            v_reviseTouchArea(moreImage);
                            v_setClick(moreImage, v -> {
                                CommentVM item = ad.getItem(holder.getAdapterPosition());
                                boolean isSendByMe = safeGet(() -> item.userID == MineManager.getInstance().getmMe().index, false);
                                if (isSendByMe) {
                                    GMFBottomSheet sheet = new GMFBottomSheet.Builder(getActivity())
                                            .setTitle(item.name + ": " + item.content)
                                            .addContentItem(new GMFBottomSheet.BottomSheetItem("delete", "删除评论", R.mipmap.ic_bottomsheet_delete))
                                            .addContentItem(new GMFBottomSheet.BottomSheetItem("copy", "复制", R.mipmap.ic_bottomsheet_copy))
                                            .create();
                                    sheet.setOnItemClickListener((bottomSheet, sheetItem) -> {
                                        bottomSheet.dismiss();
                                    });
                                    sheet.setOnItemClickListener((bottomSheet, sheetItem) -> {
                                        bottomSheet.dismiss();
                                        String tag = safeGet(() -> sheetItem.tag.toString(), "");
                                        if (StringExtension.anyMatch(tag, "delete")) {
                                            GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                                            dialog.show();
                                            consumeEventMR(ChatController.deleteBarComment(manager, message, item.raw))
                                                    .onNextStart(response -> dialog.dismiss())
                                                    .onNextSuccess(response -> {
                                                        List<RemaindFeed> newLocalFeeds = Stream.of(localFeeds).filter(it -> notMatch(it.fid, item.raw.fid)).collect(Collectors.toList());
                                                        resetContentView(manager, message, newLocalFeeds, false);
                                                        NotificationCenter.onMessageStateChangedSubject.onNext(null);
                                                    })
                                                    .onNextFail(response -> showToast(this, getErrorMessage(response)))
                                                    .done();
                                        } else if (StringExtension.anyMatch(tag, "copy")) {
                                            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                                            cm.setPrimaryClip(ClipData.newPlainText(null, item.name + ": " + item.content));
                                        }
                                    });
                                    sheet.show();
                                } else {
                                    GMFBottomSheet sheet = new GMFBottomSheet.Builder(getActivity())
                                            .setTitle(item.name + ": " + item.content)
                                            .addContentItem(new GMFBottomSheet.BottomSheetItem("report", "举报", R.mipmap.ic_bottomsheet_camera))
                                            .addContentItem(new GMFBottomSheet.BottomSheetItem("copy", "复制", R.mipmap.ic_bottomsheet_copy))
                                            .create();
                                    sheet.setOnItemClickListener((bottomSheet, sheetItem) -> {
                                        bottomSheet.dismiss();
                                        String tag = safeGet(() -> sheetItem.tag.toString(), "");
                                        if (StringExtension.anyMatch(tag, "copy")) {
                                            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                                            cm.setPrimaryClip(ClipData.newPlainText(null, item.name + ": " + item.content));
                                            showToast(this, "复制成功");
                                        } else if (StringExtension.anyMatch(tag, "report")) {
                                            ShareInfo shareInfo = message.shareInfo;
                                            if (shareInfo != null) {
                                                stat_click_event(UmengUtil.eEVENTIDTopicDetailReportComment);
                                                consumeEventMR(ChatController.getMessageManager(2, "10000", ""))
                                                        .onNextSuccess(response -> {
                                                            MessageManager supportManager = response.data;
                                                            String text = String.format(Locale.getDefault(), "【举报】 %s 来自%s的评论" + "\"" + "%s" + "\"", shareInfo.url, item.name, item.content);
                                                            supportManager.saveLocalMessage(new SendMessage(text));
                                                            showActivity(this, an_FeedbackPage(opt(this)));
                                                        })
                                                        .done();
                                            }
                                        }
                                    });
                                    sheet.show();
                                }
                            });
                        })
                        .create();
                adapter.addHeader(adapter.createHeaderView(this, R.layout.header_circle_detail_page_mood));
                recyclerView.setAdapter(adapter);
            }

            SimpleRecyclerViewAdapter<CommentVM> adapter = getSimpleAdapter(recyclerView);
            resetHeader(manager, message, adapter.getHeader(0));
            int itemCount = adapter.getItemCount();
            if (itemCount > 0 && scrollToBottom) {
                recyclerView.smoothScrollToPosition(itemCount - 1);
            }
        }

        @SuppressWarnings("deprecation")
        private void resetBottomBar(BarMessageManager manager, GMFMessage message, List<RemaindFeed> localFeeds) {
            View bottomBar = v_findView(this, R.id.bar_bottom);
            EditText contentField = v_findView(bottomBar, R.id.field_content);
            contentField.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, dp2px(4)).border(LINE_SEP_COLOR, dp2px(1))));
            v_setClick(bottomBar, R.id.btn_send, v -> {
                hideKeyboardFromWindow(this);
                String content = contentField.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                    dialog.setMessage("评论中...");
                    dialog.show();

                    RemaindFeed comment = new RemaindFeed();
                    comment.text = content;
                    comment.user = MineManager.getInstance().getmMe();
                    comment.createTime = SecondUtil.currentSecond();
                    consumeEventMR(ChatController.sendComment(manager, message, comment, 0))
                            .setTag("send_comment")
                            .onNextStart(response -> dialog.dismiss())
                            .onNextSuccess(response -> {
                                if (content.startsWith("回复 ")) {
                                    stat_click_event(UmengUtil.eEVENTIDTopicDetailReplyComment);
                                } else {
                                    stat_click_event(UmengUtil.eEVENTIDTopicDetailComment);
                                }


                                contentField.setText("");
                                showToast(this, "评论成功");
                                localFeeds.add(comment);
                                resetRecyclerView(manager, message, localFeeds, true);
                                NotificationCenter.onWriteNewCommentSubject.onNext(null);
                            })
                            .onNextFail(response -> showToast(this, getErrorMessage(response)))
                            .done();
                }
            });
        }

        private void resetHeader(BarMessageManager manager, GMFMessage message, View header) {
            CircleHelper.CellVM vm = new CircleHelper.CellVM(manager.getSession(), message).optimize();
            View cell = ViewGroup.class.cast(header).getChildAt(0);
            ViewGroup rewardSection = v_findView(header, R.id.section_reward);

            ChildBinder binder = ChildBinders.createWithView(cell);
            CircleHelper.bindChildViews(binder);
            Func0<CircleHelper.CellVM> itemGetter = () -> vm;
            FrameLayout rateAnimViewContainer = v_findView(this, R.id.layer_top);
            Func0<Boolean> ignoreAvatarClickEvent = () -> false;
            Action0 rewardCallback = () -> {
                resetUserGallery(rewardSection, manager, message);
            };
            CircleHelper.afterViewHolderCreated(cell, itemGetter, counter, rateAnimViewContainer, ignoreAvatarClickEvent, rewardCallback);
            int flags = CircleHelper.FLAG_SHOW_USER_TYPE | CircleHelper.FLAG_HIDE_COMMENTS | CircleHelper.FLAG_DISALLOW_LIMIT_LINE_COUNT;
            Func0<Boolean> isContentNotChange = () -> true;
            CircleHelper.configureView(binder, isContentNotChange, vm, flags);
            v_setClick(cell, v -> {
            });
            resetUserGallery(rewardSection, manager, message);
            v_setVisibility(rewardSection, vm.hasInteligence ? View.GONE : View.VISIBLE);

            View commentHeader = v_findView(header, R.id.header_comment);
            v_setVisibility(commentHeader, R.id.span_empty, vm.comments.isEmpty() ? View.VISIBLE : View.GONE);

        }

        @SuppressWarnings("deprecation")
        private void resetUserGallery(View rewardSection, BarMessageManager manager, GMFMessage message) {
            final int MAX_CELL_COUNT = 7;

            Context ctx = rewardSection.getContext();

            LinearLayout userGallery = v_findView(rewardSection, R.id.gallery_user);
            View emptySpan = v_findView(rewardSection, R.id.span_empty);
            List<RemaindFeed> rewards = safeGet(() -> message.donateList, Collections.<RemaindFeed>emptyList());
            v_globalLayout(userGallery, true, gallery -> {
                int parentWidth = getScreenSize(ctx).width();
                int cellMinWidth = dp2px(30);
                int remainingSpace = parentWidth - cellMinWidth * MAX_CELL_COUNT;
                int cellWidth = cellMinWidth + (remainingSpace / MAX_CELL_COUNT);

                int[] index = {0};
                Stream.of(rewards)
                        .limit(MAX_CELL_COUNT)
                        .forEach(feed -> {
                            if (index[0] < userGallery.getChildCount()) {
                                LinearLayout userCell = (LinearLayout) userGallery.getChildAt(index[0]);
                                String imageURL = safeGet(() -> feed.user.getPhotoUrl(), "");
                                int userType = safeGet(() -> feed.user.type, User_Type.Custom);
                                UserAvatarView avatarImage = (UserAvatarView) userCell.getChildAt(0);
                                avatarImage.updateView(imageURL, userType);
                                TextView countLabel = (TextView) userCell.getChildAt(1);
                                String scoreText = safeGet(() -> String.valueOf(feed.score), "0");
                                v_setText(countLabel, scoreText);
                                v_setClick(userCell, v -> {
                                    consumeEventMR(ChatController.commitDonateImmediately(manager, message)).done();
                                    showActivity(this, an_CircleRateUserListPage(manager.getSession(), message, 2));
                                });
                            } else {
                                LinearLayout userCell = new LinearLayout(ctx);
                                userCell.setOrientation(LinearLayout.VERTICAL);

                                {

                                    UserAvatarView avatarImage = new UserAvatarView(ctx);
                                    String imageURL = safeGet(() -> feed.user.getPhotoUrl(), "");
                                    int userType = safeGet(() -> feed.user.type, User_Type.Custom);
                                    avatarImage.updateView(imageURL, userType);
                                    userCell.addView(avatarImage, apply(new LinearLayout.LayoutParams(-2, -2), params -> {
                                        params.gravity = Gravity.CENTER_HORIZONTAL;
                                    }));

                                    TextView countLabel = new TextView(ctx);
                                    countLabel.setTextSize(12);
                                    countLabel.setTextColor(TEXT_GREY_COLOR);
                                    String scoreText = safeGet(() -> String.valueOf(feed.score), "0");
                                    countLabel.setText(scoreText);
                                    countLabel.setLines(1);
                                    countLabel.setEllipsize(TextUtils.TruncateAt.END);
                                    userCell.addView(countLabel, apply(new LinearLayout.LayoutParams(-2, -2), params -> {
                                        params.gravity = Gravity.CENTER_HORIZONTAL;
                                    }));
                                }

                                v_setClick(userCell, v -> {
                                    consumeEventMR(ChatController.commitDonateImmediately(manager, message)).done();
                                    showActivity(this, an_CircleRateUserListPage(manager.getSession(), message, 2));
                                });

                                userGallery.addView(userCell, new LinearLayout.LayoutParams(cellWidth, -2));
                            }

                            index[0]++;
                        });

                int maxChildCount = Math.min(rewards.size(), MAX_CELL_COUNT);
                int removeChildCount = maxChildCount - userGallery.getChildCount();
                while (removeChildCount > 0) {
                    userGallery.removeViewAt(userGallery.getChildCount() - 1);
                    removeChildCount--;
                }
            });
            v_setVisibility(emptySpan, rewards.isEmpty() ? View.VISIBLE : View.GONE);
            v_setVisibility(userGallery, rewards.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private static class CommentVM {
        RemaindFeed raw;
        int userID;
        String commentID;
        int userType;
        String avatarURL;
        CharSequence name;
        CharSequence content;
        CharSequence date;

        public static List<CommentVM> createFromGMFMessage(GMFMessage message) {
            return Stream.of(safeGet(() -> message.commentList.data(), emptyList())).map(it -> new CommentVM(it)).collect(Collectors.toList());
        }

        public static List<CommentVM> createFromGMFMessage(List<RemaindFeed> feeds) {
            return Stream.of(safeGet(() -> feeds, emptyList())).map(it -> new CommentVM(it)).collect(Collectors.toList());
        }

        @Override
        public int hashCode() {
            int result = commentID != null ? commentID.hashCode() : 0;
            result = 31 * result + (avatarURL != null ? avatarURL.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (content != null ? content.hashCode() : 0);
            result = 31 * result + (date != null ? date.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CommentVM) {
                String otherCommendID = ((CommentVM) o).commentID;
                if (!TextUtils.isEmpty(commentID) && !TextUtils.isEmpty(otherCommendID) && commentID.equals(otherCommendID)) {
                    return true;
                }
            }

            return super.equals(o);
        }

        public CommentVM(RemaindFeed raw) {
            this.raw = raw;
            this.userID = safeGet(() -> raw.user.index, -1);
            this.commentID = safeGet(() -> raw.fid, "");
            this.userType = safeGet(() -> raw.user.type, User_Type.Custom);
            this.avatarURL = safeGet(() -> raw.user.getPhotoUrl(), "");
            this.name = safeGet(() -> raw.user.getName(), PlaceHolder.NULL_VALUE);
            this.content = safeGet(() -> raw.text, "");
            this.date = safeGet(() -> formatTimeByNow(raw.createTime), PlaceHolder.NULL_VALUE);
        }
    }
}
