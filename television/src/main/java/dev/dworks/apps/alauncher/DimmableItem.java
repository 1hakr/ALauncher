package dev.dworks.apps.alauncher;

import dev.dworks.apps.alauncher.animation.ViewDimmer;

public interface DimmableItem {
    void setDimState(ViewDimmer.DimState dimState, boolean z);
}
