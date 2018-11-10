package dev.dworks.apps.alauncher.extras.tvrecommendations.service;

public class Signals {
    public int mClicks;
    public int mImpressions;

    public Signals(int clicks, int impressions) {
        this.mClicks = clicks;
        this.mImpressions = impressions;
    }

    public Signals() {
        this(0, 0);
    }
}
