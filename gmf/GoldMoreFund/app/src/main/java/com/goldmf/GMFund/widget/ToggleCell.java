package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 15/8/20.
 */
public class ToggleCell extends RelativeLayout {

    private TextView mTitleLabel;
    private ImageView mToggleControl;
    private Listener mListener;

    public ToggleCell(Context context) {
        this(context, null);
    }

    public ToggleCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(0xFFFFFFFF);

        String title = null;
        boolean hasTopLine = false;
        boolean hasBottomLine = false;
        boolean bottomLineHasLeftMargin = false;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleCell, defStyleAttr, 0);
        if (a != null) {
            title = a.getString(R.styleable.ToggleCell_title);
            hasTopLine = a.getBoolean(R.styleable.ToggleCell_lineTop, hasTopLine);
            hasBottomLine = a.getBoolean(R.styleable.ToggleCell_lineBottom, hasBottomLine);
            bottomLineHasLeftMargin = a.getBoolean(R.styleable.ToggleCell_lineBottomHasMarginLeft, bottomLineHasLeftMargin);
            a.recycle();
        }

        mTitleLabel = new TextView(context);
        {
            mTitleLabel.setTextSize(16);
            mTitleLabel.setTextColor(getResources().getColor(R.color.gmf_text_black));
            mTitleLabel.setText(TextUtils.isEmpty(title) ? "" : title);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL);
            params.leftMargin = dp2px(this, 16);

            addView(mTitleLabel, params);
        }

        mToggleControl = new ImageView(context);
        {
            StateListDrawable background = new StateListDrawable();
            background.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(R.mipmap.bg_toggle_on));
            background.addState(new int[]{}, getResources().getDrawable(R.mipmap.bg_toggle_off));
            mToggleControl.setImageDrawable(background);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.rightMargin = dp2px(this, 16);

            addView(mToggleControl, params);

            v_setClick(mToggleControl, v -> {
                v.setSelected(!v.isSelected());
                if (mListener != null) {
                    mListener.onToggleChange(v.isSelected());
                }
            });
        }

        if (hasTopLine) {
            View line = new View(context);
            line.setBackgroundColor(getResources().getColor(R.color.gmf_border_line));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, dp2px(this, 1));
            params.addRule(ALIGN_PARENT_TOP);

            addView(line, params);
        }

        if (hasBottomLine) {
            View line = new View(context);
            line.setBackgroundColor(getResources().getColor(bottomLineHasLeftMargin ? R.color.gmf_sep_Line : R.color.gmf_border_line));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, dp2px(this, 1));
            params.addRule(ALIGN_PARENT_BOTTOM);

            addView(line, params);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            final int heightSize = dp2px(this, 52);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setToggleControlState(boolean isOpen) {
        mToggleControl.setSelected(isOpen);
    }

    public void setListener(Listener listener) {
        mListener = listener;
        if (mListener != null) {
            mListener.onToggleChange(mToggleControl.isSelected());
        }
    }

    public interface Listener {
        void onToggleChange(boolean isOn);
    }
}
