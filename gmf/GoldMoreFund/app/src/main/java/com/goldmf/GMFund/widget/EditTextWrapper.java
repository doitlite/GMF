package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.goldmf.GMFund.R;

/**
 * Created by yale on 15/8/11.
 */
public class EditTextWrapper extends RelativeLayout {

    private boolean mHasTopLine = true;
    private boolean mHasBottomLine = true;
    private boolean mHasBottomLineLeftMargin = false;

    public EditTextWrapper(Context context) {
        this(context, null);
    }

    public EditTextWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // initialize attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditTextWrapper, defStyleAttr, 0);
        if (a != null) {

            mHasTopLine = a.getBoolean(R.styleable.EditTextWrapper_lineTop, mHasTopLine);
            mHasBottomLine = a.getBoolean(R.styleable.EditTextWrapper_lineBottom, mHasBottomLine);
            mHasBottomLineLeftMargin = a.getBoolean(R.styleable.EditTextWrapper_lineBottomHasMarginLeft, mHasBottomLineLeftMargin);

            a.recycle();
        }

    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams p) {
        super.addView(child, index, p);
        if (child instanceof EditText) {

            // add top line to this view
            if (mHasTopLine) {
                View topLine = new View(getContext());
                topLine.setBackgroundResource(R.color.gmf_border_line);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, dp2px(this, 1));

                addView(topLine, params);
            }


            // add bottom line to this view
            if (mHasBottomLine) {
                View bottomView = new View(getContext());
                bottomView.setBackgroundResource(mHasBottomLineLeftMargin ? R.color.gmf_sep_Line : R.color.gmf_border_line);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, 1);
                params.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                params.leftMargin = mHasBottomLineLeftMargin ? dp2px(this, 16) : 0;

                addView(bottomView, params);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(getResources().getDimensionPixelSize(R.dimen.gmf_list_cell_height), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private static int dp2px(View view, float dp) {
        Resources resources = view.getContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
