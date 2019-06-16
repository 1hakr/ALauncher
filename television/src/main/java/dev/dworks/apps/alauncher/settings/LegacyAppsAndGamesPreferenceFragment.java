package dev.dworks.apps.alauncher.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedAction.Builder;
import androidx.core.content.res.ResourcesCompat;

import dev.dworks.apps.alauncher.R;
import dev.dworks.apps.alauncher.apps.AppsManager;
import dev.dworks.apps.alauncher.apps.AppsManager.SortingMode;

import java.util.ArrayList;

public class LegacyAppsAndGamesPreferenceFragment extends GuidedStepSupportFragment {
    @NonNull
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new Guidance(getString(R.string.home_screen_order_content_title), getString(R.string.home_screen_order_content_description), getString(R.string.settings_dialog_title), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_settings_home, null));
    }

    public void onResume() {
        super.onResume();
        ArrayList<GuidedAction> actions = new ArrayList<>();
        SortingMode sortingMode = AppsManager.getSavedSortingMode(getActivity());
        actions.add(new Builder(getActivity()).id(1).title(R.string.home_screen_order_content_title).description(sortingMode == SortingMode.FIXED ? R.string.select_app_order_action_description_fixed : R.string.select_app_order_action_description_recency).build());

        // BROKEN
        //if (sortingMode == SortingMode.FIXED) {
        //    actions.add(new Builder(getActivity()).id(2).title((int) R.string.customize_app_order_action_title).build());
        //    actions.add(new Builder(getActivity()).id(3).title((int) R.string.customize_game_order_action_title).build());
        //}

        actions.add(new Builder(getActivity()).id(2).title(R.string.edit_row).build());

        setActions(actions);
    }

    public void onGuidedActionClicked(GuidedAction action) {
        Intent startMain;
        switch ((int) action.getId()) {
            case 1:
                GuidedStepSupportFragment.add(getFragmentManager(), new LegacyAppOrderPreferenceFragment());
                return;
            case 2:
                GuidedStepSupportFragment.add(getFragmentManager(), new LegacyAppRowPreferenceFragment());
                return;
            default:
                return;
        }
    }
}
