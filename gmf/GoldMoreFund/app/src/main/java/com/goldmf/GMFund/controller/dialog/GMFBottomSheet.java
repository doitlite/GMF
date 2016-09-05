package com.goldmf.GMFund.controller.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.extension.ViewExtension;
import com.orhanobut.logger.Logger;

import java.util.LinkedList;
import java.util.WeakHashMap;

import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;

/**
 * Created by yale on 15/8/19.
 */
public class GMFBottomSheet extends BasicDialog {
    private OnItemClickListener mOnItemClickListener = OnItemClickListener.NULL;

    private TextView mTitleLabel;
    private View mHeaderSection;
    private LinearLayout mHeaderList;
    private LinearLayout mContentList;

    public GMFBottomSheet(Context context) {
        super(context, R.style.GMFDialog);
        setContentView(R.layout.bottom_sheet);
        getWindow().setLayout(-1, -2);
        getWindow().setGravity(Gravity.BOTTOM);

        // bind child views
        mTitleLabel = v_findView(this, R.id.label_title);
        mHeaderSection = v_findView(this, R.id.section_header);
        mHeaderList = v_findView(this, R.id.list_header);
        mContentList = v_findView(this, R.id.list_content);

        ScrollView scrollView = (ScrollView) mHeaderSection.getParent().getParent();
        v_preDraw(scrollView, true, v -> {
            v_updateLayoutParams(scrollView, ViewGroup.MarginLayoutParams.class, params -> {
                Rect screenSize = UIControllerExtension.getScreenSize(getContext());
                params.height = Math.min(screenSize.height() / 2, scrollView.getMeasuredHeight());
            });
        });

        v_preDraw(v_findView(this, R.id.contentView), true, v -> animateToShow());
    }

    public GMFBottomSheet setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener == null ? OnItemClickListener.NULL : listener;
        return this;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            animateToDismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void dismiss() {
        animateToDismiss();
    }

    private boolean mIsAnimating = false;

    private void animateToShow() {
        if (mIsAnimating) {
            return;
        }

        mIsAnimating = true;
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

        mIsAnimating = true;
        View contentView = v_findView(this, R.id.contentView);
        ObjectAnimator animator = ObjectAnimator.ofFloat(contentView, "translationY", contentView.getHeight());
        animator.setDuration(250L);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mIsAnimating = false;
                GMFBottomSheet.super.dismiss();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsAnimating = false;
                GMFBottomSheet.super.dismiss();
            }
        });
        animator.start();
    }

    public void setTitle(CharSequence text) {
        mTitleLabel.setText(text);
    }

    public void addHeaderItem(BottomSheetItem item) {
        appendHeaderCell(item);
    }

    public void addContentItem(BottomSheetItem item) {
        appendContentCell(item);
    }

    private void appendHeaderCell(BottomSheetItem item) {
        View cell = createCell(mHeaderList, item);
        mHeaderList.addView(cell);
    }

    private void appendContentCell(BottomSheetItem item) {
        View cell = createCell(mContentList, item);
        mContentList.addView(cell);
    }

    private View createCell(ViewGroup parent, final BottomSheetItem item) {
        final View cell = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_bottom_sheet, parent, false);

        if (item.iconResId != 0) {
            v_setImageResource(cell, android.R.id.icon, item.iconResId);
        } else {
            ViewExtension.v_setGone(cell, android.R.id.icon);
        }

        TextView titleLabel = (TextView) cell.findViewById(android.R.id.text1);
        titleLabel.setText(item.title);

        v_setClick(cell, v -> mOnItemClickListener.onItemClick(GMFBottomSheet.this, item));

        return cell;
    }

    public static class BottomSheetItem {
        public final Object tag;
        public final CharSequence title;
        public final int iconResId;

        public BottomSheetItem(Object tag, CharSequence title, int iconResId) {
            this.tag = tag;
            this.title = title;
            this.iconResId = iconResId;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(GMFBottomSheet bottomSheet, BottomSheetItem item);

        OnItemClickListener NULL = new OnItemClickListener() {
            @Override
            public void onItemClick(GMFBottomSheet bottomSheet, BottomSheetItem item) {
                bottomSheet.dismiss();
            }
        };
    }

    public static class Builder {
        private Context mContext;
        private CharSequence mTitle;
        private LinkedList<BottomSheetItem> mHeaderItems = new LinkedList<>();
        private LinkedList<BottomSheetItem> mContentItems = new LinkedList<>();

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setTitle(CharSequence text) {
            mTitle = text;
            return this;
        }

        public Builder addHeaderItem(BottomSheetItem item) {
            mHeaderItems.add(item);
            return this;
        }

        public Builder addContentItem(BottomSheetItem item) {
            mContentItems.add(item);
            return this;
        }

        public Builder addContentItem(CharSequence title) {
            mContentItems.add(new BottomSheetItem(null, title, 0));
            return this;
        }

        public Builder addContentItem(Object tag, CharSequence title) {
            mContentItems.add(new BottomSheetItem(tag, title, 0));
            return this;
        }

        public GMFBottomSheet create() {
            GMFBottomSheet sheet = new GMFBottomSheet(mContext);

            if (mTitle == null) {
                sheet.mTitleLabel.setVisibility(View.GONE);
            } else {
                sheet.mTitleLabel.setText(mTitle);
            }

            if (mHeaderItems.isEmpty()) {
                sheet.mHeaderSection.setVisibility(View.GONE);
            } else {
                for (BottomSheetItem item : mHeaderItems) {
                    sheet.addHeaderItem(item);
                }
            }

            for (BottomSheetItem item : mContentItems) {
                sheet.addContentItem(item);
            }

            return sheet;
        }
    }
}
