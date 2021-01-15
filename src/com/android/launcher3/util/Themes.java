/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.WallpaperColorInfo;

/**
 * Various utility methods associated with themeing.
 */
public class Themes {

    public static final String KEY_DEVICE_THEME = "pref_device_theme";
    private static final String DEVICE_THEME_SYSTEM = "system";
    private static final String DEVICE_THEME_LIGHT = "light";
    private static final String DEVICE_THEME_DARK = "dark";
    private static final String DEVICE_THEME_WALLPAPER = "wallpaper";

    public static int getActivityThemeRes(Context context) {
        WallpaperColorInfo wallpaperColorInfo = WallpaperColorInfo.getInstance(context);
        boolean darkTheme = isDarTheme(context);
        if (darkTheme) {
            if(wallpaperColorInfo.supportsDarkText()){
                return R.style.AppTheme_Dark_DarkText;
            } else if(wallpaperColorInfo.isMainColorDark()) {
                return R.style.AppTheme_Dark_DarkMainColor;
            } else if (darkTheme != isDarkMode(context)){
                return R.style.AppTheme_Dark_Alt;
            } else {
                return  R.style.AppTheme_Dark;
            }
        } else {
            if(wallpaperColorInfo.supportsDarkText()){
                return R.style.AppTheme_DarkText;
            } else if(wallpaperColorInfo.isMainColorDark()) {
                return R.style.AppTheme_DarkMainColor;
            } else if (darkTheme != isDarkMode(context)){
                return R.style.AppTheme_Alt;
            } else {
                return  R.style.AppTheme;
            }
        }
    }

    public static int getSettingActivityThemeRes(Context context) {
        boolean darkTheme = isDarTheme(context);
        if (darkTheme) {
            if (darkTheme != isDarkMode(context)){
                return R.style.Shade_SettingsTheme_Dark;
            } else {
                return  R.style.Shade_SettingsTheme;
            }
        } else {
            if (darkTheme != isDarkMode(context)){
                return R.style.Shade_SettingsTheme_Light;
            } else {
                return  R.style.Shade_SettingsTheme;
            }
        }
    }

    public static boolean isDarTheme(Context context) {
        WallpaperColorInfo wallpaperColorInfo = WallpaperColorInfo.getInstance(context);
        boolean darkTheme = wallpaperColorInfo.isDark();;
        String theme = Utilities.getPrefs(context).getString(KEY_DEVICE_THEME, DEVICE_THEME_SYSTEM);
        if (theme.equals(DEVICE_THEME_LIGHT)) {
            darkTheme = false;
        } else if (theme.equals(DEVICE_THEME_DARK)) {
            darkTheme = true;
        } else if (theme.equals(DEVICE_THEME_WALLPAPER)) {
            darkTheme = wallpaperColorInfo.isDark();
        }  else if(Utilities.ATLEAST_Q) {
            darkTheme = isDarkMode(context);
        }
        return darkTheme;
    }

    public static boolean isDarkMode(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        int nightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return  nightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isTrickyMode(Context context) {
        return isDarkMode(context) != isDarTheme(context);
    }

    public static String getDefaultBodyFont(Context context) {
        TypedArray ta = context.obtainStyledAttributes(android.R.style.TextAppearance_DeviceDefault,
                new int[]{android.R.attr.fontFamily});
        String value = ta.getString(0);
        ta.recycle();
        return value;
    }

    public static float getDialogCornerRadius(Context context) {
        float fallback = context.getResources().getDimension(R.dimen.default_dialog_corner_radius);
        return Utilities.ATLEAST_Q
                ? getDimension(context, android.R.attr.dialogCornerRadius, fallback)
                : fallback;
    }

    public static float getDimension(Context context, int attr, float defaultValue) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        float value = ta.getDimension(0, defaultValue);
        ta.recycle();
        return value;
    }

    public static int getColorAccent(Context context) {
        return getAttrColor(context, android.R.attr.colorAccent);
    }

    public static int getShadeColorAccent(Context context) {
        return getAttrColor(context, R.attr.shadeColorAccent);
    }

    public static int getAttrColor(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    public static boolean getAttrBoolean(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        boolean value = ta.getBoolean(0, false);
        ta.recycle();
        return value;
    }

    public static Drawable getAttrDrawable(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        Drawable value = ta.getDrawable(0);
        ta.recycle();
        return value;
    }

    public static int getAttrInteger(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int value = ta.getInteger(0, 0);
        ta.recycle();
        return value;
    }

    /**
     * Returns the alpha corresponding to the theme attribute {@param attr}, in the range [0, 255].
     */
    public static int getAlpha(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        float alpha = ta.getFloat(0, 0);
        ta.recycle();
        return (int) (255 * alpha + 0.5f);
    }

    /**
     * Scales a color matrix such that, when applied to color R G B A, it produces R' G' B' A' where
     * R' = r * R
     * G' = g * G
     * B' = b * B
     * A' = a * A
     *
     * The matrix will, for instance, turn white into r g b a, and black will remain black.
     *
     * @param color The color r g b a
     * @param target The ColorMatrix to scale
     */
    public static void setColorScaleOnMatrix(int color, ColorMatrix target) {
        target.setScale(Color.red(color) / 255f, Color.green(color) / 255f,
                Color.blue(color) / 255f, Color.alpha(color) / 255f);
    }

    /**
     * Changes a color matrix such that, when applied to srcColor, it produces dstColor.
     *
     * Note that values on the last column of target ColorMatrix can be negative, and may result in
     * negative values when applied on a color. Such negative values will be automatically shifted
     * up to 0 by the framework.
     *
     * @param srcColor The color to start from
     * @param dstColor The color to create by applying target on srcColor
     * @param target The ColorMatrix to transform the color
     */
    public static void setColorChangeOnMatrix(int srcColor, int dstColor, ColorMatrix target) {
        target.reset();
        target.getArray()[4] = Color.red(dstColor) - Color.red(srcColor);
        target.getArray()[9] = Color.green(dstColor) - Color.green(srcColor);
        target.getArray()[14] = Color.blue(dstColor) - Color.blue(srcColor);
        target.getArray()[19] = Color.alpha(dstColor) - Color.alpha(srcColor);
    }

    /**
     * Creates a map for attribute-name to value for all the values in {@param attrs} which can be
     * held in memory for later use.
     */
    public static SparseArray<TypedValue> createValueMap(Context context, AttributeSet attrSet,
            IntArray keysToIgnore) {
        int count = attrSet.getAttributeCount();
        IntArray attrNameArray = new IntArray(count);
        for (int i = 0; i < count; i++) {
            attrNameArray.add(attrSet.getAttributeNameResource(i));
        }
        attrNameArray.removeAllValues(keysToIgnore);

        int[] attrNames = attrNameArray.toArray();
        SparseArray<TypedValue> result = new SparseArray<>(attrNames.length);
        TypedArray ta = context.obtainStyledAttributes(attrSet, attrNames);
        for (int i = 0; i < attrNames.length; i++) {
            TypedValue tv = new TypedValue();
            ta.getValue(i, tv);
            result.put(attrNames[i], tv);
        }

        return result;
    }
}
