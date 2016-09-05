package com.goldmf.GMFund.controller.dialog;

import android.content.Context;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.model.GMFCommResult;

import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by Evan on 16/2/27 下午3:25.
 */
public class ScoreAwardDialog extends BasicDialog {

    public ScoreAwardDialog(Context context, GMFCommResult data) {
        super(context, R.style.GMFDialog_FullScreen);
        getWindow().setLayout(-1, -2);
        setContentView(R.layout.dialog_score_award);
        updateContent(data);
    }

//    private void fetchData() {
//        Context ctx = getContext();
//        gainTodayScore()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(callback -> {
//                    if (isSuccess(callback) && callback.data != null) {
//                        updateContent(callback.data);
//                    } else {
//                        dismiss();
//                        UIControllerExtension.createAlertDialog(ctx, getErrorMessage(callback)).show();
//                    }
//                });
//        dismiss();
//    }

    private void updateContent(GMFCommResult data) {
        v_setImageUri(this, R.id.img_icon, safeGet(() -> data.imgUrl, ""));
        v_setText(this, R.id.label_score_account, safeGet(() -> data.title, ""));
        v_setText(this, R.id.label_extra_info, safeGet(() -> data.content, ""));
        v_setClick(this, R.id.btn_positive, v -> performGainedAward());
    }

    private void performGainedAward() {
        dismiss();
        NotificationCenter.scoreChangedSubject.onNext(null);
    }
}
