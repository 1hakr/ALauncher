package dev.dworks.apps.alauncher.recommendations;

import android.service.notification.StatusBarNotification;

import dev.dworks.apps.alauncher.recommendations.GservicesRankerParameters.Factory;
import dev.dworks.apps.alauncher.extras.tvrecommendations.service.BaseNotificationsService;

public class NotificationsServiceV4 extends BaseNotificationsService {
    private NotificationServiceDelegate mDelegate;

    public interface NotificationServiceDelegate {
        void onFetchExistingNotifications(StatusBarNotification[] statusBarNotificationArr);

        void onNotificationPosted(StatusBarNotification statusBarNotification);

        void onNotificationRemoved(StatusBarNotification statusBarNotification);
    }

    public NotificationsServiceV4() {
        super(false, new Factory());
    }

    public void onListenerConnected() {
        super.onListenerConnected();
    }

    protected void onFetchExistingNotifications(StatusBarNotification[] notifications) {
        if (isEnabled()) {
            super.onFetchExistingNotifications(notifications);
            if (this.mDelegate != null) {
                this.mDelegate.onFetchExistingNotifications(notifications);
            }
        }
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        if (isEnabled()) {
            super.onNotificationPosted(sbn);
            if (this.mDelegate != null) {
                this.mDelegate.onNotificationPosted(sbn);
            }
        }
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (isEnabled()) {
            super.onNotificationRemoved(sbn);
            if (this.mDelegate != null) {
                this.mDelegate.onNotificationRemoved(sbn);
            }
        }
    }

    private boolean isEnabled() {
        return true;
    }
}
