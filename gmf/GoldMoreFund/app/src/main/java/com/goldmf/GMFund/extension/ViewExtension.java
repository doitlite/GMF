package com.goldmf.GMFund.extension;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.SubscriptionManager;
import com.goldmf.GMFund.controller.internal.RegexPatternHolder;
import com.goldmf.GMFund.manager.dev.AccessLogFormatter;
import com.goldmf.GMFund.util.DimensionConverter;
import com.goldmf.GMFund.widget.ProgressButton;
import com.orhanobut.logger.Logger;

import rx.functions.Action0;
import rx.functions.Action1;

import static com.goldmf.GMFund.extension.ObjectExtension.*;
import static com.goldmf.GMFund.extension.SpannableStringExtension.*;
import static com.goldmf.GMFund.extension.StringExtension.*;
import static com.goldmf.GMFund.extension.StringExtension.formatBankCardNoTransformer;
import static com.goldmf.GMFund.extension.StringExtension.formatMoneyTransformer;
import static com.goldmf.GMFund.extension.StringExtension.map;
import static com.goldmf.GMFund.extension.StringExtension.normalBankCardNoTransformer;

/**
 * Created by yale on 15/8/11.
 */
public class ViewExtension {
    private ViewExtension() {
    }

    public static int dp2px(View view, float dp) {
        return (int) DimensionConverter.dp2px(view.getContext(), dp);
    }

    public static int dp2px(float dp) {
        return (int) DimensionConverter.dp2px(MyApplication.SHARE_INSTANCE, dp);
    }

    public static int px2dp(View view, float px) {
        return (int) DimensionConverter.px2dp(view.getContext(), px);
    }

    public static int px2dp(float px) {
        return (int) DimensionConverter.px2dp(MyApplication.SHARE_INSTANCE, px);
    }

    public static int sp2px(View view, float sp) {
        return (int) DimensionConverter.sp2px(view.getContext(), sp);
    }

    public static int sp2px(float sp) {
        return (int) DimensionConverter.sp2px(MyApplication.SHARE_INSTANCE, sp);
    }

    public static void v_updateLayoutParams(View parent, int childId, Action1<ViewGroup.LayoutParams> operation) {
        View view = parent.findViewById(childId);
        v_updateLayoutParams(view, operation);
    }
    public static void v_updateLayoutParams(View view, Action1<ViewGroup.LayoutParams> operation) {
        v_updateLayoutParams(view, ViewGroup.LayoutParams.class, operation);
    }

    public static <T extends ViewGroup.LayoutParams> void v_updateLayoutParams(View parent, int childId, Class<T> clazz, Action1<T> operation) {
        View view = parent.findViewById(childId);
        v_updateLayoutParams(view, clazz, operation);
    }public static <T extends ViewGroup.LayoutParams> void v_updateLayoutParams(Fragment fragment, int childId, Class<T> clazz, Action1<T> operation) {
        View view = fragment.getView().findViewById(childId);
        v_updateLayoutParams(view, clazz, operation);
    }
    public static <T extends ViewGroup.LayoutParams> void v_updateLayoutParams(View view, Class<T> clazz, Action1<T> operation) {
        if (view != null && view.getLayoutParams() != null) {
            operation.call((T) view.getLayoutParams());
        }
    }

