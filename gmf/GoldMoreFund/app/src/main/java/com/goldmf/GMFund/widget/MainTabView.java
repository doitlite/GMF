package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 16/2/18.
 */
public class MainTabView extends RelativeLayout {

    private View mRedDotView;

    public MainTabView(Context context) {
        this(context, null);
    }

    public MainTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setClickable(true);

        String titleText = "No Title";
        int iconResId = 0;
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.MainTabView, defStyleAttr, 0);
        if (arr != null) {
            titleText = Optional.of(arr.getString(R.styleable.MainTabView_title)).or(titleText);
            iconResId = arr.getResourceId(R.styleable.MainTabView_gmf_icon, iconResId);
            arr.recycle();
        }

        Resources res = context.getResources();

        {
            TextView mTitleLabel = new TextView(context);
            mTitleLabel.setDuplicateParentStateEnabled(true);
            mTitleLabel.setId(android.R.id.text1);
            mTitleLabel.setTextSize(10);
            mTitleLabel.setTextColor(res.getColorStateList(R.color.sel_tab_main_text));
            mTitleLabel.setText(titleText);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(CENTER_HORIZONTAL);
            params.addRule(ALIGN_PARENT_BOTTOM);
            params.bottomMargin = dp2px(this, 8);

            addView(mTitleLabel, params);
        }

        FrameLayout imageContainer = new FrameLayout(context);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, 0);
            params.addRule(ALIGN_PARENT_TOP);
            params.addRule(ABOVE, android.R.id.text1);

            addView(imageContainer, params);
        }

        ImageView mIconImage = new ImageView(context);
        mIconImage.setImageResource(iconResId);
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.CENTER;

            imageContainer.addView(mIconImage, params);
        }

        View redDotView = new View(context);
        redDotView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0xFFEC1919, dp2px(this, 3))));
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 6), dp2px(this, 6));
            params.addRule(ALIGN_PARENT_RIGHT);
            params.topMargin = dp2px(this, 6);
            params.rightMargin = dp2px(this, 16);

            addView(redDotView, params);
        }
        mRedDotView = redDotView;

        setRedDotViewVisibility(View.GONE);
    }

    public void setRedDotViewVisibility(int visibility) {
        mRedDotView.setVisibility(visibility);
    }
}
