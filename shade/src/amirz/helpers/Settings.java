package amirz.helpers;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherFiles;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.graphics.IconPalette;
import com.android.launcher3.util.Themes;

import amirz.shade.customization.DockSearch;
import amirz.shade.search.AllAppsQsb;

import static com.android.launcher3.LauncherState.ALL_APPS;

public class Settings {
    private final SharedPreferences mSharedPreferences;

    private static final String GOOGLE_QSB = "com.google.android.googlequicksearchbox";

    public static final String KEY_LOCK_DESKTOP = "pref_lock_desktop";
    public static final String KEY_HOME_ACTION = "pref_home_action";
    public static final String KEY_SWIPE_DOWN = "pref_swipe_down";
    public static final String KEY_DOUBLE_TAP_LOCK = "pref_double_tap_lock";

    public Settings(Context context) {
        mSharedPreferences = context.getSharedPreferences(
                LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public static Settings instance(Context context) {
        return new Settings(context);
    }

    private static SharedPreferences prefs(Context context) {
        return Utilities.getPrefs(context);
    }

    public static void showSnackBar(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    public static int getNotificationColor(Context context) {
        return IconPalette.getMutedColor(Themes.getShadeColorAccent(context), 0.2f);
    }

    public static boolean isDesktopLocked(Context context) {
        return prefs(context).getBoolean(KEY_LOCK_DESKTOP, false);
    }

    public static String getHomeAction(Context context) {
        return prefs(context).getString(KEY_HOME_ACTION, "");
    }

    public static boolean isSwipeDownEnabled(Context context) {
        return prefs(context).getBoolean(KEY_SWIPE_DOWN, true);
    }

    public static boolean isDoubleTapLockEnabled(Context context) {
        return prefs(context).getBoolean(KEY_DOUBLE_TAP_LOCK, false);
    }

    public static void handleHomeAction(Launcher launcher) {
        switch (Settings.getHomeAction(launcher)) {
            case "quicksearch":
                startQuickSearch(launcher);
                break;
            case "voicesearch":
                startVoiceSearch(launcher);
                break;
            case "assistant":
                startAssistant(launcher);
                break;
            case "appdrawer":
                openAppDrawer(launcher);
                break;
            case "appsearch":
                openAppSearch(launcher);
                break;
            default:
                // ignore
                break;
        }
    }
    public static void startQuickSearch(final Launcher launcher) {
        final String provider = DockSearch.getDockSearch(launcher);
        if (provider.contains("google")) {
            Point point = new Point(0, 0);
            Intent intent = new Intent("com.google.nexuslauncher.FAST_TEXT_SEARCH")
                    .setPackage("com.google.android.googlequicksearchbox")
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("source_round_left", true)
                    .putExtra("source_round_right", true)
                    .putExtra("source_logo_offset", point)
                    .putExtra("source_mic_offset", point)
                    .putExtra("use_fade_animation", true);
            intent.setSourceBounds(new Rect());
            launcher.sendOrderedBroadcast(intent, null,
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            Log.e("HotseatQsbSearch", getResultCode() + " " + getResultData());
                            if (getResultCode() == 0) {
                                try {
                                    launcher.startActivity(new Intent("com.google.android.googlequicksearchbox.TEXT_ASSIST")
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            .setPackage(GOOGLE_QSB));
                                } catch (ActivityNotFoundException e) {
                                    try {
                                        launcher.getPackageManager().getPackageInfo(GOOGLE_QSB, 0);
                                        LauncherAppsCompat.getInstance(launcher)
                                                .showAppDetailsForProfile(new ComponentName(GOOGLE_QSB, ".SearchActivity"),
                                                        Process.myUserHandle(), null, null);
                                    } catch (PackageManager.NameNotFoundException ignored) {
                                        launcher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(provider)));
                                    }
                                }
                            }
                        }
                    }, null, 0, null, null);
        } else {
            launcher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(provider)));
        }
    }

    public static void startVoiceSearch(Launcher launcher) {
        String clazz = ".SearchActivity";
        try {
            launcher.startActivity(new Intent("android.intent.action.VOICE_ASSIST")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setPackage(GOOGLE_QSB));
        } catch (ActivityNotFoundException e) {
            try {
                launcher.getPackageManager().getPackageInfo(GOOGLE_QSB, 0);
                LauncherAppsCompat.getInstance(launcher)
                        .showAppDetailsForProfile(new ComponentName(GOOGLE_QSB, clazz),
                                Process.myUserHandle(), null, null);
            } catch (PackageManager.NameNotFoundException ignored) {
                launcher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")));
            }
        }
    }

    public static void startAssistant(Launcher launcher) {
        String clazz = "com.google.android.apps.gsa.staticplugins.opa.OpaActivity";
        try {
            launcher.startActivity(new Intent(Intent.ACTION_VOICE_COMMAND)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setPackage(GOOGLE_QSB));
        } catch (ActivityNotFoundException e) {
            try {
                launcher.getPackageManager().getPackageInfo(GOOGLE_QSB, 0);
                LauncherAppsCompat.getInstance(launcher)
                        .showAppDetailsForProfile(new ComponentName(GOOGLE_QSB, clazz),
                                Process.myUserHandle(), null, null);
            } catch (PackageManager.NameNotFoundException ignored) {
                launcher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")));
            }
        }
    }

    public static void openAppDrawer(Launcher launcher) {
        launcher.getStateManager().goToState(ALL_APPS);
    }

    public static void openAppSearch(Launcher launcher) {
        AllAppsQsb search = (AllAppsQsb) launcher.getAppsView().getSearchView();
        search.requestSearch();
        launcher.getStateManager().goToState(ALL_APPS);
    }

    public static void handleWorkspaceTouchEvent(Launcher launcher) {
        if (Settings.isDoubleTapLockEnabled(launcher)) {
            DoubleTapLockHelper.timeoutLock(launcher);
        }
    }

    public static boolean isTransparentTone(Context context) {
        int overlayEndScrim = Themes.getAttrColor(context, R.attr.shadeColorAllAppsOverlay);
        boolean isDark = Themes.getAttrBoolean(context, R.attr.isMainColorDark);
        int alpha = Color.alpha(overlayEndScrim);
        return !isDark && alpha == 0;
    }

    public static int getAllAppsTextColor(Context context) {
        return Themes.getAttrColor(context, R.attr.workspaceTextColor);
    }

    public static void setAllAppsTextColor(BubbleTextView icon) {
        Context context = icon.getContext();
        if(isTransparentTone(context)) {
            int textColor = Themes.getAttrColor(icon.getContext(), R.attr.workspaceTextColor);
            icon.setTextColor(textColor);
        }
    }
}
