package amirz.unread.media;

import android.os.Handler;
import android.view.View;

class MultiClickListener implements Runnable {
    private final Handler mHandler = new Handler();
    private final int mDelay;
    private OnClickListener[] mListeners = new OnClickListener[0];
    private OnClickListener[] mNewListeners = mListeners;
    private int mUnprocessedTaps;
    private View mView;

    MultiClickListener(int delay) {
        mDelay = delay;
    }

    void setListeners(OnClickListener... listeners) {
        mNewListeners = listeners;

        // Defer update if there are unprocessed taps.
        if (mUnprocessedTaps == 0) {
            mListeners = listeners;
        }
    }

    void onClick(View v) {
        if (mListeners.length > 0) {
            mView = v;

            // Intermediate click callback.
            mListeners[mUnprocessedTaps++].onClick(v, false);

            // Final click callback.
            mHandler.removeCallbacks(this);
            if (mUnprocessedTaps == mListeners.length) {
                run();
            } else {
                mHandler.postDelayed(this, mDelay);
            }
        }
    }

    @Override
    public void run() {
        // Unprocessed tap count cannot become higher than the listeners count.
        mListeners[mUnprocessedTaps - 1].onClick(mView, true);
        mUnprocessedTaps = 0;

        // Update the listeners after the taps have been processed.
        mListeners = mNewListeners;
    }

    interface OnClickListener {
        void onClick(View v, boolean finalClick);
    }
}
