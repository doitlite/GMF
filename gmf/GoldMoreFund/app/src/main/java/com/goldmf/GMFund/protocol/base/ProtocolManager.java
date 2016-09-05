package com.goldmf.GMFund.protocol.base;


import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.manager.dev.RequestLogManager;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.util.FileUtil;
import com.goldmf.GMFund.util.OKHttpUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.MyConfig.isProTag;

/**
 * Created by cupide on 15/7/22.
 */
public class ProtocolManager {

    public final Integer versionProtocol = 9;

    public String appToken = null;
    private String tradeToken = null;
    private String snsToken = null;

    private static String sProtocolManagerKey = "ProtocolManager";
    public static String sAppTokenrKey = "app_token";
    private static String sTraderTokenrKey = "trade_token";
    public static String sSNSTokenrKey = "sns_token";
    private static String UserAgentOrNil = null;

    private List<ProtocolBase> loginProtocolQueue = new ArrayList<>();

    /**
     * 静态方法
     */
    private static ProtocolManager sInstance = new ProtocolManager();

    public static ProtocolManager getInstance() {
        return sInstance;
    }

    /**
     * 私有的默认构造
     */
    private ProtocolManager() {
        {
            String token = FileUtil.getValue(sProtocolManagerKey, sAppTokenrKey);
            if (token != null) {
                this.setAppToken(token);
            }
        }
        {
            String token = FileUtil.getValue(sProtocolManagerKey, sSNSTokenrKey);
            if (token != null) {
                this.setSNSToken(token);
            }
        }


        NotificationCenter.loginSubject.subscribe(user -> {

            if (loginProtocolQueue.size() > 0) {
                for (ProtocolBase p : loginProtocolQueue) {
                    p.startWork();
                }
            }
        });

        NotificationCenter.cancelLoginSubject.subscribe(nil -> {
            if (loginProtocolQueue.size() > 0) {
                for (ProtocolBase p : loginProtocolQueue) {
                    ProtocolManager.this.processErr(p.returnCode, null, p);
                }
            }
        });
    }


    /**
     * 设置appToken
     *
     * @param token
     */
    public void setAppToken(String token) {
        this.appToken = token;
        OKHttpUtil.addCookie(domain1(), sAppTokenrKey, this.appToken);
        FileUtil.saveValue(sProtocolManagerKey, sAppTokenrKey, this.appToken);
    }


    /**
     * 设置traderToken
     *
     * @param token
     */
    public void setTradeToken(String token) {
        this.tradeToken = token;
        OKHttpUtil.addCookie(domain1(), sTraderTokenrKey, this.tradeToken);

    }

    public String getSNSToken() {
        return this.snsToken;
    }

    /**
     * 设置snsToken
     *
     * @param token
     */
    public void setSNSToken(String token) {
        this.snsToken = token;
        OKHttpUtil.addCookie(domain2(), sSNSTokenrKey, this.snsToken);

        FileUtil.saveValue(sProtocolManagerKey, sSNSTokenrKey, this.snsToken);
    }


    private static final String GMF_VISION_PROTOCOL_KEY_PRO = "pro";
    private static final String GMF_VISION_PROTOCOL_KEY_COMMON = "common";

    private static final MediaType MEDIA_TYPE_POST = MediaType.parse("application/x-www-form-urlencoded");

