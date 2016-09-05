package com.goldmf.GMFund.extension;

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.base.StringPair;
import com.goldmf.GMFund.protocol.base.ProtocolManager;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;

/**
 * Created by yale on 16/4/21.
 */
@SuppressWarnings("deprecation")
public class WebViewExtension {
    private WebViewExtension() {
    }

    public static void syncCookiesImmediately(String urlStr) {
        safeCall(() -> {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            MyApplication application = MyApplication.SHARE_INSTANCE;
            List<StringPair> cookies = new LinkedList<>();
            cookies.add(StringPair.create(ProtocolManager.sAppTokenrKey, opt(ProtocolManager.getInstance().appToken).or("")));
            cookies.add(StringPair.create(ProtocolManager.sSNSTokenrKey, opt(ProtocolManager.getInstance().getSNSToken()).or("")));
            if (CookieSyncManager.getInstance() == null) {
                CookieSyncManager.createInstance(application);
            }
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            URL url = new URL(urlStr);
            String domain = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
            for (StringPair cookie : cookies) {
                cookieManager.setCookie(domain, cookie.first + "=" + cookie.second + ";");
            }
            CookieSyncManager.getInstance().sync();
//            }
        });
    }

    public static void removeCookiesImmediately() {
        safeCall(() -> {
            MyApplication application = MyApplication.SHARE_INSTANCE;
            if (CookieSyncManager.getInstance() == null) {
                CookieSyncManager.createInstance(application);
            }
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        });
    }
}
