package dev.dworks.apps.alauncher.icons;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.launcher3.R;
import com.android.launcher3.util.ComponentKey;
import com.google.android.apps.nexuslauncher.CustomIconUtils;

import androidx.recyclerview.widget.RecyclerView;
import dev.dworks.apps.alauncher.Settings;

public class AppIconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ViewGroup container;
    private ImageView icon;

    private String appComponentName;
    private String packKey;
    private AppIconInfo appIconInfo;

    public AppIconViewHolder(View itemView) {
        super(itemView);
        container = itemView.findViewById(R.id.lean_item_app_icon_container);
        icon = itemView.findViewById(R.id.lean_item_app_icon);

        container.setOnClickListener(this);
    }

    public void bind(String appComponentName, String packKey, AppIconInfo appIconInfo) {
        this.appComponentName = appComponentName;
        this.packKey = packKey;
        this.appIconInfo = appIconInfo;

        Drawable drawable;
        try {
            drawable = itemView.getContext().getPackageManager().getDrawable(packKey, appIconInfo.iconResourceId, null);
        } catch (Throwable t) {
            drawable = null;
        }

        if (drawable == null) {
            icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            icon.setImageDrawable(drawable);
        }
    }

    @Override
    public void onClick(View v) {
        Settings.setCustomIcon(v.getContext(), appComponentName, packKey, appIconInfo.iconResourceId);
        CustomIconUtils.reloadIconByKey(v.getContext(), new ComponentKey(v.getContext(), appComponentName));
        Toast.makeText(v.getContext(), R.string.applying_icon, Toast.LENGTH_SHORT).show();

        if (v.getContext() instanceof Activity) {
            ((Activity) v.getContext()).finish();
        }
    }
}
