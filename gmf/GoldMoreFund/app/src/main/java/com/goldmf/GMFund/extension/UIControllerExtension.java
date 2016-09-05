package com.goldmf.GMFund.extension;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.BaseActivity;
import com.goldmf.GMFund.controller.BaseFragment;
import com.goldmf.GMFund.controller.FragmentStackActivity;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 15/9/7.
 */
public class UIControllerExtension {

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static Rect getScreenSize(Fragment fragment) {
        return getScreenSize(fragment.getActivity());
    }

    public static Rect getScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        return new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);
    }

    public static void runOnMain(Runnable runnable) {
        sHandler.post(runnable);
    }

    public static void showToast(Fragment fragment, CharSequence msg) {
        showToast(fragment.getActivity(), msg);
    }

    public static void showToast(Context context, CharSequence msg) {
        if (context == null || msg == null)
            return;


        if (context instanceof MyApplication) {
            MyApplication.SHARE_INSTANCE.mHandler.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showAlertDialogOrToastWithActivity(WeakReference<BaseActivity> actRef, CharSequence msg) {
        if (actRef.get() != null) {
            MyApplication.SHARE_INSTANCE.mHandler.post(() -> createAlertDialog(actRef.get(), msg).show());
        } else {
            showToast(MyApplication.SHARE_INSTANCE, msg);
        }
    }

    public static void showAlertDialogOrToastWithFragment(WeakReference<BaseFragment> fragRef, CharSequence msg) {
        if (fragRef.get() != null && fragRef.get().getActivity() != null) {
            MyApplication.SHARE_INSTANCE.mHandler.post(() -> createAlertDialog(fragRef.get(), msg).show());
        } else {
            showToast(MyApplication.SHARE_INSTANCE, msg);
        }
    }

    public static void showErrorDialogOrToastWithActivity(WeakReference<BaseActivity> actRef, CharSequence msg) {
        if (actRef.get() != null) {
            MyApplication.SHARE_INSTANCE.mHandler.post(() -> createErrorDialog(actRef.get(), msg).show());
        } else {
            showToast(MyApplication.SHARE_INSTANCE, msg);
        }
    }

    public static void showErrorDialogOrToastWithFragment(WeakReference<BaseFragment> fragRef, CharSequence msg) {
        if (fragRef.get() != null && fragRef.get().getActivity() != null) {
            MyApplication.SHARE_INSTANCE.mHandler.post(() -> createErrorDialog(fragRef.get(), msg).show());
        } else {
            showToast(MyApplication.SHARE_INSTANCE, msg);
        }
    }

    public static Toolbar findToolbar(Activity activity) {
        return findToolbar(activity.getWindow().getDecorView());
    }

    public static Toolbar findToolbar(Fragment fragment) {
        return findToolbar(fragment.getView());
    }

    public static Toolbar findToolbar(View view) {
        return (Toolbar) view.findViewById(R.id.toolbar);
    }

    public static int getStatusBarHeight(View view) {
        return getStatusBarHeight(view.getContext());
    }

    public static int getStatusBarHeight(Fragment fragment) {
        return getStatusBarHeight(fragment.getActivity());
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return dp2px(25);
    }

    public static void setStatusBarBackgroundColor(Activity activity, int color) {
        setStatusBarBackgroundColor(ViewGroup.class.cast(activity.findViewById(android.R.id.content)).getChildAt(0), color, true);
    }

    public static void setStatusBarBackgroundColor(Fragment fragment, int color) {
        setStatusBarBackgroundColor(fragment.getView(), color, false);
    }

    private static void setStatusBarBackgroundColor(View root, int color, boolean fromActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (root != null && root instanceof LinearLayout) {
                Activity context = (Activity) root.getContext();
                Window window = context.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(color);
                    int systemUIVisibility = window.getDecorView().getSystemUiVisibility();
                    boolean isLightStatusBar = color == SignalColorHolder.YELLOW_COLOR;
                    if (isLightStatusBar) {
                        systemUIVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    } else {
                        systemUIVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }
                    window.getDecorView().setSystemUiVisibility(systemUIVisibility);
                }


                if (!fromActivity || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    FrameLayout container = null;
                    if (((LinearLayout) root).getChildCount() > 0 && ((LinearLayout) root).getChildAt(0).getId() == R.id.gmf_status_bar_layer) {
                        container = (FrameLayout) ((LinearLayout) root).getChildAt(0);
                    }

                    final int statusBarHeight = getStatusBarHeight(root);

                    if (container != null) {
                        container.setBackgroundColor(color);
                    } else {
                        container = new FrameLayout(context);
                        container.setId(R.id.gmf_status_bar_layer);
                        container.setBackgroundColor(color);

                        {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, statusBarHeight);
                            ((LinearLayout) root).addView(container, 0, params);
                        }

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            FrameLayout darkLayerVIew = new FrameLayout(context);
                            darkLayerVIew.setBackgroundResource(R.color.gmf_dark_layer);
                            {
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, statusBarHeight);
                                container.addView(darkLayerVIew, params);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void setupBackButton(Fragment fragment, Toolbar toolbar) {
        if (fragment.getActivity() instanceof FragmentStackActivity) {
            setupBackButton((FragmentStackActivity) fragment.getActivity(), toolbar, 0);
        } else {
            setupBackButton(fragment.getActivity(), toolbar, 0);
        }
    }

    public static void setupBackBlackArrow(Fragment fragment, Toolbar toolbar) {
        if (fragment.getActivity() instanceof FragmentStackActivity) {
            setupBackBlackArrow((FragmentStackActivity) fragment.getActivity(), toolbar, 0);
        } else {
            setupBackBlackArrow(fragment.getActivity(), toolbar, 0);
        }
    }

    public static void setupBackButton(Fragment fragment, Toolbar toolbar, int navigationIcon) {
        if (fragment.getActivity() instanceof FragmentStackActivity) {
            setupBackButton((FragmentStackActivity) fragment.getActivity(), toolbar, navigationIcon);
        } else {
            setupBackButton(fragment.getActivity(), toolbar, navigationIcon);
        }
    }

    public static void setupBackButton(Activity activity, Toolbar toolbar) {
        setupBackButton(activity, toolbar, 0);
    }

    public static void setupBackButton(Activity activity, Toolbar toolbar, int navigationIcon) {
        if (navigationIcon == 0) {
            navigationIcon = R.drawable.ic_arrow_left_light;
        }
        toolbar.setNavigationIcon(navigationIcon);
        toolbar.setNavigationOnClickListener(v -> {
            activity.finish();
            hideKeyboardFromWindow(activity);
        });
    }

    public static void setupBackBlackArrow(Activity activity, Toolbar toolbar,
                                           int navigationIcon) {
        if (navigationIcon == 0) {
            navigationIcon = R.drawable.ic_arrow_left_dark;
        }
        toolbar.setNavigationIcon(navigationIcon);
        toolbar.setNavigationOnClickListener(v -> {
            activity.finish();
            hideKeyboardFromWindow(activity);
        });
    }

    public static void setupBackButton(FragmentStackActivity activity, Toolbar toolbar) {
        setupBackButton(activity, toolbar, 0);
    }

    public static void setupBackBlackArrow(FragmentStackActivity activity, Toolbar toolbar) {
        setupBackBlackArrow(activity, toolbar, 0);
    }

    public static void setupBackButton(FragmentStackActivity activity, Toolbar toolbar,
                                       int navigationIcon) {
        if (navigationIcon == 0) {
            navigationIcon = R.drawable.ic_arrow_left_light;
        }
        toolbar.setNavigationIcon(navigationIcon);
        toolbar.setNavigationOnClickListener(v -> {
            activity.goBack();
            hideKeyboardFromWindow(activity);
        });
    }

    public static void setupBackBlackArrow(FragmentStackActivity activity, Toolbar toolbar,
                                           int navigationIcon) {
        if (navigationIcon == 0) {
            navigationIcon = R.drawable.ic_arrow_left_dark;
        }
        toolbar.setNavigationIcon(navigationIcon);
        toolbar.setNavigationOnClickListener(v -> {
            activity.goBack();
            hideKeyboardFromWindow(activity);
        });
    }

    public static void setupBackButton(Fragment fragment, View backButton) {
        if (fragment.getActivity() instanceof FragmentStackActivity) {
            setupBackButton((FragmentStackActivity) fragment.getActivity(), backButton);
        } else {
            setupBackButton(fragment.getActivity(), backButton);
        }
    }

    public static void setupBackButton(Activity activity, View backButton) {
        v_setClick(backButton, v -> {
            performGoBack(activity);
        });
    }

    public static void setupBackButton(FragmentStackActivity activity, View backButton) {
        v_setClick(backButton, v -> {
            performGoBack(activity);
        });
    }

    public static void performGoBack(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            if (activity instanceof FragmentStackActivity) {
                performGoBack((FragmentStackActivity) activity);
            } else {
                performGoBack(activity);
            }
        }
    }


    public static void performGoBack(Activity activity) {
        activity.finish();
        hideKeyboardFromWindow(activity);
    }

    public static void performGoBack(FragmentStackActivity activity) {
        activity.goBack();
        hideKeyboardFromWindow(activity);
    }

    public static void hideKeyboardFromWindow(Context context) {
        if (context instanceof Activity) {
            hideKeyboardFromWindow((Activity) context);
        }
    }

    public static void hideKeyboardFromWindow(Fragment fragment) {
        hideKeyboardFromWindow(fragment.getActivity());
    }

    public static void hideKeyboardFromWindow(View view) {
        hideKeyboardFromWindow(view.getContext());
    }

    public static void hideKeyboardFromWindow(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
            InputMethodManager im = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showKeyboardFromWindow(Context context) {
        if (context != null && context instanceof Activity) {
            showKeyboardFromWindow((Activity) context);
        }
    }

    public static void showKeyboardFromWindow(Fragment fragment) {
        if (fragment != null)
            showKeyboardFromWindow(fragment.getActivity());
    }

    public static void showKeyboardFromWindow(View view) {
        if (view != null)
            showKeyboardFromWindow(view.getContext());
    }

    public static void showKeyboardFromWindow(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager im = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.showSoftInput(activity.getCurrentFocus(), 0);
        }
    }

    public static GMFDialog createErrorDialog(BaseFragment fragment, CharSequence message) {
        return createErrorDialog(fragment, null, message, null);
    }

    public static GMFDialog createErrorDialog(BaseFragment fragment, CharSequence
            titleOrNil, CharSequence message) {
        return createErrorDialog(fragment, titleOrNil, message, null);
    }


    public static GMFDialog createErrorDialog(BaseFragment fragment, CharSequence
            titleOrNil, CharSequence message, CharSequence buttonTextOrNil) {
        if (fragment.getActivity() instanceof FragmentStackActivity) {
            return createErrorDialog((FragmentStackActivity) fragment.getActivity(), titleOrNil, message, buttonTextOrNil);
        } else {
            return createErrorDialog((BaseActivity) fragment.getActivity(), titleOrNil, message, buttonTextOrNil);
        }
    }

    public static GMFDialog createErrorDialog(final BaseActivity activity, CharSequence
            message) {
        return createErrorDialog(activity, null, message, null);
    }

    public static GMFDialog createErrorDialog(final BaseActivity activity, CharSequence
            titleOrNil, CharSequence message) {
        return createErrorDialog(activity, titleOrNil, message, null);
    }

    public static GMFDialog createErrorDialog(final BaseActivity activity, CharSequence
            titleOrNil, CharSequence message, CharSequence buttonTextOrNil) {
        return new GMFDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(titleOrNil)
                .setMessage(message)
                .setPositiveButton(TextUtils.isEmpty(buttonTextOrNil) ? "知道了" : buttonTextOrNil, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setOnDismissListener(d -> activity.finish())
                .create();
    }

    public static GMFDialog createErrorDialog(FragmentStackActivity activity, CharSequence
            message) {
        return createErrorDialog(activity, null, message, null);
    }

    public static GMFDialog createErrorDialog(FragmentStackActivity activity, CharSequence
            titleOrNil, CharSequence message) {
        return createErrorDialog(activity, titleOrNil, message, null);
    }

    public static GMFDialog createErrorDialog(FragmentStackActivity activity, CharSequence
            titleOrNil, CharSequence message, CharSequence buttonTextOrNil) {
        return new GMFDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(TextUtils.isEmpty(titleOrNil) ? "提示" : titleOrNil)
                .setMessage(message)
                .setPositiveButton(TextUtils.isEmpty(buttonTextOrNil) ? "知道了" : buttonTextOrNil, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setOnDismissListener(d -> activity.goBack())
                .create();
    }


    public static GMFDialog createAlertDialog(Fragment fragment, CharSequence message) {
        return createAlertDialog(fragment.getActivity(), null, message, null);
    }

    public static GMFDialog createAlertDialog(Fragment
                                                      fragment, List<Pair<Integer, CharSequence>> list, OnContinueRegisterListener listener) {
        return createAlertDialog(fragment.getActivity(), null, list, null, listener);
    }

    public static GMFDialog createAlertDialog(Fragment fragment, CharSequence
            titleOrNil, CharSequence message) {
        return createAlertDialog(fragment.getActivity(), titleOrNil, message, null);
    }


    public static GMFDialog createAlertDialog(Fragment fragment, CharSequence
            titleOrNil, CharSequence message, CharSequence buttonTextOrNil) {
        return createAlertDialog(fragment.getActivity(), titleOrNil, message, buttonTextOrNil);
    }


    public static GMFDialog createAlertDialog(Context context, CharSequence message) {
        return createAlertDialog(context, null, message, null);
    }

    public static GMFDialog createAlertDialog(Context context, CharSequence
            titleOrNil, CharSequence message) {
        return createAlertDialog(context, titleOrNil, message, null);
    }

    public static GMFDialog createAlertDialog(Context context, CharSequence
            titleOrNil, CharSequence message, CharSequence buttonTextOrNil) {
        return new GMFDialog.Builder(context)
                .setTitle(TextUtils.isEmpty(titleOrNil) ? "提示" : titleOrNil)
                .setMessage(message)
                .setPositiveButton(TextUtils.isEmpty(buttonTextOrNil) ? "知道了" : buttonTextOrNil, (dialog, which) -> dialog.dismiss())
                .create();
    }

    /**
     * 邀请人号码不存在或未注册，点击继续注册
     */
    public static GMFDialog createAlertDialog(Context context, CharSequence
            titleOrNil, List<Pair<Integer, CharSequence>> list, CharSequence
                                                      buttonTextOrNil, OnContinueRegisterListener listener) {
        int errCode = -1;
        CharSequence message = "未知错误";
        boolean isContinue;
        GMFDialog gmfDialog;

        for (Pair<Integer, CharSequence> pair : list) {
            errCode = pair.first;
            message = pair.second;
        }

        if (errCode == MResultExtension.GMF_CODE_NO_REGISTER || errCode == MResultExtension.GMF_CODE_ERROR_PHONE) {
            gmfDialog = new GMFDialog.Builder(context)
                    .setTitle(TextUtils.isEmpty(titleOrNil) ? "提示" : titleOrNil)
                    .setMessage(message)
                    .setPositiveButton(TextUtils.isEmpty(buttonTextOrNil) ? "继续注册" : buttonTextOrNil,
                            (dialog, which) -> {
                                dialog.dismiss();
                                if (listener != null) {
                                    listener.onContinueRegister();
                                }
                            })
                    .setNegativeButton("取消",
                            (dialog, which) -> {
                                dialog.dismiss();
                                if (listener != null) {
                                    listener.onCancel();
                                }
                            })
                    .create();
        } else {
            gmfDialog = new GMFDialog.Builder(context)
                    .setTitle(TextUtils.isEmpty(titleOrNil) ? "提示" : titleOrNil)
                    .setMessage(message)
                    .setPositiveButton(TextUtils.isEmpty(buttonTextOrNil) ? "知道了" : buttonTextOrNil, (dialog, which) -> {
                        dialog.dismiss();
                        if (listener != null) {
                            listener.onCancel();
                        }
                    })
                    .create();
        }
        return gmfDialog;
    }

    public interface OnContinueRegisterListener {
        void onContinueRegister();

        void onCancel();
    }

}
