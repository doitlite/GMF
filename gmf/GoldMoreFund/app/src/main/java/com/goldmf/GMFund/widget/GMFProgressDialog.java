package com.goldmf.GMFund.widget;

import android.content.Context;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.dialog.BasicDialog;

import static com.goldmf.GMFund.extension.ViewExtension.v_findView;

/**
 * Created by yale on 15/8/11.
 */
public class GMFProgressDialog extends BasicDialog {
    private TextView mTitleLabel;

    public GMFProgressDialog(Context context) {
        this(context, "请稍候...");
    }

    public GMFProgressDialog(Context context, CharSequence message) {
        super(context, R.style.GMFDialog);
        setContentView(R.layout.dialog_progress);
        mTitleLabel = v_findView(this, R.id.label_title);
        setMessage(message);
        setCancelable(false);
    }

    public void setMessage(CharSequence text) {
        mTitleLabel.setText(text);
    }
}
