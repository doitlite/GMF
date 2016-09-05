package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.UIControllerExtension;

import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;

/**
 * Created by yale on 15/8/29.
 */
public class ProgressButton extends RelativeLayout {

    public static final int BUTTON_THEME_BLACK = 0;
    public static final int BUTTON_THEME_BLUE = 1;
    public static final int BUTTON_THEME_YELLOW = 2;
    public static final int BUTTON_THEME_RED = 3;
    public static final int BUTTON_THEME_ORANGE = 4;

    public enum Mode {
        Normal,
        Loading,
        FAILURE
    }

    private TextView mMessageLabel;
    private View mLoadingProgress;
    private ButtonTheme mButtonTheme;
    private Mode mMode;
    private CharSequence mNormalText;
    private CharSequence mLoadingText;
    private CharSequence mFailureText;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.widget_progress_button, this, true);

        int textSizeInPx = sp2px(this, 16);
        int theme = BUTTON_THEME_BLACK;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton, defStyleAttr, 0);
        if (a != null) {
            textSizeInPx = a.getDimensionPixelSize(R.styleable.ProgressButton_textSize, textSizeInPx);
            mNormalText = a.getString(R.styleable.ProgressButton_text_for_normal);
            mLoadingText = a.getString(R.styleable.ProgressButton_text_for_loading);
            mLoadingText = TextUtils.isEmpty(mLoadingText) ? mNormalText : mLoadingText;
            mFailureText = a.getString(R.styleable.ProgressButton_text_for_failure);
            mFailureText = TextUtils.isEmpty(mFailureText) ? mNormalText : mFailureText;
            theme = a.getInt(R.styleable.ProgressButton_buttonTheme, theme);
            a.recycle();
        }

        mLoadingProgress = v_findView(this, R.id.progress);

        mMessageLabel = v_findView(this, R.id.text1);
        mMessageLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx);

        setButtonTheme(newButtonTheme(theme));
        setMode(Mode.Normal);
        updateMessageLabelText();
    }

    public void setText(CharSequence message, Mode mode) {
        switch (mode) {
            case Normal:
                mNormalText = message;
                break;
            case Loading:
                mLoadingText = message;
                break;
            case FAILURE:
                mFailureText = message;
                break;
        }

        updateMessageLabelText();
    }

    public CharSequence getTextWithMode(Mode mode) {
        switch (mode) {
            case Normal:
                return mNormalText;
            case Loading:
                return mLoadingText;
            case FAILURE:
                return mFailureText;
            default:
                return "";
        }
    }

    public CharSequence getTextOfCurrentMode() {
        switch (mMode) {
            case Normal:
                return mNormalText;
            case Loading:
                return mLoadingText;
            case FAILURE:
                return mFailureText;
            default:
                return "";
        }
    }

    public void setMode(Mode mode) {
        if (mode != null) {
            mMode = mode;
            if (mode == Mode.Loading) {
                UIControllerExtension.hideKeyboardFromWindow(this);
            }
            updateMessageLabelText();
            switch (mode) {
                case Normal:
                    getMessageLabelParams().leftMargin = 0;
                    v_setVisible(mMessageLabel);
                    mMessageLabel.setTextColor(mButtonTheme.textColor);
                    v_setGone(mLoadingProgress);
                    setBackgroundDrawable(mButtonTheme.normalBG);
                    break;
                case Loading:
                    getMessageLabelParams().leftMargin = dp2px(this, 16);
                    v_setVisible(mMessageLabel);
                    v_setVisible(mLoadingProgress);
                    mMessageLabel.setTextColor(mButtonTheme.textColor);
                    setBackgroundDrawable(mButtonTheme.loadingBG);
                    break;
                case FAILURE:
                    getMessageLabelParams().leftMargin = 0;
                    v_setVisible(mMessageLabel);
                    v_setGone(mLoadingProgress);
                    mMessageLabel.setTextColor(mButtonTheme.failureTextColor);
                    setBackgroundDrawable(mButtonTheme.failureBG);
                    break;
            }
        }
    }

    private void updateMessageLabelText() {
        switch (mMode) {
            case Normal:
                mMessageLabel.setText(mNormalText);
                break;
            case Loading:
                mMessageLabel.setText(mLoadingText);
                break;
            case FAILURE:
                mMessageLabel.setText(mFailureText);
                break;
        }
    }

    private LinearLayout.LayoutParams getMessageLabelParams() {
        return (LinearLayout.LayoutParams) mMessageLabel.getLayoutParams();
    }

    public void setButtonTheme(ButtonTheme theme) {
        mButtonTheme = theme;
        mMessageLabel.setTextColor(theme.textColor);
        setMode(Mode.Normal);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode == Mode.Normal)
                    l.onClick(v);
            }
        });
    }

    public static class ButtonTheme {
        public final Drawable normalBG;
        public final Drawable loadingBG;
        public final Drawable failureBG;
        public final ColorStateList textColor;
        public final int failureTextColor;

        public ButtonTheme(Resources resources, int normalBgRes, int loadingBgRes, int textColorRes) {
            this.normalBG = resources.getDrawable(normalBgRes);
            this.loadingBG = resources.getDrawable(loadingBgRes);
            this.failureBG = new ShapeDrawable(new RoundCornerShape(0xFFE2E2E2, dp2px(4)));
            this.textColor = resources.getColorStateList(textColorRes);
            this.failureTextColor = 0xFF999999;
        }
    }

    public ButtonTheme newButtonTheme(int theme) {
        if (theme == BUTTON_THEME_BLACK) {
            return new ButtonTheme(getResources(), R.drawable.sel_round_button_black_bg, R.drawable.sel_round_button_black_bg, R.color.sel_button_text_light);
        } else if (theme == BUTTON_THEME_BLUE) {
            return new ButtonTheme(getResources(), R.drawable.sel_round_button_blue_bg, R.drawable.sel_round_button_blue_bg, R.color.sel_button_text_light);
        } else if (theme == BUTTON_THEME_YELLOW) {
            return new ButtonTheme(getResources(), R.drawable.sel_round_button_yellow_bg, R.drawable.sel_round_button_yellow_bg, R.color.sel_button_text_dark);
        } else if (theme == BUTTON_THEME_RED) {
            return new ButtonTheme(getResources(), R.drawable.sel_round_button_red_bg, R.drawable.sel_round_button_red_bg, R.color.sel_button_text_light);
        } else if (theme == BUTTON_THEME_ORANGE) {
            return new ButtonTheme(getResources(), R.drawable.sel_round_button_orange_bg, R.drawable.sel_round_button_orange_bg, R.color.sel_button_text_light);
        }
        return null;
    }

    private static int sp2px(View view, float sp) {
        Resources resources = view.getContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.getDisplayMetrics());
    }

}
