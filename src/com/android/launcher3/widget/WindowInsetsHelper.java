package com.android.launcher3.widget;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.android.launcher3.Utilities;

import java.lang.reflect.Field;

public class WindowInsetsHelper {

    final static String TAG = "WindowInsetsCompat";

    private static Field sViewAttachInfoField;
    private static Field sStableInsets;
    private static Field sContentInsets;
    private static boolean sReflectionSucceeded;

    static {
        try {
            sViewAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            sViewAttachInfoField.setAccessible(true);
            Class<?> sAttachInfoClass = Class.forName("android.view.View$AttachInfo");
            sStableInsets = sAttachInfoClass.getDeclaredField("mStableInsets");
            sStableInsets.setAccessible(true);
            sContentInsets = sAttachInfoClass.getDeclaredField("mContentInsets");
            sContentInsets.setAccessible(true);
            sReflectionSucceeded = true;
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "Failed to get visible insets from AttachInfo " + e.getMessage(), e);
        }
    }

    @Nullable
    public static WindowInsetsCompat getRootWindowInsets(@NonNull View v) {
        if (Utilities.ATLEAST_MARSHMALLOW) {
            WindowInsets mTempRect = v.getRootWindowInsets();
            if(null != mTempRect) {
                return WindowInsetsCompat.toWindowInsetsCompat(mTempRect);
            }
        }
        if (!sReflectionSucceeded || !v.isAttachedToWindow()) {
            return null;
        }

        View rootView = v.getRootView();
        try {
            Object attachInfo = sViewAttachInfoField.get(rootView);
            if (attachInfo != null) {
                Rect stableInsets = (Rect) sStableInsets.get(attachInfo);
                Rect visibleInsets = (Rect) sContentInsets.get(attachInfo);
                if (stableInsets != null && visibleInsets != null) {
                    WindowInsetsCompat insets = new WindowInsetsCompat.Builder()
                            .setStableInsets(Insets.of(stableInsets))
                            .setSystemWindowInsets(Insets.of(visibleInsets))
                            .build();

                    // The WindowInsetsCompat instance still needs to know about
                    // what the root window insets, and the root view visible bounds are
//                    insets.setRootWindowInsets(insets);
//                    insets.copyRootViewBounds(v.getRootView());
                    return insets;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get insets from AttachInfo. " + e.getMessage(), e);
        }
        return null;
    }
}
