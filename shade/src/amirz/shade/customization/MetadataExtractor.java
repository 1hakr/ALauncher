package amirz.shade.customization;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import java.io.File;

import static android.content.pm.PackageManager.GET_META_DATA;

class MetadataExtractor {
    private final Context mContext;
    private final PackageManager mPm;

    private String mSourcePkg = "";
    private String mPkg = "";
    private Intent mMarketIntent;

    private long mLastUpdate = 0;
    private String mVersionName;
    private long mVersionCode = 0;

    MetadataExtractor(Context context, ComponentName cn) {
        mContext = context;
        mPm = context.getPackageManager();
        String pkg = cn.getPackageName();
        mPkg = pkg;

        try {
            mSourcePkg = mPm.getInstallerPackageName(pkg);
            if (!TextUtils.isEmpty(mSourcePkg)) {
                mMarketIntent = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("market://details?id=" + pkg))
                        .setPackage(mSourcePkg);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            PackageInfo pi = mPm.getPackageInfo(pkg, GET_META_DATA);
            mLastUpdate = new File(pi.applicationInfo.sourceDir).lastModified();
            mVersionName = pi.versionName;
            mVersionCode = Utilities.ATLEAST_P ? pi.getLongVersionCode() : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    CharSequence getSource() {
        String pkg = mSourcePkg;
        if (TextUtils.isEmpty(mSourcePkg)) {
            pkg = mPkg;
        }
        try {
            ApplicationInfo pi = mPm.getApplicationInfo(pkg, 0);
            if(TextUtils.isEmpty(mSourcePkg)){
                return isSystemApp(pi) ? "System" : mContext.getString(R.string.app_info_source_unknown);
            }
            return pi.loadLabel(mPm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pkg;
    }

    boolean isSystemApp(ApplicationInfo appInfo){
        return appInfo.flags != 0 && (appInfo.flags
                & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0;
    }

    String getLastUpdate() {
        return DateUtils.formatDateTime(mContext, mLastUpdate,
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);
    }

    String getVersionName() {
        return TextUtils.isEmpty(mVersionName)
                ? mContext.getString(R.string.app_info_source_unknown)
                : mVersionName;
    }

    long getVersionCode() {
        return mVersionCode;
    }

    Intent getMarketIntent() {
        return mMarketIntent;
    }
}
