package dev.dworks.apps.alauncher.helpers;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;

import com.android.launcher3.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class Utils {
    /**
     * Returns true when running Android TV
     *
     * @param c Context to detect UI Mode.
     * @return true when device is running in tv mode, false otherwise.
     */
    public static String getDeviceType(Context c) {
        UiModeManager uiModeManager = (UiModeManager) c.getSystemService(Context.UI_MODE_SERVICE);
        int modeType = uiModeManager.getCurrentModeType();
        switch (modeType){
            case Configuration.UI_MODE_TYPE_TELEVISION:
                return "TELEVISION";
            case Configuration.UI_MODE_TYPE_WATCH:
                return "WATCH";
            case Configuration.UI_MODE_TYPE_NORMAL:
                String type = isTablet(c) ? "TABLET" : "PHONE";
                return type;
            case Configuration.UI_MODE_TYPE_UNDEFINED:
                return "UNKOWN";
            default:
                return "";
        }
    }
    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void showSnackBar(Activity activity, int text){
        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.content_view), text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public static void showSnackBar(Activity activity, String text, int duration, String action, View.OnClickListener listener){
        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.content_view), text, duration);
        int color = Color.parseColor("#EF3A0F");
        if (null != listener) {
            snackbar.setAction(action, listener)
                    .setActionTextColor(color);
        }
        snackbar.show();
    }

}
