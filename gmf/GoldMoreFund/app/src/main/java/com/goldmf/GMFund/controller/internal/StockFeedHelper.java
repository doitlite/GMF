package com.goldmf.GMFund.controller.internal;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.circle.CircleHelper;
import com.goldmf.GMFund.extension.FlagExtension;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.SimulationAccount;
import com.goldmf.GMFund.model.StockInfo;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BuySellStockIndicator;
import com.goldmf.GMFund.widget.UserAvatarView;

import rx.functions.Func0;
import rx.functions.Func1;
import yale.extension.system.SimpleRecyclerViewAdapter;
import yale.extension.system.SimpleViewHolder;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_QuotationDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockAnalysePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatTimeByNow;

/**
 * Created by yalez on 2016/7/14.
 */
public class StockFeedHelper {

    public static final int FLAG_SHOW_USER_TYPE_DECORATION = 1;
    public static final int FLAG_AVATAR_CLICKABLE = 1 << 1;

    private StockFeedHelper() {
    }

    public static SimpleViewHolder<StockFeedVM> createViewHolder(SimpleViewHolder.Builder<StockFeedVM> builder, int flags) {
        ChildBinder binder = ChildBinders.createWithBuilder(builder);
        bindChildViews(binder);
        builder.configureView((item, pos) -> {
            configureView(binder, item, flags);
        });
        return builder.create();
    }

    public static void configureView(ChildBinder binder, StockFeedVM item, int flags) {
        boolean showUserTypeDecoration = hasFlag(flags, FLAG_SHOW_USER_TYPE_DECORATION);

        UserAvatarView avatarView = binder.getChildWithTag("avatar");
        avatarView.updateView(item.avatarURL, showUserTypeDecoration ? item.userType : User.User_Type.Custom);
        v_setText(binder.getChildWithTag("userNameAndIncome"), item.userNameAndIncome);
        v_setText(binder.getChildWithTag("time"), item.time);
        v_setText(binder.getChildWithTag("stockNameAndCode"), item.stockNameAndCode);
        v_setText(binder.getChildWithTag("positionAndPrice"), item.positionAndPrice);
        v_setText(binder.getChildWithTag("capitalChange"), item.capitalChange);
        binder.<BuySellStockIndicator>getChildWithTag("indicator").setState(item.feedType == StockController.FEED_TYPE_BUY ? BuySellStockIndicator.STATE_BUY : BuySellStockIndicator.STATE_SELL);
        binder.getChildWithTag("stockAnalyseSection").setBackgroundColor(item.feedType == StockController.FEED_TYPE_BUY ? Color.parseColor("#EAF1FD") : Color.parseColor("#FCF4E1"));

        boolean avatarClickable = FlagExtension.hasFlag(flags, FLAG_AVATAR_CLICKABLE);
        if (avatarClickable) {
            v_setClick(avatarView, v -> {
                UmengUtil.stat_click_event(UmengUtil.eEVENTIDTopHomePage);
                showActivity(v.getContext(), an_UserDetailPage(item.user));
            });
        }
    }

    public static void bindChildViews(ChildBinder binder) {
        binder
                .bindChildWithTag("avatar", R.id.img_avatar)
                .bindChildWithTag("userNameAndIncome", R.id.label_user_name_and_income)
                .bindChildWithTag("stockNameAndCode", R.id.label_stock_name_and_code)
                .bindChildWithTag("time", R.id.label_time)
                .bindChildWithTag("indicator", R.id.indicator)
                .bindChildWithTag("positionAndPrice", R.id.label_position_and_price)
                .bindChildWithTag("capitalChange", R.id.label_capital_change)
                .bindChildWithTag("stockAnalyseSection", R.id.section_stock_analyse);
    }

    public static void afterViewHolderCreated(SimpleRecyclerViewAdapter<StockFeedVM> adapter, RecyclerView.ViewHolder holder) {
        afterViewHolderCreated(CircleHelper.createItemStore(adapter), holder);
    }

