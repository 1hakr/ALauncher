package dev.dworks.apps.alauncher.apps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import dev.dworks.apps.alauncher.apps.hide.HideAppsViewHolder;
import dev.dworks.apps.alauncher.apps.lock.LockAppsViewHolder;

import static dev.dworks.apps.alauncher.apps.AppsSelectionActivity.TYPE_HIDE;
import static dev.dworks.apps.alauncher.apps.AppsSelectionActivity.TYPE_LOCK;

public class AppsSelectionAdapter extends RecyclerView.Adapter<AppsSelectionViewHolder> {

    private int type = TYPE_HIDE;
    private List<AppInfo> apps;

    public AppsSelectionAdapter() {
    }

    @Override
    public AppsSelectionViewHolder  onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_apps_selection, viewGroup, false);
        switch (type) {
            case TYPE_HIDE:
                return new HideAppsViewHolder(view);
            case TYPE_LOCK:
                return new LockAppsViewHolder(view);
            default:
                return new HideAppsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(AppsSelectionViewHolder viewHolder, int i) {
        viewHolder.setAppInfo(apps.get(i));
    }

    @Override
    public int getItemCount() {
        return apps == null ? 0 : apps.size();
    }

    @Override
    public int getItemViewType(int position) {
        return type;
    }

    public void setApps(List<AppInfo> apps, int type) {
        this.apps = apps;
        this.type = type;
        notifyDataSetChanged();
    }
}
