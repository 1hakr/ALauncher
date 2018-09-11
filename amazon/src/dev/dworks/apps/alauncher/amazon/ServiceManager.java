package dev.dworks.apps.alauncher.amazon;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by andy on 26/07/2017.
 */

public class ServiceManager {
    private static Intent mServiceIntent;

    private static boolean isMyServiceRunning(Class<?> serviceClass, Context c) {
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void StartSlow(final Context c) {
        while (!Start(c)) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
    }

    public static boolean Start(Context c)
    {
        mServiceIntent = new Intent(c, HomeButtonService.class);
        if (!isMyServiceRunning(HomeButtonService.class, c)) {
            c.startService(mServiceIntent);
            return true;
        }
        return false;
    }

    public static void Stop(Context c)
    {
        c.stopService(mServiceIntent);
    }
}
