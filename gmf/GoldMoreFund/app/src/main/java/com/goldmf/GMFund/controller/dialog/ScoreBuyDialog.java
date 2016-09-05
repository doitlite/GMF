package com.goldmf.GMFund.controller.dialog;

import android.content.Context;

import com.goldmf.GMFund.R;

/**
 * Created by Evan on 16/6/17 下午3:33.
 */
public class ScoreBuyDialog extends BasicDialog {

    public ScoreBuyDialog(Context context) {
        super(context,R.style.GMFDialog_FullScreen);
        setCancelable(false);
        setContentView(R.layout.dialog_score_buy);
    }
}
