package com.goldmf.GMFund.controller.circle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Space;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.BaseActivity;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.internal.ChildBinder;
import com.goldmf.GMFund.controller.internal.ChildBinders;
import com.goldmf.GMFund.extension.SpannableStringExtension.ImageTextParams;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.GMFMessage.Intelligence;
import com.goldmf.GMFund.model.GMFMessage.ShortcutImagePair;
import com.goldmf.GMFund.model.RemaindFeed;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.IntCounter;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.CircleTextView;
import com.goldmf.GMFund.widget.StaticTableView;
import com.goldmf.GMFund.widget.UserAvatarView;
import com.orhanobut.logger.Logger;

import net.qiujuer.genius.blur.StackBlur;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Action0;
import rx.functions.Func0;
import yale.extension.system.SimpleRecyclerViewAdapter;
import yale.extension.system.SimpleViewHolder;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_PhotoViewerPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ScoreHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WriteIntelligencePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WriteMoodPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_LIGHT_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.extension.BitmapExtension.getCacheImage;
import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ListExtension.splitFromList;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.appendImageText;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setStyle;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_getGlobalHitRect;
import static com.goldmf.GMFund.extension.ViewExtension.v_isTruncate;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_removeFromParent;
import static com.goldmf.GMFund.extension.ViewExtension.v_reviseTouchArea;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setLongClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;
import static com.goldmf.GMFund.model.GMFMessage.Intelligence.*;
import static com.goldmf.GMFund.model.GMFMessage.Message_Image_2;
import static com.goldmf.GMFund.model.GMFMessage.Message_TarLink_2;
import static com.goldmf.GMFund.model.GMFMessage.Message_Text_2;
import static com.goldmf.GMFund.model.GMFMessage.isValidIntelligence;
import static com.goldmf.GMFund.util.FormatUtil.formatTimeByNow;
import static com.goldmf.GMFund.util.UmengUtil.stat_click_event;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;

/**
 * Created by yale on 16/5/10.
 */
public class CircleHelper {

    private CircleHelper() {
    }

    public static void goToWriteFragment(Context context, MessageSession session) {
        if (session != null) {
            int messageType = session.messageType;
            String linkID = session.linkID;
            String sessionID = session.sessionID;


            boolean stat = safeGet(() -> MyApplication.getTopFragmentOrNil().get() instanceof CircleListFragment, false);

            if (anyMatch(session.addButtonType, MessageSession.Session_Add_Type_normal_2)) {
                showActivity(context, an_WriteMoodPage(messageType, linkID, sessionID));
                if (stat) {
                    stat_click_event(UmengUtil.eEVENTIDTopicListWriteMood);
                }
            } else if (anyMatch(session.addButtonType, MessageSession.Session_Add_Type_Intelligence)) {
                showActivity(context, an_WriteIntelligencePage(messageType, linkID, sessionID));
                if (stat) {
                    stat_click_event(UmengUtil.eEVENTIDTopicListWriteIntelligence);
                }
            }
        }
    }

    public static IntCounter createRewardCounter() {
        return new IntCounter() {
            @Override
            public void onValueChanged(int value) {
                if (value >= 10) {
                    this.setValue(0);
                    MyApplication.post(() -> {
                        showToast(MyApplication.SHARE_INSTANCE, "长按打赏按钮可以批量打赏哦~~");
                    });
                }
            }
        };
    }

    public static final int FLAG_SHOW_BAR_INFO = 1;
    public static final int FLAG_SHOW_USER_TYPE = 1 << 1;
    public static final int FLAG_HIDE_COMMENTS = 1 << 2;
    public static final int FLAG_DISALLOW_LIMIT_LINE_COUNT = 1 << 3;
    public static final int FLAG_HIDE_RATE_BAR = 1 << 4;
    public static final int FLAG_HIDE_RATE_BTN = 1 << 5;
    public static final int FLAG_HIDE_BAR_SEP_LINE = 1 << 6;

    public interface ItemStore<T> {
        T getItem(int position);
    }

    public static <T> ItemStore<T> createItemStore(SimpleRecyclerViewAdapter<T> adapter) {
        return position -> adapter.getItem(position);
    }

    public static SimpleViewHolder<CellVM> createViewHolder(SimpleViewHolder.Builder<CellVM> builder, int flags) {
        bindChildViews(ChildBinders.createWithBuilder(builder));
        builder.configureView((holder, vm, pos) -> {
            Func0<Boolean> isContentNotChange = () -> holder.getAdapterPosition() == pos;
            configureView(ChildBinders.createWithBuilder(builder), isContentNotChange, vm, flags);
        });
        return builder.create();
    }

