package dev.dworks.apps.alauncher.extras.firetv.tvrecommendations;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.NotificationCompat;
import dev.dworks.apps.alauncher.MainActivity;
import dev.dworks.apps.alauncher.R;
import dev.dworks.apps.alauncher.recommendations.NotificationsServiceV4;

import static dev.dworks.apps.alauncher.notifications.NotificationUtils.INFO_CHANNEL;

public class NotificationListenerMonitor extends Service {
    private static final String TAG = "NotifyListenerMonitor";
    private static final int MAXIMUM_RECONNECT_ATTEMPTS = 15;

    private int mReconnectAttempts = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification listener monitor created.");

        if (!listenerIsRunning()) {
            ensureNotificationPermissions(getApplicationContext());

            toggleNotificationListenerService();

            final Timer[] timer = new Timer[]{new Timer()};
            timer[0].schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!listenerIsRunning() && mReconnectAttempts < MAXIMUM_RECONNECT_ATTEMPTS) {
                        ensureNotificationPermissions(getApplicationContext());

                        toggleNotificationListenerService();

                        mReconnectAttempts++;
                    } else {
                        timer[0].cancel();
                    }
                }
            }, 5000, 5000);
        }
    }

    private boolean listenerIsRunning() {
        ComponentName notificationListenerComp = new ComponentName(this, NotificationsServiceV4.class);
        Log.v(TAG, "Ensuring the notification listener is running: " + notificationListenerComp);

        boolean running = false;

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);

        if (services == null) {
            Log.w(TAG, "No running services found. Aborting listener monitoring.");
            return false;
        }

        for (ActivityManager.RunningServiceInfo service : services) {
            if (notificationListenerComp.equals(service.service)) {
                Log.w(TAG, "Ensuring Notification Listener { PID: " + service.pid + " | currentPID: " + Process.myPid() + " | clientPackage: " + service.clientPackage + " | clientCount: " + service.clientCount
                        + " | clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")}"));

                if (service.pid == Process.myPid()) {
                    running = true;
                }
            }
        }

        if (running) {
            Log.d(TAG, "Listener is running!");
            return true;
        }

        Log.d(TAG, "The listener has been killed... Attempting to start.");
        return false;
    }

    private void toggleNotificationListenerService() {
        Log.d(TAG, "Toggling notification listener...");
        ComponentName thisComponent = new ComponentName(this, NotificationsServiceV4.class);

        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void ensureNotificationPermissions(Context context) {
        Log.d(TAG, "Checking notify perms");
        if (context.getPackageManager().checkPermission("android.permission.WRITE_SECURE_SETTINGS", context.getPackageName()) != PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "Perms granted");


            String listeners = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");

            // Settings.Secure.putString(context.getContentResolver(), "enabled_notification_listeners", "");

            String component = new ComponentName(context, NotificationsServiceV4.class).flattenToShortString();
            String[] list = listeners == null ? new String[0] : listeners.split("\\s*:\\s*");
            boolean enabled = false;
            for (CharSequence equals : list) {
                if (TextUtils.equals(equals, component)) {
                    enabled = true;
                    break;
                }
            }
            if (!enabled) {
                if (listeners == null || listeners.length() == 0) {
                    listeners = component;
                } else {
                    listeners = listeners + ":" + component;
                }
                Log.d(TAG, "Perms enabled");
            } else {
                Log.d(TAG, "Perms enabled (2)");
            }

            Settings.Secure.putString(context.getContentResolver(), "enabled_notification_listeners", listeners);
        } else {
            Log.d(TAG, "Perms denied");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, INFO_CHANNEL)
                .setSmallIcon(R.drawable.ic_app_default)
                .setContentTitle("LeanbackOnFire")
                .setContentText("Keeping Launcher In Memory");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1111, builder.build());
        startForeground(1111, builder.build());
        return START_STICKY;
    }
}