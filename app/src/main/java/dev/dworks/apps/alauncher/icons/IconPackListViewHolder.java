package dev.dworks.apps.alauncher.icons;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.util.ComponentKey;
import com.google.android.apps.nexuslauncher.CustomIconUtils;

import androidx.recyclerview.widget.RecyclerView;
import dev.dworks.apps.alauncher.Settings;

public class IconPackListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private String componentName;
    private String packageName;

    private String key;
    private CharSequence value;

    private ViewGroup container;
    private TextView name;

    public IconPackListViewHolder(View itemView, String componentName, String packageName) {
        super(itemView);
        this.componentName = componentName;
        this.packageName = packageName;

        container = itemView.findViewById(R.id.item_icon_pack_container);
        name = itemView.findViewById(R.id.item_icon_pack_name);

        container.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.equals(Settings.SYSTEM_DEFAULT_ICON_KEY, key) && TextUtils.equals(Settings.SYSTEM_DEFAULT_ICON_VALUE, value)) {
            Settings.setCustomIcon(v.getContext(), componentName, Settings.SYSTEM_DEFAULT_ICON_PACK, Settings.SYSTEM_DEFAULT_ICON_RES_ID);
            CustomIconUtils.reloadIconByKey(v.getContext(), new ComponentKey(v.getContext(), componentName));
        } else {
            IconPickerActivity.fromPackForApp(v.getContext(), componentName, packageName, key, value);
        }

        if (v.getContext() instanceof Activity) {
            ((Activity) v.getContext()).finish();
        }
    }

    public void bind(String key, CharSequence value) {
        this.key = key;
        this.value = value;

        if (TextUtils.equals(Settings.SYSTEM_DEFAULT_ICON_VALUE, value)) {
            name.setText(R.string.system_default);
        } else {
            name.setText(value);
        }
    }
}
