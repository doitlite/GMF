package com.goldmf.GMFund.util;

import android.text.TextUtils;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.manager.mine.MineManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.paperdb.Paper;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by yale on 16/1/15.
 */
public class PersistentObjectUtil {
    private static final String KEY_HAS_SHOW_HOME_PAGE_AWARD_GUIDE = "has_show_home_page_award_guide";
    private static final String KEY_HAS_SHOW_AWARD_PAGE_INTRODUCTION = "has_show_award_page_introduction";
    private static final String KEY_HAS_SHOW_FUND_PAGE_BONUS_GUIDE = "has_show_fund_page_bonus_guide";
    private static final String KEY_HAS_REQUEST_PERMISSION_BEFORE = "has_request_permission_before";
    private static final String KEY_HAS_BUY_OR_SELL_STOCK_BEFORE = "has_buy_or_sell_stock_before";      //第一次买入 卖出
    private static final String KEY_NEED_TO_SHOW_SET_AVATAR_ALERT = "need_to_show_set_avatar_alert";      //是否需要弹出设置头像提醒
    private static final String KEY_HAS_OPEN_SIMULATION_PERFORMANCE = "has_open_simulation_performance";

    private PersistentObjectUtil() {
    }

    public static void writeHasRequestPermissionsBefore(boolean value) {
        Paper.book().write(KEY_HAS_REQUEST_PERMISSION_BEFORE, value);
    }

    public static boolean readHasRequestPermissionsBefore() {
        return Paper.book().read(KEY_HAS_REQUEST_PERMISSION_BEFORE, false);
    }

    public static void writeHasShowFundPageBonusGuide(int fundId, boolean value) {
        getLoginUserId().apply(user_id -> Paper.book(user_id).write(KEY_HAS_SHOW_FUND_PAGE_BONUS_GUIDE + fundId, value));
    }

    public static boolean readHasShowFundPageBonusGuide(int fundId) {
        Optional<String> userIdRef = getLoginUserId();
        if (userIdRef.isPresent()) {
            return Paper.book(userIdRef.get()).read(KEY_HAS_SHOW_FUND_PAGE_BONUS_GUIDE + fundId, Boolean.FALSE);
        }
        return true;
    }

    public static void writeHasShowHomeAwardGuide(boolean value) {
        getLoginUserId().apply(user_id -> Paper.book(user_id).write(KEY_HAS_SHOW_HOME_PAGE_AWARD_GUIDE, value));
    }

    public static boolean readHasShowHomeAwardGuide() {
        Optional<String> userIdRef = getLoginUserId();
        if (userIdRef.isPresent()) {
            return Paper.book(userIdRef.get()).read(KEY_HAS_SHOW_HOME_PAGE_AWARD_GUIDE, Boolean.FALSE);
        }
        return true;
    }

    public static void writeHasBuyOrSellStockBefore(boolean value) {
        getLoginUserId().apply(user_id -> Paper.book(user_id).write(KEY_HAS_BUY_OR_SELL_STOCK_BEFORE + AppUtil.getVersionName(MyApplication.SHARE_INSTANCE), value));
    }

    public static boolean readHasBuyOrSellStockBefore() {
        Optional<String> userIdRef = getLoginUserId();
        if (userIdRef.isPresent()) {
            return Paper.book(userIdRef.get()).read(KEY_HAS_BUY_OR_SELL_STOCK_BEFORE + AppUtil.getVersionName(MyApplication.SHARE_INSTANCE), Boolean.FALSE);
        }
        return true;
    }

    public static void writeHasShowAwardPageIntroduction(boolean value) {
        getLoginUserId().apply(userId -> Paper.book(userId).write(KEY_HAS_SHOW_AWARD_PAGE_INTRODUCTION, value));
    }

    public static boolean readHasShowAwardPageIntroduction() {
        Optional<String> userIdRef = getLoginUserId();
        if (userIdRef.isPresent()) {
            return Paper.book(userIdRef.get()).read(KEY_HAS_SHOW_AWARD_PAGE_INTRODUCTION, Boolean.FALSE);
        }
        return true;
    }

    public static void writeIsNeedToShowSetAvatarAlert() {
        boolean isLogin = safeGet(() -> MineManager.getInstance().isLoginOK(), false);
        if (isLogin) {
            getLoginUserId().consume(userID -> {
                String key = KEY_NEED_TO_SHOW_SET_AVATAR_ALERT + "_" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                Paper.book(userID).write(key, Boolean.FALSE);
            });
        }
    }

    public static boolean readIsNeedToShowSetAvatarAlert() {
        String avatarURL = safeGet(() -> MineManager.getInstance().getmMe().getPhotoUrl(), "");
        boolean hasSetAvatar = !TextUtils.isEmpty(avatarURL) && !avatarURL.contains("/images/avatar/user_avatar_01.png");
        if (!hasSetAvatar) {
            String userID = String.valueOf(safeGet(() -> MineManager.getInstance().getmMe().index, 0));
            String key = KEY_NEED_TO_SHOW_SET_AVATAR_ALERT + "_" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            return Paper.book(userID).read(key, Boolean.TRUE);
        }

        return false;
    }

    private static Optional<String> getLoginUserId() {
        if (MineManager.getInstance().isLoginOK()) {
            return Optional.of(MineManager.getInstance().getmMe()).let(mine -> String.valueOf(mine.index));
        }
        return Optional.empty();
    }
}
