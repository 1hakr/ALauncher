package dev.dworks.apps.alauncher.amazon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ServiceManager.start(context);
    }
}