    public static void bindChildViews(ChildBinder binder) {
        binder.bindChildWithTag("avatarImage", R.id.area_user, R.id.img_avatar)
                .bindChildWithTag("nameLabel", R.id.area_user, R.id.label_name)
                .bindChildWithTag("timeLabel", R.id.area_user, R.id.label_time)
                .bindChildWithTag("contentLabel", R.id.label_content)
                .bindChildWithTag("expandLabel", R.id.label_expand)
                .bindChildWithTag("linkArea", R.id.area_link)
                .bindChildWithTag("linkImage", R.id.area_link, R.id.img_link)
                .bindChildWithTag("linkSpan", R.id.area_link, R.id.span_link)
                .bindChildWithTag("scoreLabel", R.id.area_link, R.id.label_score)
                .bindChildWithTag("linkDescLabel", R.id.area_link, R.id.label_link_desc)
                .bindChildWithTag("overlapImage", R.id.img_overlap)
                .bindChildWithTag("imageTable", R.id.table_img)
                .bindChildWithTag("commentList", R.id.list_comment)
                .bindChildWithTag("rateArea", R.id.area_rate)
                .bindChildWithTag("rateLabel", R.id.area_rate, R.id.label_rate)
                .bindChildWithTag("rateBtn", R.id.area_rate, R.id.btn_rate)
                .bindChildWithTag("barLine", R.id.line_bar);
    }

    public static void configureView(ChildBinder binder, Func0<Boolean> isContentNotChange, final CellVM vm, int flags) {
        boolean showBarInfo = hasFlag(flags, FLAG_SHOW_BAR_INFO);
        boolean showUserType = hasFlag(flags, FLAG_SHOW_USER_TYPE);
        boolean hideComments = hasFlag(flags, FLAG_HIDE_COMMENTS);
        boolean disallowLimitLineCount = hasFlag(flags, FLAG_DISALLOW_LIMIT_LINE_COUNT);
        boolean hideRateBar = hasFlag(flags, FLAG_HIDE_RATE_BAR);
        boolean hideRateBtn = hasFlag(flags, FLAG_HIDE_RATE_BTN);
        boolean hideBarSepLine = hasFlag(flags, FLAG_HIDE_BAR_SEP_LINE);

        String barName = safeGet(() -> vm.session.title, "");
        showBarInfo = showBarInfo && !TextUtils.isEmpty(barName);

        UserAvatarView avatarView = binder.getChildWithTag("avatarImage");
        avatarView.updateView(vm.userAvatarURL, showUserType ? vm.userType : User.User_Type.Custom);
        v_setText(binder.getChildWithTag("nameLabel"), showBarInfo ? concatNoBreak(vm.userName, setFontSize(setColor(" @" + barName, TEXT_GREY_COLOR), sp2px(12))) : vm.userName);
        v_setText(binder.getChildWithTag("timeLabel"), vm.createTime);

        TextView contentLabel = binder.getChildWithTag("contentLabel");
        contentLabel.setMaxLines(disallowLimitLineCount ? Integer.MAX_VALUE : vm.contentMaxLine);
        v_setText(contentLabel, vm.createDecoratedString());
        v_setVisibility(contentLabel, TextUtils.isEmpty(vm.rawContent) ? View.GONE : View.VISIBLE);
        View expandLabel = binder.getChildWithTag("expandLabel");
        contentLabel.post(() -> {
            v_setVisibility(expandLabel, v_isTruncate(contentLabel) ? View.VISIBLE : View.GONE);
        });

        StaticTableView imageTable = binder.getChildWithTag("imageTable");
        resetImageTable(imageTable, vm, () -> vm.hasMosaicImage && isContentNotChange.call(), () -> true);
        v_setVisibility(imageTable, vm.imageList.isEmpty() ? View.GONE : View.VISIBLE);
        v_updateLayoutParams(imageTable, MarginLayoutParams.class, params -> {
            boolean isContentLabelVisible = v_isVisible(contentLabel);
            params.topMargin = isContentLabelVisible ? dp2px(16) : 0;
        });

        resetLinkArea(binder.itemView(), vm, () -> vm.hasMosaicLink && isContentNotChange.call());
        View linkArea = binder.getChildWithTag("linkArea");
        v_updateLayoutParams(linkArea, MarginLayoutParams.class, parmas -> {
            boolean isContentLabelVisible = v_isVisible(contentLabel);
            boolean isImageTableVisible = v_isVisible(imageTable);
            parmas.topMargin = (isContentLabelVisible || isImageTableVisible) ? dp2px(16) : 0;
        });

        LinearLayout commentList = binder.getChildWithTag("commentList");
        if (hideComments) {
            v_setGone(commentList);
        } else {
            resetCommentList(commentList, vm.comments, vm.hasMoreComments);
            v_setVisibility(commentList, vm.comments.isEmpty() ? View.GONE : View.VISIBLE);
        }

        if (!hideRateBar) {
            v_setText(binder.getChildWithTag("rateLabel"), vm.rateDesc);
            TextView rateBtn = binder.getChildWithTag("rateBtn");
            rateBtn.setCompoundDrawablesWithIntrinsicBounds(vm.hasRated() ? R.mipmap.ic_circle_reward : R.mipmap.ic_circle_reward_grey, 0, 0, 0);
            rateBtn.setTextColor(vm.hasRated() ? TEXT_RED_COLOR : TEXT_GREY_COLOR);
            v_setVisibility(rateBtn, vm.hasRateBtn && !hideRateBtn ? View.VISIBLE : View.GONE);
        }
        v_setVisibility(binder.getChildWithTag("rateArea"), hideRateBar ? View.GONE : View.VISIBLE);
        v_setVisibility(binder.getChildWithTag("barLine"), hideBarSepLine ? View.GONE : View.VISIBLE);
    }

