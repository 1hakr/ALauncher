package dev.dworks.apps.alauncher.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import dev.dworks.apps.alauncher.App;

public class ListPreference extends android.preference.ListPreference {

    public ListPreference(Context context) {
        super(context);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void showDialog(Bundle state) {
        if(App.isPurchased()) {
            super.showDialog(state);
        }
    }
}
