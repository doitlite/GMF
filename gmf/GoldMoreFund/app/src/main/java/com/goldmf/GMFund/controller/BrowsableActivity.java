package com.goldmf.GMFund.controller;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.cmd.CMDParser;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_SplashPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;

/**
 * Created by yale on 15/9/12.
 */
public class BrowsableActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String source = getIntent().getStringExtra("source");
        boolean openByPush = !TextUtils.isEmpty(source) && source.toLowerCase().equals("push");
        Uri uri = getIntent().getData();
        finish();
        if (MyApplication.SHARE_INSTANCE != null && MyApplication.SHARE_INSTANCE.mHasLaunchSplash && uri != null) {
            CMDParser.parse(uri.getQuery(), openByPush ? CMDParser.SOURCE_PUSH : CMDParser.SOURCE_URL).call(this);
        } else {
            showActivity(this, an_SplashPage(uri, source));
            overridePendingTransition(0, 0);
        }
    }
}
