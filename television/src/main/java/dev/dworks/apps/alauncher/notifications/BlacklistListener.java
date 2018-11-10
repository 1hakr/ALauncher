package dev.dworks.apps.alauncher.notifications;

public interface BlacklistListener {
    void onPackageBlacklisted(String str);

    void onPackageUnblacklisted(String str);
}
