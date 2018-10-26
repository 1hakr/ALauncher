package dev.dworks.apps.alauncher.companion;

import dev.dworks.apps.alauncher.companion.BridgeCallback;

interface Bridge {
    oneway void setCallback(in int index, in BridgeCallback cb);
}
