package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.goldmf.GMFund.R;

import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;

/**
 * Created by yale on 16/2/24.
 */
public class SearchView extends RelativeLayout {
    private Button mCancelButton;
    private EditText mEditText;
    private ImageView mCleanImage;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundResource(R.color.gmf_yellow);

        mCancelButton = new Button(context);
        mCancelButton.setId(android.R.id.button1);
        mCancelButton.setBackgroundDrawable(null);
        mCancelButton.setText("取消");
        mCancelButton.setTextSize(14);
        mCancelButton.setTextColor(0xFF030303);
        ViewCompat.setElevation(mCancelButton, 0);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 60), -1);
            params.addRule(ALIGN_PARENT_RIGHT);
            mCancelButton.setLayoutParams(params);
        }
        addView(mCancelButton);

        RelativeLayout wrapper = new RelativeLayout(context);
        wrapper.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0x1A000000, dp2px(this, 4))));
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, dp2px(this, 36));
            params.leftMargin = dp2px(this, 10);
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_LEFT);
            params.addRule(LEFT_OF, mCancelButton.getId());
            wrapper.setLayoutParams(params);
        }
        addView(wrapper);

        ImageView searchImage = new ImageView(context);
        searchImage.setId(android.R.id.icon1);
        searchImage.setImageResource(R.mipmap.ic_search_dark);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(CENTER_VERTICAL);
            params.leftMargin = dp2px(this, 8);
            searchImage.setLayoutParams(params);
        }
        wrapper.addView(searchImage);

        mEditText = new EditText(context);
        mEditText.setTextSize(16);
        mEditText.setPadding(0, 0, 0, 0);
        mEditText.setBackgroundColor(0);
        mEditText.setTextColor(TEXT_BLACK_COLOR);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEditText.setSingleLine(true);
        mEditText.setHint("股票代码、拼音、首字母");
        mEditText.setEllipsize(TextUtils.TruncateAt.START);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, -1);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.addRule(RIGHT_OF, searchImage.getId());
            params.addRule(CENTER_VERTICAL);
            params.leftMargin = dp2px(this, 8);
            params.rightMargin = dp2px(this, 8);
            mEditText.setLayoutParams(params);
        }
        wrapper.addView(mEditText);

        mCleanImage = new ImageView(context);
        mCleanImage.setImageResource(R.mipmap.ic_clean_text);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_RIGHT);
            params.rightMargin = dp2px(this, 8);
            mCleanImage.setLayoutParams(params);
        }
        v_setGone(mCleanImage);
        wrapper.addView(mCleanImage);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 44), MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public EditText getEditText() {
        return mEditText;
    }

    public Button getCancelButton() {
        return mCancelButton;
    }

    public ImageView getCleanButton() {
        return mCleanImage;
    }
}
