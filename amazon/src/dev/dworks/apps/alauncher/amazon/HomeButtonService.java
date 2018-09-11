package dev.dworks.apps.alauncher.amazon;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class HomeButtonService extends Service {
    private LinearLayout layout;
    private WindowManager wm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        layout =  new LinearLayout(getApplicationContext()) {
            //home or recent button
            public void onCloseSystemDialogs(String reason) {
                if (reason.contains("homekey"))
                    HomeUtils.Perform(getApplicationContext());
            }

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                return false;
            }
        };

        layout.setFocusable(false);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                0,
                0,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        wm.addView(layout, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wm.removeView(layout);;

        ServiceManager.Stop(getApplicationContext());
        ServiceManager.StartSlow(getApplicationContext());
    }
}