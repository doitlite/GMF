package com.goldmf.GMFund.controller.dialog;

import android.content.Context;
import android.view.Gravity;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.GMFCommResult;

import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by yale on 16/4/5.
 */
public class GainScoreDialog extends BasicDialog {
    public GainScoreDialog(Context context, GMFCommResult info) {
        super(context, R.style.GMFDialog);
        setContentView(R.layout.dialog_gain_score);

        VM vm = new VM(info);
        v_setImageUri(this, R.id.img_icon, vm.iconURL);
        v_setText(this, R.id.label_title, vm.title);
        v_setText(this, R.id.label_desc, vm.desc);

        v_setClick(this, R.id.btn_confirm, v -> {
            NotificationCenter.scoreChangedSubject.onNext(null);
            dismiss();
        });
    }

    private static class VM {
        private String iconURL;
        private CharSequence title;
        private CharSequence desc;


        public VM(GMFCommResult info) {
            this.iconURL = safeGet(() -> info.imgUrl, "");
            this.title = safeGet(() -> info.title, PlaceHolder.NULL_VALUE);
            this.desc = safeGet(() -> info.content, PlaceHolder.NULL_VALUE);
        }
    }
}
