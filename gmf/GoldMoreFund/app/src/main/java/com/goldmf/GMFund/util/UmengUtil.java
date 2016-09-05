package com.goldmf.GMFund.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.ShareKeys;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.BrowsableActivity;
import com.goldmf.GMFund.controller.MainActivityV2;
import com.goldmf.GMFund.controller.MainFragments;
import com.goldmf.GMFund.extension.FileExtension;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.model.FundBrief;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.common.SocializeConstants;
import com.umeng.socialize.media.UMImage;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action2;
import rx.schedulers.Schedulers;
import yale.extension.common.Optional;

import static com.umeng.analytics.MobclickAgent.onEvent;
import static java.util.Collections.singletonMap;

/**
 * Created by yale on 15/7/23.
 */
public class UmengUtil {
    private UmengUtil() {
    }

    public static void init() {
        Config.IsToastTip = MyConfig.IS_DEBUG_MODE;
        Config.dialog = null;
        SocializeConstants.APPKEY = ShareKeys.UMENG_APP_KEY;
        PlatformConfig.setWeixin(ShareKeys.WECHAT_APP_ID, ShareKeys.WECHAT_APP_SECRET);
        PlatformConfig.setSinaWeibo(ShareKeys.SINA_APP_KEY, ShareKeys.SINA_APP_SECRET);
        PlatformConfig.setQQZone(ShareKeys.QQ_APP_ID, ShareKeys.QQ_APP_KEY);
    }

    public static class WXLoginInfo implements Serializable {
        public String accessToken;          //调用凭证
        public String refreshToken;         //刷新凭证
        public String openId;               //普通用户的标识，对当前开发者帐号唯一
        public String unionId;              //用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的unionid是唯一的。
        public String nickName;             //普通用户昵称
        public String sex;                  //普通用户性别，1为男性，2为女性
        public String province;             //普通用户个人资料填写的省份
        public String city;                 //普通用户个人资料填写的城市
        public String country;              //国家，如中国为CN
        public String avatarURL;            //用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空

        @Override
        public String toString() {
            return "WXLoginInfo{" +
                    "accessToken='" + accessToken + '\'' +
                    ", refreshToken='" + refreshToken + '\'' +
                    ", openId='" + openId + '\'' +
                    ", unionId='" + unionId + '\'' +
                    ", nickName='" + nickName + '\'' +
                    ", sex='" + sex + '\'' +
                    ", province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", avatarURL='" + avatarURL + '\'' +
                    '}';
        }
    }

    public static Observable<MResults.MResultsInfo<WXLoginInfo>> fetchWXAccessToken(Activity context) {
        Observable<MResults.MResultsInfo<WXLoginInfo>> ob1 = Observable.create(sub -> {
            fetchWXAccessToken(context, (isSuccess, loginInfo) -> {
                if (!sub.isUnsubscribed()) {
                    MResults.MResultsInfo<WXLoginInfo> data = new MResults.MResultsInfo<>();
                    if (isSuccess && loginInfo.isPresent()) {
                        data.isSuccess = true;
                        data.data = loginInfo.get();
                        sub.onNext(data);
                        sub.onCompleted();
                    } else {
                        data.isSuccess = false;
                        data.data = null;
                        data.msg = !AppUtil.isInstalled(context, "com.tencent.mm") ? "尚未安装微信" : null;
                        MyApplication.postDelayed(() -> {
                            sub.onNext(data);
                            sub.onCompleted();
                        }, AppUtil.isInstalled(context, "com.tencent.mm") ? 0 : 1000L);
                    }
                }
            });
        });
        Observable<MResults.MResultsInfo<WXLoginInfo>> ob2 = Observable.just(MResults.MResultsInfo.<WXLoginInfo>FailureComRet()).delay(5, TimeUnit.SECONDS);
        return Observable.merge(ob1, ob2);
    }

