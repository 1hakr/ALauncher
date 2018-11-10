package dev.dworks.apps.alauncher.recommendations;

import dev.dworks.apps.alauncher.recommendations.GservicesRankerParameters.Factory;
import dev.dworks.apps.alauncher.extras.tvrecommendations.service.BaseRecommendationsService;

public class RecommendationsService extends BaseRecommendationsService {
    public RecommendationsService() {
        super(false, NotificationsServiceV4.class, new Factory());
    }
}
