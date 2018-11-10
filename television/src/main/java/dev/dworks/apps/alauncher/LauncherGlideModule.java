package dev.dworks.apps.alauncher;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.module.AppGlideModule;
import dev.dworks.apps.alauncher.notifications.RecommendationImageKey;
import dev.dworks.apps.alauncher.notifications.RecommendationImageLoaderFactory;

import java.io.IOException;

import androidx.annotation.NonNull;

@GlideModule
public class LauncherGlideModule extends AppGlideModule {
    private static final boolean DEBUG = false;
    private static final int DISK_CACHE_SIZE_BYTES = 52428800;
    private static final int MEMORY_CACHE_SIZE_BYTES = 4194304;

    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE_BYTES));
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE_BYTES));
        builder.setBitmapPool(new BitmapPoolAdapter());
    }

    public void registerComponents(final Context context, Registry registry) {
        registry.append(RecommendationImageKey.class, Bitmap.class, new RecommendationImageLoaderFactory(context));
        registry.append(Bitmap.class, Bitmap.class, new ResourceDecoder<Bitmap, Bitmap>() {
            private BitmapPool mBitmapPool;

            public Resource<Bitmap> decode(Bitmap source, int width, int height, Options options) {
                if (this.mBitmapPool == null) {
                    this.mBitmapPool = Glide.get(context).getBitmapPool();
                }
                return new BitmapResource(source, this.mBitmapPool);
            }

            public boolean handles(Bitmap source, Options options) {
                return true;
            }
        });
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        this.registerComponents(context, registry);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
