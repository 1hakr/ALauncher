package dev.dworks.apps.alauncher.extras.firetv.leanbacklauncher.apps;

import android.content.Context;
import android.content.SharedPreferences;

import dev.dworks.apps.alauncher.apps.AppsAdapter;
import dev.dworks.apps.alauncher.apps.LaunchPoint;
import dev.dworks.apps.alauncher.extras.firetv.leanbacklauncher.util.SharedPreferencesUtil;

/**
 * Created by rockon999 on 3/7/18.
 */

public class FavoritesAdapter extends AppsAdapter implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferencesUtil prefUtil;
    private SharedPreferences.OnSharedPreferenceChangeListener listener = this;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        refreshDataSetAsync();
    }

    class FavoritesAppFilter extends AppFilter {

        @Override
        public boolean include(LaunchPoint point) {
            return prefUtil != null && prefUtil.isFavorite(point.getPackageName());
        }
    }

    public FavoritesAdapter(Context context, ActionOpenLaunchPointListener actionOpenLaunchPointListener, AppCategory... appTypes) {
        super(context, actionOpenLaunchPointListener, appTypes);

        this.prefUtil = SharedPreferencesUtil.instance(context);
        this.mFilter = new FavoritesAppFilter();

        this.prefUtil.addFavoritesListener(listener);
    }
}
