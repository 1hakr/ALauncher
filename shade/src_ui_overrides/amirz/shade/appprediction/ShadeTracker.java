/**
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package amirz.shade.appprediction;

import static com.android.launcher3.InvariantDeviceProfile.CHANGE_FLAG_GRID;

import android.annotation.TargetApi;
import android.app.prediction.AppPredictor;
import android.app.prediction.AppTarget;
import android.app.prediction.AppTargetEvent;
import android.app.prediction.AppTargetId;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.appprediction.PredictionUiStateManager;
import com.android.launcher3.appprediction.PredictionUiStateManager.Client;
import com.android.launcher3.model.AppLaunchTracker;
import com.android.launcher3.util.Executors;
import com.android.launcher3.util.LooperExecutor;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Subclass of app tracker which publishes the data to the prediction engine and gets back results.
 */
@SuppressWarnings("unused")
@TargetApi(Build.VERSION_CODES.Q)
public class ShadeTracker extends AppLaunchTracker {
    private static final String TAG = "ShadeTracker";
    private static final boolean DBG = false;

    private static final int MSG_INIT = 0;
    private static final int MSG_DESTROY = 1;
    private static final int MSG_LAUNCH = 2;
    private static final int MSG_PREDICT = 3;

    protected final Context mContext;
    private final Handler mMessageHandler;
    private FilteredPredictor mUsageTracker;

    private final Executor mExecutor = new LooperExecutor(Looper.getMainLooper());

    public ShadeTracker(Context context) {
        mContext = context;
        mMessageHandler = new Handler(Executors.UI_HELPER_EXECUTOR.getLooper(), this::handleMessage);
        InvariantDeviceProfile.INSTANCE.get(mContext).addOnChangeListener(this::onIdpChanged);

        mMessageHandler.sendEmptyMessage(MSG_INIT);
    }

    @UiThread
    private void onIdpChanged(int changeFlags, InvariantDeviceProfile profile) {
        if ((changeFlags & CHANGE_FLAG_GRID) != 0) {
            // Reinitialize everything
            mMessageHandler.sendEmptyMessage(MSG_INIT);
        }
    }

    @WorkerThread
    private void destroy() {
        mUsageTracker = null;
    }

    /**
     * Override to add custom extras.
     */
    @WorkerThread
    @Nullable
    public Bundle getAppPredictionContextExtras(Client client){
        return null;
    }

    @WorkerThread
    private boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INIT: {
                Log.d(TAG, "Init");
                destroy();
                int count = InvariantDeviceProfile.INSTANCE.get(mContext).numAllAppsColumns;
                mUsageTracker = new FilteredPredictor(mContext, count);
                return true;
            }
            case MSG_DESTROY: {
                Log.d(TAG, "Destroy");
                destroy();
                return true;
            }
            case MSG_LAUNCH: {
                Log.d(TAG, "Launch");
                AppTargetEvent event = (AppTargetEvent) msg.obj;
                // Use event.
                return true;
            }
            case MSG_PREDICT: {
                Log.d(TAG, "Predict");
                String clientId = (String) msg.obj;
                Client client = Client.HOME.id.equals(clientId) ? Client.HOME : Client.OVERVIEW;
                AppPredictor.Callback cb = PredictionUiStateManager.INSTANCE.get(mContext)
                        .appPredictorCallback(client);

                // Do prediction
                List<AppTarget> targetList = new ArrayList<>();
                UserHandle user = Process.myUserHandle();
                for (ComponentName cn: mUsageTracker.getFilteredComponents()) {
                    AppTarget target = new AppTarget
                            .Builder(new AppTargetId("app:" + cn), cn.getPackageName(), user)
                            .setClassName(cn.getClassName())
                            .build();
                    targetList.add(target);
                }

                mExecutor.execute(() -> cb.onTargetsAvailable(targetList));
                return true;
            }
        }
        return false;
    }

    @Override
    @UiThread
    public void onReturnedToHome() {
        String client = Client.HOME.id;
        mMessageHandler.removeMessages(MSG_PREDICT, client);
        Message.obtain(mMessageHandler, MSG_PREDICT, client).sendToTarget();
        if (DBG) {
            Log.d(TAG, String.format("Sent immediate message to update %s", client));
        }
    }

    @Override
    @UiThread
    public void onStartShortcut(String packageName, String shortcutId, UserHandle user,
                                String container) {
        // TODO: Use the full shortcut info
        AppTarget target = new AppTarget
                .Builder(new AppTargetId("shortcut:" + shortcutId), packageName, user)
                .setClassName(shortcutId)
                .build();
        sendLaunch(target, container);
    }

    @Override
    @UiThread
    public void onStartApp(ComponentName cn, UserHandle user, String container) {
        if (cn != null) {
            AppTarget target = new AppTarget
                    .Builder(new AppTargetId("app:" + cn), cn.getPackageName(), user)
                    .setClassName(cn.getClassName())
                    .build();
            sendLaunch(target, container);
        }
    }

    @UiThread
    private void sendLaunch(AppTarget target, String container) {
        AppTargetEvent event = new AppTargetEvent.Builder(target, AppTargetEvent.ACTION_LAUNCH)
                .setLaunchLocation(container == null ? CONTAINER_DEFAULT : container)
                .build();
        Message.obtain(mMessageHandler, MSG_LAUNCH, event).sendToTarget();
    }
}
