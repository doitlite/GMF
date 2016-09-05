package yale.extension.system;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;

import com.annimon.stream.Stream;

import rx.functions.Action2;

/**
 * Created by yale on 16/3/23.
 */
public class RunTimePermissionChecker {
    private RunTimePermissionChecker() {
    }

    private static final int CODE_REQUEST_PERMISSION_FIRST_TIME = 2333;
    private static final int CODE_REQUEST_PERMISSION_SECOND_TIME = 2334;

    public static String[] getDeniedPermissionsImpl(Context context, String[] permissions) {
        if (permissions == null)
            return new String[0];

        return Stream.of(permissions)
                .filter(it -> {
                    int result = PermissionChecker.checkSelfPermission(context, it);
                    return result == PermissionChecker.PERMISSION_DENIED || result == PermissionChecker.PERMISSION_DENIED_APP_OP;
                })
                .toArray(count -> new String[count]);
    }

    public static RequestPermissionResultHandler requestPermissions(Activity activity, String[] permissions, Action2<Trigger, RequestResult> callback) {
        RequestPermissionResultHandler handler = new RequestPermissionResultHandler() {
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                if (requestCode == CODE_REQUEST_PERMISSION_FIRST_TIME || requestCode == CODE_REQUEST_PERMISSION_SECOND_TIME) {
                    boolean isFirstTime = requestCode == CODE_REQUEST_PERMISSION_FIRST_TIME;
                    RequestResult result = createRequestResult(activity, isFirstTime, permissions);
                    Trigger trigger = createTrigger(activity, permissions, this, callback);
                    callback.call(trigger, result);
                }
            }
        };
        requestPermissionsImpl(activity, true, permissions, handler, callback);

        return handler;
    }

    public static void requestPermissionsImpl(Activity activity, boolean isFirstTime, String[] permissions, RequestPermissionResultHandler handler, Action2<Trigger, RequestResult> callback) {
        String[] deniedPermissions = getDeniedPermissionsImpl(activity, permissions);
        if (deniedPermissions == null || deniedPermissions.length == 0) {
            RequestResult result = createRequestResult(activity, isFirstTime, permissions, true);
            Trigger trigger = createTrigger(activity, permissions, handler, callback);
            callback.call(trigger, result);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(permissions, isFirstTime ? CODE_REQUEST_PERMISSION_FIRST_TIME : CODE_REQUEST_PERMISSION_SECOND_TIME);
            } else {
                RequestResult result = createRequestResult(activity, isFirstTime, permissions, true);
                Trigger trigger = createTrigger(activity, permissions, handler, callback);
                callback.call(trigger, result);
            }
        }
    }

    private static RequestResult createRequestResult(Context context, boolean isFirstTime, String[] permissions) {
        RequestResult result = new RequestResult();
        result.isFirstTimeRequest = isFirstTime;
        result.isAllGranted = getDeniedPermissionsImpl(context, permissions).length == 0;
        result.permissions = permissions;
        return result;
    }

    private static RequestResult createRequestResult(Context context, boolean isFirstTime, String[] permissions, boolean isAllGranted) {
        RequestResult result = new RequestResult();
        result.isFirstTimeRequest = isFirstTime;
        result.isAllGranted = isAllGranted;
        result.permissions = permissions;
        return result;
    }

    private static Trigger createTrigger(Activity activity, String[] permissions, RequestPermissionResultHandler handler, Action2<Trigger, RequestResult> callback) {
        return () -> requestPermissionsImpl(activity, false, permissions, handler, callback);
    }

    public static class RequestResult {
        public boolean isFirstTimeRequest;
        public String[] permissions;
        public boolean isAllGranted;

        public String[] getDeniedPermissions(Context context) {
            return getDeniedPermissionsImpl(context, permissions);
        }
    }

    public interface Trigger {
        void requestAgain();
    }

    public interface RequestPermissionResultHandler {
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }
}
