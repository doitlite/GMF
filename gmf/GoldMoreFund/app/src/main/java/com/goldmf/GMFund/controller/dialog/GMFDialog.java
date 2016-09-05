package com.goldmf.GMFund.controller.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.internal.ActivityNavigation;

import rx.functions.Func3;

import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 15/8/18.
 */
public class GMFDialog extends BasicDialog {

    private TextView mTitleLabel;
    private TextView mMessageLabel;
    private Button mPositiveButton;
    private Button mNegativeButton;
    private View mButtonSection;
    private Func3<GMFDialog, Integer, KeyEvent, Boolean> mOnKeyDownDelegate;

    public GMFDialog(Context context) {
        super(context, R.style.GMFDialog);
        setContentView(R.layout.dialog_basic);
        mTitleLabel = v_findView(this, R.id.label_title);
        mMessageLabel = v_findView(this, R.id.label_message);
        mPositiveButton = v_findView(this, R.id.btn_positive);
        mNegativeButton = v_findView(this, R.id.btn_negative);
        mButtonSection = v_findView(this, R.id.section_button);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mOnKeyDownDelegate != null) {
            boolean hasConsumed = mOnKeyDownDelegate.call(this, keyCode, event);
            if (hasConsumed) return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setTitle(CharSequence text) {
        mTitleLabel.setText(text);
    }

    public CharSequence getTitle() {
        return mTitleLabel.getText();
    }

    public void setMessage(CharSequence text) {
        mMessageLabel.setText(text);
    }

    public CharSequence getMessage() {
        return mMessageLabel.getText();
    }

    public void setPositiveButton(CharSequence text, Dialog.OnClickListener listener) {
        if (listener == null) listener = (dialog, which) -> dismiss();
        setButton(mPositiveButton, text, listener);
    }

    public void setNegativeButton(CharSequence text, Dialog.OnClickListener listener) {
        if (listener == null) listener = (dialog, which) -> dismiss();
        setButton(mNegativeButton, text, listener);
    }

    public void setOnKeyDownDelegate(Func3<GMFDialog, Integer, KeyEvent, Boolean> onKeyDownDelegate) {
        mOnKeyDownDelegate = onKeyDownDelegate;
    }

    private void setButton(Button button, CharSequence text, final Dialog.OnClickListener listener) {
        final int id = button.getId();
        button.setText(text);
        v_setClick(button, v -> listener.onClick(this, id));
    }

    public static class Builder {
        private Context mContext;
        private CharSequence mTitle;
        private CharSequence mMessage;
        private boolean mCancelable = true;
        private Pair<CharSequence, Dialog.OnClickListener> mPositiveButtonInfo;
        private Pair<CharSequence, Dialog.OnClickListener> mNegativeButtonInfo;
        private OnDismissListener mOnDismissListener;
        private Func3<GMFDialog, Integer, KeyEvent, Boolean> mOnKeyDownDelegate;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setTitle(CharSequence title) {
            mTitle = title;
            return this;
        }

        public Builder setMessage(CharSequence message) {
            mMessage = message;
            return this;
        }

        public Builder setPositiveButton(CharSequence text) {
            return setPositiveButton(text, (dialog, which) -> dialog.dismiss());
        }

        public Builder setPositiveButton(CharSequence text, Dialog.OnClickListener listener) {
            mPositiveButtonInfo = new Pair<>(text, listener);
            return this;
        }

        public Builder setNegativeButton(CharSequence text) {
            return setNegativeButton(text, (dialog, which) -> dialog.dismiss());
        }

        public Builder setNegativeButton(CharSequence text, Dialog.OnClickListener listener) {
            mNegativeButtonInfo = new Pair<>(text, listener);
            return this;
        }

        public Builder setCancelable(boolean value) {
            mCancelable = value;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            mOnDismissListener = listener;
            return this;
        }

        public Builder setOnKeyDownDelegate(Func3<GMFDialog, Integer, KeyEvent, Boolean> onKeyDownDelegate) {
            mOnKeyDownDelegate = onKeyDownDelegate;
            return this;
        }

        public GMFDialog create() {
            GMFDialog dialog = new GMFDialog(mContext);
            dialog.setCancelable(mCancelable);

            if (TextUtils.isEmpty(mTitle)) {
                dialog.mTitleLabel.setVisibility(View.GONE);
            } else {
                dialog.setTitle(mTitle);
            }

            if (TextUtils.isEmpty(mMessage)) {
                dialog.setMessage("");
            } else {
                dialog.setMessage(mMessage);
            }

            if (mOnDismissListener != null) {
                dialog.setOnDismissListener(mOnDismissListener);
            }

            if (mOnKeyDownDelegate != null) {
                dialog.setOnKeyDownDelegate(mOnKeyDownDelegate);
            }

            if (mNegativeButtonInfo == null && mPositiveButtonInfo == null) {
                dialog.mButtonSection.setVisibility(View.GONE);
            } else {
                if (mPositiveButtonInfo == null) {
                    dialog.mPositiveButton.setVisibility(View.GONE);
                } else {
                    dialog.setPositiveButton(mPositiveButtonInfo.first, mPositiveButtonInfo.second);
                }
                if (mNegativeButtonInfo == null) {
                    dialog.mNegativeButton.setVisibility(View.GONE);
                } else {
                    dialog.setNegativeButton(mNegativeButtonInfo.first, mNegativeButtonInfo.second);
                }
            }

            return dialog;
        }
    }
}
