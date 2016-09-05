package com.goldmf.GMFund.controller.dialog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by Evan on 15/12/8 上午11:05.
 */
public class FundDetailIntroductionDialog extends BasicDialog {

    private final ImageView mIconSection;
    private final TextView mTitleSection;
    private final TextView mDescriptionSection;
    private boolean isAnimating = false;

    public FundDetailIntroductionDialog(Context context) {
        super(context, R.style.GMFDialog);
        setContentView(R.layout.bottom_detail_introduction);
        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setLayout(-1, dm.heightPixels / 2);
        getWindow().setGravity(Gravity.BOTTOM);

        mIconSection = v_findView(this, R.id.section_img);
        mTitleSection = v_findView(this, R.id.section_title);
        mDescriptionSection = v_findView(this, R.id.section_description);
        v_setClick(this, R.id.btn_close, v -> animateToDismiss());

        v_preDraw(v_findView(this, R.id.contentView), true, view -> animateToShow()
        );
    }

    @Override
    public void dismiss() {
        animateToDismiss();
    }

    private void animateToShow() {
        if (isAnimating) return;
        View contentView = v_findView(this, R.id.contentView);
        contentView.setTranslationY(contentView.getHeight());
        ObjectAnimator animator = ObjectAnimator.ofFloat(contentView, "translationY", 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void animateToDismiss() {
        if (isAnimating) return;
        View contentView = v_findView(this, R.id.contentView);
        ObjectAnimator animator = ObjectAnimator.ofFloat(contentView, "translationY", contentView.getHeight());
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                FundDetailIntroductionDialog.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
                FundDetailIntroductionDialog.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public static class FundDetailIntroductionContent {
        public final int iconResID;
        public final CharSequence title;
        public final CharSequence description;

        public FundDetailIntroductionContent(int iconResID, CharSequence title, CharSequence description) {
            this.iconResID = iconResID;
            this.title = title;
            this.description = description;
        }
    }

    public static class Builder {
        private Context mContext;
        private FundDetailIntroductionContent mFundDetailIntroductionContent;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setContent(FundDetailIntroductionContent content) {
            mFundDetailIntroductionContent = content;
            return this;
        }

        public FundDetailIntroductionDialog create() {
            FundDetailIntroductionDialog dialog = new FundDetailIntroductionDialog(mContext);
            if (mFundDetailIntroductionContent != null) {
                dialog.mIconSection.setImageResource(mFundDetailIntroductionContent.iconResID);
                dialog.mTitleSection.setText(mFundDetailIntroductionContent.title);
                dialog.mDescriptionSection.setText(mFundDetailIntroductionContent.description);
            } else {
                dialog.mIconSection.setVisibility(View.GONE);
                dialog.mTitleSection.setVisibility(View.GONE);
                dialog.mDescriptionSection.setVisibility(View.GONE);
            }
            return dialog;
        }

    }
}
