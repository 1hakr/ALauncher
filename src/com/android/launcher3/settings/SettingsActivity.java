/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.settings;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragment.OnPreferenceStartFragmentCallback;
import androidx.preference.PreferenceFragment.OnPreferenceStartScreenCallback;
import androidx.preference.PreferenceGroup.PreferencePositionCallback;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.LauncherFiles;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.graphics.GridOptionsProvider;
import com.android.launcher3.uioverrides.plugins.PluginManagerWrapper;
import com.android.launcher3.util.SecureSettingsObserver;

import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS;
import static com.android.launcher3.SessionCommitReceiver.ADD_ICON_PREFERENCE_KEY;
import static com.android.launcher3.states.RotationHelper.ALLOW_ROTATION_PREFERENCE_KEY;
import static com.android.launcher3.states.RotationHelper.getAllowRotationDefaultValue;
import static com.android.launcher3.util.SecureSettingsObserver.newNotificationSettingsObserver;

import amirz.shade.ShadeSettings;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends Activity
        implements OnPreferenceStartFragmentCallback, OnPreferenceStartScreenCallback,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String DEVELOPER_OPTIONS_KEY = "pref_developer_options";
    private static final String FLAGS_PREFERENCE_KEY = "flag_toggler";

    private static final String NOTIFICATION_DOTS_PREFERENCE_KEY = "pref_icon_badging";
    /** Hidden field Settings.Secure.ENABLED_NOTIFICATION_LISTENERS */
    private static final String NOTIFICATION_ENABLED_LISTENERS = "enabled_notification_listeners";

    public static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
    public static final String EXTRA_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args";
    private static final int DELAY_HIGHLIGHT_DURATION_MILLIS = 600;
    public static final String SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted";

    public static final String GRID_OPTIONS_PREFERENCE_KEY = "pref_grid_options";

    public static int topInset;
    public static int bottomInset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            String prefKey = getIntent().getStringExtra(EXTRA_FRAGMENT_ARG_KEY);
            if (!TextUtils.isEmpty(prefKey)) {
                args.putString(EXTRA_FRAGMENT_ARG_KEY, prefKey);
            }

            Fragment f = Fragment.instantiate(
                    this, getString(R.string.settings_fragment_name), args);
            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, f)
                    .commit();

            getFragmentManager().executePendingTransactions();
            applyInsetsToFragment((PreferenceFragment) f);

        }
        Utilities.getPrefs(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        getInsets(getWindow().getDecorView());
    }

    private void enableEdgeToEdge() {
        // Make the activity content extend behind the system bars
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Optional: Make system bars translucent/transparent
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), decorView);

        // Light status bar if needed (for dark backgrounds)
        // Remove this if your theme already handles this
        insetsController.setAppearanceLightStatusBars(false);

        // Make navigation bar translucent if desired
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    public void getInsets(View view) {
        if (view != null) {
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

                return insets;
            });
            ViewCompat.requestApplyInsets(view);
        }
    }

    public static void applyInsets(View listView) {
        if (listView != null) {
            listView.setPadding(listView.getPaddingLeft(), topInset, listView.getPaddingRight(), bottomInset);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (GRID_OPTIONS_PREFERENCE_KEY.equals(key)) {

            final ComponentName cn = new ComponentName(getApplicationContext(),
                    GridOptionsProvider.class);
            Context c = getApplicationContext();
            int oldValue = c.getPackageManager().getComponentEnabledSetting(cn);
            int newValue;
            if (Utilities.getPrefs(c).getBoolean(GRID_OPTIONS_PREFERENCE_KEY, false)) {
                newValue = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            } else {
                newValue = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            }

            if (oldValue != newValue) {
                c.getPackageManager().setComponentEnabledSetting(cn, newValue,
                        PackageManager.DONT_KILL_APP);
            }
        }
    }

    private boolean startFragment(String fragment, Bundle args, String key) {
        if (Utilities.ATLEAST_P && getFragmentManager().isStateSaved()) {
            // Sometimes onClick can come after onPause because of being posted on the handler.
            // Skip starting new fragments in that case.
            return false;
        }
        Fragment f = Fragment.instantiate(this, fragment, args);
        if (f instanceof DialogFragment) {
            ((DialogFragment) f).show(getFragmentManager(), key);
        } else {
            getFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, f)
                    .addToBackStack(key)
                    .commit();

            getFragmentManager().executePendingTransactions();
            applyInsetsToFragment((PreferenceFragment) f);
        }
        return true;
    }

    private void applyInsetsToFragment(PreferenceFragment fragment) {
        View view = fragment.getView();
        if (view != null) {
            applyInsets(fragment.getListView());
        }
    }

    @Override
    public boolean onPreferenceStartFragment(
            PreferenceFragment preferenceFragment, Preference pref) {
        return startFragment(pref.getFragment(), pref.getExtras(), pref.getKey());
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        Bundle args = new Bundle();
        args.putString(PreferenceFragment.ARG_PREFERENCE_ROOT, pref.getKey());
        return startFragment(getString(R.string.settings_fragment_name), args, pref.getKey());
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends PreferenceFragment {

        private SecureSettingsObserver mNotificationDotsObserver;

        private String mHighLightKey;
        private boolean mPreferenceHighlighted = false;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                applyInsets(getListView());
                return insets;
            });
            applyInsets(getListView());
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            final Bundle args = getArguments();
            mHighLightKey = args == null ? null : args.getString(EXTRA_FRAGMENT_ARG_KEY);
            if (rootKey == null && !TextUtils.isEmpty(mHighLightKey)) {
                rootKey = getParentKeyForPref(mHighLightKey);
            }

            if (savedInstanceState != null) {
                mPreferenceHighlighted = savedInstanceState.getBoolean(SAVE_HIGHLIGHTED_KEY);
            }

            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            setPreferencesFromResource(R.xml.launcher_preferences_plus, rootKey);

            PreferenceScreen screen = getPreferenceScreen();
            for (int i = screen.getPreferenceCount() - 1; i >= 0; i--) {
                Preference preference = screen.getPreference(i);
                if (preference instanceof PreferenceCategory) {
                    PreferenceCategory category = (PreferenceCategory) preference;
                    for (int j = 0; j < category.getPreferenceCount(); j++) {
                        Preference nestedPref = category.getPreference(j);
                        if (!initPreference(nestedPref)) {
                            category.removePreference(nestedPref);
                        }
                    }
                } else if (!initPreference(preference)) {
                    screen.removePreference(preference);
                }
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(SAVE_HIGHLIGHTED_KEY, mPreferenceHighlighted);
        }

        protected String getParentKeyForPref(String key) {
            return null;
        }

        /**
         * Initializes a preference. This is called for every preference. Returning false here
         * will remove that preference from the list.
         */
        protected boolean initPreference(Preference preference) {
            switch (preference.getKey()) {
                case NOTIFICATION_DOTS_PREFERENCE_KEY:
                    if (!getResources().getBoolean(R.bool.notification_dots_enabled)) {
                        return false;
                    }

                    // Listen to system notification dot settings while this UI is active.
                    mNotificationDotsObserver = newNotificationSettingsObserver(
                            getActivity(), (NotificationDotsPreference) preference);
                    mNotificationDotsObserver.register();
                    // Also listen if notification permission changes
                    mNotificationDotsObserver.getResolver().registerContentObserver(
                            Settings.Secure.getUriFor(NOTIFICATION_ENABLED_LISTENERS), false,
                            mNotificationDotsObserver);
                    mNotificationDotsObserver.dispatchOnChange();
                    return true;

                case ADD_ICON_PREFERENCE_KEY:
                    return Utilities.ATLEAST_OREO;

                case ALLOW_ROTATION_PREFERENCE_KEY:
                    if (getResources().getBoolean(R.bool.allow_rotation)) {
                        // Launcher supports rotation by default. No need to show this setting.
                        return false;
                    }
                    // Initialize the UI once
                    preference.setDefaultValue(getAllowRotationDefaultValue());
                    return true;

                case FLAGS_PREFERENCE_KEY:
                    // Only show flag toggler UI if this build variant implements that.
                    return FeatureFlags.showFlagTogglerUi(getActivity());

                case DEVELOPER_OPTIONS_KEY:
                    // Show if plugins are enabled or flag UI is enabled.
                    return FeatureFlags.showFlagTogglerUi(getActivity()) ||
                            PluginManagerWrapper.hasPlugins(getActivity());
                case GRID_OPTIONS_PREFERENCE_KEY:
                    return Utilities.isDevelopersOptionsEnabled(getActivity()) &&
                            Utilities.IS_DEBUG_DEVICE &&
                            Utilities.existsStyleWallpapers(getActivity());
            }

            return true;
        }

        @Override
        public void onResume() {
            super.onResume();

            if (isAdded() && !mPreferenceHighlighted) {
                PreferenceHighlighter highlighter = createHighlighter();
                if (highlighter != null) {
                    getView().postDelayed(highlighter, DELAY_HIGHLIGHT_DURATION_MILLIS);
                    mPreferenceHighlighted = true;
                } else {
                    requestAccessibilityFocus(getListView());
                }
            }
        }

        private PreferenceHighlighter createHighlighter() {
            if (TextUtils.isEmpty(mHighLightKey)) {
                return null;
            }

            PreferenceScreen screen = getPreferenceScreen();
            if (screen == null) {
                return null;
            }

            RecyclerView list = getListView();
            PreferencePositionCallback callback = (PreferencePositionCallback) list.getAdapter();
            int position = callback.getPreferenceAdapterPosition(mHighLightKey);
            return position >= 0 ? new PreferenceHighlighter(list, position) : null;
        }

        private void requestAccessibilityFocus(@NonNull final RecyclerView rv) {
            rv.post(() -> {
                if (!rv.hasFocus() && rv.getChildCount() > 0) {
                    rv.getChildAt(0)
                            .performAccessibilityAction(ACTION_ACCESSIBILITY_FOCUS, null);
                }
            });
        }

        @Override
        public void onDestroy() {
            if (mNotificationDotsObserver != null) {
                mNotificationDotsObserver.unregister();
                mNotificationDotsObserver = null;
            }
            super.onDestroy();
        }
    }
}
