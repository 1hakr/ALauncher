package dev.dworks.apps.alauncher.settings;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedAction.Builder;
import androidx.core.content.res.ResourcesCompat;

import dev.dworks.apps.alauncher.R;

import java.util.ArrayList;

public class LegacyHomeScreenPreferenceFragment extends GuidedStepSupportFragment implements RecommendationsPreferenceManager.LoadBlacklistCountCallback {

    @NonNull
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new Guidance(getString(R.string.settings_dialog_title), null, "", ResourcesCompat.getDrawable(getResources(), R.drawable.ic_settings_home, null));
    }

    public void onResume() {
        super.onResume();

        new RecommendationsPreferenceManager(getActivity()).loadBlacklistCount(this);
    }

    public void onBlacklistCountLoaded(int blacklistCount) {
        if (isAdded()) {
            ArrayList<GuidedAction> actions = new ArrayList<>();

            String description = null;
            if (blacklistCount != -1) {
                description = getResources().getQuantityString(R.plurals.recommendation_blacklist_action_description, blacklistCount, Integer.valueOf(blacklistCount));
            }
            actions.add(new Builder(getActivity()).id(1).title(R.string.recommendation_blacklist_action_title).description(description).build());
            actions.add(new Builder(getActivity()).id(2).title(R.string.home_screen_order_action_title).build());
            actions.add(new Builder(getActivity()).id(3).title(R.string.hidden_applications_title).build());
            setActions(actions);
        }
    }

    public void onGuidedActionClicked(GuidedAction action) {
        switch ((int) action.getId()) {
            case 1:
                GuidedStepSupportFragment.add(getFragmentManager(), new LegacyRecommendationsPreferenceFragment());
                return;
            case 2:
                GuidedStepSupportFragment.add(getFragmentManager(), new LegacyAppsAndGamesPreferenceFragment());
                return;
            case 3:
                GuidedStepSupportFragment.add(getFragmentManager(), new LegacyHiddenPreferenceFragment());
                return;
            default:
                return;
        }
    }
}
