package dev.dworks.apps.alauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;

public class CompanionProxySender {

     private static final String AT_A_GLANCE_WEATHER_ACTION = BuildConfig.APPLICATION_ID + ".AT_A_GLANCE_WEATHER_ACTION";
     private static final String AT_A_GLANCE_PREFERENCE_ACTION = BuildConfig.APPLICATION_ID + ".AT_A_GLANCE_PREFERENCE_ACTION";

     private static final String INTENT_EXTRA = BuildConfig.APPLICATION_ID + ".INTENT_EXTRA";
     private static final String TARGET_PACKAGE = "com.google.android.apps.nexuslauncher";
     private static final String COMPANION_VERSION_NAME = "1.0";
     private static final int COMPANION_VERSION_CODE = 1;

     private CompanionProxySender() {

     }

     public static void sendWeatherAction(Context context, Intent weatherIntent) {
         forward(context, AT_A_GLANCE_WEATHER_ACTION, weatherIntent);
     }

     public static void sendPreferenceAction(Context context, Intent preferenceIntent) {
         forward(context, AT_A_GLANCE_PREFERENCE_ACTION, preferenceIntent);
     }

     private static void forward(Context context, String action, Intent intentExtra) {
         verifyCompanionVersion(context);
         Intent intent = new Intent(action);
         intent.setPackage(TARGET_PACKAGE);
         intent.putExtra(INTENT_EXTRA, intentExtra);
         context.sendBroadcast(intent);
     }

    private static void verifyCompanionVersion(Context context) {
        String versionName;
        int versionCode;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(TARGET_PACKAGE, 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = null;
            versionCode = 0;
        }

        if (!TextUtils.equals(versionName, COMPANION_VERSION_NAME) || versionCode != COMPANION_VERSION_CODE) {
            Toast.makeText(context, R.string.companion_not_up_to_date_please_update, Toast.LENGTH_LONG).show();
        }
    }
 }