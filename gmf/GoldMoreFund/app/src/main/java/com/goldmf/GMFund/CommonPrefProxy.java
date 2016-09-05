package com.goldmf.GMFund;

import android.content.Context;
import android.content.SharedPreferences;

import com.goldmf.GMFund.util.AppUtil;

/**
 * Created by yale on 15/9/25.
 */
public class CommonPrefProxy {

    /**
     * 获取最后一次运行的版本号
     */
    private static final String KEY_LAST_LAUNCH_VERSION_CODE_INT = "last_launch_version_code";

    private static SharedPreferences sPref;

    static {
        sPref = MyApplication.SHARE_INSTANCE.getSharedPreferences("common", Context.MODE_APPEND);
    }

    private CommonPrefProxy() {
    }


    public static void updateLastLaunchVersionCode() {
        Context context = MyApplication.SHARE_INSTANCE;
        try {
            sPref.edit().putInt(KEY_LAST_LAUNCH_VERSION_CODE_INT, AppUtil.getVersionCode(context)).apply();
        } catch (Exception ignored) {
        }
    }

    public static int getLastLaunchVersionVode() {
        return sPref.getInt(KEY_LAST_LAUNCH_VERSION_CODE_INT, -1);
    }
}