    public static void fetchWXAccessToken(Activity context, Action2<Boolean, Optional<WXLoginInfo>> completionHandler) {

        Action2<Boolean, Optional<WXLoginInfo>> handlerRef = completionHandler != null ? completionHandler : (isSuccess, data) -> {
        };

        if (!AppUtil.isInstalled(context, "com.tencent.mm")) {
            handlerRef.call(false, Optional.empty());
            return;
        }

        WXLoginInfo loginInfo = new WXLoginInfo();

        UMShareAPI shareAPI = UMShareAPI.get(context);
        shareAPI.doOauthVerify(context, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
            @Override
            public void onComplete(SHARE_MEDIA share_media, int status, Map<String, String> map) {
                loginInfo.accessToken = Optional.of(map.get("access_token")).let(Object::toString).or("");
                loginInfo.refreshToken = Optional.of(map.get("refresh_token")).let(Object::toString).or("");
                shareAPI.getPlatformInfo(context, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int status, Map<String, String> info) {
                        if (status == 2) {
                            loginInfo.openId = Optional.of(info.get("openid")).let(Object::toString).or("");
                            loginInfo.unionId = Optional.of(info.get("unionid")).let(Object::toString).or("");
                            loginInfo.nickName = Optional.of(info.get("nickname")).let(Object::toString).or("");
                            loginInfo.sex = Optional.of(info.get("sex")).let(Object::toString).or("");
                            loginInfo.province = Optional.of(info.get("province")).let(Object::toString).or("");
                            loginInfo.city = Optional.of(info.get("city")).let(Object::toString).or("");
                            loginInfo.country = Optional.of(info.get("country")).let(Object::toString).or("");
                            loginInfo.avatarURL = Optional.of(info.get("headimgurl")).let(Object::toString).or("");
                            handlerRef.call(true, Optional.of(loginInfo));
                        } else {
                            handlerRef.call(false, Optional.empty());
                        }
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        handlerRef.call(false, Optional.empty());
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        handlerRef.call(false, Optional.empty());
                    }
                });
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

                handlerRef.call(false, Optional.empty());
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                handlerRef.call(false, Optional.empty());
            }
        });
    }

    public static UMShareAPI createShareService(Activity activity, String topicName) {
        if (TextUtils.isEmpty(topicName))
            topicName = "com.umeng.share";
        UMShareAPI api = UMShareAPI.get(activity);

        return api;
    }

    public static void performShare(Activity activity, UMShareAPI shareAPI, SHARE_MEDIA platform, ShareInfo shareInfo, UMShareListener listenerOrNil) {
        WeakReference<Activity> activityRef = new WeakReference<>(activity);
        Context ctx = MyApplication.SHARE_INSTANCE;
        if (platform == SHARE_MEDIA.GENERIC) {
            ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText(null, TextUtils.isEmpty(shareInfo.url) ? "about:blank" : shareInfo.url));
            UIControllerExtension.showToast(activity, "复制成功");
            if (listenerOrNil != null) {
                listenerOrNil.onCancel(platform);
            }
            return;
        }

        boolean hasShareLink = !TextUtils.isEmpty(shareInfo.url);

        transformToUMImage(shareInfo.imageUrl, platform, (isSuccess, image) -> {
            if (!isSuccess) {
                if (listenerOrNil != null) {
                    listenerOrNil.onCancel(platform);
                }
                return;
            }

            ShareAction shareAction = new ShareAction(activity)
                    .setPlatform(platform);
            if (platform == SHARE_MEDIA.SMS) {
                if (hasShareLink) {
                    shareAction.withText(shareInfo.msg + "\n" + shareInfo.url);
                } else {
                    shareAction.withMedia(image);
                }
            } else if (platform == SHARE_MEDIA.SINA) {
                shareAction.withMedia(image);
                shareAction.withTitle(shareInfo.title);
                shareAction.withText(shareInfo.msg);
                if (hasShareLink) {
                    shareAction.withTargetUrl(shareInfo.url);
                }
            } else if (platform == SHARE_MEDIA.WEIXIN_CIRCLE) {
                shareAction.withMedia(image);
                if (hasShareLink) {
                    shareAction.withTitle(shareInfo.title + "\n" + shareInfo.msg);
                    shareAction.withText(shareInfo.title + "\n" + shareInfo.msg);
                    shareAction.withTargetUrl(shareInfo.url);
                }
            } else {
                shareAction.withMedia(image);
                if (hasShareLink) {
                    shareAction.withTitle(shareInfo.title);
                    shareAction.withText(shareInfo.msg);
                    shareAction.withTargetUrl(shareInfo.url);
                }
            }

            if (activityRef.get() != null) {
                activity.runOnUiThread(() -> shareAPI.doShare(activity, shareAction, listenerOrNil));
            }
        });

    }

    public static boolean handleOnActivityResult(UMShareAPI shareAPI, int requestCode, int resultCode, Intent data) {
        if (shareAPI != null) {
            shareAPI.onActivityResult(requestCode, resultCode, data);
            return true;
        }

        return false;
    }

    private static boolean anyMatch(SHARE_MEDIA target, SHARE_MEDIA... others) {
        for (SHARE_MEDIA other : others) {
            if (other == target) {
                return true;
            }
        }

        return false;
    }

    private static void transformToUMImage(String imageUrl, SHARE_MEDIA media, Action2<Boolean, UMImage> finishCallback) {
        Context ctx = MyApplication.SHARE_INSTANCE;
        boolean isLocalImageOnly = false;
        boolean isRemoteImageOnly = anyMatch(media, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE);

        if (imageUrl.equals("")) {
            if (isRemoteImageOnly) {
                File file = new File(ctx.getCacheDir(), "tmp_img.jpg");
                Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_launcher);
                FileExtension.writeDataToFile(file, bitmap, true, Bitmap.CompressFormat.PNG, 100);
                UploadFileUtil.uploadFile(CommonManager.ChatImg, file, null, (isSuccess, remoteImageUrl) -> {
                    if (isSuccess) {
                        finishCallback.call(true, new UMImage(ctx, remoteImageUrl));
                    } else {
                        transformToUMImage("", media, finishCallback);
                    }
                });
            } else {
                finishCallback.call(true, new UMImage(ctx, R.mipmap.ic_launcher));
            }
        } else {
            Uri uri = Uri.parse(Optional.of(imageUrl).or(""));
            boolean isLocalFile = uri.getScheme().equalsIgnoreCase("file");
            if (isLocalFile) {
                if (!isRemoteImageOnly) {
                    finishCallback.call(true, new UMImage(ctx, new File(uri.getPath())));
                } else {
                    UploadFileUtil.uploadFile(CommonManager.ChatImg, new File(uri.getPath()), null, (isSuccess, remoteImageUrl) -> {
                        if (isSuccess) {
                            finishCallback.call(true, new UMImage(ctx, remoteImageUrl));
                        } else {
                            transformToUMImage("", media, finishCallback);
                        }
                    });
                }
            } else {
                if (isLocalImageOnly) {
                    File file = new File(ctx.getCacheDir(), "tmp_img.jpg");
                    Observable.empty()
                            .observeOn(Schedulers.io())
                            .doOnCompleted(() -> {
                                DownloadUtil.DownloadRequest request = DownloadUtil.createDownloadRequest(imageUrl, file, Optional.empty());
                                request.setCompleteListener(isSuccess -> {
                                    if (isSuccess) {
                                        finishCallback.call(true, new UMImage(ctx, file));
                                    } else {
                                        transformToUMImage("", media, finishCallback);
                                    }
                                });
                                request.execute();
                            })
                            .subscribe();
                } else {
                    finishCallback.call(true, new UMImage(ctx, imageUrl));
                }
            }
        }
    }

    public static void stat_enter_register_or_login_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600001", singletonMap("拉起注册登录界面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_login_success_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("登录", is_success ? "登录成功" : "登录失败");
        onEvent(ctx, "010600002", map);
    }

    public static void stat_register_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("注册", is_success ? "注册成功" : "注册失败");
        onEvent(ctx, "010600003", map);

        if (is_success) {
            onEvent(ctx, "zhuce_count", map);
        }
    }

    public static void stat_manual_inviter_register_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("手动输入邀请人注册", is_success ? "注册成功" : "注册失败");
        onEvent(ctx, "010600004", map);

        if (is_success) {
            onEvent(ctx, "zhuce_count", map);
        }
    }

    public static void stat_auto_inviter_register_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("自动带着邀请人注册", is_success ? "注册成功" : "注册失败");
        onEvent(ctx, "010600005", map);

        if (is_success) {
            onEvent(ctx, "zhuce_count", map);
        }
    }

    public static void stat_no_inviter_register_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("无邀请人注册", is_success ? "注册成功" : "注册失败");
        onEvent(ctx, "010600006", map);

        if (is_success) {
            onEvent(ctx, "zhuce_count", map);
        }
    }

    public static void stat_authentic_name_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600007", singletonMap("拉起实名认证页面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_authentic_name(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("实名认证", is_success ? "认证成功" : "认证失败");
        onEvent(ctx, is_success ? "010600008" : "010600009", map);
        onEvent(ctx, "renzheng_count", map);
    }

    public static void stat_modify_avatar_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("修改头像", is_success ? "修改成功" : "修改失败");
        onEvent(ctx, "010600010", map);
    }

    public static void stat_modify_name_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("修改昵称", is_success ? "修改成功" : "修改失败");
        onEvent(ctx, "010600011", map);
    }

    public static void stat_modify_location_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600012", singletonMap("修改常居住地", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_modify_phone_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("修改手机号码", is_success ? "修改成功" : "修改失败");
        onEvent(ctx, "010600013", map);
    }

    public static void stat_modify_login_pwd_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("修改登录密码", is_success ? "修改成功" : "修改失败");
        onEvent(ctx, "010600014", map);
    }

    public static void stat_set_transaction_pwd_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("设置登录密码", is_success ? "设置成功" : "设置失败");
        onEvent(ctx, "010600015", map);
    }

    public static void stat_modify_transaction_pwd_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("修改交易密码", is_success ? "修改成功" : "修改失败");
        onEvent(ctx, "010600016", map);
    }

    public static void stat_risk_test_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600017", singletonMap("风险测评", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_home_grid1_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600018", singletonMap("点击入口1", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_home_grid2_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600019", singletonMap("点击入口2", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_home_grid3_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600020", singletonMap("点击入口3", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_home_grid4_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600021", singletonMap("点击入口4", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_home_grid5_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600022", singletonMap("点击入口5", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_home_grid6_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600023", singletonMap("点击入口6", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_focus_image_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600024", singletonMap("点击入口6", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_apply_trader(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600025", singletonMap("点击申请操盘手", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_conversation_item(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600026", singletonMap("进入某个消息列表", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_send_image_message(Context ctx) {
        onEvent(ctx, "010600027", "发送图片");
    }

    public static void stat_send_text_message(Context ctx) {
        onEvent(ctx, "010600028", "发送聊天消息");
    }

    public static void stat_send_to_all_image_message(Context ctx) {
        onEvent(ctx, "010600029", "发送群发消息-图片");
    }

    public static void stat_send_to_all_text_message(Context ctx) {
        onEvent(ctx, "010600030", "发送群发消息-文字");
    }

    public static void stat_open_detail_event(Context ctx) {
        onEvent(ctx, "010600031", "点击消息打开下一层详情");
    }

    public static void stat_enter_cash_journal_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600032", singletonMap("进入资金明细页面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_more_setting_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600033", singletonMap("进入更多设置页面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_help_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600034", singletonMap("打开帮助中心", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_feed_back_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600035", singletonMap("打开反馈建议", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_grade_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600036", singletonMap("去评分", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_public_number_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600037", singletonMap("跳转到公众号", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_company_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600038", singletonMap("为什么选择操盘侠", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_user_protocol_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600039", singletonMap("查看用户协议", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_Intrest_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600040", singletonMap("了解余额生息", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_logout(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600041", singletonMap("退出登录", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_award_home_from_mine_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600042", singletonMap("进入佣金页面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_cn_award_detail_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600043", singletonMap("进入人民币佣金详情页面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_hk_award_detail_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600044", singletonMap("进入港币佣金详情页面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_withdraw_cn_award(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("提取人民币佣金", is_success ? "提现成功" : "提现失败");
        onEvent(ctx, "010600045", map);
    }

    public static void stat_withdraw_hk_award(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("提取港币佣金", is_success ? "提现成功" : "提现失败");
        onEvent(ctx, "010600046", map);
    }

    public static void stat_enter_bind_cn_card_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600047", singletonMap("拉起绑定沪深银行卡页面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_cancel_bind_cn_card(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600048", singletonMap("取消绑定沪深银行卡", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_confirm_bind_cn_card(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600049", singletonMap("开始绑定沪深银行卡", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_bind_cn_card_event(Context ctx, boolean is_success, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("绑定银行卡", is_success ? "绑定成功" : "绑定失败");
        onEvent(ctx, is_success ? "010600050" : "010600051", map);

        if (is_success) {
            map.clear();
            map.put("绑定银行卡", is_success ? "绑定成功" : "绑定失败");
            onEvent(ctx, "bangding_count", map);
        }
    }

    public static void stat_recharge_to_cn_account_event(Context ctx) {
        onEvent(ctx, "010600052", singletonMap("点击沪深充值按钮", ""));
    }

    public static void stat_enter_recharge_cn_account_page(Context ctx) {
        onEvent(ctx, "010600053", singletonMap("拉起沪深充值界面", ""));
    }

    public static void stat_once_recharge_to_cn_account(Context ctx, boolean is_success, String bank_name, double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("沪深一次充值", is_success ? "充值成功" : "充值失败");
        onEvent(ctx, "010600054", map);

        if (is_success) {
            onEvent(ctx, "chongzhi_count", map);
            map.clear();
            map.put("银行", bank_name);
            map.put("方式", "快捷支付");
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "chongzhi_value", map);
        }
    }

    public static void stat_continue_recharge_to_cn_account(Context ctx, boolean is_success, String bank_name, double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("沪深连续充值", is_success ? "充值成功" : "充值失败");
        onEvent(ctx, "010600055", map);

        if (is_success) {
            onEvent(ctx, "chongzhi_count", map);
            map.clear();
            map.put("银行", bank_name);
            map.put("方式", "快捷支付");
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "chongzhi_value", map);
        }
    }

    public static void stat_withdraw_from_cn_account_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600056", singletonMap("点击沪深提现按钮", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_withdraw_from_cn_account_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600064", singletonMap("拉起沪深提现界面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_withdraw_from_cn_account(Context ctx, boolean is_success, String bank_name, double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("提现", is_success ? "提现成功" : "提现失败");
        onEvent(ctx, "010600058", map);

        if (is_success) {
            onEvent(ctx, "tixian_count", map);
            map.clear();
            map.put("银行", bank_name);
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "tixian_value", map);
        }
    }

    public static void stat_recharge_to_hk_account_event(Context ctx) {
        onEvent(ctx, "010600059", singletonMap("点击港股充值按钮", ""));
    }

    public static void stat_enter_recharge_hk_account_page(Context ctx) {
        onEvent(ctx, "010600060", singletonMap("拉起港股充值界面", ""));
    }

    public static void stat_recharge_to_hk_account(Context ctx, boolean is_success, String bank_name, double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("港股充值", is_success ? "充值成功" : "充值失败");
        onEvent(ctx, "010600061", map);

        if (is_success) {
            onEvent(ctx, "chongzhi_count", map);
            map.clear();
            map.put("银行", bank_name);
            map.put("方式", "快捷支付");
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "chongzhi_value", map);
        }
    }

    public static void stat_withdraw_from_hk_account_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600062", singletonMap("点击港股提现按钮", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_withdraw_from_hk_account_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600063", singletonMap("拉起港股提现界面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_withdraw_from_hk_account(Context ctx, boolean is_success, String bank_name, double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, is_success, serverCode, serverMsg);
        map.put("提现", is_success ? "提现成功" : "提现失败");
        onEvent(ctx, "010600064", map);

        if (is_success) {
            map.clear();
            map.put("银行", bank_name);
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "tixian_value", map);
        }
    }

    public static void stat_enter_invest_fund_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600065", singletonMap("拉起投资界面", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_invest_fund_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600066", singletonMap("点击投资界面的立即投资按钮", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_fund_protocol_agree_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600067", singletonMap("协议界面选择同意", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_fund_protocol_cancel_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600068", singletonMap("协议界面选择取消", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_once_invest_fund_event(Context ctx, boolean is_success, int money_type, String fund_name, Double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, true, serverCode, serverMsg);
        map.put("一次投资", is_success ? "投资成功" : "投资失败");
        onEvent(ctx, "010600069", map);

        if (is_success) {
            onEvent(ctx, "touzi_count", map);
            map.clear();
            map.put("组合", fund_name);
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "touzi_value", map);
        }
    }

    public static void stat_continue_invest_fund_event(Context ctx, boolean is_success, int money_type, String fund_name, Double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, true, serverCode, serverMsg);
        map.put("连续投资", is_success ? "投资成功" : "投资失败");
        onEvent(ctx, "010600070", map);

        if (is_success) {
            onEvent(ctx, "touzi_count", map);
            map.clear();
            map.put("组合", fund_name);
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "touzi_value", map);
        }
    }

    public static void stat_continue_part_invest_fund_event(Context ctx, boolean is_success, FundBrief.Money_Type money_type, String fund_name, Double amount, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, true, serverCode, serverMsg);
        map.put("连续投资部分", is_success ? "投资成功" : "投资失败");
        onEvent(ctx, "010600071", map);

        if (is_success) {
            onEvent(ctx, "touzi_count", map);
            map.clear();
            map.put("组合", fund_name);
            map.put("金额", format_amount(amount, 2));
            onEvent(ctx, "touzi_value", map);
        }
    }

    public static void stat_enter_share_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600072", singletonMap("拉起分享窗口", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_share_from_web_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600073", singletonMap("拉起分享窗口--来自网页", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_share_from_fund_detail_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600074", singletonMap("拉起分享窗口--来自组合详情页", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_share_from_award_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600075", singletonMap("拉起分享窗口--来自佣金", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_share_to_wechat_friend(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600076", singletonMap("分享到微信好友", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_share_to_wechat_circle(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600077", singletonMap("分享到微信朋友圈", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_share_to_qq(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600078", singletonMap("分享到QQ", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_share_to_sms(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600079", singletonMap("分享到短信", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_share_to_qzone(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600080", singletonMap("分享到QQ空间", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_share_to_sina_large(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600081", singletonMap("分享到微博", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_share_to_copy_link(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600082", singletonMap("点击拷贝链接", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_fund_detail_from_invest_home_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600083", singletonMap("进入组合详情页", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_look_fund_description_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600084", singletonMap("点击查看组合介绍", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_look_fund_introduce_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600085", singletonMap("点击查看完整信息", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_look_fund_introduce_info_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600086", singletonMap("点击查看要素说明", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_look_invested_member_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600087", singletonMap("点击查看已投资成员", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_look_common_question_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600088", singletonMap("点击查看常见问题", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_look_fund_trade_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600089", singletonMap("点击查看持仓详情", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_to_book_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600090", singletonMap("点击预约", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_to_share_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600091", singletonMap("点击分享", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_to_invest_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "01060092", singletonMap("点击投资", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_trader_detail_from_trader_home_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600093", singletonMap("进入详情页", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_look_interview_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600094", singletonMap("查看专访", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_launch_app_from_manual(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600095", singletonMap("手动点击启动APP", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_launch_app_from_push(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600096", singletonMap("从PUSH启动APP", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_launch_app_from_url(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600097", singletonMap("从URL跳转启动APP", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_show_income_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010600098", singletonMap("点击晒收益", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_award_home_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "award_count", singletonMap("进入佣金主页，首页_操盘页_消息中心_PUSH", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_trader_detail_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "geren_count", singletonMap("进入个人详情页，首页_操盘页_消息中心_PUSH_其他", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_enter_fund_detail_page(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "zhuhe_count", singletonMap("进入组合详情页，首页_操盘页_资产_消息中心_PUSH_其他", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_add_bonus_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010700001", singletonMap("邀请加息按钮点击统计", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_fund_profile_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010700002", singletonMap("组合详情页概览", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_regist_guide_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010700003", singletonMap("红包按钮的统计", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_fund_detail_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010700004", singletonMap("组合详情页信息", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_invested_member_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010700005", singletonMap("组合详情页成员", get_ui_controller_name(ctx, fragment)));
    }

    public static void stat_fund_trade_event(Context ctx, Optional<Fragment> fragment) {
        onEvent(ctx, "010700006", singletonMap("组合详情页业绩", get_ui_controller_name(ctx, fragment)));
    }

    public static int EVENTID(int x, int y) {
        return (1000 * x + y);
    }

    //以下为2.0版本新添加
    public static int eEVENTIDWXLoginClick = EVENTID(20000, 1);        //【登录注册】点击微信登录
    public static int eEVENTIDWxLoginSuc = EVENTID(20000, 2);        //【登录注册】点击微信登录成功返回
    public static int eEVENTIDWxLoginReg = EVENTID(20000, 3);        //【登录注册】点击微信登录后成功注册
    public static int eEVENTIDXiabiEnter = EVENTID(20000, 4);        //【积分】进入积分
    public static int eEVENTIDXiabiGain = EVENTID(20000, 5);        //【积分】点击领取积分
    public static int eEVENTIDXiaBiToFinish = EVENTID(20000, 6);        //【积分】点击去完成任务
    public static int eEVENTIDXiabiIntro = EVENTID(20000, 7);        //【积分】点击什么是积分
    public static int eEVENTIDStockPostionHose = EVENTID(20000, 8);        //【模拟炒股】进入模拟炒股--主人态
    public static int eEVENTIDStockPostionGuest = EVENTID(20000, 9);        //【模拟炒股】进入模拟炒股--客人态
    public static int eEVENTIDStockBuyPriceLimit = EVENTID(20000, 10);        //【模拟炒股】买入股票--限价买入
    public static int eEVENTIDStockBuyPriceMarket = EVENTID(20000, 11);        //【模拟炒股】买入股票--市价买入
    public static int eEVENTIDStockSellPriceLimit = EVENTID(20000, 12);        //【模拟炒股】交易--限价卖出
    public static int eEVENTIDStockSellPriceMarket = EVENTID(20000, 13);        //【模拟炒股】交易-市价卖出
    public static int eEVENTIDStockBuyPostionClick = EVENTID(20000, 14);        //【模拟炒股】交易--点击持仓填写股票
    public static int eEVENTIDStockBuyFivePrice = EVENTID(20000, 15);        //【模拟炒股】交易--点击5档填写价格
    public static int eEVENTIDStockCancelClick = EVENTID(20000, 16);        //【模拟炒股】撤单--点击撤单
    public static int eEVENTIDStockCancelSuc = EVENTID(20000, 17);        //【模拟炒股】撤单--撤单成功
    public static int eEVENTIDStockIncomeNow = EVENTID(20000, 18);        //【模拟炒股】进入盈亏分析（当前持仓）
    public static int eEVENTIDStockIncomeHistory = EVENTID(20000, 19);        //【模拟炒股】进入盈亏分析（历史持仓）
    public static int eEVENTIDHomePageHost = EVENTID(20000, 20);        //【个人主页】进入个人主页--主人态
    public static int eEVENTIDHomePageGuest = EVENTID(20000, 21);        //【个人主页】进入个人主页--客人态
    public static int eEVENTIDHomePageTrader = EVENTID(20000, 22);        //【个人主页】进入个人主页--操盘手页面
    public static int eEVENTIDHomePageAccMore = EVENTID(20000, 23);        //【个人主页】点击查看帐户详情
    public static int eEVENTIDHomePageTraderMore = EVENTID(20000, 24);        //【个人主页】点击查看交易详情
    public static int eEVENTIDHomePageInfoEdit = EVENTID(20000, 25);        //【个人主页】点击编辑资料
    public static int eEVENTIDMineBindWx = EVENTID(20000, 26);        //【我】点击绑定微信
    public static int eEVENTIDMineUnBindWx = EVENTID(20000, 27);        //【我】点击取消绑定微信
    public static int eEVENTIDMineAddressEnter = EVENTID(20000, 28);        //【我】点击管理收货地址
    public static int eEVENTIDMineAddressSet = EVENTID(20000, 29);        //【我】成功设置收货地址
    public static int eEVENTIDTouziIntroPic = EVENTID(20000, 30);        //【投资】下拉查看投资说明图
    public static int eEVENTIDTouziAllFundEnter = EVENTID(20000, 31);        //【投资】进入全部组合
    public static int eEVENTIDTouziAllTraderEnter = EVENTID(20000, 32);        //【投资】进入全部操盘手
    public static int eEVENTIDClickSearch = EVENTID(20000, 33);        //【股票】点击搜索按钮
    public static int eEVENTIDClickAddOptional = EVENTID(20000, 34);        //【股票】搜索结果列表点击添加自选
    public static int eEVENTIDSelectHZSpec = EVENTID(20000, 35);        //【股票】查看沪深指数方块
    public static int eEVENTIDSelectStockLive = EVENTID(20000, 36);        //【股票】查看股市直播
    public static int eEVENTIDChangeAllHolds = EVENTID(20000, 37);        //【股票】切换全部-持有
    public static int eEVENTIDChangeStockChange = EVENTID(20000, 38);        //【股票】切换涨跌幅/额/走势
    public static int eEVENTIDRankingList = EVENTID(20000, 39);        //【股票】进入榜单
    public static int eEVENTIDStockGame = EVENTID(20000, 40);        //【股票】进入大赛
    public static int eEVENTIDTopStockProfitLoss = EVENTID(20000, 41);        //【股票】动态--进入盈亏分析
    public static int eEVENTIDTopStockDetail = EVENTID(20000, 42);        //【股票】动态--进入股票详情
    public static int eEVENTIDTopHomePage = EVENTID(20000, 43);        //【股票】动态--进入个人主页
    public static int eEVENTIDStockGameSignUp = EVENTID(20000, 44);        //【炒股大赛】报名参加大赛
    public static int eEVENTIDStockGameSignShare = EVENTID(20000, 45);        //【炒股大赛】报名阶段分享
    public static int eEVENTIDStockGameIngShare = EVENTID(20000, 46);        //【炒股大赛】进行阶段分享
    public static int eEVENTIDStockGameEndShare = EVENTID(20000, 47);        //【炒股大赛】结束阶段分享
    public static int eEVENTIDOpenStockDetail = EVENTID(20000, 48);        //【股票详情页】打开股票详情页
    public static int eEVENTIDAddOptional = EVENTID(20000, 49);        //【股票详情页】添加自选
    public static int eEVENTIDDeleteOptional = EVENTID(20000, 50);        //【股票详情页】取消自选
    public static int eEVENTIDEnterFullScreen = EVENTID(20000, 51);        //【股票详情页】进入横屏模式
    public static int eEVENTIDStockFiveDay = EVENTID(20000, 52);        //【股票详情页】切换到五日
    public static int eEVENTIDStockDay = EVENTID(20000, 53);        //【股票详情页】切换到日K
    public static int eEVENTIDStockWeek = EVENTID(20000, 54);        //【股票详情页】切换到周K
    public static int eEVENTIDStockMonth = EVENTID(20000, 55);        //【股票详情页】切换到月K
    public static int eEVENTIDStockTimeLine = EVENTID(20000, 56);        //【股票详情页】切换到分时
    public static int eEVENTIDStockBidClick = EVENTID(20000, 57);        //【股票详情页】点击买入按钮
    public static int eEVENTIDStockAskClick = EVENTID(20000, 58);        //【股票详情页】点击卖出按钮
    public static int eEVENTIDSimpleLongTouch = EVENTID(20000, 59);        //【股票详情页】长按查看详情
    public static int eEVENTIDTopicListEnter = EVENTID(20200, 1);        //【话题】进入一个话题
    public static int eEVENTIDTopicListWriteMood = EVENTID(20200, 2);        //【话题】点击编辑贴子按钮
    public static int eEVENTIDTopicListWriteIntelligence = EVENTID(20200, 3);        //【话题】点击编辑情报按钮
    public static int eEVENTIDTopicListHint = EVENTID(20200, 4);        //【话题】点击提醒按钮
    public static int eEVENTIDTopicListReward = EVENTID(20200, 5);        //【话题】点击打赏按钮
    public static int eEVENTIDTopicListViewPhoto = EVENTID(20200, 6);        //【话题】点击图片预览
    public static int eEVENTIDTopicListWeb = EVENTID(20200, 7);        //【话题】点击跳转到网页
    public static int eEVENTIDTopicListAttachment = EVENTID(20200, 8);        //【话题】点击跳转到其他类型
    public static int eEVENTIDNewTopicSuccessMood = EVENTID(20200, 9);        //【话题编辑页】成功发布一个贴子
    public static int eEVENTIDNewTopicSuccessIntelligence = EVENTID(20200, 10);        //【话题编辑页】成功发布一个情报
    public static int eEVENTIDNewTopicShareConfirm = EVENTID(20200, 11);        //【话题编辑页】发布后分享弹窗点击分享
    public static int eEVENTIDNewTopicShareCancel = EVENTID(20200, 12);        //【话题编辑页】发布后分享弹窗点击取消
    public static int eEVENTIDTopicDetailEnter = EVENTID(20200, 13);        //【话题详情页】打开一个话题详情页
    public static int eEVENTIDTopicDetailEnterFromHint = EVENTID(20200, 14);        //【话题详情页】从消息提醒中打开
    public static int eEVENTIDTopicDetailEnterFromTopicList = EVENTID(20200, 15);        //【话题详情页】从话题中打开
    public static int eEVENTIDTopicDetailEnterFromUserHome = EVENTID(20200, 16);        //【话题详情页】从个人动态中打开
    public static int eEVENTIDTopicDetailEnterFromWeb = EVENTID(20200, 17);        //【话题详情页】从外部跳转打开
    public static int eEVENTIDTopicDetailReward = EVENTID(20200, 18);        //【话题详情页】打赏
    public static int eEVENTIDTopicDetailDelete = EVENTID(20200, 19);        //【话题详情页】删除
    public static int eEVENTIDTopicDetailReportTopic = EVENTID(20200, 20);        //【话题详情页】举报
    public static int eEVENTIDTopicDetailShare = EVENTID(20200, 21);        //【话题详情页】分享
    public static int eEVENTIDTopicDetailComment = EVENTID(20200, 22);        //【话题详情页】评论
    public static int eEVENTIDTopicDetailReplyComment = EVENTID(20200, 23);        //【话题详情页】评论回复
    public static int eEVENTIDTopicDetailReportComment = EVENTID(20200, 24);        //【话题详情页】评论举报
    public static int eEVENTIDTopicDetailUnlock = EVENTID(20200, 25);        //【话题详情页】支付解锁
    public static int eEVENTIDTopicDetailLike = EVENTID(20200, 26);        //【话题详情页】选择超值
    public static int eEVENTIDTopicDetailDislike = EVENTID(20200, 27);        //【话题详情页】选择坑爹
    public static int eEVENTIDUserHomeSetAvatarAlert = EVENTID(20200, 27);        //【个人中心】弹出头像设置提示框
    public static int eEVENTIDUserHomeAlertConfirm = EVENTID(20200, 27);        //【个人中心】头像提示框选择立即设置
    public static int eEVENTIDUserHomeAlertCancel = EVENTID(20200, 27);        //【个人中心】头像提示框选择取消
    public static int eEVENTIDUserHomeAlertChooseWX = EVENTID(20200, 27);        //【个人中心】编辑头像选择“使用我的微信头像”

    // 以下为2.4版本新添加
    public static int eEvENTIDInvestExchangeCoupons = EVENTID(20400, 2);        //【我】兑换投资红包
    public static int eEvENTIDInvestUseCoupons = EVENTID(20400, 3);        //【投资】使用投资红包
    public static int eEvENTIDInvestInvite = EVENTID(20400, 4);        //【投资】完成后邀请好友
    public static int eEvENTIDTopicDetailVolumeReward = EVENTID(20400, 5);        //【话题详情页】批量打赏
    public static int eEvENTIDHomeClickBanner1 = EVENTID(20400, 6);        //【首页】点击焦点图1
    public static int eEvENTIDHomeClickBanner2 = EVENTID(20400, 7);        //【首页】点击焦点图2
    public static int eEvENTIDHomeClickBanner3 = EVENTID(20400, 8);        //【首页】点击焦点图3
    public static int eEvENTIDHomeClickBanner4 = EVENTID(20400, 9);        //【首页】点击焦点图4
    public static int eEvENTIDHomeClickBanner5 = EVENTID(20400, 10);        //【首页】点击焦点图5
    public static int eEvENTIDHomeClickBanner6 = EVENTID(20400, 11);        //【首页】点击焦点图6
    public static int eEvENTIDHomeClickBanner7 = EVENTID(20400, 12);        //【首页】点击焦点图7
    public static int eEvENTIDHomeClickBanner8 = EVENTID(20400, 13);        //【首页】点击焦点图8
    public static int eEvENTIDHomeClickBanner9 = EVENTID(20400, 14);        //【首页】点击焦点图9
    public static int eEvENTIDHomeClickBanner10 = EVENTID(20400, 15);        //【首页】点击焦点图10
    public static int eEvENTIDTopicDetailEnter1 = EVENTID(20400, 16);        //【社区】点击话题入口1
    public static int eEvENTIDTopicDetailEnter2 = EVENTID(20400, 17);        //【社区】点击话题入口2
    public static int eEvENTIDTopicDetailEnter3 = EVENTID(20400, 18);        //【社区】点击话题入口3
    public static int eEvENTIDTopicDetailEnter4 = EVENTID(20400, 19);        //【社区】点击话题入口4
    public static int eEvENTIDTopicDetailEnter5 = EVENTID(20400, 20);        //【社区】点击话题入口5
    public static int eEvENTIDTopicDetailEnter6 = EVENTID(20400, 21);        //【社区】点击话题入口6
    public static int eEvENTIDTopicDetailEnter7 = EVENTID(20400, 22);        //【社区】点击话题入口7
    public static int eEvENTIDTopicDetailEnter8 = EVENTID(20400, 23);        //【社区】点击话题入口8
    public static int eEvENTIDTopicDetailEnter9 = EVENTID(20400, 24);        //【社区】点击话题入口9
    public static int eEvENTIDTopicDetailEnter10 = EVENTID(20400, 25);        //【社区】点击话题入口10


    /**
     * 通用点击事件
     *
     * @param eventID
     */
    public static void stat_click_event(int eventID) {
        String id = String.format("%09d", eventID);
        onEvent(MyApplication.SHARE_INSTANCE, id);
    }

    /**
     * 通用点击事件
     *
     * @param eventID
     */
    public static void stat_click_event(int eventID, HashMap<String, String> params) {
        if (params == null) {
            stat_click_event(eventID);
        } else {
            onEvent(MyApplication.SHARE_INSTANCE, String.format("%09d", eventID), params);
        }
    }

    /**
     * cgi错误上报
     */
    public static void stat_cgi_err(Context ctx, String cgiUrl, String user, Optional<Integer> serverCode, Optional<String> serverMsg) {
        HashMap<String, String> map = new HashMap<>();
        appendServerInfoToMap(map, false, serverCode, serverMsg);
        map.put("cgierr", "失败");
        map.put("cgi", cgiUrl);
        map.put("user", user);
        map.put("__ct__", "1");
        onEvent(ctx, "cgierr", map);
    }

    private static String format_amount(double amount, int scale) {
        return new BigDecimal(amount).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private static String get_ui_controller_name(Context ctx, Optional<Fragment> fragment_holder) {
        Fragment fragment = fragment_holder.orNull();
        Activity activity;
        if (ctx instanceof Activity) {
            activity = (Activity) ctx;
        } else {
            activity = fragment != null ? fragment.getActivity() : null;
        }
        if (activity != null) {
            if (activity instanceof MainActivityV2) {
                if (activity.getIntent().getData() != null)
                    return "PUSH";
                else if (fragment instanceof MainFragments.InvestHomeFragment)
                    return "投资首页";
                else if (fragment instanceof MainFragments.ADHomeFragment)
                    return "首页";
            } else if (activity instanceof BrowsableActivity) {
                if (activity.getIntent().getData() != null)
                    return "PUSH";
            }
        }
        return "其它";
    }

    private static void appendServerInfoToMap(HashMap<String, String> map, boolean isSuccess, Optional<Integer> serverCode, Optional<String> serverMsg) {
        if (isSuccess) {
            map.put("server_code", serverCode.or(0).toString());
            map.put("server_msg", "success");
        } else {
            map.put("server_code", serverCode.or(-1).toString());
            map.put("server_msg", serverMsg.or("未知错误"));
        }
    }
}
