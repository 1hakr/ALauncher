package amirz.shade;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.uioverrides.UiFactory;

import java.util.ArrayList;

import amirz.helpers.AppsLockerDatabase;
import amirz.helpers.SecurityHelper;
import amirz.shade.customization.ShadeStyle;

import static amirz.helpers.SecurityHelper.REQUEST_CONFIRM_AUTH;
import static amirz.helpers.SecurityHelper.REQUEST_CONFIRM_CREDENTIALS;

public class ShadeLauncher extends Launcher {
    private enum State {
        PAUSED,
        RECREATE_DEFERRED,
        KILL_DEFERRED,
        RESUMED
    }

    private final ShadeLauncherCallbacks mCallbacks;
    private State mState = State.PAUSED;
    private SecurityHelper mSecurityHelper;
    private ArrayList<ComponentName> mUnlockedAppsList = new ArrayList<>();

    public ShadeLauncher() {
        super();
        mCallbacks = new ShadeLauncherCallbacks(this);
        setLauncherCallbacks(mCallbacks);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ShadeRestarter.cancelRestart(this);
        ShadeFont.override(this);
        ShadeStyle.override(this);
        super.onCreate(savedInstanceState);
        mSecurityHelper = new SecurityHelper(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
        ShadeStyle.overrideShape(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mState == State.KILL_DEFERRED) {
            ShadeRestarter.initiateRestart(this);
        } else if (mState == State.RECREATE_DEFERRED) {
            super.recreate();
        }
        mState = State.RESUMED;
    }

    @Override
    public void recreate() {
        if (mState == State.RESUMED) {
            super.recreate();
        } else if (mState != State.KILL_DEFERRED) {
            mState = State.RECREATE_DEFERRED;
        }
    }

    public void kill() {
        if (mState == State.RESUMED) {
            ShadeRestarter.initiateRestart(this);
        } else {
            mState = State.KILL_DEFERRED;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mState = State.PAUSED;
    }

    @Override
    public boolean startActivitySafely(View v, Intent intent, ItemInfo item, @Nullable String sourceContainer) {
        if (!hasBeenResumed()) {
            // Workaround an issue where the WM launch animation is clobbered when finishing the
            // recents animation into launcher. Defer launching the activity until Launcher is
            // next resumed.
            addOnResumeCallback(() -> startActivitySafely(v, intent, item, sourceContainer));
            UiFactory.clearSwipeSharedState(true /* finishAnimation */);
            return true;
        }
        return startActivitySecurely(v, intent, item, sourceContainer);
    }

    private SecurityHelper.SecurityCallback mSecurityCallback;

    public boolean startActivitySecurely(View v, Intent intent, ItemInfo item,
                                         @Nullable String sourceContainer) {
        boolean isAppSecured = AppsLockerDatabase.isLocked(this, item);
        boolean isUnlocked = null != item ? mUnlockedAppsList.contains(item.getTargetComponent()) : true;
        if(!isAppSecured || isUnlocked || !mSecurityHelper.isDeviceSecure()) {
            return super.startActivitySafely(v, intent, item, sourceContainer);
        }

        mSecurityCallback = (requestCode, resultCode, data) -> {
            mUnlockedAppsList.add(item.getTargetComponent());
            super.startActivitySafely(v, intent, item, sourceContainer);
        };

        String title = item.title.toString() +" is Locked";
        mSecurityHelper.authenticate(title, "Authenticate to continue", REQUEST_CONFIRM_AUTH);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONFIRM_AUTH || requestCode == REQUEST_CONFIRM_CREDENTIALS) {
            onSecurityActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onSecurityActivityResult(int requestCode, int resultCode, Intent data){
        if (null != mSecurityCallback && resultCode == RESULT_OK) {
            mSecurityCallback.onActivityResult(requestCode, resultCode, data);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUnlockedAppsList.clear();
            }
        }
    };

    public boolean toggleAppLock(ItemInfo itemInfo) {
        boolean isLocked = AppsLockerDatabase.isLocked(this, itemInfo);
        mSecurityCallback = (requestCode, resultCode, data) -> {
            AppsLockerDatabase.setLocked(this, itemInfo, !isLocked);

        };

        String title = (isLocked ? "Unlock " : "Lock ") + itemInfo.title.toString() + " ?";
        mSecurityHelper.authenticate(title, "Authenticate to continue", REQUEST_CONFIRM_CREDENTIALS);
        return true;
    }

    public ShadeLauncherCallbacks getCallbacks() {
        return mCallbacks;
    }
}
