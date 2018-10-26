package dev.dworks.apps.alauncher.companion;

interface BridgeCallback {
    oneway void onBridgeConnected(in IBinder service);

    oneway void onBridgeDisconnected();
}
