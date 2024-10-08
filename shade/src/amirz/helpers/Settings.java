package amirz.helpers;

import static android.content.Intent.ACTION_SENDTO;
import static com.android.launcher3.LauncherState.ALL_APPS;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.BuildConfig;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherFiles;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.graphics.IconPalette;
import com.android.launcher3.util.Themes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import amirz.shade.customization.DockSearch;
import amirz.shade.search.AllAppsQsb;
import amirz.shade.util.Snackbar;

public class Settings {
    private final SharedPreferences mSharedPreferences;

    private static final String GOOGLE_QSB = "com.google.android.googlequicksearchbox";

    public static final String KEY_LOCK_DESKTOP = "pref_lock_desktop";
    public static final String KEY_HOME_ACTION = "pref_home_action";
    public static final String KEY_SWIPE_DOWN = "pref_swipe_down";
    public static final String KEY_DOUBLE_TAP_LOCK = "pref_double_tap_lock";

    public static final String SUPPORT_EMAIL = "support@dworks.io";

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

    public static void showSnackBar(Activity context, int resId) {
        Snackbar.show(context, resId);
    }

    public static void showSnackBar(Activity context, String text) {
        Snackbar.show(context, text);
    }

    public static void showSnackBar(Activity context, String text, String action, Runnable runnable) {
        Snackbar.show(context, text, action, runnable);
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
                                        try {
                                            launcher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(provider)));
                                        } catch (Exception ee){

                                        }
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

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private static String getDeviceDetails(Activity activity){
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String deviceModelName = "";
        if (model.startsWith(manufacturer)) {
            deviceModelName =  model;
        } else {
            deviceModelName = manufacturer + " " + model;
        }
        Locale locale = null;
        if(Utilities.ATLEAST_NOUGAT){
            locale = activity.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = activity.getResources().getConfiguration().locale;
        }
        String versionName = BuildConfig.VERSION_NAME;
        String deviceDetails = "";
        deviceDetails += "App package: " + BuildConfig.APPLICATION_ID + " \n";
        deviceDetails += "App version: " + versionName + " \n";
        deviceDetails += "Current date: " + dateFormat.format(currentDate) + " \n";
        deviceDetails += "Device: " + deviceModelName + " \n";
        deviceDetails += "OS version: Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ") \n";
        if(null != locale) {
            deviceDetails += "Country: " + locale.getDisplayCountry() + " \n";
            deviceDetails += "Language: " + locale.getDisplayLanguage() + " \n";
        }
        return deviceDetails;
    }

    public static void openFeedback(Activity activity){
        sendEmail(activity, "Send Feedback", "ALauncher Feedback", getDeviceDetails(activity));
    }

    public static void sendError(Activity activity, String details){
        sendEmail(activity, "Report Error", "ALauncher Error", getDeviceDetails(activity) + details);
    }

    public static void sendEmail(Activity activity, String title, String subject, String details){
        final Intent result = new Intent(ACTION_SENDTO);
        result.setData(Uri.parse("mailto:"));
        result.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
        result.putExtra(Intent.EXTRA_SUBJECT, subject);
        String text = subject + " v" + BuildConfig.VERSION_NAME;
        if(!TextUtils.isEmpty(details)){
            text = details;
        }
        text += "\n\nFeedback: \n";
        result.putExtra(Intent.EXTRA_TEXT, text);

        activity.startActivity(Intent.createChooser(result, title));
    }

    public static boolean isActivityAlive(Activity activity) {
        return !(null == activity || activity.isDestroyed());
    }

    public static boolean isProVersion(){
        return BuildConfig.FLAVOR.contains("Pro");
    }

    public static boolean isGoogleBuild(){
        return BuildConfig.FLAVOR.contains("Google");
    }

    public static boolean isAmazonBuild(){
        return BuildConfig.FLAVOR.contains("Amazon");
    }

    public static final String AMAZON_SHORT_URL = "amzn://apps/android?p=";
    public static final String GOOGLE_SHORT_URL = "market://details?id=";
    public static final String AMAZON_APP_URL = "https://www.amazon.com/gp/mas/dl/android?p=";
    public static final String GOOGLE_APP_URL = "https://play.google.com/store/apps/details?id=";

    public static Uri getAppUri(){
        return Uri.parse(getAppShortUrl());
    }

    public static String getAppShortUrl(){
        String url = GOOGLE_SHORT_URL + BuildConfig.APPLICATION_ID;
        if(isAmazonBuild()){
            url = AMAZON_SHORT_URL + BuildConfig.APPLICATION_ID;
        }
        return url;
    }

    public static String getAppLongUrl(){
        String url = GOOGLE_APP_URL + BuildConfig.APPLICATION_ID;
        if(isAmazonBuild()){
            url = AMAZON_APP_URL + BuildConfig.APPLICATION_ID;
        }
        return url;
    }

    public static void openPlaystore(Context çontext){
        Intent intent = new Intent(Intent.ACTION_VIEW, getAppUri());
        if(isIntentAvailable(çontext, intent)) {
            çontext.startActivity(intent);
        }
    }

    public static Uri getAppProStoreUri(){
        String url = getAppLongUrl() + "dev.dworks.apps.alauncher.pro";
        return Uri.parse(url);
    }

    public static void openProAppLink(Activity activity){
        Intent intent = new Intent(Intent.ACTION_VIEW, getAppProStoreUri());
        if(isIntentAvailable(activity, intent)) {
            activity.startActivity(intent);
        }
    }
}
