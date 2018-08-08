package dev.dworks.apps.alauncher.client;

import android.os.Bundle;
import dev.dworks.apps.alauncher.client.WindowLayoutParams;
import dev.dworks.apps.alauncher.client.ILauncherClientProxyCallback;

interface ILauncherClientProxy {

    void closeOverlay(int options);

    void endScroll();

    void onPause();

    void onResume();

    void onScroll(float progress);

    void openOverlay(int options);

    void startScroll();

    void windowAttached(inout WindowLayoutParams attrs, int options);

    void windowAttached2(inout Bundle bundle);

    void setActivityState(int activityState);

    void windowDetached(boolean isChangingConfigurations);

    void onQsbClick(inout Intent intent);

    int init(ILauncherClientProxyCallback callback);

    int reconnect();
}