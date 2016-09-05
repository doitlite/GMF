package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 15/7/22.
 */
public class BasicCell extends RelativeLayout {

    private TextView mTitleLabel;
    private TextView mExtraTitleLabel;
    private TextView mRedPointLabel;

    public BasicCell(Context context) {
        this(context, null);
    }

    public BasicCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasicCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundResource(R.drawable.sel_cell_bg_default);
        setClickable(true);

        boolean hideIcon = true;
        boolean hideArrow = false;
        int iconResId = R.mipmap.ic_launcher;
        String titleText = null;
        String extraTitleText = null;
        boolean hasTopLine = false;
        boolean hasBottomLine = false;
        boolean bottomLineHasLeftMargin = false;
        boolean bottomLineHasRightMargin = false;
        int titleColor = getResources().getColor(R.color.gmf_text_black);
        int extraTitleColor = getResources().getColor(R.color.gmf_text_grey);
        Drawable titleDrawableRight = null;
        Drawable extraTitleDrawableRight = null;
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BasicCell, defStyleAttr, 0);

            if (a != null) {
                hideIcon = a.getBoolean(R.styleable.BasicCell_hideIcon, hideIcon);
                hideArrow = a.getBoolean(R.styleable.BasicCell_hideArrow, hideArrow);
                iconResId = a.getResourceId(R.styleable.BasicCell_icon, iconResId);
                titleText = a.getString(R.styleable.BasicCell_title);
                extraTitleText = a.getString(R.styleable.BasicCell_extraTitle);
                hasTopLine = a.getBoolean(R.styleable.BasicCell_lineTop, hasTopLine);
                hasBottomLine = a.getBoolean(R.styleable.BasicCell_lineBottom, hasBottomLine);
                bottomLineHasLeftMargin = a.getBoolean(R.styleable.BasicCell_lineBottomHasMarginLeft, bottomLineHasLeftMargin);
                bottomLineHasRightMargin = a.getBoolean(R.styleable.BasicCell_lineBottomHasMarginRight, bottomLineHasRightMargin);
                titleDrawableRight = a.getDrawable(R.styleable.BasicCell_titleDrawableRight);
                extraTitleDrawableRight = a.getDrawable(R.styleable.BasicCell_extraTitleDrawableRight);
                titleColor = a.getColor(R.styleable.BasicCell_titleColor, titleColor);
                extraTitleColor = a.getColor(R.styleable.BasicCell_extraTitleColor, extraTitleColor);
                a.recycle();
            }
        }

        ImageView iconImage = new ImageView(getContext());
        {
            iconImage.setId(android.R.id.icon);
            iconImage.setImageResource(iconResId);
            iconImage.setVisibility(hideIcon ? View.GONE : View.VISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
            params.leftMargin = dp2px(this, 10);
            addView(iconImage, params);
        }

        ImageView arrowImage = new ImageView(getContext());
        {
            arrowImage.setVisibility(hideArrow ? View.GONE : View.VISIBLE);
            arrowImage.setId(android.R.id.icon1);
            arrowImage.setImageResource(R.mipmap.ic_arrow_right_dark);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(ALIGN_PARENT_RIGHT, TRUE);
            params.addRule(CENTER_VERTICAL, TRUE);
            params.rightMargin = dp2px(this, 10);
            addView(arrowImage, params);
        }

        TextView titleLabel = new TextView(getContext());
        {
            titleLabel.setId(R.id.label_title);
            titleLabel.setTextColor(titleColor);
            titleLabel.setTextSize(16);
            titleLabel.setCompoundDrawables(null, null, titleDrawableRight, null);
            titleLabel.setText(titleText);
            titleLabel.setSingleLine();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL, TRUE);
            params.leftMargin = dp2px(this, 10);
            params.addRule(RIGHT_OF, iconImage.getId());
            params.alignWithParent = true;
            addView(titleLabel, params);
            mTitleLabel = titleLabel;
        }

        TextView redPointLabel = new TextView(getContext());
        {
            redPointLabel.setMinWidth(dp2px(this, 16));
            redPointLabel.setHeight(dp2px(this, 16));
            redPointLabel.setTextSize(12);
            redPointLabel.setTextColor(getResources().getColor(R.color.gmf_text_white));
            redPointLabel.setGravity(Gravity.CENTER);
            redPointLabel.setSingleLine();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL, TRUE);
            params.leftMargin = dp2px(this, 8);
            params.addRule(RIGHT_OF, titleLabel.getId());
            params.alignWithParent = true;
            addView(redPointLabel, params);
            mRedPointLabel = redPointLabel;
        }

        TextView extraTitleLabel = new TextView(getContext());
        {
            extraTitleLabel.setText(extraTitleText);
            extraTitleLabel.setTextColor(extraTitleColor);
            extraTitleLabel.setTextSize(12);
            extraTitleLabel.setCompoundDrawables(null, null, extraTitleDrawableRight, null);
            extraTitleLabel.setSingleLine();
            extraTitleLabel.setGravity(Gravity.RIGHT);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = dp2px(this, 10);
            params.rightMargin = dp2px(this, 10);
            params.addRule(LEFT_OF, arrowImage.getId());
//            params.addRule(RIGHT_OF, titleLabel.getId());
            params.addRule(CENTER_VERTICAL, TRUE);
            params.alignWithParent = true;
            addView(extraTitleLabel, params);
            mExtraTitleLabel = extraTitleLabel;
        }


        if (hasTopLine) {
            View line = new View(getContext());
            line.setBackgroundColor(getResources().getColor(R.color.gmf_border_line));
            if (isInEditMode()) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                addView(line, params);
            } else {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(this, 1));
                addView(line, params);
            }
        }

        if (hasBottomLine) {
            View line = new View(getContext());
            line.setBackgroundColor(getResources().getColor(bottomLineHasLeftMargin ? R.color.gmf_sep_Line : R.color.gmf_border_line));
            if (isInEditMode()) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, TRUE);
                if(bottomLineHasLeftMargin){
                    params.addRule(RelativeLayout.ALIGN_LEFT, mTitleLabel.getId());
                }else {
                    params.leftMargin = 0;
                }
                params.rightMargin = 0;//bottomLineHasRightMargin ? 10 : 0;
                addView(line, params);
            } else {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(this, 1));
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, TRUE);
                if (bottomLineHasLeftMargin) {
                    params.addRule(RelativeLayout.ALIGN_LEFT, mTitleLabel.getId());
                } else {
                    params.leftMargin = 0;
                }
                params.rightMargin = 0;//bottomLineHasRightMargin ? dp2px(this, 10) : 0;
                addView(line, params);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // update height
        {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            if (mode != MeasureSpec.EXACTLY) {
                int heightSize = getResources().getDimensionPixelSize(R.dimen.gmf_list_cell_height);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public TextView getTitleLabel() {
        return mTitleLabel;
    }

    public TextView getRedPointLabel() {
        return mRedPointLabel;
    }

    public TextView getExtraTitleLabel() {
        return mExtraTitleLabel;
    }
}
