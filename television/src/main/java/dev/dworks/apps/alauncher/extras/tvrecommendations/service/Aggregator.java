package dev.dworks.apps.alauncher.extras.tvrecommendations.service;

import java.util.Date;

public interface Aggregator<T> {
    void add(Date date, T t);

    double getAggregatedScore();

    void reset();
}
