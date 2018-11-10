package dev.dworks.apps.alauncher.settings;

import android.os.Bundle;

import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.res.ResourcesCompat;

import dev.dworks.apps.alauncher.R;

public class LegacyHomeScreenSettingsActivity extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.settings_dialog_bg_protection, null));

        if (savedInstanceState == null) {
            GuidedStepSupportFragment.addAsRoot(this, new LegacyHomeScreenPreferenceFragment(), 16908290);
        }
    }
}
