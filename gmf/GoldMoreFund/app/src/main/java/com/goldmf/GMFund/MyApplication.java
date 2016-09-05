package com.goldmf.GMFund;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Process;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.goldmf.GMFund.controller.BaseActivity;
import com.goldmf.GMFund.controller.BaseFragment;
import com.goldmf.GMFund.controller.FragmentStackActivity;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;
import com.goldmf.GMFund.util.JPushUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.manager.dev.AccessLogManager;
import com.goldmf.GMFund.manager.dev.AccessLoggerWriter;
import com.goldmf.GMFund.util.FileUtil;
import com.orhanobut.logger.AndroidLogTool;
import com.orhanobut.logger.LogTool;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Stack;

import io.paperdb.Paper;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.manager.dev.AccessLogManager.generateDefaultLogFileName;
import static com.goldmf.GMFund.manager.dev.AccessLogManager.getDefaultLogFileParentDir;

/**
 * Created by yale on 15/7/20.
 */
public class MyApplication extends MultiDexApplication {

    public static MyApplication SHARE_INSTANCE = null;
    public boolean mHasLaunchSplash = false;
    public boolean mLoginPageShowing = false;
    private WeakReference<Activity> mTopActivityOrNil = null;
    private WeakReference<Dialog> mTopDialogOrNil = null;
    public Handler mHandler = new Handler();
    public boolean mHasEnterSplashPageBefore = false;
    public boolean mHasRequestFreshCommon = false;
    private AccessLoggerWriter mAccessLoggerWriter = new AccessLoggerWriter(null);

    public static void setTopActivity(Activity activity) {
        MyApplication.SHARE_INSTANCE.mTopActivityOrNil = activity == null ? null : new WeakReference<>(activity);
    }

    public static WeakReference<Activity> getTopActivityOrNil() {
        return opt(SHARE_INSTANCE).let(it -> it.mTopActivityOrNil).or(null);
    }

    public static WeakReference<Fragment> getTopFragmentOrNil() {
        return safeGet(() -> {
            Activity activity = MyApplication.SHARE_INSTANCE.mTopActivityOrNil.get();
            if (activity != null && activity instanceof FragmentStackActivity) {
                Stack<BaseFragment> stack = ((FragmentStackActivity) activity).mFragmentStack;
                if (!stack.isEmpty()) {
                    return new WeakReference<Fragment>(stack.peek());
                }
            }
            return new WeakReference<Fragment>(null);
        })
                .def(null).get();
    }

    public static boolean hasTopActivity() {
        return MyApplication.SHARE_INSTANCE.mTopActivityOrNil != null && MyApplication.SHARE_INSTANCE.mTopActivityOrNil.get() != null;
    }

    public static void setTopDialog(Dialog dialog) {
        MyApplication.SHARE_INSTANCE.mTopDialogOrNil = dialog == null ? null : new WeakReference<>(dialog);
    }

    public static WeakReference<Dialog> getTopDialogOrNil() {
        return opt(SHARE_INSTANCE).let(it -> it.mTopDialogOrNil).or(null);
    }

    public static boolean hasTopDialog() {
        return MyApplication.SHARE_INSTANCE.mTopDialogOrNil != null && MyApplication.SHARE_INSTANCE.mTopDialogOrNil.get() != null;
    }

    public static void post(Runnable runnable) {
        if (SHARE_INSTANCE != null && runnable != null) {
            SHARE_INSTANCE.mHandler.post(runnable);
        }
    }

    public static void postDelayed(Runnable runnable, long delayInTimeMills) {
        if (SHARE_INSTANCE != null && runnable != null) {
            SHARE_INSTANCE.mHandler.postDelayed(runnable, delayInTimeMills);
        }
    }

    public static Resources getResource() {
        return MyApplication.SHARE_INSTANCE.getResources();
    }

    @Override
    public void onCreate() {
        SHARE_INSTANCE = this;
        super.onCreate();
        runInMainProcess(() -> {
            FileUtil.mContext = MyApplication.this;
            LeakCanary.install(this);
            Paper.init(this);
            initLogger();
            initFresco();
            SignalColorHolder.init(SHARE_INSTANCE);
            UmengUtil.init();
            initUmengStatisticService();
            NotificationCenter.init();
            initJPushService();
        });
    }

