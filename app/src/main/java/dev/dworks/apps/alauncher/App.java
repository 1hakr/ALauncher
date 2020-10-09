package dev.dworks.apps.alauncher;

import android.content.Context;
import android.content.Intent;

import dev.dworks.apps.alauncher.pro.PurchaseActivity;

public class App extends AppFlavour {
    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static synchronized App getInstance() {
        return sInstance;
    }

    public static void openPurchaseActivity(Context context){
        context.startActivity(new Intent(context, PurchaseActivity.class));
    }
}
