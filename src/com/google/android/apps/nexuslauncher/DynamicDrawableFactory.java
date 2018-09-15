package com.google.android.apps.nexuslauncher;

import android.content.Context;
import android.os.Process;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.DrawableFactory;
import com.google.android.apps.nexuslauncher.clock.DynamicClock;

public class DynamicDrawableFactory extends DrawableFactory {
    private final DynamicClock mDynamicClockDrawer;

    public DynamicDrawableFactory(Context context) {
        mDynamicClockDrawer = new DynamicClock(context);
    }

    @Override
    public FastBitmapDrawable newIcon(ItemInfoWithIcon info) {
        if (info != null &&
                Utilities.ATLEAST_OREO &&
                info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                DynamicClock.DESK_CLOCK.equals(info.getTargetComponent()) &&
                info.user.equals(Process.myUserHandle())) {
            return mDynamicClockDrawer.drawIcon(info.iconBitmap);
        }
        return super.newIcon(info);
    }
}