    public boolean onForeground() {
        if (mTopActivityOrNil != null && mTopActivityOrNil.get() != null) {
            Activity activity = mTopActivityOrNil.get();
            if (activity instanceof BaseActivity) {
                BaseActivity cast = (BaseActivity) activity;
                return cast.onForeground();
            }
        }

        return false;
    }

    public void onEnterSplashActivity() {
        if (!mHasEnterSplashPageBefore) {
            mHasEnterSplashPageBefore = true;
        } else {
            AccessLogManager.increaseBuildCount();
            try {
                mAccessLoggerWriter.reset(new FileOutputStream(new File(getDefaultLogFileParentDir(), generateDefaultLogFileName()), true));
            } catch (FileNotFoundException ignored) {
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SHARE_INSTANCE = null;
    }

    private void initFresco() {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .setResizeAndRotateEnabledForNetwork(true)
//                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initLogger() {
        if (!MyConfig.IS_DEBUG_MODE) {
            Logger.init()
                    .methodCount(0)
                    .hideThreadInfo()
                    .logTool(new LogTool() {
                        @Override
                        public void d(String tag, String message) {
                            mAccessLoggerWriter.write(Log.DEBUG, tag, message);
                        }

                        @Override
                        public void e(String tag, String message) {
                            mAccessLoggerWriter.write(Log.ERROR, tag, message);
                        }

                        @Override
                        public void w(String tag, String message) {
                            mAccessLoggerWriter.write(Log.WARN, tag, message);
                        }

                        @Override
                        public void i(String tag, String message) {
                            mAccessLoggerWriter.write(Log.INFO, tag, message);
                        }

                        @Override
                        public void v(String tag, String message) {
                            mAccessLoggerWriter.write(Log.VERBOSE, tag, message);
                        }

                        @Override
                        public void wtf(String tag, String message) {
                        }
                    });
        } else {
            Logger.init()
                    .methodCount(0)
                    .hideThreadInfo()
                    .logTool(new AndroidLogTool() {
                        @Override
                        public void d(String tag, String message) {
                            super.d(tag, message);
                            mAccessLoggerWriter.write(Log.DEBUG, tag, message);
                        }

                        @Override
                        public void e(String tag, String message) {
                            super.e(tag, message);
                            mAccessLoggerWriter.write(Log.ERROR, tag, message);
                        }

                        @Override
                        public void w(String tag, String message) {
                            super.w(tag, message);
                            mAccessLoggerWriter.write(Log.WARN, tag, message);
                        }

                        @Override
                        public void i(String tag, String message) {
                            super.i(tag, message);
                            mAccessLoggerWriter.write(Log.INFO, tag, message);
                        }

                        @Override
                        public void v(String tag, String message) {
                            super.v(tag, message);
                            mAccessLoggerWriter.write(Log.VERBOSE, tag, message);
                        }
                    });
        }
        String fileName = generateDefaultLogFileName();
        File parentDir = getDefaultLogFileParentDir();
        parentDir.mkdirs();
        Observable.empty()
                .subscribeOn(Schedulers.newThread())
                .doOnCompleted(() -> AccessLogManager.sweepDepricatedFile(Optional.of(null), Optional.of(null), Optional.of(null)))
                .subscribe();
        try {
            mAccessLoggerWriter.reset(new FileOutputStream(new File(parentDir, fileName), true));
        } catch (FileNotFoundException ignored) {
        }
        Observable.merge(NotificationCenter.loginSubject, NotificationCenter.logoutSubject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nil -> {
                    try {
                        mAccessLoggerWriter.reset(new FileOutputStream(new File(parentDir, fileName), true));
                    } catch (FileNotFoundException ignored) {
                    }
                });
    }

    private void initJPushService() {
        JPushUtil.init(this);
    }

    private void initUmengStatisticService() {
        AnalyticsConfig.setAppkey(this, ShareKeys.UMENG_APP_KEY);
        AnalyticsConfig.setChannel(MyConfig.CHANNEL_ID);
        MobclickAgent.setDebugMode(MyConfig.IS_DEBUG_MODE);
        MobclickAgent.openActivityDurationTrack(false);
    }

    private void runInMainProcess(Runnable task) {
        int pid = Process.myPid();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                if (info.processName.equalsIgnoreCase(getPackageName())) {
                    task.run();
                }
                break;
            }
        }
    }
}
