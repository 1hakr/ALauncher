package amirz;

import dev.dworks.apps.alauncher.AppFlavour;

public class App extends AppFlavour {
    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static synchronized App getInstance() {
        return sInstance;
    }
}
