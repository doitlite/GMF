package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.extension.ViewExtension.*;

/**
 * Created by yale on 15/8/11.
 */
public class EmbedProgressView extends RelativeLayout {

    private GMFProgressBar mProgressBar;
    private TextView mMessageLabel;

    public EmbedProgressView(Context context) {
        this(context, null);
    }

    public EmbedProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmbedProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // initialize ProgressContainer
        FrameLayout progressContainer = new FrameLayout(context);
        progressContainer.setId(R.id.progress);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(CENTER_HORIZONTAL);
            addView(progressContainer, params);
        }

        // initialize ProgressBar
        {
            mProgressBar = new GMFProgressBar(context);
            mProgressBar.setTheme(GMFProgressBar.THEME_DARK);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp2px(this, 48), dp2px(this, 48));
            params.gravity = Gravity.CENTER;

            progressContainer.addView(mProgressBar, params);
        }

        // initialize LoadingArrowImage
        {
            ImageView loadingArrowImage = new ImageView(context);
            loadingArrowImage.setImageResource(R.mipmap.ic_loading_light);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.CENTER;

            progressContainer.addView(loadingArrowImage, params);
        }


        // initialize MessageLabel
        {
            mMessageLabel = new TextView(context);
            mMessageLabel.setId(R.id.label_title);
            mMessageLabel.setText("页面加载中...");

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(BELOW, progressContainer.getId());
            params.addRule(CENTER_HORIZONTAL, TRUE);
            if (isInEditMode()) {
                params.topMargin = 16;
            } else {
                params.topMargin = dp2px(this, 8);
            }

            addView(mMessageLabel, params);
        }
    }

    public void setMessage(CharSequence text) {
        mMessageLabel.setText(text);
    }
}