    public static void afterViewHolderCreated(CircleHelper.ItemStore<StockFeedVM> itemStore, RecyclerView.ViewHolder holder) {
        Context ctx = holder.itemView.getContext();
        View itemView = holder.itemView;

        v_setClick(itemView, R.id.section_stock, v -> {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDTopStockDetail);
            StockFeedHelper.StockFeedVM item = itemStore.getItem(holder.getAdapterPosition());
            showActivity(ctx, an_QuotationDetailPage(safeGet(() -> item.stockID, null)));
        });
        v_setClick(itemView, R.id.section_analyse, v -> {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDTopStockProfitLoss);
            StockFeedHelper.StockFeedVM item = itemStore.getItem(holder.getAdapterPosition());
            showActivity(ctx, an_StockAnalysePage(item.userID, item.stockID, item.range));
        });
    }

    public static class StockFeedVM {
        public User user;
        public String userID;
        public String stockID;
        public String range;
        public int userType;
        public String avatarURL;
        public String userName;
        public CharSequence userNameAndIncome;
        public CharSequence time;
        public CharSequence stockNameAndCode;
        public CharSequence positionAndPrice;
        public CharSequence capitalChange;
        public int feedType;

        public long rawAddTime;
        public double rawChangeRatio;

        private static Func1<Integer, CharSequence> getChangeHint = in -> in == StockController.FEED_TYPE_BUY ? "调仓后涨跌" : "本次收益";


        public StockFeedVM(FeedOrder raw) {
            Order order = safeGet(() -> raw.orderInfo, null);
            StockInfo.StockSimple stock = safeGet(() -> raw.orderInfo.stock, null);
            user = safeGet(() -> raw.user, null);
            SimulationAccount account = safeGet(() -> raw.stockAccount, null);

            this.userID = safeGet(() -> String.valueOf(user.index), "");
            this.stockID = safeGet(() -> raw.orderInfo.stock.index, "");
            this.range = safeGet(() -> order.range, "");
            this.userType = safeGet(() -> raw.user.type, User.User_Type.Custom);
            this.avatarURL = user.getPhotoUrl();
            this.userName = safeGet(() -> user.getName(), "");
            this.userNameAndIncome = generateTitle(() -> concatNoBreak(user.getName(), " ", setColor(formatRatio(account.totalIncomeRatio, true, 2), getIncomeTextColor(account.totalIncome))));
            this.time = safeGet(() -> formatTimeByNow(raw.createTime), CommonDefine.PlaceHolder.NULL_VALUE);
            this.stockNameAndCode = generateTitleAndSubtitle(() -> stock.name, () -> stock.code, TEXT_GREY_COLOR);
            this.feedType = safeGet(() -> raw.feedType, 0);

            this.positionAndPrice = generateTitleAndSubtitle(() -> setColor(concatNoBreak(formatRatio(order.beforePosition, false, 2), " ➔ ", formatRatio(order.afterPosition, false, 2)), TEXT_BLACK_COLOR),
                    () -> concatNoBreak("成交价 ", formatMoney(order.transactionPrice, false, 2)), TEXT_GREY_COLOR);
            this.capitalChange = generateTitleAndSubtitle(() -> setColor(formatRatio(order.incomeRatio, true, 2), getIncomeTextColor(order.incomeRatio)), () -> getChangeHint.call(raw.feedType), TEXT_GREY_COLOR);

            this.rawAddTime = raw.createTime;
            this.rawChangeRatio = safeGet(() -> order.incomeRatio, 0.0);
        }

        private CharSequence generateTitle(Func0<CharSequence> titleGetter) {
            return safeGet(() -> concatNoBreak(setFontSize(titleGetter.call(), sp2px(14))))
                    .def(concatNoBreak(setFontSize(CommonDefine.PlaceHolder.NULL_VALUE, sp2px(14)))).get();
        }

        private CharSequence generateTitleAndSubtitle(Func0<CharSequence> titleGetter, Func0<CharSequence> subTitleGetter, int subTitleColor) {
            return safeGet(() -> concat(titleGetter.call(), setFontSize(setColor(subTitleGetter.call(), subTitleColor), sp2px(10))))
                    .def(concat(CommonDefine.PlaceHolder.NULL_VALUE, setFontSize(CommonDefine.PlaceHolder.NULL_VALUE, sp2px(10)))).get();
        }

        private CharSequence generateTitleAndSubtitle(Func0<CharSequence> titleGetter, Func0<CharSequence> subTitleGetter) {
            return safeGet(() -> concat(titleGetter.call(), setFontSize(subTitleGetter.call(), sp2px(10))))
                    .def(concat(CommonDefine.PlaceHolder.NULL_VALUE, setFontSize(CommonDefine.PlaceHolder.NULL_VALUE, sp2px(10)))).get();
        }
    }
}
