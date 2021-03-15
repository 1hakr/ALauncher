package amirz.shade;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.settings.SettingsActivity;
import com.android.launcher3.util.SystemUiController;

import java.util.List;

import amirz.App;
import amirz.aidlbridge.LauncherClientIntent;
import amirz.helpers.DefaultLauncher;
import amirz.helpers.Settings;
import amirz.shade.customization.IconDatabase;
import amirz.shade.customization.IconShapeOverride;
import amirz.shade.customization.ShadeStyle;
import amirz.shade.icons.pack.IconPackManager;
import amirz.shade.settings.ColorListPreference;
import amirz.shade.settings.DockSearchPrefSetter;
import amirz.shade.settings.FeedProviderPrefSetter;
import amirz.shade.settings.IconPackPrefSetter;
import amirz.shade.settings.ReloadingListPreference;
import amirz.shade.util.AppReloader;

import static amirz.shade.ShadeFont.KEY_FONT;
import static amirz.shade.ShadeLauncherCallbacks.KEY_ENABLE_MINUS_ONE;
import static amirz.shade.ShadeLauncherCallbacks.KEY_FEED_PROVIDER;
import static amirz.shade.customization.DockSearch.KEY_DOCK_SEARCH;
import static amirz.shade.customization.IconShapeOverride.KEY_ICON_SHAPE;
import static amirz.shade.customization.ShadeStyle.KEY_THEME;
import static com.android.launcher3.util.Themes.KEY_DEVICE_THEME;

public class ShadeSettings extends SettingsActivity {

    public interface OnResumePreferenceCallback {
        void onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        ShadeStyle.override(this);
        ShadeStyle.overrideTheme(this);
        super.onCreate(savedInstanceState);

        if (Utilities.ATLEAST_OREO && !Utilities.ATLEAST_P) {
            new SystemUiController(getWindow())
                    .updateUiState(SystemUiController.UI_STATE_BASE_WINDOW, true);
        }
        setupActionbar("",true);
    }

