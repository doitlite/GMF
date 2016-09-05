package com.goldmf.GMFund.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.KeepClassProtocol;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.dialog.ShareDialog.SharePlatform;
import com.goldmf.GMFund.controller.internal.RegexPatternHolder;
import com.goldmf.GMFund.controller.protocol.UMShareHandlerProtocol;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.widget.GMFWebview;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.microedition.khronos.opengles.GL;

import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;
import yale.extension.common.rx.ProxyCompositionSubscription;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.FragmentStackActivity.replaceTopFragment;
import static com.goldmf.GMFund.controller.FragmentStackActivity.resetFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_RECHARGE_TYPE_INT;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.extension.FileExtension.writeDataToFile;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.runOnMain;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.WebViewExtension.syncCookiesImmediately;

/**
 * Created by yale on 15/10/14.
 */
public class WebViewFragments {

    public static PublishSubject<Pair<ShareInfo, SharePlatform[]>> sEditDefaultShareInfoSubject = PublishSubject.create();
    public static PublishSubject<Pair<ShareInfo, SharePlatform[]>> sPerformScreenShotShareSubject = PublishSubject.create();
    public static PublishSubject<Pair<ShareInfo, SharePlatform[]>> sShowShareButtonSubject = PublishSubject.create();
    public static PublishSubject<Pair<ShareInfo, SharePlatform[]>> sHideShareButtonSubject = PublishSubject.create();

    private WebViewFragments() {
    }

    public static class WebViewFragmentDelegate {
        private SimpleFragment mFragment;
        private GMFWebview mWebView;
        private boolean mHasRequestLogin = false;
        private boolean mAllowToInterceptGoBack = true;
        private boolean mResetHeight = false;
        private ProxyCompositionSubscription mSubscription = ProxyCompositionSubscription.create();

        public WebViewFragmentDelegate(SimpleFragment fragment, GMFWebview webView) {
            this(fragment, webView, false);
        }

        public WebViewFragmentDelegate(SimpleFragment fragment, GMFWebview webView, boolean resetHeight) {
            mFragment = fragment;
            mWebView = webView;
            mResetHeight = resetHeight;
        }

