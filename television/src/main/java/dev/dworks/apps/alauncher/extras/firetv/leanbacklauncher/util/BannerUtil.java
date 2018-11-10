package dev.dworks.apps.alauncher.extras.firetv.leanbacklauncher.util;

import java.util.HashMap;
import java.util.Map;

import dev.dworks.apps.alauncher.R;


public class BannerUtil {
    public static final Map<String, Integer> BANNER_OVERRIDES = new HashMap<>();

    static {
//        BANNER_OVERRIDES.put("hulu", R.drawable.banner_hulu);
//        BANNER_OVERRIDES.put("bueller.music", R.drawable.banner_amazon_music);
        BANNER_OVERRIDES.put("showtime", R.drawable.banner_showtime);
    }
}
