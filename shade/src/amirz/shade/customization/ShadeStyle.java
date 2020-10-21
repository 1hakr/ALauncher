package amirz.shade.customization;

import android.app.Activity;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Themes;

import java.util.HashMap;
import java.util.Map;

public class ShadeStyle {
    public static final String KEY_THEME = "pref_theme";
    public static final int[] THEMES = {R.style.ShadeOverride_Shade, R.style.ShadeOverride_Campfire,
            R.style.ShadeOverride_Sunset, R.style.ShadeOverride_Sunrise, R.style.ShadeOverride_Forest,
            R.style.ShadeOverride_Ocean, R.style.ShadeOverride_Twilight, R.style.ShadeOverride_Blossom,
            R.style.ShadeOverride_Midnight, R.style.ShadeOverride_Transparent};
    public static final String[] COLORS = {"#009688", "#D32F2F",
            "#FF8B27", "#FFD600", "#388E3C",  "#03A9F4",
            "#855CCC", "#C51162", "#0F0F0F", "#00FFFFFF"};

    public static void override(Activity activity) {
        String theme = Utilities.getPrefs(activity).getString(KEY_THEME, "");

        Map<String, Integer> themes = new HashMap<>();
        themes.put("shade", THEMES[0]);
        themes.put("campfire", THEMES[1]);
        themes.put("sunset", THEMES[2]);
        themes.put("sunrise", THEMES[3]);
        themes.put("forest",THEMES[4]);
        themes.put("ocean",THEMES[5]);
        themes.put("twilight", THEMES[6]);
        themes.put("blossom", THEMES[7]);
        themes.put("midnight", THEMES[8]);
        themes.put("transparent", THEMES[9]);

        //noinspection ConstantConditions
        int override = themes.getOrDefault(theme, R.style.ShadeOverride);
        activity.getTheme().applyStyle(override, true);
    }

    public static void overrideShape(Activity activity) {
        if (Utilities.ATLEAST_Q) {
            int curveTheme = IconShapeOverride.curveTheme(activity);
            activity.getTheme().applyStyle(curveTheme, true);
        }
    }

    public static void overrideTheme(Activity activity) {
        int themeRes = Themes.getSettingActivityThemeRes(activity);
        activity.setTheme(themeRes);
    }
}