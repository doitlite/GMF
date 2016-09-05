package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.util.DimensionConverter;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;

/**
 * Created by yale on 15/7/22.
 */
public class SectionHeader extends RelativeLayout {
    private TextView mTitleLabel;
    private TextView mExtraTitleLabel;


    public SectionHeader(Context context) {
        this(context, null);
    }

    public SectionHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ConstantConditions")
    public SectionHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundResource(R.drawable.sel_cell_bg_default);
        setClickable(true);

        boolean hideArrow = true;
        boolean hideTopLine = true;
        boolean hideBottomLine = true;
        String titleText = null;
        String extraTitleText = null;
        int indicatorColor = R.color.gmf_blue;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SectionHeader, defStyleAttr, 0);
        if (a != null) {

            hideArrow = a.getBoolean(R.styleable.SectionHeader_hideArrow, hideArrow);
            titleText = a.getString(R.styleable.SectionHeader_title);
            extraTitleText = a.getString(R.styleable.SectionHeader_extraTitle);
            indicatorColor = a.getColor(R.styleable.SectionHeader_indicatorColor, indicatorColor);
            hideTopLine = a.getBoolean(R.styleable.SectionHeader_hideTopLine, hideTopLine);
            hideBottomLine = a.getBoolean(R.styleable.SectionHeader_hideBottomLine, hideBottomLine);
            a.recycle();
        }

        View indicatorView = new View(getContext());
        {
            indicatorView.setId(android.R.id.icon);
            indicatorView.setBackgroundColor(indicatorColor);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 4), dp2px(this, 20));
            params.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
            params.leftMargin = dp2px(this, 10);
            addView(indicatorView, params);
        }

        ImageView arrowImage = new ImageView(getContext());
        {
            arrowImage.setVisibility(hideArrow ? View.GONE : View.VISIBLE);
            arrowImage.setId(android.R.id.icon1);
            arrowImage.setImageResource(R.mipmap.ic_arrow_right_dark);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 4), dp2px(this, 8));
            params.addRule(ALIGN_PARENT_RIGHT, TRUE);
            params.addRule(CENTER_VERTICAL, TRUE);
            params.rightMargin = dp2px(this, 10);
            addView(arrowImage, params);
        }

        mExtraTitleLabel = new TextView(getContext());
        {
            mExtraTitleLabel.setText(extraTitleText);
            mExtraTitleLabel.setTextSize(14);
            mExtraTitleLabel.setTextColor(getResources().getColor(R.color.gmf_text_black));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = dp2px(this, 24);
            params.addRule(CENTER_VERTICAL, TRUE);
            params.addRule(ALIGN_PARENT_RIGHT, TRUE);
            params.alignWithParent = true;
            addView(mExtraTitleLabel, params);
        }

        {
            mTitleLabel = new TextView(getContext());
            mTitleLabel.setTextColor(getResources().getColor(R.color.gmf_text_black));
            mTitleLabel.setTextSize(16);
            mTitleLabel.getPaint().setFakeBoldText(true);
            if (!TextUtils.isEmpty(titleText)) {
                mTitleLabel.setText(titleText);
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL, TRUE);
            params.leftMargin = dp2px(this, 16);
            params.rightMargin = dp2px(this, 8);
            params.addRule(RIGHT_OF, indicatorView.getId());
            params.addRule(LEFT_OF, mExtraTitleLabel.getId());
            params.alignWithParent = true;
            addView(mTitleLabel, params);
        }

        {
            View topLine = new View(getContext());
            topLine.setBackgroundColor(getResources().getColor(R.color.gmf_border_line));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(this, 1));
            addView(topLine, params);
            v_setVisibility(topLine, hideTopLine ? View.GONE : View.GONE);
        }

        {
            View bottomLine = new View(getContext());
            bottomLine.setBackgroundColor(getResources().getColor(R.color.gmf_sep_Line));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(this, 1));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, TRUE);
            params.leftMargin = dp2px(this, 16);
            addView(bottomLine, params);
            v_setVisibility(bottomLine, hideBottomLine ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // update height
        {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            if (mode != MeasureSpec.EXACTLY) {
                int heightSize = dp2px(this, 40);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setTitle(CharSequence text) {
        mTitleLabel.setText(text);
    }

    public void setExtraTitle(CharSequence text) {
        mExtraTitleLabel.setText(text);
    }
}
