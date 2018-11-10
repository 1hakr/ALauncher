package dev.dworks.apps.alauncher.extras.tvrecommendations.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

class ServiceAppReceiver extends BroadcastReceiver {
    private Listener mListener;

    public interface Listener {
        void onPackageAdded(String str);

        void onPackageChanged(String str);

        void onPackageFullyRemoved(String str);

        void onPackageRemoved(String str);

        void onPackageReplaced(String str);
    }

    public ServiceAppReceiver(Listener listener) {
        this.mListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        String packageName = getPackageName(intent);
        if (packageName != null && packageName.length() != 0) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                this.mListener.onPackageAdded(packageName);
            } else if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                this.mListener.onPackageChanged(packageName);
            } else if ("android.intent.action.PACKAGE_FULLY_REMOVED".equals(action)) {
                this.mListener.onPackageFullyRemoved(packageName);
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    this.mListener.onPackageRemoved(packageName);
                }
            } else if ("android.intent.action.PACKAGE_REPLACED".equals(action)) {
                this.mListener.onPackageReplaced(packageName);
            }
        }
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_FULLY_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        return filter;
    }

    private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        return uri != null ? uri.getSchemeSpecificPart() : null;
    }
}
