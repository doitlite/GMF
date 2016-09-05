package com.goldmf.GMFund.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.manager.mine.MineManager;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FeedbackPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLUE_COLOR;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;

/**
 * Created by cupide on 16/4/15.
 */
public class StringFactoryUtil {

    /**
     * 联系客服 的富文本
     */
    public static SpannableStringBuilder contactCustomerService(Context ctx, TextView textView) {
        textView.setMovementMethod(new LinkMovementMethod());


        return setColor(setClickEvent("联系客服", v -> {

            if (MineManager.getInstance().isLoginOK()) {
                showActivity(ctx, an_FeedbackPage(Optional.empty()));
            } else {
                GMFDialog.Builder builder = new GMFDialog.Builder(ctx);
                builder.setTitle("联系客服");
                builder.setMessage("客服电话：400 009 3581");
                builder.setPositiveButton("拨打", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "4000093581"));
                    ctx.startActivity(intent);
                });
                builder.setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.create().show();
            }
        }), TEXT_BLUE_COLOR);
    }

}
