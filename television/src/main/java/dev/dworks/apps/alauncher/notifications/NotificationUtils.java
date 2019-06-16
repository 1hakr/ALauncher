package dev.dworks.apps.alauncher.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import dev.dworks.apps.alauncher.extras.tvrecommendations.TvRecommendation;

public class NotificationUtils {
    public static final String INFO_CHANNEL = "info_channel";

    public static boolean equals(TvRecommendation left, TvRecommendation right) {
        if (left == null || right == null) {
            return left == right;
        } else return TextUtils.equals(left.getKey(), right.getKey());
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void createNotificationChannels(Context context){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(manager, INFO_CHANNEL, "Info",
                "Important news and information from ALauncher", Color.GREEN);
    }

    private static void createNotificationChannel(NotificationManager manager, String id,
                                                  String name, String description, int color){
        createNotificationChannel(manager, id, name, description, color,
                NotificationManager.IMPORTANCE_DEFAULT);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void createNotificationChannel(NotificationManager manager, String id,
                                                  String name, String description, int color,
                                                  int importance){
        if(manager.getNotificationChannel(id) == null) {
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(color);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }
    }
}
