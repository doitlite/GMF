package com.goldmf.GMFund.controller.internal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.FlagExtension;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.model.GMFRankUser;
import com.goldmf.GMFund.util.SecondUtil;

import rx.functions.Func0;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockMatchDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;
import static com.goldmf.GMFund.model.GMFMatch.STATE_ING;
import static com.goldmf.GMFund.model.GMFMatch.STATE_OVER;
import static com.goldmf.GMFund.model.GMFMatch.STATE_SIGNUP;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static com.goldmf.GMFund.util.FormatUtil.formateRemainingDays;

/**
 * Created by yalez on 2016/7/29.
 */
public class StockMatchHomeCardHelper {
    public static final int FLAG_USE_HOME_STYLE = 1;

    private StockMatchHomeCardHelper() {
    }

    public static View createCell(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_stock_match_list_page, parent, false);
    }

    public static void afterCellCreated(View itemView, Func0<StockMatchHomeVM> itemGetter, int flags) {
        boolean useHomeStyle = hasFlag(flags, FLAG_USE_HOME_STYLE);

        Context ctx = itemView.getContext();
        StockMatchHomeVM item = itemGetter.call();
        v_setClick(itemView, v -> showActivity(ctx, an_StockMatchDetailPage(item.matchId)));

        v_updateLayoutParams(itemView, params -> {
            params.height = useHomeStyle ? dp2px(84) : dp2px(64);
        });
    }

    public static void bindChilds(ChildBinder binder) {
        binder.bindChildWithTag("matchNameLabel", R.id.label_match_name)
                .bindChildWithTag("matchStateImage", R.id.img_match_state)
                .bindChildWithTag("signingBtn", R.id.btn_signing)
                .bindChildWithTag("matchRankLabel", R.id.label_match_rank);
    }

    public static void configureView(ChildBinder binder, Func0<StockMatchHomeVM> itemGetter, int flags) {
        boolean useHomeStyle = hasFlag(flags, FLAG_USE_HOME_STYLE);

        Context ctx = binder.itemView().getContext();
        StockMatchHomeVM vm = itemGetter.call();
        v_setImageResource(binder.getChildWithTag("matchStateImage"), vm.resId);

        View signingButton = binder.getChildWithTag("signingBtn");
        TextView matchRankLabel = binder.getChildWithTag("matchRankLabel");
        if (useHomeStyle) {
            v_setText(binder.getChildWithTag("matchNameLabel"), vm.matchTitleWithoutReward);
            v_setText(matchRankLabel, vm.matchReward);
            v_setGone(signingButton);
            v_setVisible(matchRankLabel);
            matchRankLabel.setLineSpacing(dp2px(8), 1f);

        } else {
            v_setText(binder.getChildWithTag("matchNameLabel"), vm.matchTitle);
            v_setText(matchRankLabel, vm.hasSignUp ? vm.rankPosition : "");
            boolean notSignUpAndSignable = (!vm.hasSignUp && vm.matchState == STATE_SIGNUP || vm.matchState == STATE_ING);
            v_setClick(signingButton, v -> {
                if (notSignUpAndSignable) {
                    showActivity(ctx, an_StockMatchDetailPage(vm.matchId));
                }
            });
            matchRankLabel.setLineSpacing(dp2px(4), 1f);
            v_setVisibility(signingButton, notSignUpAndSignable ? View.VISIBLE : View.GONE);
            v_setVisibility(matchRankLabel, vm.hasSignUp ? View.VISIBLE : View.GONE);
        }
    }

    public static class StockMatchHomeVM {

        public GMFMatch raw;
        public String tarlink;
        public String pageUrl;
        public String matchId;
        public String matchName;
        public int matchState;
        public CharSequence matchTitle;
        public CharSequence matchTitleWithoutReward;
        public boolean hasSignUp;
        public int resId;
        public String coverURL;
        public CharSequence rankPosition;
        public GMFRankUser rankUser;
        public CharSequence startTime;
        public CharSequence stopTime;
        public CharSequence remainOpenTime;
        public CharSequence matchReward;

        public StockMatchHomeVM(GMFMatch raw) {
            this.raw = raw;
            this.matchId = raw.mid;
            this.matchName = raw.title;
            this.matchState = raw.state;
            this.hasSignUp = raw.bSignUp;
            this.coverURL = raw.imgUrl;

            this.startTime = safeGet(() -> formatSecond(raw.startTime, "MM月dd日"), "--");
            this.stopTime = safeGet(() -> formatSecond(raw.stopTime, "MM月dd日"), "--");
            this.remainOpenTime = concatNoBreak(safeGet(() -> formateRemainingDays((raw.startTime - SecondUtil.currentSecond()) * 1f / (24 * 60 * 60)), ""), "后开始");

            this.matchTitle = concat(setColor(raw.title, raw.state != STATE_OVER ? TEXT_BLACK_COLOR : TEXT_GREY_COLOR),
                    concatNoBreak(setFontSize(setColor(startTime + " ~ " + stopTime + " · ", TEXT_GREY_COLOR), sp2px(10)),
                            setFontSize(setColor(raw.maxAward, raw.state != STATE_OVER ? TEXT_RED_COLOR : TEXT_GREY_COLOR), sp2px(10))));
            this.matchTitleWithoutReward = concat(setColor(raw.title, raw.state != STATE_OVER ? TEXT_BLACK_COLOR : TEXT_GREY_COLOR),
                    setFontSize(setColor(startTime + " ~ " + stopTime + " · ", TEXT_GREY_COLOR), sp2px(10)));
            this.tarlink = raw.tarLink;
            this.pageUrl = raw.pageUrl;
            if (raw.result != null) {
                this.rankPosition = concat(setColor("" + (raw.state == STATE_SIGNUP ? remainOpenTime : raw.result.position), raw.state != STATE_OVER ? (raw.state == STATE_SIGNUP ? TEXT_BLACK_COLOR : TEXT_RED_COLOR) : TEXT_GREY_COLOR),
                        setFontSize(setColor("我的排名", TEXT_GREY_COLOR), sp2px(10)));
                this.rankUser = raw.result;
            }
            if (raw.state == STATE_SIGNUP) {
                this.resId = R.mipmap.ic_sign_up;
            } else if (raw.state == STATE_ING) {
                this.resId = R.mipmap.ic_sign_in;
            } else {
                this.resId = R.mipmap.ic_over;
            }

            this.matchReward = safeGet(() -> setFontSize(concat(setColor(raw.maxAward, TEXT_RED_COLOR), setColor(raw.maxAwardDesc, TEXT_GREY_COLOR)), sp2px(12)), "");
        }
    }
}
