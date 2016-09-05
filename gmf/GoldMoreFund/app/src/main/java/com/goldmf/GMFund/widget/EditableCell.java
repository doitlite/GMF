package com.goldmf.GMFund.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 15/7/22.
 */
public class EditableCell extends RelativeLayout {

    private EditText mInputField;
    private TextView mTitleLabel;
    private TextView mHintLabel;
    private TextView mExtraHintLabel;

    public EditableCell(Context context) {
        this(context, null);
    }

    public EditableCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditableCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(getResources().getColor(R.color.gmf_white));
        setClickable(true);

        String title;
        String text;
        String hintText;
        int textColor = getResources().getColor(R.color.gmf_text_black);
        boolean hasTopLine;
        boolean hasBottomLine;
        boolean bottomLineHasLeftMargin;
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditableCell, defStyleAttr, 0);

            title = a.getString(R.styleable.EditableCell_title);
            text = a.getString(R.styleable.EditableCell_text);
            hintText = a.getString(R.styleable.EditableCell_hintText);
            hasTopLine = a.getBoolean(R.styleable.EditableCell_lineTop, false);
            hasBottomLine = a.getBoolean(R.styleable.EditableCell_lineBottom, false);
            bottomLineHasLeftMargin = a.getBoolean(R.styleable.EditableCell_lineBottomHasMarginLeft, false);
            textColor = a.getColor(R.styleable.EditableCell_textColor, textColor);
            a.recycle();
        }

        {
            mTitleLabel = new TextView(context);
            mTitleLabel.setId(R.id.text1);
            mTitleLabel.setText(title);
            mTitleLabel.setTextColor(getResources().getColor(R.color.gmf_text_black));

            LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL);
            params.leftMargin = dp2px(this, 20);

            addView(mTitleLabel, params);
        }


        // initialize inputContainer
        LinearLayout inputContainer = new LinearLayout(context);
        {
            LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.addRule(RIGHT_OF, R.id.text1);

            addView(inputContainer, params);
        }

        mInputField = new EditText(context);
        {
            mInputField.setText(text);
            mInputField.setHint(hintText);
            mInputField.setTextColor(textColor);
            mInputField.setHintTextColor(getResources().getColor(R.color.gmf_text_hint));
            mInputField.setTextSize(14);
            mInputField.setBackgroundColor(0);
            mInputField.setSingleLine();
            mInputField.setEllipsize(TextUtils.TruncateAt.END);
            mInputField.setGravity(Gravity.RIGHT);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.leftMargin = dp2px(this, 16);
            params.rightMargin = dp2px(this, 16);

            inputContainer.addView(mInputField, params);
        }

        if (hasTopLine) {
            View line = new View(getContext());
            line.setBackgroundColor(getResources().getColor(R.color.gmf_border_line));
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            addView(line, params);
        }

        if (hasBottomLine) {
            View line = new View(getContext());
            line.setBackgroundColor(getResources().getColor(bottomLineHasLeftMargin ? R.color.gmf_sep_Line : R.color.gmf_border_line));
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
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

    public EditText getInputField() {
        return mInputField;
    }

    private ObjectAnimator mRunningAnimatorOrNil;
}
