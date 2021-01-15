package amirz.shade.appprediction;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.Utilities;
import com.android.launcher3.model.LoaderCursor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.content.Context.USAGE_STATS_SERVICE;

class UsageTracker {
    private static final int MAX_ENTRIES = 30;
    public static final String CONTAINER_HOTSEAT = "-101";

    private final PackageManager mPm;
    private final UsageStatsManager mManager;
    private final Context mContext;

    @SuppressLint("WrongConstant")
    UsageTracker(Context context) {
        mContext = context;
        mPm = context.getPackageManager();
        mManager = (UsageStatsManager) context.getSystemService(USAGE_STATS_SERVICE);
    }

    List<ComponentName> getSortedComponents() {
        List<ComponentName> components = new ArrayList<>();
        for (String pkg : getSortedPackages()) {
            Intent intent = mPm.getLaunchIntentForPackage(pkg);
            if (intent != null) {
                components.add(intent.getComponent());
            }
        }
        return components;
    }

    PackageManager getPm() {
        return mPm;
    }

    private List<String> getSortedPackages() {
        List<String> packages = new ArrayList<>();
        for (UsageStats stat : getSortedStats()) {
            packages.add(stat.getPackageName());
        }
        return packages;
    }

    private List<String> getHotSeatPackages() {
        List<String> packages = new ArrayList<>();
        final ContentResolver contentResolver = mContext.getContentResolver();
        LauncherAppState mApp = LauncherAppState.getInstance(mContext);
        final LoaderCursor cursor = new LoaderCursor(contentResolver.query(
                LauncherSettings.Favorites.CONTENT_URI, null,
                "container = ?", new String[] { CONTAINER_HOTSEAT }, null), mApp);
        while (cursor.moveToNext()) {
            Intent intent = cursor.parseIntent();
            if(null == intent){
                continue;
            }
            ComponentName cn = intent.getComponent();
            String targetPkg = cn == null ? intent.getPackage() : cn.getPackageName();
            packages.add(targetPkg);
        }
        return packages;
    }

    private List<UsageStats> getSortedStats() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);

        List<UsageStats> stats = mManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                calendar.getTimeInMillis(), System.currentTimeMillis());
        SortedMap<Long, UsageStats> sortedTime = new TreeMap<>();
        SortedMap<Long, UsageStats> sortedUsed = new TreeMap<>();
        for (UsageStats usageStats : stats) {
            sortedUsed.put(-usageStats.getLastTimeUsed(), usageStats);
            long time = Utilities.ATLEAST_Q ? usageStats.getTotalTimeVisible() :
            usageStats.getTotalTimeInForeground();
            sortedTime.put(-time, usageStats);
        }
        List<UsageStats> sortedList = new ArrayList<>();
        if(sortedTime.isEmpty()){
            return sortedList;
        }
        int sortedTimeLimit = Math.min(sortedTime.size(), 100);
        int sortedUsedLimit = Math.min(sortedTime.size(), 100);
        List<UsageStats> sorted = new ArrayList<UsageStats>(sortedTime.values()).subList(0, sortedTimeLimit);
        List<UsageStats> sortedExtras = new ArrayList<UsageStats>(sortedUsed.values()).subList(0, sortedUsedLimit);
        sorted.retainAll(sortedExtras);
        Set<String> packages = new HashSet<>();
        packages.addAll(getHotSeatPackages());
        for (UsageStats stat : sorted) {
            // Prevent slowing down the phone too much.
            if (sortedList.size() == MAX_ENTRIES) {
                break;
            }
            String pkg = stat.getPackageName();
            if (!packages.contains(pkg)) {
                packages.add(pkg);
                sortedList.add(stat);
            }
        }

        return sortedList;
    }
}
