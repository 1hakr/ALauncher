package dev.dworks.apps.alauncher.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.net.Uri;
import android.os.Process;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.dynamicui.WallpaperColorInfo;
import com.android.launcher3.util.LooperExecutor;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

import java.io.File;
import java.util.List;
import java.util.Locale;

import androidx.core.content.FileProvider;
import androidx.palette.graphics.Palette;
import dev.dworks.apps.alauncher.Settings;

import static com.google.android.apps.nexuslauncher.NexusLauncherActivity.BRIDGE_TAG;

public class Utils {

    private static final long WAIT_BEFORE_RESTART = 250;
    private static final String GOOGLE_QSB = "com.google.android.googlequicksearchbox";
    private static final int WHITE = 0xffffffff;
    public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
    /**
     * Returns true when running Android TV
     *
     * @param c Context to detect UI Mode.
     * @return true when device is running in tv mode, false otherwise.
     */
    public static String getDeviceType(Context c) {
        UiModeManager uiModeManager = (UiModeManager) c.getSystemService(Context.UI_MODE_SERVICE);
        int modeType = uiModeManager.getCurrentModeType();
        switch (modeType){
            case Configuration.UI_MODE_TYPE_TELEVISION:
                return "TELEVISION";
            case Configuration.UI_MODE_TYPE_WATCH:
                return "WATCH";
            case Configuration.UI_MODE_TYPE_NORMAL:
                String type = isTablet(c) ? "TABLET" : "PHONE";
                return type;
            case Configuration.UI_MODE_TYPE_UNDEFINED:
                return "UNKOWN";
            default:
                return "";
        }
    }
    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void showSnackBar(Activity activity, int text){
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }

    public static void reload(Context context) {
        LauncherAppState.getInstance(context).getModel().forceReload();
    }

    public static void reloadTheme(Context context) {
        WallpaperColorInfo.getInstance(context).notifyChange(true);
    }

    public static void restart(final Context context) {
        ProgressDialog.show(context, null, context.getString(R.string.restarting), true, false);
        new LooperExecutor(LauncherModel.getWorkerLooper()).execute(new Runnable() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void run() {
                try {
                    Thread.sleep(WAIT_BEFORE_RESTART);
                } catch (Exception e) {
                    Log.e("SettingsActivity", "Error waiting", e);
                }

                Intent intent = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                        .setPackage(context.getPackageName())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 50, pendingIntent);

                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    public static void startQuickSearch(final Launcher launcher) {
        final String provider = Settings.getSearchProvider(launcher);
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
                                                .showAppDetailsForProfile(new ComponentName(GOOGLE_QSB, ".SearchActivity"), Process.myUserHandle());
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
        try {
            launcher.startActivity(new Intent("android.intent.action.VOICE_ASSIST")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setPackage(GOOGLE_QSB));
        } catch (ActivityNotFoundException e) {
            try {
                launcher.getPackageManager().getPackageInfo(GOOGLE_QSB, 0);
                LauncherAppsCompat.getInstance(launcher).showAppDetailsForProfile(new ComponentName(GOOGLE_QSB, ".SearchActivity"), Process.myUserHandle());
            } catch (PackageManager.NameNotFoundException ignored) {
                launcher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")));
            }
        }
    }

    public static String formatDateTime(Context context, long timeInMillis) {
        try {
            String format = Settings.getDateFormat(context);
            String formattedDate;
            if (Utilities.ATLEAST_NOUGAT) {
                DateFormat dateFormat = DateFormat.getInstanceForSkeleton(format, Locale.getDefault());
                dateFormat.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
                formattedDate = dateFormat.format(timeInMillis);
            } else {
                int flags;
                if (format.equals(context.getString(R.string.date_format_long))) {
                    flags = DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE;
                } else if (format.equals(context.getString(R.string.date_format_normal))) {
                    flags = DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
                } else if (format.equals(context.getString(R.string.date_format_short))) {
                    flags = DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;
                } else {
                    flags = DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
                }

                formattedDate = DateUtils.formatDateTime(context, timeInMillis, flags);
            }
            return formattedDate;
        } catch (Throwable t) {
            CrashHelper.logException(new Exception(t));
            return DateUtils.formatDateTime(context, timeInMillis, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        try {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }

            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Throwable t) {
            bitmap = null;
        }
        return bitmap;
    }

    public static int extractAdaptiveBackgroundFromBitmap(Bitmap bitmap) {
        int background;
        try {
            Palette palette = Palette.from(bitmap).generate();
            if (palette.getLightMutedSwatch() != null) {
                background = palette.getLightMutedSwatch().getRgb();
            } else if (palette.getMutedSwatch() != null) {
                background = palette.getMutedSwatch().getRgb();
            } else if (palette.getLightVibrantSwatch() != null) {
                background = palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getVibrantSwatch() != null) {
                background = palette.getVibrantSwatch().getRgb();
            } else if (palette.getDarkVibrantSwatch() != null) {
                background = palette.getDarkVibrantSwatch().getRgb();
            } else if (palette.getDarkMutedSwatch() != null) {
                background = palette.getDarkMutedSwatch().getRgb();
            } else if (palette.getDominantSwatch() != null) {
                background = palette.getDominantSwatch().getRgb();
            } else {
                background = WHITE;
            }
        } catch (Throwable t) {
            background = WHITE;
        }
        return background;
    }

    public static void openAppDrawer(Launcher launcher) {
        launcher.showAppsView(true, false, false);
    }

    public static void openAppSearch(Launcher launcher) {
        launcher.showAppsViewWithSearch(true, false);
    }

    public static void openOverview(Launcher launcher) {
        launcher.showOverviewMode(true);
    }

    public static boolean isBridgeInstalled(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(LauncherClient.BRIDGE_PACKAGE, PackageManager.GET_SIGNATURES);
            if (info.versionName.equals(context.getString(R.string.bridge_download_version))) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    public static void checkBridge(Activity context) {
        if(isBridgeInstalled(context)){
            return;
        }

        FragmentManager fm = context.getFragmentManager();
        if (fm.findFragmentByTag(BRIDGE_TAG) == null) {
            NexusLauncherActivity.InstallFragment fragment = new NexusLauncherActivity.InstallFragment();
            fragment.show(fm, BRIDGE_TAG);
        }
    }

    public static  void installCompanionApp(Context context, File file) {
        Intent install;
        if (Utilities.ATLEAST_NOUGAT) {
            Uri apkUri = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".bridge", file);

            install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            install.setData(apkUri);
            install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            Uri apkUri = Uri.fromFile(file);

            install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(install);
    }

    public static boolean isGoogleBuild(){
        return BuildConfig.FLAVOR.contains("google");
    }

    public static boolean isAmazonBuild(){
        return BuildConfig.FLAVOR.contains("amazon");
    }

    public static Uri getAppUri(){
        if(isAmazonBuild()){
            return Uri.parse("amzn://apps/android?p=" + BuildConfig.APPLICATION_ID);
        }

        return Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
    }

    public static Uri getAppShareUri(){
        if(isAmazonBuild()){
            return Uri.parse("https://www.amazon.com/gp/mas/dl/android?p=" + BuildConfig.APPLICATION_ID);
        }

        return Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
    }

    public static Uri getAppStoreUri(){
        if(isAmazonBuild()){
            return Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=" + BuildConfig.APPLICATION_ID + "&showAll=1");
        }
        return Uri.parse("https://play.google.com/store/apps/dev?id=8683545855643814241");
    }

}
