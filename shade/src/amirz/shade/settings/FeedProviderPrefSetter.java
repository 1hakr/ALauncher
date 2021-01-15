package amirz.shade.settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import amirz.aidlbridge.LauncherClientIntent;

public class FeedProviderPrefSetter implements ReloadingListPreference.OnReloadListener {
    private final Context mContext;
    private final PackageManager mPm;
    private final String IGNORE_PROVIDER = "ALauncher Companion 1.0";

    public FeedProviderPrefSetter(Context context) {
        mContext = context;
        mPm = mContext.getPackageManager();
    }

    @Override
    public Runnable listUpdater(ReloadingListPreference pref) {
        List<ApplicationInfo> aiList = LauncherClientIntent.query(mContext);

        ArrayList<CharSequence> names = new ArrayList<CharSequence>();
        ArrayList<CharSequence> packages = new ArrayList<CharSequence>();
        String defaultValue = LauncherClientIntent.getRecommendedPackage(mContext);

        // First value, disabled
        names.add(mContext.getString(R.string.pref_value_disabled));
        packages.add("");

        // List of available feeds
        for (ApplicationInfo ai : aiList) {
            CharSequence label = ai.loadLabel(mPm);
            String packageName = ai.packageName;
            CharSequence name = label;
            try {
                PackageInfo pi = mPm.getPackageInfo(packageName, 0);
                name = mContext.getString(R.string.feed_provider_value, label, pi.versionName);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            if(name.equals(IGNORE_PROVIDER)){
                continue;
            }
            names.add(name);
            packages.add(packageName);
        }

        CharSequence[] keys = names.toArray(new String[0]);
        CharSequence[] values = packages.toArray(new String[0]);

        return () -> {
            pref.setEntriesWithValues(keys, values);
            pref.setDefaultValue(defaultValue);
            String v = pref.getValue();
            if (!TextUtils.isEmpty(v) && !Arrays.asList(values).contains(v)) {
                pref.setValue(defaultValue);
            }
        };
    }
}
