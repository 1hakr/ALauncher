package amirz.shade.appprediction;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.model.AppLaunchTracker;
import com.android.launcher3.util.ComponentKey;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DismissedAppsDatabase {
    private static final String KEY_HIDDEN = "pref_dismissed_apps";

    public static boolean isDismissed(Context context, ComponentName app, UserHandle user) {
        Set<String> hiddenSet = Utilities.getPrefs(context)
                .getStringSet(KEY_HIDDEN, Collections.emptySet());
        return hiddenSet.contains(new ComponentKey(app, user).toString());
    }

    public static boolean isDismissed(Context context, ItemInfo item) {
        return isDismissed(context, item.getTargetComponent(), item.user);
    }

    public static void setDismissed(Context context, ComponentName app, UserHandle user,
                                 boolean hidden) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_HIDDEN, Collections.emptySet()));
        String key = new ComponentKey(app, user).toString();
        if (hidden) {
            set.add(key);
        } else {
            set.remove(key);
        }
        prefs.edit().putStringSet(KEY_HIDDEN, set).apply();

        // Reload app predictions when hidden state changes.
        AppLaunchTracker.INSTANCE.get(context).onReturnedToHome();
    }

    public static void setDismissed(Context context, ItemInfo item, boolean hidden) {
        setDismissed(context, item.getTargetComponent(), item.user, hidden);
    }
}
