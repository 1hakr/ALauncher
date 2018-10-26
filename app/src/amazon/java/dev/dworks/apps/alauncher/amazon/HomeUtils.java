package dev.dworks.apps.alauncher.amazon;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.android.launcher3.BuildConfig;

/**
 * Created by andy on 26/07/2017.
 */

public class HomeUtils {

    public static Intent getHomeIntent(Context context) {
        String launcherPackage = BuildConfig.APPLICATION_ID;
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(launcherPackage)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    }

    public static void openHome(Context context) {
        Intent i = getHomeIntent(context);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
