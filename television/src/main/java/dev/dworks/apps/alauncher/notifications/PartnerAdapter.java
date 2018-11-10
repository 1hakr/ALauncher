package dev.dworks.apps.alauncher.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dev.dworks.apps.alauncher.R;
import dev.dworks.apps.alauncher.apps.AppsAdapter.AppBannerViewHolder;
import dev.dworks.apps.alauncher.core.LaunchException;
import dev.dworks.apps.alauncher.util.Util;
import dev.dworks.apps.alauncher.extras.tvrecommendations.TvRecommendation;

public class PartnerAdapter extends NotificationsServiceAdapter<PartnerAdapter.PartnerBannerViewHolder> {
    private final BlacklistListener mListener;

    static final class PartnerBannerViewHolder extends AppBannerViewHolder {
        private PendingIntent mIntent;

        public PartnerBannerViewHolder(View v) {
            super(v, null);
        }

        public void init(CharSequence title, Drawable banner, PendingIntent intent, int launchColor) {
            super.init(title, banner, launchColor);
            this.mIntent = intent;
        }

        protected void performLaunch() {
            if (this.mIntent != null) {
                try {
                    Util.startActivity(this.mCtx, this.mIntent);
                } catch (Throwable t) {
                    LaunchException launchException = new LaunchException("Could not launch partner intent", t);
                }
            } else {
                throw new LaunchException("No partner intent to launch: " + getPackageName());
            }
        }
    }

    public PartnerAdapter(Context context, BlacklistListener listener) {
        super(context, 15000, 60000);
        this.mListener = listener;
    }

    public int getItemViewType(int position) {
        return 2;
    }

    public PartnerBannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PartnerBannerViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.app_banner, parent, false));
    }

    public void onBindViewHolder(PartnerBannerViewHolder appHolder, int position) {
        if (position < getItemCount()) {
            TvRecommendation recommendation = getRecommendation(position);
            appHolder.init(recommendation.getTitle(), new BitmapDrawable(this.mContext.getResources(), recommendation.getContentImage()), recommendation.getContentIntent(), recommendation.getColor());
        }
    }

    protected boolean isPartnerClient() {
        return true;
    }

    protected void onNewRecommendation(TvRecommendation rec) {
        String pkgName = rec.getReplacedPackageName();
        if (!TextUtils.isEmpty(pkgName) && this.mListener != null) {
            this.mListener.onPackageBlacklisted(pkgName);
        }
    }

    protected void onRecommendationRemoved(TvRecommendation rec) {
        String pkgName = rec.getReplacedPackageName();
        if (!TextUtils.isEmpty(pkgName) && this.mListener != null) {
            this.mListener.onPackageUnblacklisted(pkgName);
        }
    }
}