        public void onViewCreated(String requestUrl) {
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onLoadResource(WebView view, String url) {
                    syncCookiesImmediately(url);
                    super.onLoadResource(view, url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    v_setGone(mFragment.mLoadingSection);
                    if (mResetHeight) {
                        view.loadUrl("javascript:window.common._updateHeight(document.body.scrollHeight)");
                    }
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            });

            if (isSecureRequest(requestUrl)) {
                mWebView.addJavascriptInterface(new KeepClassProtocol() {

                    @JavascriptInterface
                    public void nativeopen(String link) {
                        getActivity().runOnUiThread(() -> CMDParser.parse(link).call(getActivity()));
                    }

                    @JavascriptInterface
                    public void nativeLogin() {
                        getActivity().runOnUiThread(() -> {
                            if (!MineManager.getInstance().isLoginOK()) {
                                mHasRequestLogin = true;
                                showActivity(getActivity(), an_LoginPage());
                            }
                        });
                    }

                    @JavascriptInterface
                    public void nativeAlert(String title, String msg) {
                        getActivity().runOnUiThread(() -> {
                            GMFDialog.Builder builder = new GMFDialog.Builder(getActivity());
                            builder.setTitle(title);
                            builder.setMessage(msg);
                            builder.setPositiveButton("确定");
                            builder.create().show();
                        });
                    }

                    @JavascriptInterface
                    public void nativeClose() {
                        mAllowToInterceptGoBack = false;
                        getActivity().runOnUiThread(() -> goBack(mFragment));
                    }

                    @JavascriptInterface
                    public void riskTestResult(int level, String text) {
                        getActivity().runOnUiThread(() -> NotificationCenter.receiveRiskTestResultSubject.onNext(Pair.create(level, text)));
                    }

                    @JavascriptInterface
                    public void riskTestFinish() {
                        getActivity().runOnUiThread(() -> NotificationCenter.riskTestFinishSubject.onNext(getActivity()));
                    }

                    @JavascriptInterface
                    public String nativeGetProperty() {
                        try {
                            String mineStr = MineManager.getInstance().getMineString();
                            JSONObject jsonObj = new JSONObject(mineStr);
                            jsonObj.put("appKey", ProtocolManager.getInstance().appToken);
                            return jsonObj.toString();
                        } catch (Exception e) {
                            return "{}";
                        }
                    }

                    @JavascriptInterface
                    public void _updateHeight(int height) {
                        if (mResetHeight) {
                            runOnMain(() -> {
                                if (height > 0) {
                                    mWebView.setMinimumHeight(dp2px(height));
                                }
                            });
                        }
                    }

                }, "common");
            }

            mFragment.consumeEvent(NotificationCenter.loginSubject)
                    .setTag("on_user_login")
                    .onNextFinish(user -> {
                        if (mHasRequestLogin) {
                            mHasRequestLogin = false;
                            mWebView.callJS("loginFinish", true);
                        }
                    })
                    .done();
            mFragment.consumeEvent(NotificationCenter.cancelLoginSubject)
                    .setTag("on_user_cancel_login")
                    .onNextFinish(nil -> {
                        if (mHasRequestLogin) {
                            mHasRequestLogin = false;
                            mWebView.callJS("loginFinish", false);
                        }
                    })
                    .done();

            mFragment.consumeEvent(NotificationCenter.closeEditShippingAddressPageSubject)
                    .setTag("on_close_edit_address_page")
                    .onNextFinish(ignored -> {
                        boolean isFilledShippingAddress = MineManager.getInstance().getmMe().isFilledShippingAddress();
                        if (isFilledShippingAddress) {
                            Mine.ShippingAddress address = MineManager.getInstance().getmMe().getAddress();
                            String key = "modifyAddressSuccess";
                            String value = String.format("%s%s%s,&nbsp;收货人&nbsp;%s&nbsp;%s", address.city.getProvince(), address.city.getCity(), address.address, address.name, address.cellphone);
                            value = URLEncoder.encode(value, "UTF-8");

                            String arg0 = key + ":" + value;
                            mWebView.callJS("window.top.postMessage", arg0, "*");
                        }
                    })
                    .done();

            syncCookiesImmediately(requestUrl);
        }

        public void onDestroyView() {
            safeCall(() -> {
                mWebView.loadUrl("about:blank");
            });
            mSubscription.reset();
        }

        protected boolean onInterceptGoBack() {
            if (mWebView.canGoBack() && mAllowToInterceptGoBack) {
                mWebView.goBack();
                return true;
            }
            mWebView.loadUrl("about:blank");
            return false;
        }

        public void setUserVisibleHint(boolean isVisibleToUser) {
            if (getView() != null) {
                mSubscription.reset();
                if (isVisibleToUser) {
                    mSubscription.add(sPerformScreenShotShareSubject
                            .filter(pair -> pair != null)
                            .delay(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(pair -> {
                                if (getActivity() instanceof UMShareHandlerProtocol) {
                                    ShareInfo shareInfo = pair.first;
                                    SharePlatform[] platforms = pair.second;
                                    UMShareHandlerProtocol handler = (UMShareHandlerProtocol) getActivity();
                                    File file = new File(MyApplication.SHARE_INSTANCE.getCacheDir(), "tmp.jpg");
                                    Bitmap bitmap = drawViewToBitmap(mWebView, mWebView.getWidth(), mWebView.computeVerticalScrollRange());
                                    writeDataToFile(file, bitmap, true, Bitmap.CompressFormat.JPEG, 50);
                                    shareInfo.imageUrl = Uri.fromFile(file).toString();
                                    handler.onPerformShare(shareInfo, platforms);
                                    mWebView.callJS("window.top.postMessage", "{\"type\":\"screen_shot_complete\"}", "*");
                                }
                            }));
                }
            }

        }

        private View getView() {
            return mFragment.getView();
        }

        private Activity getActivity() {
            return mFragment.getActivity();
        }

        private Bitmap drawViewToBitmap(View view, int width, int height) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);

