package dev.dworks.apps.alauncher.extras.firetv.leanbacklauncher.apps;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import dev.dworks.apps.alauncher.extras.firetv.leanbacklauncher.util.FireTVUtils;
import dev.dworks.apps.alauncher.extras.firetv.leanbacklauncher.util.SharedPreferencesUtil;
import dev.dworks.apps.alauncher.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.widget.GuidanceStylist.Guidance;
import androidx.leanback.widget.GuidedAction;

/**
 * TODO: Javadoc
 */
public class AppInfoFragment extends GuidedStepFragment {

    private static final int ACTION_ID_IN_STORE = 1;
    private static final int ACTION_ID_SETTINGS = ACTION_ID_IN_STORE + 1;
    private static final int ACTION_ID_FAVORITE = ACTION_ID_SETTINGS + 1;
    private static final int ACTION_ID_HIDE = ACTION_ID_FAVORITE + 1;

    public static AppInfoFragment newInstance(String title, String pkg, Drawable icon) {
        Bundle args = new Bundle();
        AppInfoFragment fragment = new AppInfoFragment();
        fragment.icon = icon;
        fragment.title = title;
        fragment.pkg = pkg;
        fragment.setArguments(args);
        return fragment;
    }

    private Drawable icon;
    private String title, pkg;

    @NonNull
    @Override
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new Guidance(title,
                pkg,
                "",
                icon);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        Context context = getActivity().getApplicationContext();

        SharedPreferencesUtil util = SharedPreferencesUtil.instance(context);

        GuidedAction action = new GuidedAction.Builder(context)
                .id(ACTION_ID_IN_STORE)
                .title(getString(R.string.app_info_in_store)).build();
        actions.add(action);
        action = new GuidedAction.Builder(context)
                .id(ACTION_ID_SETTINGS)
                .title(getString(R.string.app_info_settings)).build();
        actions.add(action);
        action = new GuidedAction.Builder(context)
                .id(ACTION_ID_FAVORITE)
                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                .checked(util.isFavorite(pkg)) // TODO use full component?
                .title(getString(R.string.app_info_add_favorites)).build();
        actions.add(action);
        action = new GuidedAction.Builder(context)
                .id(ACTION_ID_HIDE)
                .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                .checked(util.isHidden(pkg))
                .title(getString(R.string.app_info_hide_app)).build();
        actions.add(action);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == ACTION_ID_IN_STORE) {
            FireTVUtils.openAppInAmazonStore(getActivity(), pkg);

            getActivity().finish();
        } else if (action.getId() == ACTION_ID_SETTINGS) {
            FireTVUtils.startAppSettings(getActivity(), pkg);

            getActivity().finish();
        } else {
            Context context = getActivity().getApplicationContext();
            SharedPreferencesUtil util = SharedPreferencesUtil.instance(context);

            if (action.getId() == ACTION_ID_FAVORITE) {
                boolean favorited = !util.isFavorite(pkg);

                action.setChecked(favorited);

                if (favorited) {
                    util.favorite(pkg);
                } else {
                    util.unfavorite(pkg);
                }
            } else if (action.getId() == ACTION_ID_HIDE) {
                boolean hidden = !util.isHidden(pkg);

                action.setChecked(hidden);

                if (hidden) {
                    util.hide(pkg);
                } else {
                    util.unhide(pkg);
                }
            }
        }


    }
}