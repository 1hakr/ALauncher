package dev.dworks.apps.alauncher.apps;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.AppInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;

import androidx.recyclerview.widget.RecyclerView;
import dev.dworks.apps.alauncher.App;

public abstract class AppsSelectionViewHolder extends RecyclerView.ViewHolder {

    protected ViewGroup container;
    protected ImageView launcherIcon;
    protected TextView appName;
    protected TextView className;
    protected CheckBox isHidden;
    protected AppInfo appInfo;

    public AppsSelectionViewHolder(View itemView) {
        super(itemView);
        container = (ViewGroup) itemView.findViewById(R.id.hide_app_container);
        launcherIcon = (ImageView) itemView.findViewById(R.id.hide_app_icon);
        appName = (TextView) itemView.findViewById(R.id.hide_app_name);
        className = (TextView) itemView.findViewById(R.id.hide_app_class_name);
        isHidden = (CheckBox) itemView.findViewById(R.id.hide_app_checkbox);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!App.isPurchased()){
                    App.openPurchaseActivity(v.getContext());
                    return;
                }
                toggleSelection();
            }
        });
    }

    public void setAppInfo(AppInfo appInfo){
        this.appInfo = appInfo;

        if (appInfo.usingLowResIcon) {
            LauncherAppState.getInstance(itemView.getContext()).getIconCache().getTitleAndIcon(appInfo, false);
        }
        launcherIcon.setImageBitmap(appInfo.iconBitmap);
        appName.setText(appInfo.title);
        className.setText(appInfo.componentName.getClassName());
    }

    public abstract void toggleSelection();
}
