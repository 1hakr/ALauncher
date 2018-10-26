package dev.dworks.apps.alauncher.amazon;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

public class FixAccessibilityService extends AccessibilityService {
    public static final String FIRE_LAUNCHER = "com.amazon.firelauncher";
    static final String TAG = "FixAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event.getPackageName().equals(FIRE_LAUNCHER))
            HomeUtils.openHome(getApplicationContext());
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        ServiceManager.start(getApplicationContext());;
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.packageNames = new String[]{ FIRE_LAUNCHER };
        setServiceInfo(info);
        HomeUtils.openHome(getApplicationContext());
    }

}