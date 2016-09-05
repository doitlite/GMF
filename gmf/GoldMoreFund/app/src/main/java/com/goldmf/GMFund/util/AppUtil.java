package com.goldmf.GMFund.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.Fragment;

import java.io.File;

/**
 * Created by yale on 15/9/25.
 */
public class AppUtil {
    private AppUtil() {
    }

    public static void installApk(Fragment fragment, File file) {
        installApk(fragment.getActivity(), file);
    }

    public static void installApk(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean openPackage(Fragment fragment, String packageName) {
        return openPackage(fragment.getActivity(), packageName);
    }

    public static boolean openPackage(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInstalled(Fragment fragment, String packageName) {
        return isInstalled(fragment.getActivity(), packageName);
    }

    public static boolean isInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static int getVersionCode(Context context) {
        return getVersionCode(context, context.getPackageName());
    }

    public static int getVersionCode(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return -1;
    }

    public static String getVersionName(Context context) {
        return getVersionName(context, context.getPackageName());
    }

    public static String getVersionName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return "unknown";
    }
}
