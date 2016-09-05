package com.goldmf.GMFund.util;

import android.content.Context;

import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;

import java.util.LinkedHashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by yale on 16/1/6.
 */
public class JPushUtil {

    public static void init(Context context) {
        JPushInterface.setDebugMode(MyConfig.IS_DEBUG_MODE);
        JPushInterface.init(context);
        updateTagsAndAlias(context);
    }

    public static void updateTagsAndAlias(Context context) {
        JPushInterface.setAlias(context, MineManager.getInstance().isLoginOK() ? MineManager.getInstance().getmMe().phone : "", null);
        JPushInterface.setTags(context, generateUserTags(context), null);
    }

    private static Set<String> generateUserTags(Context context) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(AppUtil.getVersionName(context).replace("build/intermediates/exploded-aar/com.github.dmytrodanylyk.shadow-layout/library/1.0.3/res", "_"));
        Mine mine = MineManager.getInstance().getmMe();
        if (mine == null || !mine.isLoginOk()) {
            set.add("type1");
        } else {
            if (!mine.setAuthenticate) {
                set.add("type2");
            } else if (FortuneManager.getInstance().cnAccount != null && FortuneManager.getInstance().cnAccount.investMoney <= 0) {
                set.add("type3");
            }
        }
        return set;
    }
}