    private void setupActionbar(CharSequence title, boolean isHome) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(isHome);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (isHome){
                actionBar.setTitle(R.string.settings_title);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_logo);
            } else {
                actionBar.setTitle(title);
                actionBar.setHomeAsUpIndicator(null);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    public static class ShadeSettingsFragment extends SettingsActivity.LauncherSettingsFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private static final String CATEGORY_STYLE = "pref_screen_style";
        private static final String CATEGORY_SEARCH = "pref_screen_search";
        private static final String CATEGORY_APPS = "pref_screen_apps";
        private static final String CATEGORY_MISC = "pref_screen_misc";
        private static final String CATEGORY_ABOUT = "pref_screen_about";

        private static final String KEY_ICON_PACK = "pref_icon_pack";
        private static final String KEY_APP_VERSION = "pref_app_version";
        private static final String KEY_DONATE = "pref_donate";
        private static final String KEY_APP_INFO = "app_info";
        private static final String KEY_RESTART_LAUNCHER = "pref_restart_launcher";
        private static final String KEY_DEFAULT_LAUNCHER = "pref_default_launcher";
        private static final String KEY_PRO = "pref_pro";
        private static final String KEY_CONTACT = "pref_contact";
        private static final String KEY_REVIEW = "pref_review";
        private Activity context;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            context = getActivity();

            // Load the icon pack once to set the correct default icon pack.
            IconPackManager.get(context);
            if(null == rootKey) {
                Preference purchase = findPreference(KEY_PRO);
                if(null != purchase) {
                    if(App.isPurchased()) {
                        getPreferenceScreen().removePreference(purchase);
                    } else {
                        purchase.setOnPreferenceClickListener(this);
                    }
                }
                Preference contact = findPreference(KEY_CONTACT);
                if(null != contact){
                    contact.setOnPreferenceClickListener(this);
                }

                Preference review = findPreference(KEY_REVIEW);
                if(null != contact){
                    review.setOnPreferenceClickListener(this);
                }
                return;
            }
            if(rootKey.equals(CATEGORY_STYLE)) {
                // Style
                Preference theme = findPreference(KEY_THEME);
                if (null !=  theme){
                    theme.setOnPreferenceChangeListener(this);
                }
                Preference tone = findPreference(KEY_DEVICE_THEME);
                if (null !=  tone){
                    tone.setOnPreferenceChangeListener(this);
                }
                Preference font = findPreference(KEY_FONT);
                if (null !=  font){
                    font.setOnPreferenceChangeListener(this);
                }
            } else if(rootKey.equals(CATEGORY_SEARCH)) {
                ReloadingListPreference search =
                        (ReloadingListPreference) findPreference(KEY_DOCK_SEARCH);
                if (null != search) {
                    search.setOnReloadListener(DockSearchPrefSetter::new);
                }
                ReloadingListPreference feed =
                        (ReloadingListPreference) findPreference(KEY_FEED_PROVIDER);
                if (null != feed) {
                    feed.setOnReloadListener(FeedProviderPrefSetter::new);
                    feed.setOnPreferenceChangeListener(this);
                    List<ApplicationInfo> aiList = LauncherClientIntent.query(context);
                    if (aiList.isEmpty()){
                        Settings.showSnackBar(context, R.string.bridge_missing_message);
                    }
                }
            } else if(rootKey.equals(CATEGORY_APPS)) {
                ReloadingListPreference icons = (ReloadingListPreference) findPreference(KEY_ICON_PACK);
                if (null != icons) {
                    icons.setValue(IconDatabase.getGlobal(context));
                    icons.setOnReloadListener(IconPackPrefSetter::new);
                    icons.setOnPreferenceChangeListener(this);
                }

                PreferenceScreen style = (PreferenceScreen) findPreference(CATEGORY_APPS);
                if (null != style) {
                    Preference iconShapeOverride = findPreference(KEY_ICON_SHAPE);
                    if (iconShapeOverride != null) {
                        if (Utilities.ATLEAST_OREO) {
                            IconShapeOverride.handlePreferenceUi((ListPreference) iconShapeOverride);
                        } else {
                            style.removePreference(iconShapeOverride);
                        }
                    }
                }
            } else if(rootKey.equals(CATEGORY_MISC)) {
                Preference restartLauncher = findPreference(KEY_RESTART_LAUNCHER);
                if (null != restartLauncher) {
                    restartLauncher.setOnPreferenceClickListener(this);
                }

                Preference defaultLauncher = findPreference(KEY_DEFAULT_LAUNCHER);
                if (null != defaultLauncher) {
                    defaultLauncher.setOnPreferenceClickListener(this);
                }
            } else if(rootKey.equals(CATEGORY_ABOUT)) {
                // About
                String versionName = BuildConfig.VERSION_NAME;
                PackageManager pm = context.getPackageManager();
                try {
                    PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
                    versionName = pi.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                Preference version = findPreference(KEY_APP_VERSION);
                if (null != version) {
                    version.setSummary(context.getString(R.string.about_app_version_value,
                            versionName, ""));
                }

                Preference info = findPreference(KEY_APP_INFO);
                if (null != info) {
                    Uri intentData = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                    Intent intent = info.getIntent();
                    if (null != intent) {
                        info.setIntent(intent.setData(intentData));
                    }
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            PreferenceScreen screen = getPreferenceScreen();
            for (int i = 0; i < screen.getPreferenceCount(); i++) {
                Preference preference = screen.getPreference(i);
                if (null !=  preference && preference instanceof PreferenceCategory) {
                    PreferenceCategory cat = (PreferenceCategory) preference;
                    for (int j = 0; j < cat.getPreferenceCount(); j++) {
                        Preference preference2 = cat.getPreference(j);
                        if (null !=  preference2 && preference2 instanceof OnResumePreferenceCallback) {
                            ((OnResumePreferenceCallback) preference2).onResume();
                        }
                    }
                }
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            final PreferenceScreen preferenceScreen = getPreferenceScreen();
            if (preferenceScreen != null) {
                Boolean isHome = TextUtils.isEmpty(preferenceScreen.getTitle());
                CharSequence title = preferenceScreen.getTitle();
                ((ShadeSettings)getActivity()).setupActionbar(title, isHome);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case KEY_FEED_PROVIDER:
                    Utilities.getPrefs(context).edit()
                            .putBoolean(KEY_ENABLE_MINUS_ONE, !TextUtils.isEmpty((String) newValue))
                            .apply();
                    break;
                case KEY_ICON_PACK:
                    IconDatabase.clearAll(context);
                    IconDatabase.setGlobal(context, (String) newValue);
                    AppReloader.get(context).reload();
                    break;
                case KEY_THEME:
                case KEY_DEVICE_THEME:
                case KEY_FONT:
                    startActivity(getActivity().getIntent()
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                        ActivityOptions.makeCustomAnimation(
                                context, R.anim.fade_in, R.anim.fade_out).toBundle());
                    getActivity().recreate();
                    break;
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case KEY_RESTART_LAUNCHER:
                    ShadeRestarter.initiateRestart(context);
                    break;
                case KEY_DEFAULT_LAUNCHER:
                    new DefaultLauncher(getActivity()).launchHomeOrClearDefaultsDialog();
                    break;
                case KEY_CONTACT:
                    Settings.openFeedback(getActivity());
                    break;
                case KEY_REVIEW:
                    Settings.openPlaystore(getActivity());
                    break;
                case KEY_PRO:
                    App.getInstance().openPurchaseActivity(getActivity());
                    break;
            }
            return false;
        }
        private static final String DIALOG_FRAGMENT_TAG =
                "androidx.preference.PreferenceFragment.DIALOG";
        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof ColorListPreference) {
                final DialogFragment f = ColorListPreference.ColorPreferenceFragment.newInstance(preference.getKey());
                f.setTargetFragment(this, 0);
                f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }
}
