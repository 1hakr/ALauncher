package amirz.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.android.launcher3.Launcher;

public class DoubleTapLockHelper {

    private static final long TIMEOUT_THRESHOLD = 350L;

    protected static void timeoutLock(final Launcher launcher) {
        if(isTimeoutLockEnabled(launcher)){
            LockTimeoutActivity.startTimeout(launcher);
        } else {
            enableTimeoutLock(launcher);
        }
    }

    public static boolean isTimeoutLockEnabled(Context context){
        return android.provider.Settings.System.canWrite(context);
    }

    public static void enableTimeoutLock(Context context){
        if (!isTimeoutLockEnabled(context)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
