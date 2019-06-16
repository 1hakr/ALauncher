package dev.dworks.apps.alauncher.lock;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.launcher3.R;

public class LockDeviceAdmin extends DeviceAdminReceiver {

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.double_tap_to_lock_disable_warning);
    }
}
