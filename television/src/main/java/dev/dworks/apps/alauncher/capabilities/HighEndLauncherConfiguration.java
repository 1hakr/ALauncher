package dev.dworks.apps.alauncher.capabilities;

public class HighEndLauncherConfiguration extends LauncherConfiguration {
    public boolean isCardElevationEnabled() {
        return true;
    }

    public boolean isRichRecommendationViewEnabled() {
        return true;
    }

    public boolean isLegacyRecommendationLayoutEnabled() {
        return false;
    }

    public boolean isRoundCornersEnabled() {
        return true;
    }
}
