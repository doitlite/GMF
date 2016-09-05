package com.goldmf.GMFund;

/**
 * Created by yale on 16/6/13.
 */
public class ShareKeys {
    public static final String UMENG_APP_KEY = notPro() ? "55b0b4fc67e58ee03d001dd5" : "575e7e2267e58e63490001f3";
    public static final String WECHAT_APP_ID = notPro() ? "wx475f35fd7466153c" : "wxa9fc0d6eb172a908";
    public static final String WECHAT_APP_SECRET = notPro() ? "32bc11e0d28b82ebdc75f59f7c8ab01c" : "c3dd8020951d2e7654d0f92646d2660d";
    public static final String QQ_APP_ID = notPro() ? "1104772855" : "1105291853";
    public static final String QQ_APP_KEY = notPro() ? "wgFM1HAJyXLhVwqD" : "Z40VStkFUZqdaTB3";
    public static final String SINA_APP_KEY = notPro() ? "4042013183" : "4197121120";
    public static final String SINA_APP_SECRET = notPro() ? "ca7b851b4eaa60be7770aac7ebd06744" : "f497d24631cd1fccbafd047a8b10758c";

    private static boolean notPro() {
        return !BuildConfig.BUILD_TYPE.equalsIgnoreCase("pro");
    }
}
