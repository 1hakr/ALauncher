/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.nexuslauncher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.widget.WidgetsBottomSheet;

import dev.dworks.apps.alauncher.App;
import dev.dworks.apps.alauncher.Settings;
import dev.dworks.apps.alauncher.apps.lock.AppLockHelper;
import dev.dworks.apps.alauncher.helpers.Utils;
import dev.dworks.apps.alauncher.icons.IconPackListActivity;

public class CustomBottomSheet extends WidgetsBottomSheet {
    private FragmentManager mFragmentManager;

    public CustomBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFragmentManager = Launcher.getLauncher(context).getFragmentManager();
    }

    @Override
    public void populateAndShow(final ItemInfo itemInfo) {
        super.populateAndShow(itemInfo);
        final EditText editText = (EditText) findViewById(R.id.title);
        editText.setText(itemInfo.title);
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    handleAppNameChange(v.getContext(), editText.getText().toString(), itemInfo.getTargetComponent());
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                handleAppNameChange(v.getContext(), editText.getText().toString(), itemInfo.getTargetComponent());
                return true;
            }
        });
        ((PrefsFragment) mFragmentManager.findFragmentById(R.id.sheet_prefs)).loadForApp(itemInfo);
    }

    @Override
    public void onDetachedFromWindow() {
        Fragment pf = mFragmentManager.findFragmentById(R.id.sheet_prefs);
        if (pf != null) {
            mFragmentManager.beginTransaction().remove(pf).commitAllowingStateLoss();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWidgetsBound() {
    }

    private void handleAppNameChange(Context context, String newName, ComponentName componentName) {
        Settings.setCustomAppName(context, componentName, newName);
        Utils.reload(context);
    }

    public static class PrefsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private static final String PREF_OVERRIDE_ICON = "pref_override_app_icon";
        private final static String PREF_PACK = "pref_app_icon_pack";
        private final static String PREF_HIDE = "pref_app_hide";
        private final static String PREF_LOCK = "pref_app_lock";
        private Preference mPrefOverride;
        private SwitchPreference mPrefPack;
        private SwitchPreference mPrefHide;
        private SwitchPreference mPrefLock;

        private ComponentKey mKey;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_edit_prefs);
        }

        public void loadForApp(ItemInfo itemInfo) {
            mKey = new ComponentKey(itemInfo.getTargetComponent(), itemInfo.user);

            mPrefOverride = findPreference(PREF_OVERRIDE_ICON);
            mPrefPack = (SwitchPreference) findPreference(PREF_PACK);
            mPrefHide = (SwitchPreference) findPreference(PREF_HIDE);
            mPrefLock = (SwitchPreference) findPreference(PREF_LOCK);

            Context context = getActivity();
            CustomDrawableFactory factory = (CustomDrawableFactory) DrawableFactory.get(context);

            ComponentName componentName = itemInfo.getTargetComponent();
            boolean enable = factory.packCalendars.containsKey(componentName) || factory.packComponents.containsKey(componentName);
            mPrefPack.setEnabled(enable);
            mPrefPack.setChecked(enable && CustomIconProvider.isEnabledForApp(context, mKey));
            if (enable) {
                PackageManager pm = context.getPackageManager();
                try {
                    mPrefPack.setSummary(pm.getPackageInfo(factory.iconPack, 0).applicationInfo.loadLabel(pm));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            mPrefHide.setChecked(CustomAppFilter.isHiddenApp(context, mKey));
            mPrefPack.setOnPreferenceChangeListener(this);
            mPrefHide.setOnPreferenceChangeListener(this);
            mPrefLock.setOnPreferenceChangeListener(this);

            mPrefPack.setOnPreferenceClickListener(this);
            mPrefHide.setOnPreferenceClickListener(this);
            mPrefLock.setOnPreferenceClickListener(this);
            mPrefOverride.setOnPreferenceClickListener(this);

            getPreferenceScreen().removePreference(findPreference(PREF_PACK));
            getPreferenceScreen().removePreference(findPreference(PREF_LOCK));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean enabled = (boolean) newValue;
            Launcher launcher = Launcher.getLauncher(getActivity());
            if(!App.isPurchased()) {
                return true;
            }
            switch (preference.getKey()) {
                case PREF_PACK:
                    CustomIconProvider.setAppState(launcher, mKey, enabled);
                    CustomIconUtils.reloadIconByKey(launcher, mKey);
                    break;
                case PREF_HIDE:
                    CustomAppFilter.setComponentNameState(launcher, mKey, enabled);
                    break;
                case PREF_LOCK:
                    AppLockHelper.setComponentNameState(launcher, mKey, enabled);
                    break;
            }
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case PREF_PACK:
                    if(!App.isPurchased()){
                        App.openPurchaseActivity(getActivity());
                    }
                    IconPackListActivity.openForComponent(getActivity(), mKey);
                    return true;
                case PREF_HIDE:
                case PREF_LOCK:
                    if(!App.isPurchased()){
                        App.openPurchaseActivity(getActivity());
                    }
                    return true;
                case PREF_OVERRIDE_ICON:
                    if(!App.isPurchased()){
                        App.openPurchaseActivity(getActivity());
                    } else {
                        IconPackListActivity.openForComponent(getActivity(), mKey);
                    }
                    return true;
            }
            return false;
        }
    }
}
