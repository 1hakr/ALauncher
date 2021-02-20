package amirz.shade.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.core.content.res.TypedArrayUtils;

import com.android.launcher3.R;

import amirz.App;


public class ListPreference extends androidx.preference.ListPreference {

    private final boolean showPro;

    public ListPreference(Context context) {
        this(context, null);
    }

    @SuppressLint("RestrictedApi")
    public ListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle,
                android.R.attr.dialogPreferenceStyle));
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.Preference);
        showPro = a.getBoolean(R.styleable.Preference_showPro, false);
        a.recycle();
    }

    @Override
    protected void onClick() {
        if(showPro && !App.isPurchased()){
            App.getInstance().openPurchaseActivity(getContext());
        } else {
            super.onClick();
        }
    }
}
