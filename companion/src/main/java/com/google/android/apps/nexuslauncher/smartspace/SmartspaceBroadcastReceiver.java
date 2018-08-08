package com.google.android.apps.nexuslauncher.smartspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class SmartspaceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "ALauncher Companion";
    public static final String TARGET_PACKAGE_NAME = "dev.dworks.apps.alauncher";
    public static final String ENABLE_UPDATE_ACTION = "com.google.android.apps.gsa.smartspace.ENABLE_UPDATE";
    public static final String ENABLE_UPDATE_PACKAGE = "com.google.android.googlequicksearchbox";

    public static final String AT_A_GLANCE_SOURCE = "com.google.android.apps.nexuslauncher.UPDATE_SMARTSPACE";
    public static final String AT_A_GLANCE_PROXY_ACTION = TARGET_PACKAGE_NAME + ".AT_A_GLANCE";
    public static final String AT_A_GLANCE_FORCE_ENABLE = TARGET_PACKAGE_NAME + ".AT_A_GLANCE_FORCE_ENABLE";
    public static final String AT_A_GLANCE_WEATHER_ACTION = TARGET_PACKAGE_NAME +  ".AT_A_GLANCE_WEATHER_ACTION";
    public static final String AT_A_GLANCE_PREFERENCE_ACTION = TARGET_PACKAGE_NAME + ".AT_A_GLANCE_PREFERENCE_ACTION";
    public static final String ACTION_PING = TARGET_PACKAGE_NAME + ".AT_A_GLANCE_PING";
    public static final String ACTION_PING_RESPONSE = TARGET_PACKAGE_NAME + ".AT_A_GLANCE_PING_RESPONSE";
    public static final String INTENT_EXTRA = TARGET_PACKAGE_NAME + ".INTENT_EXTRA";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            Log.i(TAG, action);
            switch (action) {
                case AT_A_GLANCE_SOURCE:
                    Intent proxy = new Intent(AT_A_GLANCE_PROXY_ACTION);
                    proxy.setPackage(TARGET_PACKAGE_NAME);
                    proxy.putExtras(intent.getExtras() == null ? Bundle.EMPTY : intent.getExtras());
                    context.sendBroadcast(proxy);
                    break;
                case AT_A_GLANCE_FORCE_ENABLE:
                    Intent enable = new Intent(ENABLE_UPDATE_ACTION);
                    enable.setPackage(ENABLE_UPDATE_PACKAGE);
                    enable.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(enable);
                    break;
                case ACTION_PING:
                    String version;
                    try {
                        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                        version = pInfo.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        version = "NaN";
                    }

                    Intent ping = new Intent(ACTION_PING_RESPONSE);
                    ping.setPackage(TARGET_PACKAGE_NAME);
                    ping.putExtra(ACTION_PING_RESPONSE, version);
                    context.sendBroadcast(ping);
                    break;
                case AT_A_GLANCE_WEATHER_ACTION:
                    Intent weather = intent.getParcelableExtra(INTENT_EXTRA);
                    context.sendBroadcast(weather);
                    break;
                case AT_A_GLANCE_PREFERENCE_ACTION:
                    Intent preference = intent.getParcelableExtra(INTENT_EXTRA);
                    context.sendBroadcast(preference);
                    break;
            }
        }
    }
}