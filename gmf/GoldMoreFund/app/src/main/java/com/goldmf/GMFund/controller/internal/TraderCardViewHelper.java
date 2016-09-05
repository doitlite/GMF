package com.goldmf.GMFund.controller.internal;

import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.FlagExtension;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.User;

import rx.functions.Func0;
import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.*;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by yalez on 2016/7/29.
 */
public class TraderCardViewHelper {
    public static final int FLAG_BACKGROUND_PURE_WHITE = 1;

    private TraderCardViewHelper() {
    }

    public static View createCell(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_trader_card, parent, false);
    }

    public static void bindChildren(ChildBinder binder) {
        binder.bindChildWithTag("avatarImage", R.id.img_avatar)
                .bindChildWithTag("nameLabel", R.id.label_name)
                .bindChildWithTag("fundLabel", R.id.label_fund)
                .bindChildWithTag("descLabel", R.id.label_desc);
    }

    public static void configureCell(ChildBinder binder, Func0<TraderCardVM> itemGetter) {
        TraderCardVM item = itemGetter.call();
        v_setImageUri(binder.getChildWithTag("avatarImage"), item.avatarURL);
        v_setText(binder.getChildWithTag("nameLabel"), item.name);
        v_setText(binder.getChildWithTag("fundLabel"), item.fundState);
        v_setText(binder.getChildWithTag("descLabel"), item.desc);
    }

    public static void afterCellCreated(View itemView, Func0<TraderCardVM> itemGetter, int flags) {
        boolean backgroundPureWhite = hasFlag(flags, FLAG_BACKGROUND_PURE_WHITE);

        if (backgroundPureWhite) {
            itemView.setBackgroundColor(WHITE_COLOR);
        } else {
            itemView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(2)).border(LINE_SEP_COLOR, dp2px(0.5f))));
        }

        v_setClick(itemView, v -> {
            TraderCardViewHelper.TraderCardVM item = itemGetter.call();
            if (item != null) {
                showActivity(itemView.getContext(), an_UserDetailPage(item.raw));
            }
        });
    }

    public static class TraderCardVM {
        public User raw;
        public String avatarURL;
        public CharSequence name;
        public CharSequence fundState;
        public CharSequence desc;


        public TraderCardVM(User user) {
            this.raw = user;
            this.avatarURL = safeGet(() -> user.getPhotoUrl(), "");
            this.name = safeGet(() -> user.getName(), CommonDefine.PlaceHolder.NULL_VALUE);
            this.fundState = safeGet(() -> user.trader.secondText, "");
            this.desc = safeGet(() -> user.trader.brief, "");
        }
    }
}
