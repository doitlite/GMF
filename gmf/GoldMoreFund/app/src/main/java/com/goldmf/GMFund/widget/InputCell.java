package com.goldmf.GMFund.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 15/7/22.
 */
public class InputCell extends RelativeLayout {

    private TextView mInputField;
    private TextView mTitleLabel;
    private TextView mHintLabel;
    private TextView mExtraHintLabel;
    private View mCursor;

    public InputCell(Context context) {
        this(context, null);
    }

    public InputCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(getResources().getColor(R.color.gmf_white));
        setClickable(true);

        String title;
        String text;
        String hintText;
        String extraHint;
        boolean hasTopLine;
        boolean hasBottomLine;
        boolean bottomLineHasLeftMargin;
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InputCell, defStyleAttr, 0);

            title = a.getString(R.styleable.InputCell_title);
            text = a.getString(R.styleable.InputCell_text);
            hintText = a.getString(R.styleable.InputCell_hintText);
            extraHint = a.getString(R.styleable.InputCell_extraHintText);
            hasTopLine = a.getBoolean(R.styleable.InputCell_lineTop, false);
            hasBottomLine = a.getBoolean(R.styleable.InputCell_lineBottom, false);
            bottomLineHasLeftMargin = a.getBoolean(R.styleable.InputCell_lineBottomHasMarginLeft, false);
            a.recycle();
        }

        {
            mTitleLabel = new TextView(context);
            mTitleLabel.setText(title);
            mTitleLabel.setTextColor(getResources().getColor(R.color.gmf_text_black));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL);
            params.leftMargin = dp2px(this, 16);

            addView(mTitleLabel, params);
        }


        // initialize inputContainer
        LinearLayout inputContainer = new LinearLayout(context);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_RIGHT);

            addView(inputContainer, params);
        }

        {
            mExtraHintLabel = new TextView(context);
            mExtraHintLabel.setText(extraHint);
            mExtraHintLabel.setBackgroundColor(getResources().getColor(R.color.gmf_red));
            mExtraHintLabel.setTextColor(getResources().getColor(R.color.gmf_text_white));
            mExtraHintLabel.setHintTextColor(getResources().getColor(R.color.gmf_text_hint));
            mExtraHintLabel.setTextSize(12);
            mExtraHintLabel.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.rightMargin = dp2px(this, 16);
            params.gravity = Gravity.CENTER_VERTICAL;

            inputContainer.addView(mExtraHintLabel, params);
        }

        mInputField = new TextView(context);
        {
            mInputField.setText(text);
            mInputField.setTextColor(getResources().getColor(R.color.gmf_black));
            mInputField.setTextSize(14);
            mInputField.setBackgroundColor(0);
            mInputField.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            inputContainer.addView(mInputField, params);
        }

        mHintLabel = new TextView(context);
        {
            mHintLabel.setText(hintText);
            mHintLabel.setTextColor(getResources().getColor(R.color.gmf_text_grey));
            mInputField.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            inputContainer.addView(mHintLabel, params);
        }

        mCursor = new View(context);
        {
            mCursor.setBackgroundColor(0xFF3498DB);
            mCursor.setTranslationX(-dp2px(this, 2));
            mCursor.setAlpha(0);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(this, 2), dp2px(this, 20));
            params.gravity = Gravity.CENTER_VERTICAL;
            params.rightMargin = dp2px(this, 16);

            inputContainer.addView(mCursor, params);
        }


        if (hasTopLine) {
            View line = new View(getContext());
            line.setBackgroundColor(getResources().getColor(R.color.gmf_border_line));
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(this, 1));
            addView(line, params);
        }

        if (hasBottomLine) {
            View line = new View(getContext());
            line.setBackgroundColor(getResources().getColor(bottomLineHasLeftMargin ? R.color.gmf_sep_Line : R.color.gmf_border_line));
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(this, 1));
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, TRUE);
            params.leftMargin = bottomLineHasLeftMargin ? dp2px(this, 16) : 0;
            addView(line, params);
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

    public TextView getHintLabel() {
        return mHintLabel;
    }

    public TextView getExtraHintLabel() {
        return mExtraHintLabel;
    }

    public TextView getInputField() {
        return mInputField;
    }

    private ObjectAnimator mRunningAnimatorOrNil;

    public void animateCursor() {
        mRunningAnimatorOrNil = ObjectAnimator.ofFloat(mCursor, "alpha", 0, 1).setDuration(500);
        mRunningAnimatorOrNil.setInterpolator(new FastOutSlowInInterpolator());
        mRunningAnimatorOrNil.setRepeatCount(ValueAnimator.INFINITE);
        mRunningAnimatorOrNil.setRepeatMode(ObjectAnimator.REVERSE);
        mRunningAnimatorOrNil.start();
    }

    public void stopAnimateCursor() {
        if (mRunningAnimatorOrNil != null) {
            mRunningAnimatorOrNil.cancel();
            mRunningAnimatorOrNil = null;
        }
        mCursor.setAlpha(0);
    }
}