    public static void resetLinkArea(View itemView, CellVM vm, Func0<Boolean> needBlur) {
        boolean needBlurLinkArea = vm.hasMosaicLink;
        if (vm.linkInfo != null) {
            v_setText(v_findView(itemView, R.id.label_link_desc), vm.linkInfo.linkDesc);
            SimpleDraweeView linkImage = v_findView(itemView, R.id.img_link);
            if (!needBlurLinkArea) {
                v_setImageUri(linkImage, vm.linkInfo.linkImageURL);
                ImageView overlapImage = v_findView(itemView, R.id.img_overlap);
                overlapImage.setImageResource(0);
            } else {
                if (vm.mCacheOverlapBitmap != null) {
                    ImageView overlapImage = v_findView(itemView, R.id.img_overlap);
                    overlapImage.setImageBitmap(vm.mCacheOverlapBitmap);
                } else {
                    ImageView.class.cast(v_findView(itemView, R.id.img_overlap)).setImageDrawable(new ColorDrawable(0xFFEEEEEE));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        linkImage.setController(Fresco.newDraweeControllerBuilder()
                                .setOldController(linkImage.getController())
                                .setImageRequest(ImageRequest.fromUri(vm.linkInfo.linkImageURL))
                                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                                    @Override
                                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                                        super.onFinalImageSet(id, imageInfo, animatable);
                                        if (needBlur.call()) {
                                            itemView.post(() -> {
                                                ImageView overlapImage = v_findView(itemView, R.id.img_overlap);
                                                View linkSpan = v_findView(itemView, R.id.span_link);
                                                if (vm.mCacheOverlapBitmap == null) {
                                                    Bitmap overlapBitmap = getCacheImage(linkSpan).orNull();
                                                    if (overlapBitmap != null) {
                                                        overlapBitmap = blur(overlapBitmap, dp2px(16), true);
                                                    }
                                                    vm.mCacheOverlapBitmap = overlapBitmap;
                                                }
                                                if (vm.mCacheOverlapBitmap != null) {
                                                    overlapImage.setImageBitmap(vm.mCacheOverlapBitmap);
                                                }
                                            });
                                        }
                                    }
                                })
                                .build());
                    }
                }
            }
        }
        decorateScoreLabel(v_findView(itemView, R.id.label_score), String.format(Locale.getDefault(), "%d积分解锁", vm.intelligenceScore), vm.mosaicType);
        v_setVisibility(v_findView(itemView, R.id.label_score), needBlurLinkArea ? View.VISIBLE : View.GONE);
        v_setVisibility(v_findView(itemView, R.id.area_link), vm.linkInfo == null ? View.GONE : View.VISIBLE);
    }

    public static void afterViewHolderCreated(View itemView, Func0<CellVM> itemGetter, IntCounter counter, FrameLayout rateAnimViewContainer, Func0<Boolean> ignoreAvatarClickEvent, Action0 rewardCallback) {
        v_setClick(itemView, v -> {
            Context ctx = itemView.getContext();
            CellVM vm = itemGetter.call();
            MessageSession session = vm.session;
            GMFMessage message = vm.message;
            int headID = vm.headID;
            showActivity(ctx, an_CircleDetailPage(session, headID, message));
        });

        v_setClick(v_findView(itemView, R.id.area_link), v -> {
            CellVM vm = itemGetter.call();
            if (!vm.hasMosaicLink && vm.linkInfo != null) {
                String cmd = CMDParser.parseCMDString(vm.linkInfo.tarLink);
                if (!TextUtils.isEmpty(cmd)) {
                    if (cmd.equals("web")) {
                        stat_click_event(UmengUtil.eEVENTIDTopicListWeb);
                    } else {
                        stat_click_event(UmengUtil.eEVENTIDTopicListAttachment);
                    }
                }

                CMDParser.parse(vm.linkInfo.tarLink).call(itemView.getContext());
            } else {
                itemView.performClick();
            }
        });

        View avatarImage = v_findView(itemView, R.id.img_avatar);
        v_reviseTouchArea(avatarImage);
        v_setClick(avatarImage, v -> {
            if (!ignoreAvatarClickEvent.call()) {
                User user = itemGetter.call().message.getUser().orNull();
                showActivity(itemView.getContext(), an_UserDetailPage(user));
            }
        });

        View rateBtn = v_findView(itemView, R.id.btn_rate);
        v_reviseTouchArea(rateBtn);

        Rect rateBtnVisibleRect = new Rect();
        Rect containVisibleRect = new Rect();
        {
            v_setClick(rateBtn, v -> {
                int availableScore = safeGet(() -> ScoreManager.getInstance().account.cashBalance, 0);
                if (availableScore < 1) {
                    showPurchaseScoreDialog(rateBtn.getContext());
                    return;
                }

                rateWithAnimation(1, itemView, itemGetter, rateAnimViewContainer, rateBtnVisibleRect, containVisibleRect);
                rewardCallback.call();
                counter.increase();
                counter.setValueLater(0, 2000L);
            });
            v_setLongClick(rateBtn, v -> {
                counter.setValue(0);
                showRateMenu(itemView, itemGetter, rateAnimViewContainer, rewardCallback, rateBtn, rateBtnVisibleRect, containVisibleRect);
                UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailVolumeReward);
            });
        }
    }

    private static void showPurchaseScoreDialog(Context context) {
        new GMFDialog.Builder(context)
                .setMessage("当前积分不足，是否要获取积分?")
                .setPositiveButton("去获取", (dialog, which) -> {
                    dialog.dismiss();
                    showActivity(context, an_ScoreHomePage());
                })
                .setNegativeButton("取消")
                .create()
                .show();
    }

    private static void showRateMenu(View itemView, Func0<CellVM> itemGetter, FrameLayout rateAnimViewContainer, Action0 rewardCallback, View rateBtn, Rect rateBtnVisibleRect, Rect containVisibleRect) {
        Context ctx = rateBtn.getContext();
        Rect screenSize = UIControllerExtension.getScreenSize(ctx);
        Rect hitRect = new Rect();
        if (screenSize.width() > 0) {
            v_getGlobalHitRect(rateBtn, hitRect);
            final int RADIUS = dp2px(4);
            final int TRIANGLE_WIDTH = dp2px(8);
            final int TRIANGLE_HEIGHT = dp2px(16);

            int windowWidth = hitRect.left - dp2px(20); //screenSize - leftMargin(16dp) - rightMargin(4dp)
            int windowHeight = dp2px(32);
            int cellWidth = (windowWidth - TRIANGLE_WIDTH) / 4;
            int cellHeight = windowHeight;
            LinearLayout contentView = new LinearLayout(ctx);
            contentView.setOrientation(LinearLayout.HORIZONTAL);

            PopupWindow window = new PopupWindow(contentView, windowWidth, windowHeight);

            Stream.of(10000, 1000, 100, 10)
                    .forEach(score -> {
                        TextView scoreLabel = new TextView(ctx);
                        scoreLabel.setText("+" + score);
                        scoreLabel.setTextColor(WHITE_COLOR);
                        scoreLabel.setTextSize(14);
                        scoreLabel.setGravity(Gravity.CENTER);
                        contentView.addView(scoreLabel, cellWidth, cellHeight);
                        v_setClick(scoreLabel, label -> {
                            window.dismiss();
                            int availableScore = safeGet(() -> ScoreManager.getInstance().account.cashBalance, 0);
                            if (availableScore < score) {
                                showPurchaseScoreDialog(ctx);
                                return;
                            }
                            rateWithAnimation(score, itemView, itemGetter, rateAnimViewContainer, rateBtnVisibleRect, containVisibleRect);
                            rewardCallback.call();
                        });
                    });

            window.setBackgroundDrawable(new ShapeDrawable(new Shape() {
                final Path path = new Path();
                RectF rectF = new RectF();

                @Override
                public void draw(Canvas canvas, Paint paint) {
                    path.reset();
                    // draw border
                    rectF.set(0, 0, cellWidth * 4, cellHeight);
                    path.addRoundRect(rectF, RADIUS, RADIUS, Path.Direction.CW);

                    // draw triangle
                    int startX = cellWidth * 4 - (TRIANGLE_WIDTH >> 1);
                    int startY = (cellHeight - TRIANGLE_HEIGHT) >> 1;
                    path.moveTo(startX, startY);
                    path.lineTo(startX + TRIANGLE_WIDTH, startY + (TRIANGLE_HEIGHT >> 1));
                    path.lineTo(startX, startY + TRIANGLE_HEIGHT);
                    path.lineTo(startX, startY);

                    paint.setAlpha(255);
                    paint.setColor(RED_COLOR);
                    canvas.drawPath(path, paint);

                    // draw sep line
                    paint.setAlpha((int) (255 * 0.2));
                    paint.setColor(WHITE_COLOR);
                    for (int i = 1; i < 4; i++) {
                        canvas.drawLine(cellWidth * i, 0, cellWidth * i, cellHeight, paint);
                    }
                }
            }));
            window.setFocusable(true);
            window.setOutsideTouchable(true);

            window.showAtLocation(rateBtn, Gravity.LEFT | Gravity.TOP, dp2px(16), hitRect.top - ((windowHeight - hitRect.height()) >> 1));
        }
    }

    private static void rateWithAnimation(int score, View itemView, Func0<CellVM> itemGetter, FrameLayout rateAnimViewContainer, Rect rateBtnVisibleRect, Rect containerVisibleRect) {
        TextView rateBtn = v_findView(itemView, R.id.btn_rate);

        CellVM item = itemGetter.call();
        ChatController.donate(item.session, item.message, score);
        item.rateDesc = item.createRateDesc();
        v_setText(itemView, R.id.label_rate, item.rateDesc);
        rateBtn.setTextColor(TEXT_RED_COLOR);
        rateBtn.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_circle_reward, 0, 0, 0);

        rateBtn.getGlobalVisibleRect(rateBtnVisibleRect);
        rateAnimViewContainer.getGlobalVisibleRect(containerVisibleRect);

        addRewardLabelWithAnimation(score, rateAnimViewContainer, rateBtnVisibleRect, containerVisibleRect);
        NotificationCenter.afterDonateScoreSubject.onNext(null);
    }

    private static void addRewardLabelWithAnimation(int score, FrameLayout topLayer, Rect btnRect, Rect topLayerRect) {
        TextView rewardLabel = new TextView(topLayer.getContext());
        rewardLabel.setTextSize(20);
        rewardLabel.setTextColor(TEXT_RED_COLOR);
        rewardLabel.setText("+" + score);
        rewardLabel.setGravity(Gravity.CENTER);
        float textWidth = rewardLabel.getPaint().measureText(rewardLabel.getText().toString());

        int initTopMargin = btnRect.top - topLayerRect.top - dp2px(28);
        topLayer.addView(rewardLabel);
        MarginLayoutParams params = (MarginLayoutParams) rewardLabel.getLayoutParams();
        apply(params, it -> {
            it.height = dp2px(30);
            it.leftMargin = btnRect.left + (int) ((btnRect.width() - textWidth) / 2);
            it.topMargin = initTopMargin;
        });

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.addUpdateListener(animation -> {
            Integer value = (Integer) animation.getAnimatedValue();
            rewardLabel.setAlpha((float) (100 - value) / 100);
            rewardLabel.setScaleX(1.0f + (float) value / 100);
            rewardLabel.setScaleY(rewardLabel.getScaleX());
            params.topMargin = initTopMargin - (int) (dp2px(value) * 0.7f);
            rewardLabel.requestLayout();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v_removeFromParent(rewardLabel);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                v_removeFromParent(rewardLabel);
            }
        });
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(400L);
        animator.start();
    }

    private static void resetImageTable(StaticTableView imageTable, CellVM vm, Func0<Boolean> needBlur, Func0<Boolean> allowToOpenDetailPage) {
        Rect screenSize = UIControllerExtension.getScreenSize(MyApplication.SHARE_INSTANCE);
        Context ctx = imageTable.getContext();

        imageTable.removeAllViews();

        List<ShortcutImagePair> imageURLs = vm.imageList;
        int[] viewIdx = {0};
        View[] cells = new View[imageURLs.size()];
        if (!imageURLs.isEmpty()) {
            float sizeUnit = (float) (screenSize.width() - dp2px(36)) / 3;
            if (sizeUnit > 0) {
                int cellSize = (int) (imageURLs.size() == 1 ? 2 * sizeUnit + dp2px(2) : sizeUnit);
                int cellSpacing = dp2px(2);
                int columnNum = min(anyMatch(imageURLs.size(), 2, 4) ? 2 : 3, imageURLs.size());

                List<List<ShortcutImagePair>> chunks = splitFromList(imageURLs, columnNum);
                Stream.of(chunks)
                        .forEach(chunk -> {
                            LinearLayout row = new LinearLayout(ctx);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            Stream.of(chunk)
                                    .forEach(it -> {
                                        int currentIdx = viewIdx[0];

                                        FrameLayout container = new FrameLayout(ctx);

                                        GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(ctx.getResources())
                                                .setPlaceholderImage(new ColorDrawable(TEXT_GREY_LIGHT_COLOR))
                                                .setFadeDuration(0)
                                                .build();

                                        SimpleDraweeView draweeView = new SimpleDraweeView(ctx, hierarchy);
                                        ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(it.shortcutUrl));
                                        if (needBlur.call()) {
                                            requestBuilder.setPostprocessor(new BasePostprocessor() {
                                                @Override
                                                public void process(Bitmap destBitmap, Bitmap sourceBitmap) {
                                                    super.process(destBitmap, sourceBitmap);
                                                    if (needBlur.call()) {
                                                        blur(destBitmap, dp2px(24), true);
                                                    }
                                                }
                                            });
                                        }
                                        draweeView.setController(Fresco.newDraweeControllerBuilder()
                                                .setOldController(draweeView.getController())
                                                .setImageRequest(requestBuilder.build())
                                                .build());

                                        container.addView(draweeView, new FrameLayout.LayoutParams(-1, -1));

                                        if (needBlur.call()) {
                                            CircleTextView scoreLabel = new CircleTextView(ctx);
                                            decorateScoreLabel(scoreLabel, String.format("%d积分解锁", vm.intelligenceScore), vm.mosaicType);
                                            container.addView(scoreLabel, apply(new FrameLayout.LayoutParams(-2, -2), params -> {
                                                params.gravity = Gravity.CENTER;
                                            }));
                                        }
                                        v_setClick(draweeView, v -> {
                                            if (!needBlur.call()) {
                                                Rect[] rectArray = new Rect[imageURLs.size()];
                                                int[] location = new int[2];
                                                for (int i = 0; i < cells.length; i++) {
                                                    Rect rect = new Rect();
                                                    cells[i].getLocationInWindow(location);
                                                    rect.right = cells[i].getMeasuredWidth();
                                                    rect.bottom = cells[i].getMeasuredHeight();
                                                    rect.offset(location[0], location[1]);
                                                    rectArray[i] = rect;
                                                }
                                                boolean stat = safeGet(() -> MyApplication.getTopFragmentOrNil().get() instanceof CircleListFragment, false);
                                                if (stat) {
                                                    stat_click_event(UmengUtil.eEVENTIDTopicListViewPhoto);
                                                }

                                                showActivity(ctx, an_PhotoViewerPage(imageURLs, rectArray, currentIdx), BaseActivity.TRANSACTION_DIRECTION.NONE);
                                            } else if (allowToOpenDetailPage.call()) {
                                                MessageSession session = vm.session;
                                                int headID = vm.headID;
                                                showActivity(ctx, an_CircleDetailPage(session, headID, vm.message));
                                            }
                                        });

                                        row.addView(container, new LinearLayout.LayoutParams(cellSize, cellSize));
                                        row.addView(new Space(ctx), new LinearLayout.LayoutParams(cellSpacing, cellSpacing));
                                        cells[viewIdx[0]] = draweeView;
                                        viewIdx[0]++;
                                    });
                            row.removeViewAt(row.getChildCount() - 1);

                            imageTable.addView(row, new LinearLayout.LayoutParams(-1, -2));
                            imageTable.addView(new Space(ctx), new LinearLayout.LayoutParams(-1, cellSpacing));
                        });
                imageTable.removeViewAt(imageTable.getChildCount() - 1);
            }
        }
    }

    private static void resetCommentList(LinearLayout commentList, List<CharSequence> comments, boolean hasMoreComment) {
        Context ctx = commentList.getContext();

        commentList.removeAllViewsInLayout();
        if (!comments.isEmpty()) {
            Stream.of(comments)
                    .forEach(it -> {
                        TextView commentLabel = new TextView(ctx);
                        commentLabel.setTextSize(12);
                        commentLabel.setText(it);
                        commentLabel.setMaxLines(2);
                        commentLabel.setEllipsize(TextUtils.TruncateAt.END);
                        commentList.addView(commentLabel, new LinearLayout.LayoutParams(-1, -2));
                    });


            if (hasMoreComment) {
                TextView header = new TextView(ctx);
                header.setTextSize(12);
                header.setTextColor(0xFFCCCCCC);
                header.setText("查看更多评论");
                commentList.addView(header, new LinearLayout.LayoutParams(-1, -2));
            }
        }
    }

    private static void decorateScoreLabel(CircleTextView scoreLabel, CharSequence text, int mosaicType) {
        if (scoreLabel != null) {
            scoreLabel.setRadius(dp2px(4))
                    .setContentPadding(dp2px(4), dp2px(2), dp2px(4), dp2px(2))
                    .setDrawablePadding(dp2px(4))
                    .setTextSize(sp2px(16))
                    .setTileResId(R.mipmap.tile_circle)
                    .setText(text)
                    .setTextColor(getMosaicTextColor(mosaicType))
                    .setIconResId(getMosaicIcon(mosaicType))
                    .setContentBackgroundColor(getMosaicBGColor(mosaicType))
                    .prepare(true);
        }
    }

    private static Bitmap blur(Bitmap original, int radius, boolean canReuseInBitmap) {
        return StackBlur.blurNativelyPixels(original, radius, canReuseInBitmap);
    }

    static class REGEX {
        private REGEX() {
        }

        public static final Pattern CONTAIN_MOSAIC_TEXT = Pattern.compile("#[^#]*#");
        public static final Pattern CAPTURE_MOSAIC_TEXT = Pattern.compile("#([^#]*)#");
    }


    public static class CellVM {
        public static final int MOSAIC_TYPE_LOCKED = 0;
        public static final int MOSAIC_TYPE_UNLOCKED = 1;
        public static final int MOSAIC_TYPE_LIKE = 2;
        public static final int MOSAIC_TYPE_DISLIKE = 3;
        private static final int[] MOSAIC_TYPE_ICON_ID_ARRAY = {R.mipmap.ic_locked, R.mipmap.ic_unlocked, R.mipmap.ic_like, R.mipmap.ic_dislike};
        private static final int[] MOSAIC_TYPE_TEXT_COLOR_ARRAY = {TEXT_BLACK_COLOR, TEXT_BLACK_COLOR, TEXT_WHITE_COLOR, TEXT_WHITE_COLOR};
        private static final int[] MOSAIC_TYPE_BG_COLOR_ARRAY = {YELLOW_COLOR, YELLOW_COLOR, RED_COLOR, GREY_COLOR};

        private MessageSession session;
        private int headID;
        private GMFMessage message;

        public int userType;
        public String userAvatarURL;
        public String userName;
        public CharSequence createTime;
        public String rawTitle;
        public String rawContent;
        public List<ShortcutImagePair> imageList;
        public LinkInfo linkInfo;
        public CharSequence rateDesc;
        public int mosaicType;
        public boolean hasRateBtn;
        public boolean hasInteligence;
        public boolean hasMosaicText;
        public boolean hasMosaicImage;
        public boolean hasMosaicLink;
        public boolean hasMoreComments;
        public int intelligenceScore;
        public int gainedScore;
        List<CharSequence> comments;
        public Bitmap mCacheOverlapBitmap;
        public int contentMaxLine = 0;
        public List<User> likeUsers;
        public List<User> dislikeUsers;
        public boolean isLikeArticle;
        public boolean isDislikeArticle;

//        public CellVM(GMFMessage message) {
//            this(null, -1, message);
//        }

        public CellVM(MessageSession session, GMFMessage message) {
            this(session, -1, message);
        }

        public CellVM(MessageSession session, int headID, GMFMessage message) {

            this.session = session;
            this.headID = headID;
            this.message = message;

            userType = safeGet(() -> message.getUser().get().type, User.User_Type.Custom);
            userAvatarURL = safeGet(() -> message.getUser().let(it -> it.getPhotoUrl()).or(""), "");
            userName = safeGet(() -> message.getUser().let(it -> it.getName()).or(""), "");
            createTime = safeGet(() -> formatTimeByNow(message.createTime), "");
            rawTitle = safeGet(() -> message.title, "");
            if (!TextUtils.isEmpty(rawTitle)) {
                rawTitle = rawTitle + "\n";
            }
            rawContent = safeGet(() -> message.content, "");
            imageList = safeGet(() -> message.imageList, Collections.<ShortcutImagePair>emptyList());
            linkInfo = safeGet(() -> opt(message.attachInfo).let(it -> new LinkInfo(it)).orNull(), null);
            mosaicType = computeMosaicType(session, message);
            hasRateBtn = safeGet(() -> {
                boolean hasIntelligence = safeGet(() -> message.intelligence.unlockScore > 0, false);
                boolean isSendByOther = safeGet(() -> message.getUser().get().index != MineManager.getInstance().getmMe().index, true);
                return !hasIntelligence && isSendByOther;
            }, false);
            hasInteligence = safeGet(() -> {
                return message.intelligence.unlockScore > 0;
            }, false);
            hasMosaicText = safeGet(() -> isValidIntelligence(message) && anyMatch(message.templateType, Message_Text_2), false);
            hasMosaicImage = safeGet(() -> isValidIntelligence(message) && anyMatch(message.templateType, Message_Image_2), false);
            hasMosaicLink = safeGet(() -> isValidIntelligence(message) && anyMatch(message.templateType, Message_TarLink_2), false);
            intelligenceScore = safeGet(() -> message.intelligence.unlockScore, 0);
            gainedScore = safeGet(() -> message.intelligence.gainScore, 0);
            comments = safeGet(() -> Stream.of(message.commentBrief)
                    .map(it -> createComment(it))
                    .collect(Collectors.toList()), Collections.<CharSequence>emptyList());
            hasMoreComments = safeGet(() -> message.commentCount > 3, false);
            contentMaxLine = safeGet(() -> anyMatch(session.addButtonType, MessageSession.Session_Add_Type_Intelligence) ? Integer.MAX_VALUE : 5, Integer.MAX_VALUE);
            likeUsers = safeGet(() -> Stream.of(message.intelligence.likeUserList).map(it -> it.user).collect(Collectors.toList()), Collections.<User>emptyList());
            dislikeUsers = safeGet(() -> Stream.of(message.intelligence.unlikeUserList).map(it -> it.user).collect(Collectors.toList()), Collections.<User>emptyList());
            int myID = MineManager.getInstance().getmMe().index;
            isLikeArticle = safeGet(() -> anyMatch(message.intelligence.myAction, Intelligence_Action_Like, Intelligence_Action_Timeout), false);
            isDislikeArticle = safeGet(() -> anyMatch(message.intelligence.myAction, Intelligence_Action_Unlike), false);

            rateDesc = createRateDesc();
        }

        public boolean hasRated() {
            return safeGet(() -> message.myDonateScore() > 0, false);
        }

        private static CharSequence createComment(RemaindFeed feed) {
            String userName = safeGet(() -> feed.user.getName(), "");
            String content = safeGet(() -> feed.text, "").replaceAll("\n+", " ");
            return setColor(concatNoBreak(setStyle(userName, Typeface.BOLD), " : " + content), 0xFF666666);
        }

        private static int computeMosaicType(MessageSession session, GMFMessage message) {
            return safeGet(() -> {
                boolean isSendByMe = safeGet(() -> message.getUser().get().index == MineManager.getInstance().getmMe().index, false);

                if (isSendByMe) {
                    return MOSAIC_TYPE_UNLOCKED;
                }

                Intelligence intelligence = message.intelligence;

                if (!intelligence.bMyUnlock) {
                    return MOSAIC_TYPE_LOCKED;
                }

                switch (intelligence.myAction) {
                    case Intelligence_Action_Waiting:
                        return MOSAIC_TYPE_UNLOCKED;
                    case Intelligence_Action_Like:
                    case Intelligence_Action_Timeout:
                        return MOSAIC_TYPE_LIKE;
                    case Intelligence_Action_Unlike:
                        return MOSAIC_TYPE_DISLIKE;
                    default:
                        return MOSAIC_TYPE_LOCKED;
                }

            }, MOSAIC_TYPE_LOCKED);
        }

        public CharSequence createRateDesc() {
            boolean hasIntelligence = safeGet(() -> message.intelligence.unlockScore > 0, false);
            if (hasIntelligence) {
                CharSequence likeCount = safeGet(() -> String.format("%d人觉得值", message.intelligence.likeCount), "");
                if (isLikeArticle) {
                    likeCount = setColor(likeCount, TEXT_RED_COLOR);
                }
                CharSequence dislikeCount = safeGet(() -> String.format("%d人觉得坑", message.intelligence.unlikeCount), "");
                if (isDislikeArticle) {
                    dislikeCount = setColor(dislikeCount, TEXT_RED_COLOR);
                }
                return concatNoBreak(likeCount, " · ", dislikeCount);
            }

            return safeGet(() -> {
                int donateCount = message.donateCount();
                if (donateCount > 0) {
                    return String.format("%d个评论 · %d人打赏了%d积分", message.commentCount, donateCount, message.donateScore());
                } else {
                    return String.format("%d个评论 · 0人打赏", message.commentCount);
                }
            }, "");
        }

        public CellVM optimize() {
            hasMosaicText = hasMosaicText && REGEX.CONTAIN_MOSAIC_TEXT.matcher(rawContent).find();
            hasMosaicLink = hasMosaicLink && linkInfo != null && anyMatch(mosaicType, MOSAIC_TYPE_LOCKED);
            hasMosaicImage = hasMosaicImage && !imageList.isEmpty() && anyMatch(mosaicType, MOSAIC_TYPE_LOCKED);

            if (hasMosaicLink) {
                hasMosaicText = false;
                hasMosaicImage = false;

                imageList = emptyList();
            }

            if (hasMosaicImage) {
                hasMosaicText = false;

                if (imageList.size() > 1) {
                    imageList = imageList.subList(0, 1);
                }
            }

            if (comments.size() > 3) {
                comments.subList(0, 3);
            }
            hasMoreComments = hasMoreComments && !comments.isEmpty();

            if (linkInfo != null && TextUtils.isEmpty(linkInfo.linkDesc.toString().trim())) {
                linkInfo = null;
            }

            if (linkInfo != null) {
                imageList.clear();
            }

            return this;
        }

        private CharSequence mCacheDecoratedContent;

        public CharSequence createDecoratedString() {
            if (hasMosaicText) {

                if (mCacheDecoratedContent != null) {
                    return mCacheDecoratedContent;
                }

                Matcher matcher = REGEX.CAPTURE_MOSAIC_TEXT.matcher(rawContent);
                if (matcher.find()) {
                    /**
                     * 注意！！！group(0)不算在groupCount当中，蛋疼！！！
                     */
                    if (matcher.groupCount() >= 1) {
                        String allText = matcher.group();
                        String onlyContent = matcher.group(1);

                        int indexOfAllText = rawContent.indexOf(allText);
                        boolean isLeading = indexOfAllText == 0;
                        boolean isTail = indexOfAllText >= 0 && (indexOfAllText + allText.length()) == rawContent.length();

                        String[] spiltTexts = new String[3];
                        spiltTexts[0] = isLeading ? " " : rawContent.substring(0, indexOfAllText) + "  ";
                        spiltTexts[1] = anyMatch(mosaicType, MOSAIC_TYPE_LOCKED) ? String.format(Locale.getDefault(), "%d积分解锁", intelligenceScore) : onlyContent;
                        spiltTexts[2] = isTail ? " " : "  " + rawContent.substring(indexOfAllText + allText.length(), rawContent.length());
                        CharSequence out = new SpannableStringBuilder(spiltTexts[0]);
                        ImageTextParams params = new ImageTextParams();
                        params.maxWidth -= dp2px(20);
                        params.fontSize = sp2px(16);
                        params.iconResID = getMosaicIcon(mosaicType);
                        params.bgColor = getMosaicBGColor(mosaicType);
                        params.textColor = getMosaicTextColor(mosaicType);
                        appendImageText(out, spiltTexts[1], params);
                        out = concatNoBreak(out, spiltTexts[2]);
                        mCacheDecoratedContent = concatNoBreak(setStyle(rawTitle, Typeface.BOLD), out);
                        return mCacheDecoratedContent;
                    }
                }
            }
            return concatNoBreak(setStyle(rawTitle, Typeface.BOLD), rawContent);
        }
    }

    public static int getMosaicIcon(int type) {
        return CellVM.MOSAIC_TYPE_ICON_ID_ARRAY[type % CellVM.MOSAIC_TYPE_ICON_ID_ARRAY.length];
    }

    public static int getMosaicBGColor(int type) {
        return CellVM.MOSAIC_TYPE_BG_COLOR_ARRAY[type % CellVM.MOSAIC_TYPE_BG_COLOR_ARRAY.length];
    }

    public static int getMosaicTextColor(int type) {
        return CellVM.MOSAIC_TYPE_TEXT_COLOR_ARRAY[type % CellVM.MOSAIC_TYPE_TEXT_COLOR_ARRAY.length];
    }

    public static class LinkInfo {
        String linkImageURL;
        CharSequence linkDesc;
        String tarLink;

        public LinkInfo(ShareInfo raw) {
            tarLink = raw.url;
            linkImageURL = safeGet(() -> raw.imageUrl, "");

            CharSequence defaultLinkDesc = setFontSize(setColor(" ", TEXT_BLACK_COLOR), sp2px(16));
            linkDesc = safeGet(() -> {
                CharSequence title = setStyle(setFontSize(setColor(raw.title, TEXT_BLACK_COLOR), sp2px(16)), Typeface.BOLD);
                CharSequence content = setFontSize(setColor(raw.msg, TEXT_GREY_COLOR), sp2px(14));

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
                    return concat(title, content);
                } else if (TextUtils.isEmpty(title)) {
                    return content;
                } else if (TextUtils.isEmpty(content)) {
                    return title;
                } else {
                    return "";
                }
            }).def(defaultLinkDesc).get();
        }
    }
}
