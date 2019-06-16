package dev.dworks.apps.alauncher.apps.lock;

import android.view.View;

import com.android.launcher3.AppInfo;
import com.android.launcher3.util.ComponentKey;

import dev.dworks.apps.alauncher.apps.AppsSelectionViewHolder;

public class LockAppsViewHolder extends AppsSelectionViewHolder {

    public LockAppsViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setAppInfo(AppInfo appInfo) {
        super.setAppInfo(appInfo);
        isHidden.setChecked(AppLockHelper.isSecured(itemView.getContext(), new ComponentKey(appInfo.componentName, appInfo.user)));
    }

    @Override
    public void toggleSelection() {
        boolean currentState = isHidden.isChecked();
        AppLockHelper.secureComponent(itemView.getContext(), new ComponentKey(appInfo.componentName, appInfo.user), !currentState);
        isHidden.setChecked(!currentState);
    }
}