            view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (view instanceof WebView) {
                    ((WebView) view).capturePicture().draw(canvas);
                } else {
                    view.draw(canvas);
                }
            } else {
                view.draw(canvas);
            }
            return bitmap;
        }

        public static boolean isSecureRequest(String requestURL) {
            if (TextUtils.isEmpty(requestURL)) {
                return true;
            }

            Uri uri = Uri.parse(requestURL);
            if (uri != null) {
                String host = uri.getHost();
                if (!TextUtils.isEmpty(host)) {
                    if (RegexPatternHolder.MATCH_LAN_ADDRESS_ENTIRE.matcher(host).find()) {
                        return true;
                    }

                    List<String> whiteList = Optional.of(CommonManager.getInstance().getWhiteList()).or(Collections.emptyList());
                    for (String rule : whiteList) {
                        if (Pattern.compile("(\\." + rule + "/?$)|(^" + rule + "/?$)").matcher(host).find()) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    public static class WebViewFragment extends SimpleFragment {
        public static final PublishSubject<Void> goBackSubject = PublishSubject.create();

        protected GMFWebview mWebView;

        private TextView mToolbarTitleLabel;
        private ImageButton mShareButton;
        private ProxyCompositionSubscription mSubscription = ProxyCompositionSubscription.create();

        private String mRequestURL;
        private WebViewFragmentDelegate mDelegate = null;
        private boolean mHasToolBar;
        private boolean mHideShareButton;
        private boolean mPreferCloseButtonAtToolbar;

        public WebViewFragment init(String requestURL) {
            return init(requestURL, true, false);
        }

        public WebViewFragment init(String requestURL, boolean hasToolBar) {
            return init(requestURL, hasToolBar, false);
        }

        public WebViewFragment init(String requestURL, boolean hasToolBar, boolean hideShareButton) {
            Bundle arguments = new Bundle();
            arguments.putString(CommonProxyActivity.KEY_URL_STRING, requestURL);
            arguments.putBoolean(CommonProxyActivity.KEY_HAS_TOOLBAR_BOOLEAN, hasToolBar);
            arguments.putBoolean(CommonProxyActivity.KEY_HIDE_SHARE_BTN_BOOLEAN, hideShareButton);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mRequestURL = getArguments().getString(CommonProxyActivity.KEY_URL_STRING);
            mHasToolBar = getArguments().getBoolean(CommonProxyActivity.KEY_HAS_TOOLBAR_BOOLEAN, true);
            mHideShareButton = getArguments().getBoolean(CommonProxyActivity.KEY_HIDE_SHARE_BTN_BOOLEAN, false);
            mPreferCloseButtonAtToolbar = getArguments().getBoolean(CommonProxyActivity.KEY_PREFER_CLOSE_BUTTON_AT_TOOLBAR_BOOLEAN, false);
            if (!RegexPatternHolder.MATCH_START_WITH_HTTP.matcher(mRequestURL.toLowerCase()).matches()) {
                mRequestURL = "http://" + mRequestURL;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WebView.enableSlowWholeDocumentDraw();
            }
            return inflater.inflate(R.layout.frag_webview, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (mHasToolBar) {
                setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
                if (mPreferCloseButtonAtToolbar) {
                    setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
                } else {
                    setupBackButton(this, findToolbar(this));
                }
                updateTitle("加载中");
            }
            v_setVisibility(findToolbar(this), mHasToolBar ? View.VISIBLE : View.GONE);

            mShareButton = v_findView(this, R.id.btn_share);
            mToolbarTitleLabel = v_findView(this, R.id.toolbarTitle);
            v_setVisible(mLoadingSection);
            mWebView = v_findView(this, R.id.webView);
            v_setClick(mShareButton, this::performShareFromButton);

            consumeEvent(goBackSubject)
                    .setTag("on_press_key_back")
                    .onNextFinish(nil -> goBack(this))
                    .done();

            setOnSwipeRefreshListener(() -> mWebView.reload());

            mWebView.setWebChromeClient(new WebChromeClient() {
                boolean isSecure = WebViewFragmentDelegate.isSecureRequest(mRequestURL);

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    if (getView() != null) {
                        if (isSecure) {
                            mToolbarTitleLabel.setSingleLine();
                            if (!title.startsWith("http")) {
                                updateTitle(title);
                            }
                        } else {
                            mToolbarTitleLabel.setSingleLine(false);
                            mToolbarTitleLabel.setLines(2);
                            CharSequence secureTitle = concat(
                                    "操盘侠安全提示",
                                    setFontSize("网站未经验证，请谨慎输入帐号密码", sp2px(10)));
                            updateTitle(secureTitle);
                        }
                    }
                }
            });

            boolean isSecureRequest = WebViewFragmentDelegate.isSecureRequest(mRequestURL);
            boolean isShareButtonVisible = isSecureRequest && !mHideShareButton;
            mToolbarTitleLabel.setPadding(0, 0, isSecureRequest ? 0 : dp2px(56), 0);
            v_setVisibility(mShareButton, isShareButtonVisible ? View.VISIBLE : View.GONE);

            mDelegate = new WebViewFragmentDelegate(this, mWebView);
            mDelegate.onViewCreated(mRequestURL);

            mWebView.loadUrl(mRequestURL);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mSubscription.reset();
        }

        @Override
        protected boolean onInterceptGoBack() {
            return Optional.of(mDelegate).let(it -> it.onInterceptGoBack()).or(false) || super.onInterceptGoBack();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            Optional.of(mDelegate).apply(it -> it.setUserVisibleHint(isVisibleToUser));
            if (getView() != null) {
                mSubscription.reset();
                if (isVisibleToUser) {
                    mSubscription.add(sEditDefaultShareInfoSubject
                            .filter(pair -> pair != null)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(pair -> {
                                ShareInfo info = pair.first;
                                SharePlatform[] platforms = pair.second;
                                resetShareContent(info.title, info.msg, info.url, info.imageUrl, platforms);
                            }));
                    mSubscription.add(sShowShareButtonSubject.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(nil -> {
                                v_setVisible(mShareButton);
                                mToolbarTitleLabel.setPadding(0, 0, 0, 0);
                            }));
                    mSubscription.add(sHideShareButtonSubject.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(nil -> {
                                v_setGone(mShareButton);
                                mToolbarTitleLabel.setPadding(0, 0, dp2px(56), 0);
                            }));
                }
            }
        }

        private ShareInfo mDefaultShareInfo;
        private SharePlatform[] mDefaultSharePlatforms = null;

        private void resetShareContent(String title, String content, String link, String imageUrl, SharePlatform[] platforms) {
            ShareInfo info = new ShareInfo();
            info.title = Optional.of(title).or("来自操盘侠:带你股市翻红");
            info.msg = Optional.of(content).or("来自操盘侠:带你股市翻红");
            info.url = Optional.of(link).or("");
            info.imageUrl = Optional.of(imageUrl).or("");
            mDefaultShareInfo = info;
            mDefaultSharePlatforms = platforms;
        }

        private void performShareFromButton() {
            if (getActivity() instanceof UMShareHandlerProtocol) {
                UMShareHandlerProtocol handler = (UMShareHandlerProtocol) getActivity();
                if (mDefaultShareInfo == null) {
                    ShareInfo info = new ShareInfo();
                    info.title = mWebView.getTitle();
                    info.imageUrl = "";
                    info.msg = "来自操盘侠:带你股市翻红";
                    info.url = mWebView.getUrl();
                    handler.onPerformShare(info, (SharePlatform[]) null);
                } else {
                    handler.onPerformShare(mDefaultShareInfo, mDefaultSharePlatforms);
                }
            }
        }
    }
}
