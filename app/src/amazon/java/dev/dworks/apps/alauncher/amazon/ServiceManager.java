package dev.dworks.apps.alauncher.amazon;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by andy on 26/07/2017.
 */

public class ServiceManager {
    private static Intent mServiceIntent;

    private static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startSlow(final Context context) {
        while (!start(context)) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
    }

    public static boolean start(Context context) {
        mServiceIntent = new Intent(context, HomeButtonService.class);
        if (!isMyServiceRunning(HomeButtonService.class, context)) {
            context.startService(mServiceIntent);
            return true;
        }
        return false;
    }

    public static void stop(Context context) {
        context.stopService(mServiceIntent);
    }
}
