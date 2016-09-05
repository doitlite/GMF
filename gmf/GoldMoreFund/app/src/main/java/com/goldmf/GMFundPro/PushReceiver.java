package com.goldmf.GMFundPro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.BrowsableActivity;

import org.json.JSONObject;

import java.util.HashMap;

import cn.jpush.android.api.JPushInterface;

import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by yale on 15/7/23.
 */
public class PushReceiver extends BroadcastReceiver {

    /**
     * SDK 向 JPush Server 注册所得到的注册 ID。
     * 一般来说，可不处理此广播信息。
     */
    private static final String ACTION_REGISTRATION = "cn.jpush.android.intent.REGISTRATION";

    /**
     * 收到了自定义消息 Push 。
     * SDK 对自定义消息，只是传递，不会有任何界面上的展示。
     */
    private static final String ACTION_MESSAGE_RECEIVED = "cn.jpush.android.intent.MESSAGE_RECEIVED";

    /**
     * 收到了通知 Push。
     * 如果通知的内容为空，则在通知栏上不会展示通知。
     * 但是，这个广播 Intent 还是会有。开发者可以取到通知内容外的其他信息。
     */
    private static final String ACTION_NOTIFICATION_RECEIVED = "cn.jpush.android.intent.NOTIFICATION_RECEIVED";

    /**
     * 用户点击了通知。
     * 一般情况下，用户不需要配置此 receiver action。
     */
    private static final String ACTION_NOTIFICATION_OPENED = "cn.jpush.android.intent.NOTIFICATION_OPENED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            if (action.equalsIgnoreCase(ACTION_REGISTRATION)) {
                handleRegistrationAction(intent);
            } else if (action.equalsIgnoreCase(ACTION_MESSAGE_RECEIVED)) {
                handleMessageReceivedAction(context, intent);
            } else if (action.equalsIgnoreCase(ACTION_NOTIFICATION_RECEIVED)) {
                handleNotificationReceivedAction(context, intent);
            } else if (action.equalsIgnoreCase(ACTION_NOTIFICATION_OPENED)) {
                handleNotificationOpenedAction(context, intent);
            }
        }
    }

    protected void handleRegistrationAction(Intent intent) {
    }

    protected void handleMessageReceivedAction(Context ctx, Intent intent) {
    }

    @SuppressWarnings("ConstantConditions")
    protected void handleNotificationReceivedAction(Context ctx, Intent intent) {

        safeCall(() -> {
            MyApplication application = MyApplication.SHARE_INSTANCE;
            if (application != null && application.onForeground()) {
                if (MyApplication.hasTopActivity()) {
                    Integer notificationId = intent.getIntExtra(JPushInterface.EXTRA_NOTIFICATION_ID, 0);
                    String extra = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
                    if (!TextUtils.isEmpty(extra)) {
                        JSONObject jsonObj = new JSONObject(extra);
                        if (jsonObj.has("gmf")) {
                            String link = jsonObj.getString("gmf");
                            HashMap<String, String> params = CMDParser.toMap(link);
                            String cmd = safeGet(() -> params.get("cmd"), "");
                            if (cmd.equalsIgnoreCase(CMDParser.CMD_SESSION)) {
                                JPushInterface.clearNotificationById(ctx, notificationId);
                                CMDParser.parse_sessionCommandImpl(params, true).call(ctx);
                            } else if (cmd.equalsIgnoreCase(CMDParser.CMD_MESSAGE_HOME)) {
                                JPushInterface.clearNotificationById(ctx, notificationId);
                                CMDParser.parse_messagehomeCommandImpl(params, true).call(ctx);
                            }
                        }
                    }
                }
            }
        });
    }

    protected void handleNotificationOpenedAction(Context ctx, Intent intent) {

        safeCall(() -> {
            Integer notificationId = intent.getIntExtra(JPushInterface.EXTRA_NOTIFICATION_ID, 0);
            String extra = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
            String uriString = "gmf://www.caopanman.com";
            String link = "";
            if (!TextUtils.isEmpty(extra)) {
                JSONObject jsonObj = new JSONObject(extra);
                if (jsonObj.has("gmf")) {
                    link = jsonObj.getString("gmf");
                    uriString = uriString + "?" + link;
                }
            }

            Uri uri = Uri.parse(uriString);
            Intent tent = new Intent(ctx, BrowsableActivity.class);
            tent.setData(uri);
            tent.putExtra("source", "PUSH");
            tent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(tent);
        });
    }
}
