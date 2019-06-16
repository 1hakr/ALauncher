package dev.dworks.apps.alauncher;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import dev.dworks.apps.alauncher.core.LaunchException;
import dev.dworks.apps.alauncher.trace.AppTrace;
import dev.dworks.apps.alauncher.trace.AppTrace.TraceTag;

public abstract class LauncherViewHolder extends ViewHolder implements OnClickListener {
    protected final Context mCtx;
    private int mLaunchColor;
    private Intent mLaunchIntent;
    private TraceTag mLaunchTag;
    private boolean mLaunchTranslucent;

    protected LauncherViewHolder(View v) {
        super(v);
        this.mCtx = v.getContext();
        v.setOnClickListener(this);
    }

    public void onClick(View v) {
        this.mLaunchTag = AppTrace.beginAsyncSection("LaunchAnimation");
        if (v != null && v == this.itemView) {
            ((MainActivity) this.mCtx).beginLaunchAnimation(v, this.mLaunchTranslucent, this.mLaunchColor, new Runnable() {
                public void run() {
                    AppTrace.endAsyncSection(LauncherViewHolder.this.mLaunchTag);
                    try {
                        LauncherViewHolder.this.performLaunch();
                    } catch (LaunchException e) {
                        Log.e("LauncherViewHolder", "Could not perform launch:", e);
                        Toast.makeText(LauncherViewHolder.this.mCtx, R.string.failed_launch, 0).show();
                    }
                }
            });
        }
    }

    protected final void setLaunchTranslucent(boolean launchTranslucent) {
        this.mLaunchTranslucent = launchTranslucent;
    }

    protected final void setLaunchColor(int launchColor) {
        this.mLaunchColor = launchColor;
    }

    protected final void setLaunchIntent(Intent launchIntent) {
        this.mLaunchIntent = launchIntent;
    }

    protected void performLaunch() {
        try {
            this.mCtx.startActivity(this.mLaunchIntent);
            onLaunchSucceeded();
        } catch (Throwable t) {
            //  LaunchException launchException = new LaunchException("Failed to launch intent: " + this.mLaunchIntent, t);
            t.printStackTrace();
        }
    }

    protected void onLaunchSucceeded() {
    }
}
