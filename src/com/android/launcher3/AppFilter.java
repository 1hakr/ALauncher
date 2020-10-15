package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.util.ResourceBasedOverride;

public class AppFilter implements ResourceBasedOverride {

    public static AppFilter newInstance(Context context) {
        return Overrides.getObject(AppFilter.class, context, R.string.app_filter_class);
    }

    public boolean shouldShowApp(ComponentName app) {
        return true;
    }

    public boolean shouldShowApp(ComponentName app, UserHandle user) {
        return shouldShowApp(app);
    }
}
