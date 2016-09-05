package com.goldmf.GMFund.controller;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.CommonPrefProxy;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.CommonController;
import com.goldmf.GMFund.controller.dialog.DownloadDialog;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.dialog.RequestPermissionDialog;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.UpdateInfo;
import com.goldmf.GMFund.model.LoadingInfo;
import com.goldmf.GMFund.util.AppUtil;
import com.goldmf.GMFund.util.DownloadUtil;
import com.goldmf.GMFund.util.PersistentObjectUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.SplashCalendarView;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.JPushInterface;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.RunTimePermissionChecker;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FunctionIntroductionPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MainPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.BitmapExtension.getCacheImage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static yale.extension.system.RunTimePermissionChecker.getDeniedPermissionsImpl;

/**
 * Created by yale on 15/7/20.
 */
public class SplashActivity extends BaseActivity {
    private RunTimePermissionChecker.RequestPermissionResultHandler mHandler;

    private static ArrayList<String> strArray = new ArrayList<>();

    static {
        strArray.add("你今天聪明投资了吗");
        strArray.add("参加模拟炒股大赛\r\n立马大赚真钱");
        strArray.add("账户余额、已投资未运行资金每天收益\r\n不用等到最后一天再投资");
        strArray.add("操盘侠的账户资金安全\r\n由中国人保保驾护航");
        strArray.add("在这里遇到你\r\n真高兴");
        strArray.add("股票牛人圈\r\n跟对牛人，炒对股");
        strArray.add("真正高明的人\r\n就是能够借重别人的智慧\r\n来使自己不受蒙蔽的人\r\n--苏格拉底");
        strArray.add("买卖点比买卖什么股票更重要");
        strArray.add("风险来自于你不知道自己在干什么\r\n--沃沦・巴菲特");
        strArray.add("操盘乐\r\n雇个牛人，为你操盘");
        strArray.add("分红乐\r\n本金收益保障，更有浮动分红");
        strArray.add("盈多点，赢多点\r\n买涨买跌都能赚");
        strArray.add("公益乐\r\n赚钱的时候，不要忘了奉献爱心");
        strArray.add("一个好友多个帮\r\n邀请好友为你的分红乐加息");
        strArray.add("投资资金会进入证券会监管的托管帐户\r\n由券商进行独立的第三方监管");
        strArray.add("安全、便捷、透明\r\n满足不同需求和风险偏好的产品组合");
        strArray.add("每天签到赚积分\r\n商城好礼等你来兑");
        strArray.add("牛B的操盘手都在这里");
        strArray.add("只有穿越牛熊的操盘手才经得起考验");
        strArray.add("邀请好友投资，佣金马上到手");
        strArray.add("理解、尊重风险\r\n这就是顶尖操盘手标志\r\n--操盘侠明星操盘手・趋势信徒");
        strArray.add("独乐乐不如众乐乐\r\n邀请好友投资，佣金马上到手");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fresco.getImagePipeline().clearMemoryCaches();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_splash);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        LoadingInfo loadingInfo = CommonManager.getInstance().getLoadingAnimate();
        v_setVisibility(findViewById(R.id.section_normal), loadingInfo == null ? View.VISIBLE : View.GONE);
        v_setVisibility(findViewById(R.id.section_promotion), loadingInfo == null ? View.GONE : View.VISIBLE);
        if (loadingInfo == null) {
            TextView versionLabel = v_findView(this, R.id.text_copyright);
            String versionText = versionLabel.getText().toString().replace("-version-", AppUtil.getVersionName(this));
            versionLabel.setText(versionText);

            SplashCalendarView calendar = v_findView(this, R.id.splash_calendar);
            Random random = new Random();
            int index = random.nextInt(strArray.size());
            calendar.setInfos(strArray.get(index));

            //动画
            calendar.setAlpha(0f);
            ObjectAnimator anim = ObjectAnimator.ofFloat(calendar, "alpha", 0f, 1f);
            anim.setDuration(1200);
            anim.start();

            setStatusBarBackgroundColor(this, WHITE_COLOR);
            MyApplication.SHARE_INSTANCE.onEnterSplashActivity();
            if (needToCheckPermission()) {
                PersistentObjectUtil.writeHasRequestPermissionsBefore(true);
                String[] deniedPermissions = getDeniedPermissionsImpl(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, ACCESS_FINE_LOCATION});
                if (deniedPermissions.length > 0) {
                    new RequestPermissionDialog(this, deniedPermissions) {
                        @Override
                        protected void onNextButtonClick(Dialog dialog, View button) {
                            dialog.dismiss();
                            performCheckPermission(deniedPermissions);
                        }
                    }.show();
                } else {
                    goToNextAct();
                }
            } else {
                goToNextAct();
            }
        } else {
            View promotionSection = v_findView(this, R.id.section_promotion);
            v_setImageUri(promotionSection, R.id.img_promotion, loadingInfo.imageUrl);

            TextView versionLabel = v_findView(promotionSection, R.id.text_copyright2);
            String versionText = versionLabel.getText().toString().replace("-version-", AppUtil.getVersionName(this));
            versionLabel.setText(versionText);

            TextView timerLabel = v_findView(promotionSection, R.id.label_timer);
            timerLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0x4D000000, dp2px(18))));

            Handler handler = new Handler();

            int[] remainSecond = {loadingInfo.cotinueSecond};
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (remainSecond[0] <= 0) {
                        goToNextAct();
                    } else {
                        timerLabel.setText("跳过\n" + remainSecond[0]-- + "s");
                        handler.postDelayed(this, 1000L);
                    }
                }
            };
            runnable.run();

            v_setClick(promotionSection, R.id.img_promotion, v -> {
                handler.removeCallbacks(runnable);
                if (!TextUtils.isEmpty(loadingInfo.tarLink)) {
                    getIntent().setData(Uri.parse("gmf://www.caopanman.com?" + loadingInfo.tarLink));
                    goToNextAct();
                }
            });
            v_setClick(timerLabel, v -> {
                handler.removeCallbacks(runnable);
                goToNextAct();
            });
        }
        performLaunchCount();

    }

    private void performCheckPermission(String[] permissions) {
        mHandler = RunTimePermissionChecker.requestPermissions(SplashActivity.this, permissions, (trigger, result) -> {
            if (result.isAllGranted || !result.isFirstTimeRequest) {
                goToNextAct();
            } else {
                String[] deniedPermissions = result.getDeniedPermissions(SplashActivity.this);
                createPermissionInfoDialog(deniedPermissions, trigger).show();
            }
        });
    }

    private Dialog createPermissionInfoDialog(String[] deniedPermissions, RunTimePermissionChecker.Trigger trigger) {
        List<String> descList = Stream.of(deniedPermissions)
                .map(it -> getPermissionDesc(it))
                .filter(it -> !TextUtils.isEmpty(it))
                .collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        for (String desc : descList) {
            builder.append("\n").append(desc);
        }
        String content = builder.toString().replaceFirst("\n", "");
        return new GMFDialog.Builder(this)
                .setTitle("获取权限失败")
                .setMessage(content)
                .setPositiveButton("重试", (dialog, which) -> {
                    dialog.dismiss();
                    trigger.requestAgain();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    goToNextAct();
                })
                .setCancelable(false)
                .create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mHandler != null)
            mHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private String getPermissionDesc(String permission) {
        switch (permission) {
            case WRITE_EXTERNAL_STORAGE:
                return "需要获取存储空间，以加快响应速度；";
            case READ_PHONE_STATE:
                return "需要获取设备信息，以保证投资信息推送；";
            case ACCESS_FINE_LOCATION:
                return "需要获取地理位置，以提升用户体验。";
            default:
                return "";
        }
    }

    private boolean needToCheckPermission() {
        if (BuildConfig.DEBUG) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        }

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PersistentObjectUtil.readHasRequestPermissionsBefore();
    }

    private boolean mGoingToNextAct = false;

    private void goToNextAct() {
        if (mGoingToNextAct) {
            return;
        }
        mGoingToNextAct = true;
        runOnUIThreadDelayed(() -> {
            MyApplication.SHARE_INSTANCE.mHasLaunchSplash = true;

            int lastLaunchVersionCode = CommonPrefProxy.getLastLaunchVersionVode();
            if (lastLaunchVersionCode < AppUtil.getVersionCode(this)) {
                showActivity(this, an_FunctionIntroductionPage(true));
            } else {
                showActivity(this, an_MainPage(getIntent().getData()));
            }
            overridePendingTransition(0, 0);
            CommonPrefProxy.updateLastLaunchVersionCode();
            performCheckUpdate();

            finish();
            mGoingToNextAct = false;
        }, 1500L);
    }

    private void performLaunchCount() {
        String source = getIntent().getStringExtra("source");
        if (source == null) {
            UmengUtil.stat_launch_app_from_manual(this, Optional.of(null));
        } else if (source.equals("source")) {
            UmengUtil.stat_launch_app_from_push(this, Optional.of(null));
        } else {
            UmengUtil.stat_launch_app_from_url(this, Optional.of(null));
        }

    }

    private static void performCheckUpdate() {
        CommonController.checkUpdate()
                .delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (isSuccess(data) && data.data != null) {
                        UpdateInfo updateInfo = data.data;

                        if (updateInfo.showAlert || updateInfo.needForceUpdate) {
                            safeCall(() -> {
                                opt(createUpdateDialog(updateInfo)).consume(it -> it.show());
                            });
                        }
                    }
                });
    }

    private static GMFDialog createUpdateDialog(UpdateInfo updateInfo) {
        Context context = MyApplication.SHARE_INSTANCE;
        File savePath = new File(context.getCacheDir().getAbsoluteFile() + File.separator + "update.apk");
        boolean isNeedToDownload = DownloadUtil.isNeedToDownload(savePath, updateInfo.md5);
        if (MyApplication.hasTopActivity()) {
            GMFDialog.Builder builder = new GMFDialog.Builder(MyApplication.getTopActivityOrNil().get());
            builder.setCancelable(false);
            builder.setTitle(updateInfo.updateTitle);
            builder.setMessage(updateInfo.updateMsg);
            builder.setPositiveButton(isNeedToDownload ? "立即更新" : "免流量更新", (dialog, which) -> {
                dialog.dismiss();
                showDownloadDialog(updateInfo, savePath);
            });
            if (!updateInfo.needForceUpdate) {
                builder.setNegativeButton("下次", (dialog, which) -> {
                    dialog.dismiss();
                    CommonManager.getInstance().delayUpdateAlert();
                    DownloadDialog.downloadOnBackground(updateInfo.url, savePath, updateInfo.md5, true);
                });
            }
            return builder.create();
        } else {
            return null;
        }
    }

    private static void showDownloadDialog(UpdateInfo updateInfo, File savePath) {
        if (MyApplication.hasTopActivity()) {
            DownloadDialog dialog = new DownloadDialog(MyApplication.getTopActivityOrNil().get(), updateInfo.url, savePath, Optional.of(updateInfo.md5));
            dialog.setCancelable(false);
            dialog.setFinishDownloadListener((d, isSuccess, file) -> {
                d.dismiss();
                if (isSuccess) {
                    file.setReadable(true, false);
                    AppUtil.installApk(MyApplication.SHARE_INSTANCE, file);
                    if (updateInfo.needForceUpdate) {
                        Process.killProcess(Process.myPid());
                    }
                } else {
                    if (MyApplication.hasTopActivity()) {
                        if (updateInfo.needForceUpdate) {
                            Activity topActivity = MyApplication.getTopActivityOrNil().get();
                            if (topActivity instanceof BaseActivity) {
                                createErrorDialog((BaseActivity) topActivity, "下载失败").show();
                            }
                        } else {
                            showToast(MyApplication.getTopActivityOrNil().get(), "下载失败");
                        }
                    }
                }
            });
            dialog.show();
            dialog.startDownload();
        }
    }


}
