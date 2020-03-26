package dev.dworks.apps.alauncher.icons;

import android.content.Context;

import com.android.launcher3.graphics.IconShapeOverride;

import dev.dworks.apps.alauncher.App;

public class IconShapeManager {

    private final Context mContext;

    public IconShapeManager(Context context){
        mContext = context;
    }
    public static IconShapeManager getInstance() {
        return new IconShapeManager(App.getInstance().getApplicationContext());
    }

    public String getOverride() {
        return IconShapeOverride.getAppliedValue(mContext);
    }
}