    public static void v_expandTouchArea(View view, Rect padding) {
        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof View) {
            View castParent = (View) parent;
            castParent.post(() -> {
                Rect hitRect = new Rect();
                view.getHitRect(hitRect);
                hitRect.left -= padding.left;
                hitRect.top -= padding.top;
                hitRect.right += padding.right;
                hitRect.bottom += padding.bottom;
                castParent.setTouchDelegate(new TouchDelegate(hitRect, view));
            });
        }
    }

    public static void v_reviseTouchArea(View view) {
        v_reviseTouchArea(view, dp2px(40), dp2px(40));
    }

    public static void v_reviseTouchArea(View view, int minWidth, int minHeight) {
        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof View) {
            View castParent = (View) parent;
            castParent.post(() -> {
                Rect hitRect = new Rect();
                view.getHitRect(hitRect);
                if (hitRect.width() < minWidth) {
                    int outsetX = (minWidth - hitRect.width()) >> 1;
                    hitRect.left -= outsetX;
                    hitRect.right += outsetX;
                }
                if (hitRect.height() < minHeight) {
                    int outsetY = (minHeight - hitRect.height()) >> 1;
                    hitRect.top -= outsetY;
                    hitRect.bottom += outsetY;
                }
                castParent.setTouchDelegate(new TouchDelegate(hitRect, view));
            });
        }
    }

    public static void v_getGlobalHitRect(View view, Rect rect) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        view.getHitRect(rect);
        rect.offset(location[0] - rect.left, location[1] - rect.top);
    }

    public static void v_removeFromParent(View view) {
        if (view != null && view.getParent() != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    /**
     * must be call after TextView is ready!!!!
     * simple way is to call this function in View.post(Runnable runnable)
     */
    public static boolean v_isTruncate(TextView label) {
        Layout layout = label.getLayout();
        int lastLineIdx = label.getLineCount() - 1;
        if (lastLineIdx >= 0) {
            int ellipsisCount = layout.getEllipsisCount(lastLineIdx);
            if (ellipsisCount > 0) {
                return true;
            }
        }
        return false;
    }

    public static <T extends View> void v_setBehavior(T view, CoordinatorLayout.Behavior<T> behavior) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.setBehavior(behavior);
        view.setLayoutParams(params);
    }

    public static void v_preDraw(final View view, final boolean autoRelease, final Action1<View> callback) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (autoRelease)
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                callback.call(view);
                return true;
            }
        });
    }

    public static void v_globalLayout(final View view, final boolean autoRelease, final Action1<View> callback) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (autoRelease)
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                callback.call(view);
            }
        });
    }

    public static void v_getSizePreDraw(final View view, final boolean autoRelease, final Action1<Pair<Integer, Integer>> callback) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (view.getHeight() > 0 || view.getWidth() > 0) {
                    if (autoRelease)
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                    callback.call(new Pair<>(view.getWidth(), view.getHeight()));
                }
                return true;
            }
        });
    }

    public static void v_addTranslationX(View view, float extraTranslationX) {
        view.setTranslationX(view.getTranslationX() + extraTranslationX);
    }

    public static void v_addTranslationY(View view, float extraTranslationY) {
        view.setTranslationY(view.getTranslationY() + extraTranslationY);
    }

    public static void v_setOnRefreshing(SwipeRefreshLayout refreshLayout, SwipeRefreshLayout.OnRefreshListener listener) {
        refreshLayout.setOnRefreshListener(() -> {
            listener.onRefresh();
            afterRefreshing();
        });
    }

    public static void v_setOnRefreshing(SwipeRefreshLayout refreshLayout, Action1<SwipeRefreshLayout> listener) {
        refreshLayout.setOnRefreshListener(() -> {
            listener.call(refreshLayout);
            afterRefreshing();
        });
    }

    private static void afterRefreshing() {
        Logger.i(AccessLogFormatter.logForPullToRefreshEvent());
    }


    public static void v_setClick(View parent, @IdRes int viewId, View.OnClickListener listener) {
        parent.findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.onClick(v);
            afterViewClick(v);
        }));
    }

    public static void v_setClick(Activity parent, @IdRes int viewId, View.OnClickListener listener) {
        parent.findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.onClick(v);
            afterViewClick(v);
        }));
    }

    public static void v_setClick(Fragment parent, @IdRes int viewId, View.OnClickListener listener) {
        parent.getView().findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.onClick(v);
            afterViewClick(v);
        }));
    }

    public static void v_setClick(Dialog dialog, @IdRes int viewId, View.OnClickListener listener) {
        dialog.getWindow().getDecorView().findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.onClick(v);
            afterViewClick(v);
        }));
    }

    public static void v_setClick(View view, View.OnClickListener listener) {
        view.setOnClickListener(v -> safeCall(() -> {
            listener.onClick(v);
            afterViewClick(v);
        }));
    }

    public static void v_setClick(View parent, @IdRes int viewId, Action0 listener) {
        parent.findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.call();
            afterViewClick(v);
        }));
    }

    public static void v_setClick(Activity parent, @IdRes int viewId, Action0 listener) {
        parent.findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.call();
            afterViewClick(v);
        }));
    }

    public static void v_setClick(Fragment parent, @IdRes int viewId, Action0 listener) {
        parent.getView().findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.call();
            afterViewClick(v);
        }));
    }

    public static void v_setClick(Dialog dialog, @IdRes int viewId, Action0 listener) {
        dialog.getWindow().getDecorView().findViewById(viewId).setOnClickListener(v -> safeCall(() -> {
            listener.call();
            afterViewClick(v);
        }));
    }

    public static void v_setClick(View view, Action0 listener) {
        view.setOnClickListener(v -> safeCall(() -> {
            listener.call();
            afterViewClick(v);
        }));
    }

    public static void v_setLongClick(View parent, int childId, Action1<View> listener) {
        v_setLongClick(parent.findViewById(childId), listener);
    }
    public static void v_setLongClick(View view, Action1<View> listener) {
        view.setOnLongClickListener(v -> {
            safeCall(() -> {
                listener.call(v);
            });
            return true;
        });
    }

    private static void afterViewClick(View view) {
        String log = null;
        if (view instanceof TextView) {
            log = AccessLogFormatter.logForClickEvent(((TextView) view).getText());
        } else if (view instanceof ProgressButton) {
            log = AccessLogFormatter.logForClickEvent(((ProgressButton) view).getTextWithMode(ProgressButton.Mode.Normal));
        } else if (view.getId() == R.id.section_reload) {
            log = AccessLogFormatter.logForForceReloadEvent();
        }

        if (!TextUtils.isEmpty(log)) Logger.i(log);
    }

    public static <T extends ViewGroup.LayoutParams> T v_getLayoutParams(View view) {
        return (T) view.getLayoutParams();
    }

    public static void v_setVisibility(View finder, int viewId, int visibility) {
        finder.findViewById(viewId).setVisibility(visibility);
    }

    public static void v_setVisibility(Dialog finder, int viewId, int visibility) {
        finder.findViewById(viewId).setVisibility(visibility);
    }

    public static void v_setVisibility(Activity finder, int viewId, int visibility) {
        finder.findViewById(viewId).setVisibility(visibility);
    }

    public static void v_setVisibility(Fragment fragment, int viewId, int visibility) {
        fragment.getView().findViewById(viewId).setVisibility(visibility);
    }

    public static void v_setVisibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    public static boolean v_isVisible(View parent, @IdRes int viewId) {
        return parent.findViewById(viewId).getVisibility() == View.VISIBLE;
    }

    public static boolean v_isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    public static void v_setVisible(View finder, int viewId) {
        finder.findViewById(viewId).setVisibility(View.VISIBLE);
    }

    public static void v_setVisible(Activity finder, int viewId) {
        finder.findViewById(viewId).setVisibility(View.VISIBLE);
    }

    public static void v_setVisible(Fragment fragment, int viewId) {
        fragment.getView().findViewById(viewId).setVisibility(View.VISIBLE);
    }

    public static void v_setVisible(View view) {
        view.setVisibility(View.VISIBLE);
    }

    public static void v_setInvisible(View finder, int viewId) {
        finder.findViewById(viewId).setVisibility(View.INVISIBLE);
    }

    public static void v_setInvisible(Activity finder, int viewId) {
        finder.findViewById(viewId).setVisibility(View.INVISIBLE);
    }

    public static void v_setInvisible(Fragment fragment, int viewId) {
        fragment.getView().findViewById(viewId).setVisibility(View.INVISIBLE);
    }

    public static void v_setInvisible(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    public static void v_setGone(View parent, int viewId) {
        parent.findViewById(viewId).setVisibility(View.GONE);
    }

    public static void v_setGone(Activity finder, int viewId) {
        finder.findViewById(viewId).setVisibility(View.GONE);
    }

    public static void v_setGone(Fragment fragment, int viewId) {
        fragment.getView().findViewById(viewId).setVisibility(View.GONE);
    }

    public static void v_setGone(View view) {
        view.setVisibility(View.GONE);
    }


    public static void v_setText(Activity activity, @IdRes int textViewId, CharSequence text) {
        v_setText(activity.getWindow().getDecorView(), textViewId, text);
    }

    public static void v_setText(Dialog dialog, @IdRes int textViewId, CharSequence text) {
        v_setText(dialog.getWindow().getDecorView(), textViewId, text);
    }

    public static void v_setText(Fragment fragment, @IdRes int textViewId, CharSequence text) {
        v_setText(fragment.getView(), textViewId, text);
    }

    public static void v_setText(View parent, @IdRes int textViewId, CharSequence text) {
        TextView textView = (TextView) parent.findViewById(textViewId);
        textView.setText(text);
    }

    public static void v_setText(TextView textView, CharSequence text) {
        textView.setText(text);
    }

    public static void v_setTextWithFitBound(View parent, int layoutID, CharSequence text) {
        TextView textView = (TextView) parent.findViewById(layoutID);
        v_setTextWithFitBound(textView, text, false);
    }

    public static void v_setTextWithFitBound(TextView textView, CharSequence text) {
        v_setTextWithFitBound(textView, text, false);
    }

    private static void v_setTextWithFitBound(TextView textView, CharSequence text, boolean hasPosted) {
        if (textView.getMeasuredWidth() > 0) {
            int labelWidth = textView.getMeasuredWidth();
            float textWidth = textView.getPaint().measureText(text, 0, text.length());
            textWidth += textWidth / text.length();
            if (labelWidth > 0 && textWidth > labelWidth) {
                float relativeSize = (float) labelWidth / textWidth;
                textView.setText(setRelativeFontSize(text, relativeSize));
            } else {
                textView.setText(text);
            }
        } else if (!hasPosted) {
            textView.post(() -> v_setTextWithFitBound(textView, text, true));
        }
    }

    public static void v_setTextColor(View parent, int textViewId, int color) {
        TextView textView = (TextView) parent.findViewById(textViewId);
        textView.setTextColor(color);
    }

    public static void v_setTextColor(TextView textView, int color) {
        textView.setTextColor(color);
    }

    public static void v_setSelected(View view, boolean selected) {
        view.setSelected(selected);
    }

    public static void v_setEnabled(View parent, int childId, boolean enabled) {
        parent.findViewById(childId).setEnabled(enabled);
    }

    public static void v_setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
    }

    public static void v_setHint(TextView textView, CharSequence text) {
        textView.setHint(text);
    }

    public static void v_setHint(View parent, @IdRes int textViewId, CharSequence text) {
        TextView textView = (TextView) parent.findViewById(textViewId);
        textView.setHint(text);
    }

    public static void v_setImageUri(Activity activity, @IdRes int draweeViewId, String imageURL) {
        v_setImageUri(activity.getWindow().getDecorView(), draweeViewId, imageURL);
    }

    public static void v_setImageUri(Dialog dialog, @IdRes int draweeViewId, String imageURL) {
        v_setImageUri(dialog.getWindow().getDecorView(), draweeViewId, imageURL);
    }

    public static void v_setImageUri(Fragment fragment, @IdRes int draweeViewId, String imageURL) {
        v_setImageUri(fragment.getView(), draweeViewId, imageURL);
    }

    public static void v_setImageUri(View parent, @IdRes int draweeViewId, String imageURL) {
        if (!TextUtils.isEmpty(imageURL)) {
            SimpleDraweeView draweeView = (SimpleDraweeView) parent.findViewById(draweeViewId);
            draweeView.setImageURI(Uri.parse(imageURL));
        }
    }

    public static void v_setImageUri(SimpleDraweeView draweeView, String imageURL) {
        if (imageURL != null) {
            draweeView.setImageURI(Uri.parse(imageURL));
        }
    }

    public static void v_setImageUri(SimpleDraweeView draweeView, Uri imageURL) {
        if (imageURL != null) {
            draweeView.setImageURI(imageURL);
        }
    }

    public static void v_setImageResource(View parent, @IdRes int imageViewId, int imageResId) {
        ImageView imageView = (ImageView) parent.findViewById(imageViewId);
        imageView.setImageResource(imageResId);
    }

    public static void v_setImageResource(ImageView imageView, int imageResId) {
        imageView.setImageResource(imageResId);
    }

    public static void v_setBackgroundColor(Activity activity, @IdRes int viewId, int color) {
        activity.findViewById(viewId).setBackgroundColor(color);
    }

    public static void v_setBackgroundColor(Fragment fragment, @IdRes int viewId, int color) {
        fragment.getView().findViewById(viewId).setBackgroundColor(color);
    }

    public static void v_setBackgroundColor(Dialog dialog, @IdRes int viewId, int color) {
        dialog.getWindow().getDecorView().findViewById(viewId).setBackgroundColor(color);
    }

    public static void v_setBackgroundColor(View parent, @IdRes int viewId, int color) {
        parent.findViewById(viewId).setBackgroundColor(color);
    }

    public static void v_setBackgroundColor(View view, int color) {
        view.setBackgroundColor(color);
    }

    public static void v_setProgress(View parent, @IdRes int progressBarId, int progress) {
        ProgressBar progressBar = (ProgressBar) parent.findViewById(progressBarId);
        progressBar.setProgress(progress);
    }

    public static void v_setProgress(View parent, @IdRes int progressBarId, int progress, int maxProgress) {
        ProgressBar progressBar = (ProgressBar) parent.findViewById(progressBarId);
        progressBar.setMax(maxProgress);
        progressBar.setProgress(progress);
    }

    public static void v_addTextChangedListener(View parent, @IdRes int textViewId, Action1<Editable> listener) {
        v_addTextChangedListener((TextView) parent.findViewById(textViewId), listener);
    }

    public static void v_addTextChangedListener(TextView view, Action1<Editable> listener) {
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                listener.call(s);
            }
        });
    }

    public static void v_setOnEditorActionListener(Activity activity, @IdRes int editTextId, TextView.OnEditorActionListener listener) {
        v_setOnEditorActionListener(activity.getWindow().getDecorView(), editTextId, listener);
    }

    public static void v_setOnEditorActionListener(Fragment fragment, @IdRes int editTextId, TextView.OnEditorActionListener listener) {
        v_setOnEditorActionListener(fragment.getView(), editTextId, listener);
    }

    public static void v_setOnEditorActionListener(View parent, @IdRes int editTextId, TextView.OnEditorActionListener listener) {
        v_setOnEditorActionListener((EditText) parent.findViewById(editTextId), listener);
    }

    public static void v_setOnEditorActionListener(EditText editText, TextView.OnEditorActionListener listener) {
        editText.setOnEditorActionListener(listener);
    }

    public static <T extends View> T v_findView(Activity activity, @IdRes int viewId) {
        return (T) activity.getWindow().getDecorView().findViewById(viewId);
    }

    public static <T extends View> T v_findView(Fragment fragment, @IdRes int viewId) {
        return (T) fragment.getView().findViewById(viewId);
    }

    public static <T extends View> T v_findView(Dialog dialog, @IdRes int viewId) {
        return (T) dialog.getWindow().getDecorView().findViewById(viewId);
    }

    public static <T extends View> T v_findView(View parent, @IdRes int viewId) {
        return (T) parent.findViewById(viewId);
    }

    public static Action1<Editable> v_bankCardNumFormatter(EditText editText) {
        return new Action1<Editable>() {
            @Override
            public void call(Editable editable) {
                if ((editable.length() > 0 && !TextUtils.isDigitsOnly(map(editable, normalBankCardNoTransformer())))) {
                    editable.delete(editable.length() - 1, editable.length());
                } else {
                    String formattedCardNo = map(editable, formatBankCardNoTransformer());
                    if (!formattedCardNo.trim().equalsIgnoreCase(editable.toString().trim())) {
                        editText.setText(formattedCardNo);
                        editText.setSelection(formattedCardNo.length());
                    }
                }
            }
        };
    }

    public static Action1<Editable> v_unsignedNumberFormatter() {
        return new Action1<Editable>() {
            @Override
            public void call(Editable editable) {
                if (editable.length() > 0 && !TextUtils.isDigitsOnly(editable)) {
                    editable.delete(editable.length() - 1, editable.length());
                }
            }
        };
    }

    public static Action1<Editable> v_moneyFormatterFormatter(EditText editText, boolean symbol, int scale) {
        return new Action1<Editable>() {
            @Override
            public void call(Editable editable) {
                String normalMoney = map(editable, normalMoneyTransformer());
                if (!TextUtils.isEmpty(normalMoney) && !TextUtils.isDigitsOnly(normalMoney)) {
                    editable.delete(editable.length() - 1, editable.length());
                    return;
                }

                String formatMoney = map(normalMoney, formatMoneyTransformer(symbol, scale));
                if (!formatMoney.equalsIgnoreCase(editable.toString())) {
                    editText.setText(formatMoney);
                    editText.setSelection(formatMoney.length());
                    return;
                }

                if (editable.length() == 1 && editable.toString().equalsIgnoreCase("0")) {
                    editable.clear();
                    return;
                }
            }
        };
    }

    public static Action1<Editable> v_moneyFormatterFormatter(EditText editText, boolean symbol, int minScale, int maxScale) {
        return new Action1<Editable>() {
            @Override
            public void call(Editable editable) {
                String normalMoney = map(editable, normalMoneyTransformer());
                if (!TextUtils.isEmpty(normalMoney) && !RegexPatternHolder.MATCH_DOUBLE_VALUE_ENTIRE.matcher(normalMoney).find()) {
                    editable.delete(editable.length() - 1, editable.length());
                    return;
                }

                String formatMoney = map(normalMoney, formatMoneyTransformer(symbol, minScale, maxScale));
                if (!formatMoney.equalsIgnoreCase(editable.toString())) {
                    editText.setText(formatMoney);
                    editText.setSelection(formatMoney.length());
                    return;
                }

                if (editable.length() == 1 && editable.toString().equalsIgnoreCase("0")) {
                    editable.clear();
                    return;
                }
            }
        };
    }
}
