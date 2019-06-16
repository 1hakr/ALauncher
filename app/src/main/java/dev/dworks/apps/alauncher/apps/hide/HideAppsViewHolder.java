package dev.dworks.apps.alauncher.apps.hide;

import android.view.View;

import com.android.launcher3.AppInfo;
import com.android.launcher3.util.ComponentKey;
import com.google.android.apps.nexuslauncher.CustomAppFilter;

import dev.dworks.apps.alauncher.apps.AppsSelectionViewHolder;

public class HideAppsViewHolder extends AppsSelectionViewHolder {

    public HideAppsViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setAppInfo(AppInfo appInfo) {
        super.setAppInfo(appInfo);
        isHidden.setChecked(CustomAppFilter.isHidden(itemView.getContext(), new ComponentKey(appInfo.componentName, appInfo.user)));
    }

    @Override
    public void toggleSelection() {
        boolean currentState = isHidden.isChecked();
        CustomAppFilter.hideComponent(itemView.getContext(), new ComponentKey(appInfo.componentName, appInfo.user), !currentState);
        isHidden.setChecked(!currentState);
    }
}
