package dev.dworks.apps.alauncher;

import dev.dworks.apps.alauncher.helpers.AnalyticsHelper;
import dev.dworks.apps.alauncher.helpers.CrashHelper;

/**
 * Created by HaKr on 16/05/17.
 */

public abstract class AppFlavour extends AppPaymentFlavour {
    @Override
    public void onCreate() {
        super.onCreate();
        AnalyticsHelper.intialize(getApplicationContext());
        CrashHelper.enable(getApplicationContext(), true);
    }
}