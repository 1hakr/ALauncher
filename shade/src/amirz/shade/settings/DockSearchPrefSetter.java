package amirz.shade.settings;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.android.launcher3.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import amirz.shade.customization.DockSearch;

public class DockSearchPrefSetter implements ReloadingListPreference.OnReloadListener {
    private final Context mContext;
    private final PackageManager mPm;

    public DockSearchPrefSetter(Context context) {
        mContext = context;
        mPm = context.getPackageManager();
    }

    @Override
    public Runnable listUpdater(ReloadingListPreference pref) {
        List<AppWidgetProviderInfo> widgets = DockSearch.validWidgets(mContext);
        String defaultValue = DockSearch.getRecommendedProvider(mContext);
        CharSequence[] keys = new String[widgets.size() + 1];
        CharSequence[] values = new String[keys.length];
        int i = 0;

        // First value, system default
        keys[i] = mContext.getResources().getString(R.string.pref_value_disabled);
        values[i++] = "";

        Collections.sort(widgets, (o1, o2) ->
                normalize(o1.loadLabel(mPm)).compareTo(normalize(o2.loadLabel(mPm))));

        for (AppWidgetProviderInfo widget : widgets) {
            keys[i] = widget.loadLabel(mPm);
            String pkg = widget.provider.getPackageName();
            try {
                CharSequence app = mPm.getApplicationInfo(pkg, 0).loadLabel(mPm);
                if (!keys[i].toString().startsWith(app.toString())) {
                    keys[i] = mContext.getString(R.string.dock_search_value, app, keys[i]);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String provider = widget.provider.flattenToShortString();
            values[i++] = provider;
        }

        return () -> {
            pref.setEntriesWithValues(keys, values);
            pref.setDefaultValue(defaultValue);
            String v = pref.getValue();
            if (!TextUtils.isEmpty(v) && !Arrays.asList(values).contains(v)) {
                pref.setValue(defaultValue);
            }
        };
    }

    private String normalize(String title) {
        return title.toLowerCase();
    }
}
