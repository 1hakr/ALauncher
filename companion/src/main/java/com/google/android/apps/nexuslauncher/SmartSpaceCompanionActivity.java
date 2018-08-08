package com.google.android.apps.nexuslauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import dev.dworks.apps.alauncher.companion.BuildConfig;
import dev.dworks.apps.alauncher.companion.R;

import static com.google.android.apps.nexuslauncher.smartspace.SmartspaceBroadcastReceiver.ENABLE_UPDATE_ACTION;
import static com.google.android.apps.nexuslauncher.smartspace.SmartspaceBroadcastReceiver.ENABLE_UPDATE_PACKAGE;
import static com.google.android.apps.nexuslauncher.smartspace.SmartspaceBroadcastReceiver.TARGET_PACKAGE_NAME;

public class SmartSpaceCompanionActivity extends Activity {

    private static final String HIDE_ACTION = TARGET_PACKAGE_NAME + ".HIDE_AT_A_GLANCE_COMPANION";
    private static final String SETTINGS_ACTION = TARGET_PACKAGE_NAME + ".OPEN_LEAN_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUi();
        sendEnableBroadcast();
    }

    private void setupUi() {
        setContentView(R.layout.activity_home);

        TextView status = findViewById(R.id.status);
        status.setText("" + BuildConfig.DEBUG);
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setActionBar(toolbar);

        Button hideButton = findViewById(R.id.home_hide_companion_app);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(HIDE_ACTION, TARGET_PACKAGE_NAME);
                finish();
            }
        });

        Button settingsButton = findViewById(R.id.home_open_lean_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEnableBroadcast();
                sendBroadcast(SETTINGS_ACTION, TARGET_PACKAGE_NAME);
                // finish();
            }
        });
    }

    private void sendEnableBroadcast() {
        sendBroadcast(ENABLE_UPDATE_ACTION, ENABLE_UPDATE_PACKAGE);
    }

    private void sendBroadcast(String action, String packageName) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendBroadcast(intent);
    }
}