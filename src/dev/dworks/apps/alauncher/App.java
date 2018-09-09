package dev.dworks.apps.alauncher;

import com.android.launcher3.BuildConfig;

import dev.dworks.apps.alauncher.helpers.AnalyticsHelper;
import dev.dworks.apps.alauncher.helpers.CrashHelper;

public class App extends AppFlavour {
    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        if(!BuildConfig.DEBUG) {
            AnalyticsHelper.intialize(getApplicationContext());
        }
        CrashHelper.enable(getApplicationContext(), true);
    }

    public static synchronized App getInstance() {
        return sInstance;
    }
}
