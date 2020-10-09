/*
 * Copyright 2014 Google Inc. All rights reserved.
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

package dev.dworks.apps.alauncher.helpers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.android.launcher3.BuildConfig;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsHelper {
    private static Context sAppContext = null;

    private static FirebaseAnalytics mFirebaseAnalytics;
    private final static String TAG = "ALauncher";

    private static boolean canSend() {
        return sAppContext != null && mFirebaseAnalytics != null
                && !BuildConfig.DEBUG ;
    }

    public static synchronized void intialize(Context context) {
        sAppContext = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        setProperty("DeviceType", Utils.getDeviceType(context));
    }

    public static void setProperty(String propertyName, String propertyValue){
        if (!canSend()) {
            return;
        }
        mFirebaseAnalytics.setUserProperty(propertyName, propertyValue);
    }

    public static void logEvent(String eventName){
        if (!canSend()) {
            return;
        }
        mFirebaseAnalytics.logEvent(eventName, new Bundle());
    }

    public static void logEvent(String eventName, Bundle params){
        if (!canSend()) {
            return;
        }
        mFirebaseAnalytics.logEvent(eventName, params);
    }

    public static void setCurrentScreen(Activity activity, String screenName){
        if (!canSend()) {
            return;
        }

        if(null != screenName) {
            mFirebaseAnalytics.setCurrentScreen(activity, screenName, screenName);
        }
    }
}
