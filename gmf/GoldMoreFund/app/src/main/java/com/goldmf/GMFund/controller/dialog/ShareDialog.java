package com.goldmf.GMFund.controller.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.SpannableStringExtension;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.umeng.socialize.bean.SHARE_MEDIA;

import rx.functions.Action2;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.extension.SpannableStringExtension.*;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.umeng.socialize.bean.SHARE_MEDIA.WEIXIN;

/**
 * Created by yale on 15/10/23.
 */
public class ShareDialog extends BasicDialog {

    private static SharePlatform[] sDefaultPlatforms = new SharePlatform[]{
            SharePlatform.WX,
            SharePlatform.WX_CIRCLE,
            SharePlatform.QQ,
            SharePlatform.SMS,
            SharePlatform.QZONE,
            SharePlatform.SINA,
            SharePlatform.COPY
    };
    private static SharePlatform[] sPlainImagePlatforms = new SharePlatform[]{
            SharePlatform.WX,
            SharePlatform.WX_CIRCLE,
            SharePlatform.QQ,
            SharePlatform.SINA
    };


    private Action2<ShareDialog, SharePlatform> mShareItemClickEventDelegate = (dialog, platform) -> {
    };

    private boolean mIsAnimating = false;

    public ShareDialog(Activity activity, ShareInfo shareInfo) {
        this(activity, shareInfo, null);
    }

    public ShareDialog(Activity activity, ShareInfo shareInfo, SharePlatform[] platforms) {
        super(activity, R.style.GMFDialog);
        setContentView(R.layout.dialog_share);
        getWindow().setLayout(-1, -2);
        getWindow().setGravity(Gravity.BOTTOM);

        // init child views
        v_setImageUri(this, R.id.img_share, shareInfo.imageUrl);
        v_setText(this, R.id.label_title_and_content, concat(shareInfo.title, setColor(shareInfo.msg, TEXT_GREY_COLOR)));
        if (platforms != null && platforms.length > 0) {
            setSharePlatforms(platforms);
        } else {
            setSharePlatforms(TextUtils.isEmpty(shareInfo.url) ? sPlainImagePlatforms : sDefaultPlatforms);
        }
        v_preDraw(v_findView(this, R.id.contentView), true, view -> animateToShow());
        UmengUtil.stat_enter_share_page(activity, Optional.of(null));
    }

    public void setShareItemClickEventDelegate(Action2<ShareDialog, SharePlatform> delegate) {
        if (delegate != null) {
            mShareItemClickEventDelegate = delegate;
        }
    }

    @Override
    public void dismiss() {
        animateToDismiss();
    }

    private void animateToShow() {
        if (mIsAnimating)
            return;
        View contentView = v_findView(this, R.id.contentView);
        contentView.setTranslationY(contentView.getHeight());
        ObjectAnimator animator = ObjectAnimator.ofFloat(contentView, "translationY", 0);
        animator.setDuration(250L);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mIsAnimating = false;
            }
        });
        animator.start();
    }

    private void animateToDismiss() {
        if (mIsAnimating)
            return;

        View contentView = v_findView(this, R.id.contentView);
        ObjectAnimator animator = ObjectAnimator.ofFloat(contentView, "translationY", contentView.getHeight());
        animator.setDuration(250L);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                ShareDialog.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsAnimating = false;
                ShareDialog.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    private void setSharePlatforms(SharePlatform[] platforms) {
        Context ctx = getContext();
        LinearLayout platformSection = v_findView(this, R.id.section_platform);
        platformSection.removeAllViewsInLayout();

        if (platforms != null && platforms.length > 0) {
            int columnIdx = 0;
            LinearLayout currentRow = createNewRow(ctx);
            platformSection.addView(currentRow);
            for (SharePlatform platform : platforms) {
                currentRow.addView(createNewColumn(ctx, currentRow, platform));
                if (columnIdx >= 3) {
                    columnIdx = 0;
                    View spanView = new View(ctx);
                    platformSection.addView(spanView, -1, dp2px(10));
                    currentRow = createNewRow(ctx);
                    platformSection.addView(currentRow);
                } else {
                    columnIdx++;
                }
            }
            while (columnIdx <= 3) {
                currentRow.addView(createEmptyNewColumn(ctx));
                columnIdx++;
            }
        }
    }

    private LinearLayout createNewRow(Context ctx) {
        LinearLayout row = new LinearLayout(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        row.setLayoutParams(params);
        return row;
    }

    private View createNewColumn(Context ctx, LinearLayout parent, SharePlatform platform) {
        View column = LayoutInflater.from(ctx).inflate(R.layout.cell_dialog_share, parent, false);
        v_setImageResource(column, R.id.img_icon, platform.iconResId);
        v_setText(column, R.id.label_title, platform.title);

        v_setClick(column, v -> {
            mShareItemClickEventDelegate.call(this, platform);
            switch (platform.shareMedia) {
                case WEIXIN:
                    UmengUtil.stat_share_to_wechat_friend(ctx, Optional.of(null));
                    break;
                case WEIXIN_CIRCLE:
                    UmengUtil.stat_share_to_wechat_circle(ctx, Optional.of(null));
                    break;
                case QQ:
                    UmengUtil.stat_share_to_qq(ctx, Optional.of(null));
                    break;
                case SMS:
                    UmengUtil.stat_share_to_sms(ctx, Optional.of(null));
                    break;
                case QZONE:
                    UmengUtil.stat_share_to_qzone(ctx, Optional.of(null));
                    break;
                case SINA:
                    UmengUtil.stat_share_to_sina_large(ctx, Optional.of(null));
                    break;
                case GENERIC:
                    UmengUtil.stat_share_to_copy_link(ctx, Optional.of(null));
                    break;
            }
        });
        return column;
    }

    private View createEmptyNewColumn(Context ctx) {
        LinearLayout column = new LinearLayout(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2);
        params.weight = 1;
        column.setLayoutParams(params);
        return column;
    }

    public static class SharePlatform {
        public static SharePlatform WX = new SharePlatform(WEIXIN, R.mipmap.ic_wechat_friend, "微信好友");
        public static SharePlatform WX_CIRCLE = new SharePlatform(SHARE_MEDIA.WEIXIN_CIRCLE, R.mipmap.ic_wechat_circle, "微信朋友圈");
        public static SharePlatform QQ = new SharePlatform(SHARE_MEDIA.QQ, R.mipmap.ic_qq, "QQ好友");
        public static SharePlatform SMS = new SharePlatform(SHARE_MEDIA.SMS, R.mipmap.ic_sms, "短信");
        public static SharePlatform QZONE = new SharePlatform(SHARE_MEDIA.QZONE, R.mipmap.ic_qzone, "QQ空间");
        public static SharePlatform SINA = new SharePlatform(SHARE_MEDIA.SINA, R.mipmap.ic_sina_large, "新浪微博");
        public static SharePlatform COPY = new SharePlatform(SHARE_MEDIA.GENERIC, R.mipmap.ic_copy_link, "拷贝链接");
        public static SharePlatform REPORT = new SharePlatform(SHARE_MEDIA.GENERIC, R.mipmap.ic_share_report, "举报");
        public static SharePlatform DELETE = new SharePlatform(SHARE_MEDIA.GENERIC, R.mipmap.ic_share_delete, "删除");

        public final SHARE_MEDIA shareMedia;
        public final int iconResId;
        public final String title;

        public SharePlatform(SHARE_MEDIA shareMedia, int iconResId, String title) {
            this.shareMedia = shareMedia;
            this.iconResId = iconResId;
            this.title = title;
        }
    }
}
