package dev.dworks.apps.alauncher.helpers;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import com.android.launcher3.BuildConfig;

/**
 * Created by HaKr on 23/05/16.
 */

public class CrashHelper {

    public static FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

    public static void enable(Context context, boolean enable){
        if(!enable){
            return;
        }
        crashlytics.setCrashlyticsCollectionEnabled(enable);
    }

    public static void logException(Exception e) {
        logException(e, false);
    }

    public static void logException(Exception e, boolean log) {
        if(BuildConfig.DEBUG){
            e.printStackTrace();
        } else if(log) {
            crashlytics.recordException(e);
        }
    }

    public static void log(String s) {
        crashlytics.log(s);
    }

    public static void log(String tag, String s) {
        crashlytics.log(tag+":"+s);
    }
}