package dev.dworks.apps.alauncher.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.TtsSpan.TextBuilder;
import android.widget.Toast;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;

import java.util.List;
import java.util.Objects;

public class DefaultLauncher {

    private static final String LAUNCHER_CLASS = "com.android.launcher.launcher3.Launcher";
    private static final String LAUNCHER_PACKAGE = "com.android.launcher";
    enum HomeState {
        GEL_IS_DEFAULT, OTHER_LAUNCHER_IS_DEFAULT, NO_DEFAULT
    }

    private Activity activity;

    public DefaultLauncher(Activity activity) {
        this.activity = activity;
    }

    public boolean launchHomeOrClearDefaultsDialog() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveActivity = activity.getPackageManager().resolveActivity(
                intent, 0);
        if(resolveActivity != null && Objects.equals(resolveActivity.activityInfo.processName, BuildConfig.APPLICATION_ID)){
            Toast.makeText(activity, R.string.launcher_is_already_default, Toast.LENGTH_SHORT).show();
            return true;
        }
        HomeState homeState = (LAUNCHER_PACKAGE.equals(resolveActivity.activityInfo.applicationInfo.packageName)
                && LAUNCHER_CLASS.equals(resolveActivity.activityInfo.name)) ? HomeState.GEL_IS_DEFAULT
                : (resolveActivity == null || resolveActivity.activityInfo == null ||
                !inResolveInfoList(resolveActivity, activity.getPackageManager()
                        .queryIntentActivities(intent, 0))) ? HomeState.NO_DEFAULT
                : HomeState.OTHER_LAUNCHER_IS_DEFAULT;
        switch (homeState) {
            case GEL_IS_DEFAULT:
            case NO_DEFAULT:
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return true;
            default:
                showClearDefaultsDialog(resolveActivity);
                return false;
        }
    }

    @SuppressLint("NewApi")
    private void showClearDefaultsDialog(ResolveInfo resolveInfo) {
        CharSequence string;
        final Intent intent;
        CharSequence loadLabel = resolveInfo.loadLabel(activity.getPackageManager());
        if (activity.getPackageManager().resolveActivity(
                new Intent("android.settings.HOME_SETTINGS"), 0) == null) {
            string = activity.getString(R.string.change_default_home_dialog_body,
                    new Object[]{loadLabel});
            intent = new Intent(
                    "android.settings.APPLICATION_DETAILS_SETTINGS",
                    Uri.fromParts("package",
                            resolveInfo.activityInfo.packageName, null));
        } else {
            intent = new Intent("android.settings.HOME_SETTINGS");
            string = new SpannableString(activity.getString(
                    R.string.change_default_home_dialog_body_settings,
                    new Object[]{loadLabel}));
            ((SpannableString) string)
                    .setSpan(
                            new TextBuilder(
                                    activity.getString(
                                            R.string.change_default_home_dialog_body_settings_tts,
                                            loadLabel)).build(), 0, string
                                    .length(), 18);
        }


        new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(string)
                .setNegativeButton(
                        activity.getString(R.string.change_default_home_dialog_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.finish();
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                    }
                })
                .setPositiveButton(
                        activity.getString(R.string.change_default_home_dialog_proceed),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    intent.setFlags(276856832);
                                    activity.startActivity(intent);
                                } catch (Exception e) {
                                    setDefLauncher(activity);
                                }
                            }
                        }).create().show();
    }

    private boolean inResolveInfoList(ResolveInfo resolveInfo, List<ResolveInfo> list) {
        for (ResolveInfo resolveInfo2 : list) {
            if (resolveInfo2.activityInfo.name
                    .equals(resolveInfo.activityInfo.name)
                    && resolveInfo2.activityInfo.packageName
                    .equals(resolveInfo.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setDefLauncher(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, NexusLauncherActivity.class);
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(selector);
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}