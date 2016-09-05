package com.goldmf.GMFund.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.model.TarLinkButton;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 16/3/12.
 */
public class TopNotificationView extends RelativeLayout {
    private TextView mContentLabel;

    public TopNotificationView(Context context) {
        this(context, null);
    }

    public TopNotificationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopNotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(getResources().getColor(R.color.gmf_yellow));

        ImageView arrowImage = new ImageView(context);
        {
            arrowImage.setId(R.id.img_arrow);
            arrowImage.setImageResource(R.mipmap.ic_arrow_right_dark);
            arrowImage.setScaleType(ImageView.ScaleType.CENTER);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 40), dp2px(this, 40));
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_RIGHT);
            addView(arrowImage, params);
        }

        mContentLabel = new TextView(context);
        {
            mContentLabel.setPadding(dp2px(this, 10), dp2px(this, 10), 0, dp2px(this, 10));
            mContentLabel.setTextSize(14);
            mContentLabel.setTextColor(getResources().getColor(R.color.gmf_text_black));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, -2);
            params.addRule(ALIGN_PARENT_LEFT);
            params.addRule(LEFT_OF, arrowImage.getId());
            params.addRule(CENTER_VERTICAL);
            addView(mContentLabel, params);
        }

        if (isInEditMode()) {
            mContentLabel.setText("重要提示：新浪支付接到银行通知");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (TextUtils.isEmpty(mContentLabel.getText())) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setText(CharSequence text) {
        setText(text, false);
    }

    public void setText(CharSequence text, boolean animate) {
        mContentLabel.setText(text);
        if (animate) {
            ViewExtension.v_getSizePreDraw(this, true, size -> {
                final int height = size.second;
                updateMarginTop(-height);
                ValueAnimator animator = ValueAnimator.ofInt(-height, 0);
                animator.setStartDelay(250L);
                animator.addUpdateListener(animation -> {
                    int value = (int) animation.getAnimatedValue();
                    updateMarginTop(value);
                });
                animator.setDuration(500L);
                animator.start();
            });
        }
    }

    private void updateMarginTop(int topMargin) {
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        params.topMargin = topMargin;
        requestLayout();
    }

    public void setupWithTarLinkText(TarLinkButton.TarLinkText text) {
        setupWithTarLinkText(text, false);
    }

    public void setupWithTarLinkText(TarLinkButton.TarLinkText text, boolean animate) {
        if (text != null) {
            setText(text.content, animate);
            v_setClick(this, v -> CMDParser.parse(text.tarLink).call(getContext()));
        }
    }
}
