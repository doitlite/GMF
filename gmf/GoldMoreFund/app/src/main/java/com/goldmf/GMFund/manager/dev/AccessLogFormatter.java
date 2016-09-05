package com.goldmf.GMFund.manager.dev;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.controller.FragmentStackActivity;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.util.UIControllerNameMapper;

import static com.goldmf.GMFund.extension.ObjectExtension.*;

/**
 * Created by yale on 15/10/29.
 */
public class AccessLogFormatter {
    private AccessLogFormatter() {
    }

    public static String logForStartLoadWebViewURL(String requestURL) {
        if (!TextUtils.isEmpty(requestURL)) {
            return "startLoadWebViewURL:" + requestURL;
        } else {
            return "startLoadWebViewURL:Unknown";
        }
    }

    public static String logForFinishLoadWebViewURL(String requestURL, boolean isSuccess) {
        return "finishLoadWebViewURL:" + requestURL + " result:" + isSuccess;
    }

    public static String logForClickEvent(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {

            if (MyApplication.hasTopDialog()) {
                Dialog dialog = opt(MyApplication.getTopDialogOrNil()).orNull();
                assert dialog != null;
                CharSequence title = "";
                CharSequence message = "";
                CharSequence place = UIControllerNameMapper.getName(dialog.getClass(), "UnknownDialog");

                if (dialog instanceof GMFDialog) {
                    title = ((GMFDialog) dialog).getTitle();
                    message = ((GMFDialog) dialog).getMessage();
                }

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(message)) {
                    return "clickButtonWithText:" + text + " ofDialog:" + place + " withTitle:" + title + " message:" + message;
                } else if (!TextUtils.isEmpty(title)) {
                    return "clickButtonWithText:" + text + " ofDialog:" + place + " withTitle:" + title;
                } else if (!TextUtils.isEmpty(message)) {
                    return "clickButtonWithText:" + text + " ofDialog:" + place + " withMessage:" + message;
                } else {
                    return "clickButtonWithText:" + text + " ofDialog:" + place;
                }
            }

            if (MyApplication.hasTopActivity()) {
                Activity act = MyApplication.getTopActivityOrNil().get();
                assert act != null;
                String actName = act.getClass().getSimpleName();
                String fragName = "";
                if (act instanceof FragmentStackActivity) {
                    Fragment topFragmentOrNil = ((FragmentStackActivity) act).peekTopFragmentOrNil();
                    if (topFragmentOrNil != null) {
                        fragName = UIControllerNameMapper.getName(topFragmentOrNil.getClass(), "UnknownFragment");
                    }
                }
                if (TextUtils.isEmpty(fragName)) {
                    return "clickButtonWithText:" + text + " ofPlace:" + actName;
                } else {
                    return "clickButtonWithText:" + text + " ofPlace:" + actName + "::" + fragName;
                }
            } else {
                return "clickButtonWithText:" + text;
            }
        }

        return "clickButtonWithText:Unknown ofPlace:Unknown";
    }

    public static String logForOpenUIController(Activity activity, Fragment fragmentOrNil) {
        String actName = activity.getClass().getSimpleName();
        String fragName = fragmentOrNil == null ? "" : UIControllerNameMapper.getName(fragmentOrNil.getClass(), "UnknownFragment");
        if (TextUtils.isEmpty(fragName)) {
            return "openUIController:" + actName;
        } else {
            return "openUIController:" + actName + "::" + fragName;
        }
    }

    public static String logForCloseUIController(Activity activity, Fragment fragmentOrNil) {
        String actName = activity.getClass().getSimpleName();
        String fragName = fragmentOrNil == null ? "" : UIControllerNameMapper.getName(fragmentOrNil.getClass(), "UnknownFragment");
        if (TextUtils.isEmpty(fragName)) {
            return "closeUIController:" + actName;
        } else {
            return "closeUIController:" + actName + "::" + fragName;
        }
    }

    public static String logForPullToRefreshEvent() {
        if (MyApplication.hasTopActivity()) {
            Activity act = MyApplication.getTopActivityOrNil().get();
            String actName = act.getClass().getSimpleName();
            String fragName = "";
            if (act instanceof FragmentStackActivity) {
                Fragment topFragmentOrNil = ((FragmentStackActivity) act).peekTopFragmentOrNil();
                if (topFragmentOrNil != null) {
                    fragName = UIControllerNameMapper.getName(topFragmentOrNil.getClass(), "UnknownFragment");
                }
            }
            if (TextUtils.isEmpty(fragName)) {
                return "pullToRefreshOfPlace:" + actName;
            } else {
                return "pullToRefreshOfPlace:" + actName + " ::" + fragName;
            }
        } else {
            return "pullToRefreshOfPlace:Unknown";
        }
    }

    public static String logForForceReloadEvent() {
        if (MyApplication.hasTopActivity()) {
            Activity act = MyApplication.getTopActivityOrNil().get();
            String actName = act.getClass().getSimpleName();
            String fragName = "";
            if (act instanceof FragmentStackActivity) {
                Fragment topFragmentOrNil = ((FragmentStackActivity) act).peekTopFragmentOrNil();
                if (topFragmentOrNil != null) {
                    fragName = UIControllerNameMapper.getName(topFragmentOrNil.getClass(), "UnknownFragment");
                }
            }
            if (TextUtils.isEmpty(fragName)) {
                return "forceReloadOfPlace:" + actName;
            } else {
                return "forceReloadOfPlace:" + actName + " ::" + fragName;
            }
        } else {
            return "forceToReloadOfPlace:Unknown";
        }
    }

}
