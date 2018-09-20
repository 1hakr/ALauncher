package dev.dworks.apps.alauncher.apps.lock;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;

import java.util.HashSet;
import java.util.Set;

public class AppLockHelper {
    public final static String LOCK_APPS_PREF = "all_apps_locked";

    public static void resetAppLock(Context context) {
        SharedPreferences.Editor editor = Utilities.getPrefs(context).edit();
        editor.putStringSet(LOCK_APPS_PREF, new HashSet<String>());
        editor.apply();
    }

    public static void secureComponent(Context context, ComponentKey componentKey, boolean secure) {
        setComponentNameState(context, componentKey, secure);
    }

    public static void setComponentNameState(Context context, ComponentKey key, boolean secured) {
        String comp = key.toString();
        Set<String> securedApps = getSecuredApps(context);
        if(secured){
            securedApps.add(comp);
        } else {
            while (securedApps.contains(comp)) {
                securedApps.remove(comp);
            }
        }
        setSecuredApps(context, securedApps);
    }

    public static boolean isSecured(Context context, ComponentKey componentKey) {
        return isSecuredApp(context, componentKey);
    }

    static boolean isSecuredApp(Context context, ComponentKey key) {
        return getSecuredApps(context).contains(key.toString());
    }

    private static Set<String> getSecuredApps(Context context) {
        return new HashSet<>(Utilities.getPrefs(context).getStringSet(LOCK_APPS_PREF, new HashSet<String>()));
    }

    private static void setSecuredApps(Context context, Set<String> hiddenApps) {
        SharedPreferences.Editor editor = Utilities.getPrefs(context).edit();
        editor.putStringSet(LOCK_APPS_PREF, hiddenApps);
        editor.apply();
    }
}
