package amirz.shade.customization;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;

import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;
import com.android.searchlauncher.SmartspaceQsbWidget;

import java.util.ArrayList;
import java.util.List;

public class DockSearch {
    public static final String KEY_DOCK_SEARCH = "pref_dock_search";
    public static final String DEFAULT_PROVIDER = "com.google.android.googlequicksearchbox/.SearchWidgetProvider";
    private static final String WIDGET_CLASS_NAME = "com.google.android.googlequicksearchbox.SearchWidgetProvider";


    public static String getDockSearch(Context context) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        return prefs.getString(KEY_DOCK_SEARCH, DockSearch.getRecommendedProvider(context));
    }

    public static AppWidgetProviderInfo getWidgetInfo(Context context) {
        String val = getDockSearch(context);
        for (AppWidgetProviderInfo info : validWidgets(context)) {
            if (val.equals(info.provider.flattenToShortString())) {
                return info;
            }
        }
        return null;
    }

    public static List<AppWidgetProviderInfo> validWidgets(Context context) {
        int highestMinHeight = context.getResources()
                .getDimensionPixelSize(R.dimen.qsb_wrapper_height);
        List<AppWidgetProviderInfo> widgets = new ArrayList<>();
        AppWidgetManagerCompat widgetManager = AppWidgetManagerCompat.getInstance(context);
        for (AppWidgetProviderInfo widgetInfo : widgetManager.getAllProviders(null)) {
            LauncherAppWidgetProviderInfo launcherwidgetInfo  = LauncherAppWidgetProviderInfo.fromProviderInfo(context, widgetInfo);
            Boolean isLarge = launcherwidgetInfo.spanX >= 4;
            if (isLarge && widgetInfo.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL
                    && Math.min(widgetInfo.minHeight, widgetInfo.minResizeHeight) <= highestMinHeight) {
                widgets.add(widgetInfo);
            }
        }
        return widgets;
    }

    public static String getRecommendedProvider(Context context) {
        for (AppWidgetProviderInfo next : AppWidgetManager.getInstance(context).getInstalledProviders()) {
            if (next.getProfile().equals(Process.myUserHandle())
                    && SmartspaceQsbWidget.WIDGET_PACKAGE_NAME.equals(next.provider.getPackageName())
            && WIDGET_CLASS_NAME.equals(next.provider.getClassName())) {
                return next.provider.flattenToShortString();
            }
        }
        return "";
    }
}
