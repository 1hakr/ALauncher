package dev.dworks.apps.alauncher.extras.firetv.leanbacklauncher.apps;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.leanback.app.GuidedStepFragment;

public class AppInfoActivity extends Activity {

    private Drawable icon;
    private String title, pkg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            pkg = getIntent().getExtras().getString("pkg");

            try {

                ApplicationInfo info = getPackageManager().getApplicationInfo(pkg, 0);
                title = getPackageManager().getApplicationLabel(info).toString(); // todo flag/tostr
                icon = getPackageManager().getApplicationIcon(info);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#21272A")));


            AppInfoFragment fragment = AppInfoFragment.newInstance(title, pkg, icon);
            GuidedStepFragment.addAsRoot(this, fragment, android.R.id.content);
        }
    }
}