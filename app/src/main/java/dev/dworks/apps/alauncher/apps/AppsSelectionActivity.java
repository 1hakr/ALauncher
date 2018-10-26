package dev.dworks.apps.alauncher.apps;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.launcher3.AppInfo;
import com.android.launcher3.IconCache;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.compat.LauncherAppsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.dworks.apps.alauncher.helpers.SecurityHelper;
import needle.Needle;
import needle.UiRelatedTask;

import static dev.dworks.apps.alauncher.helpers.SecurityHelper.REQUEST_CONFIRM_CREDENTIALS;

public class AppsSelectionActivity extends Activity {

    public static final int TYPE_HIDE = 0;
    public static final int TYPE_LOCK = 1;
    public ProgressBar progressBar;
    public RecyclerView recyclerView;
    public AppsSelectionAdapter adapter;
    public int type = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_selection);

        Bundle bundle = getIntent().getExtras();
        if(null != bundle){
            type = bundle.getInt("TYPE", 0);
        }
        setTitle(type == 0 ? R.string.hide_apps_title : R.string.lock_apps_title);
        bindViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(type == TYPE_LOCK) {
            SecurityHelper securityHelper = new SecurityHelper(this);
            securityHelper.authenticate();
        } else {
            query();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled;
        switch (item.getItemId()) {
            case android.R.id.home:
                handled = true;
                onBackPressed();
                break;
            default:
                handled = super.onOptionsItemSelected(item);
                break;
        }
        return handled;
    }

    private void bindViews() {
        progressBar = (ProgressBar) findViewById(R.id.progress);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        adapter = new AppsSelectionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void query() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        Needle.onBackgroundThread().execute(new UiRelatedTask<List<AppInfo>>(){
            @Override
            protected List<AppInfo> doWork() {
                Context context = AppsSelectionActivity.this;
                final List<AppInfo> apps = new ArrayList<>();
                final List<ComponentName> duplicatePreventionCache = new ArrayList<>();
                final UserHandle user = android.os.Process.myUserHandle();
                final IconCache iconCache = LauncherAppState.getInstance(context).getIconCache();
                for (LauncherActivityInfo info : LauncherAppsCompat.getInstance(context).getActivityList(null, user)) {
                    if (!duplicatePreventionCache.contains(info.getComponentName())) {
                        duplicatePreventionCache.add(info.getComponentName());
                        final AppInfo appInfo = new AppInfo(context, info, user);
                        iconCache.getTitleAndIcon(appInfo, false);
                        apps.add(appInfo);
                    }
                }
                Collections.sort(apps, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo o1, AppInfo o2) {
                        String t1 = o1.title == null ? "" : ((String) o1.title).trim().toLowerCase();
                        String t2 = o2.title == null ? "" : ((String) o2.title).trim().toLowerCase();
                        return t1.compareTo(t2);
                    }
                });
                return apps;
            }

            @Override
            protected void thenDoUiRelatedWork(List<AppInfo> apps) {
                adapter.setApps(apps, type);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (type == TYPE_LOCK && requestCode == REQUEST_CONFIRM_CREDENTIALS) {
            if (resultCode == RESULT_OK) {
                query();
            } else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