    /**
     * 异步启动一个协议，仅支持post OR get。
     */
    public final Call enqueue(final ProtocolBase protocol) {

        protocol.mHourglass.start();

        String url = protocol.url();
        Map<String, String> params = protocol.getParam();
        if (params == null) {
            params = new HashMap<>();
        }

        //增加常规参数
        params.put("format", "json");
        params.put("protocol_ver", versionProtocol.toString());
        params.put("os", "android");
        params.put("platform", "android");
        params.put("time", String.valueOf(SecondUtil.currentSecond()));
        params.put("app_tag", isProTag() ? GMF_VISION_PROTOCOL_KEY_PRO : GMF_VISION_PROTOCOL_KEY_COMMON);


        url = OKHttpUtil.attachHttpGetParams(url, params);

        //仅支持get or post
        String cmd = "GET";
        RequestBody body = null;
        Map<String, Object> postData = protocol.getPostData();
        if (postData != null) {
            cmd = "POST";
            body = RequestBody.create(MEDIA_TYPE_POST, OKHttpUtil.formatParams(postData).getBytes());
        }

        if (UserAgentOrNil == null) {
            UserAgentOrNil = OKHttpUtil.generateUserAgent(MyApplication.SHARE_INSTANCE);
        }
        Request request = new Request.Builder()
                .url(url)
                .method(cmd, body)
                .addHeader("User-Agent", UserAgentOrNil)
                .build();

        return OKHttpUtil.enqueue(request, new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        RequestLogManager.addLog(request, e);
                        processErr(ProtocolBase.ErrCode.ERR_HTTP, e, protocol);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseMessage = response.body().string();
                        JsonElement obj = parseJson(responseMessage);
                        RequestLogManager.addLog(request, response, responseMessage);

                        MyApplication.post(() -> {

                            if (obj == null) {
                                Logger.d("[protocol]url:{%s}\n, return:{null}", protocol.url());
                                processErr(ProtocolBase.ErrCode.ERR_JSON, null, protocol);

                            } else {

                                Logger.d(Thread.currentThread().toString());

                                Logger.d("[protocol]url:{%s}\n, return:{%s}", protocol.url(), obj.toString());

                                if (protocol.parseJson(obj)) {
                                    processSuccess(protocol);
                                } else {
                                    //处理：10000
                                    if (protocol.returnCode == 10000 && protocol.reStart == false) {

                                        ProtocolManager.getInstance().setAppToken("");
                                        ProtocolManager.getInstance().setSNSToken("");
                                        ProtocolManager.getInstance().setTradeToken("");

                                        protocol.reStart = true;
                                        loginProtocolQueue.add(protocol);

                                        //need login
                                        NotificationCenter.needLoginSubject.onNext(null);
                                    } else {
                                        processErr(protocol.returnCode, null, protocol);
                                    }
                                }
                            }
                        });
                    }
                }
        );
        }


    private JsonElement parseJson(String data) {
        try {
            JsonParser parser = new JsonParser();
            return parser.parse(data);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }


    public static String domain1() {

        String hostName = CHostName.getHost1();

        try {
            URL url = new URL(hostName);
            return url.getHost();
        } catch (Exception ignored) {
        }
        return "";
    }

    public static String domain2() {

        String hostName = CHostName.getHost2();

        try {
            URL url = new URL(hostName);
            return url.getHost();
        } catch (Exception ignored) {
        }
        return "";
    }

    private void processErr(int errCode, IOException e, final ProtocolBase protocol) {
        protocol.returnCode = errCode; //重复赋值没有什么问题。
        String errInfo = String.format("errCode:%d", errCode);
        if (e != null) {
            errInfo += String.format("e:%s", e.toString());
        }
        if (protocol.returnMsg != null) {
            errInfo += String.format("returnMsg:%s", protocol.returnMsg);
        }

        //todo，统计错误！
        long costMillis = protocol.mHourglass.stop();
        Logger.d("[processErr]url:{%s}\n,costMillis:{%d}\n, errInfo:{%s}", protocol.url(), costMillis, errInfo);

        UmengUtil.stat_cgi_err(MyApplication.SHARE_INSTANCE,
                protocol.url(),
                MineManager.getInstance().getPhone(),
                Optional.of(protocol.returnCode),
                Optional.of(protocol.returnMsg));

        if (protocol != null && protocol.mCallback != null) {
            protocol.mCallback.onFailure(protocol, errCode);
        }
    }

    private void processSuccess(final ProtocolBase protocol) {
        //耗时
        long costMillis = protocol.mHourglass.stop();
        Logger.d("[processSuccess]url:{%s}\n,costMillis:{%d}\n", protocol.url(), costMillis);

        if (protocol != null && protocol.mCallback != null)
            protocol.mCallback.onSuccess(protocol);
    }
}


