package dev.dworks.apps.alauncher;

import android.app.Application;

import com.android.launcher3.BuildConfig;

import dev.dworks.apps.alauncher.helpers.AnalyticsHelper;
import dev.dworks.apps.alauncher.helpers.CrashHelper;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(!BuildConfig.DEBUG) {
            AnalyticsHelper.intialize(getApplicationContext());
        }
        CrashHelper.enable(getApplicationContext(), true);
    }
}
