package amirz.shade.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragment;

import com.android.launcher3.R;

import static amirz.shade.customization.ShadeStyle.COLORS;

public class ColorListPreference extends ListPreference {

    public ColorListPreference(Context context) {
        this(context, null);
    }

    public ColorListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public ColorListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                               int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static int getAttr(@NonNull Context context, int attr, int fallbackAttr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        if (value.resourceId != 0) {
            return attr;
        }
        return fallbackAttr;
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
        builder.setAdapter(createListAdapter(builder.getContext()), listener);
    }

    public int getSelectedValuePos() {
        final String selectedValue = getValue();
        final int selectedIndex =
                (selectedValue == null) ? -1 : findIndexOfValue(selectedValue);
        return selectedIndex;
    }

    protected ListAdapter createListAdapter(Context context) {
        return new ColorArrayAdapter(context, getEntries(),
                getSelectedValuePos());
    }

    private Drawable getDrawableColor(Context context, String hexColor){
        Resources res = context.getResources();
        GradientDrawable colorChoiceDrawable = new GradientDrawable();
        colorChoiceDrawable.setShape(GradientDrawable.OVAL);
        int color = Color.parseColor(hexColor);

        // Set stroke to dark version of color
        int darkenedColor = Color.rgb(
                Color.red(color) * 192 / 256,
                Color.green(color) * 192 / 256,
                Color.blue(color) * 192 / 256);

        colorChoiceDrawable.setColor(color);
        colorChoiceDrawable.setStroke((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, res.getDisplayMetrics()), darkenedColor);

        return colorChoiceDrawable;
    }

    public class ColorArrayAdapter extends ArrayAdapter<CharSequence> {
        private final int mSelectedIndex;
        public ColorArrayAdapter(Context context, CharSequence[] objects, int selectedIndex) {
            super(context, R.layout.select_dialog_singlechoice, android.R.id.text1, objects);
            mSelectedIndex = selectedIndex;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View root = super.getView(position, convertView, parent);
            CharSequence entry = getItem(position);
            CheckedTextView text = (CheckedTextView) root.findViewById(android.R.id.text1);
            ImageView imageView = (ImageView) root.findViewById(android.R.id.icon1);
            imageView.setImageDrawable(getDrawableColor(getContext(), COLORS[position]));
            if (mSelectedIndex != -1) {
                text.setChecked(position == mSelectedIndex);
            }
            if (!text.isEnabled()) {
                text.setEnabled(true);
            }
            return root;
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    public static class ColorPreferenceFragment extends ListPreferenceDialogFragment {

        private static final java.lang.String KEY_CLICKED_ENTRY_INDEX
                = "settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX";

        private int mClickedDialogEntryIndex;

        public static ListPreferenceDialogFragment newInstance(String key) {
            final ListPreferenceDialogFragment fragment =
                    new ColorPreferenceFragment();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        private ColorListPreference getCustomizablePreference() {
            return (ColorListPreference) getPreference();
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            mClickedDialogEntryIndex = getCustomizablePreference()
                    .findIndexOfValue(getCustomizablePreference().getValue());
            getCustomizablePreference().onPrepareDialogBuilder(builder, getOnItemClickListener());
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            if (savedInstanceState != null) {
                mClickedDialogEntryIndex = savedInstanceState.getInt(KEY_CLICKED_ENTRY_INDEX,
                        mClickedDialogEntryIndex);
            }
            return dialog;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(KEY_CLICKED_ENTRY_INDEX, mClickedDialogEntryIndex);
        }

        protected DialogInterface.OnClickListener getOnItemClickListener() {
            return new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setClickedDialogEntryIndex(which);
                    onItemConfirmed();
                }
            };
        }
        protected void setClickedDialogEntryIndex(int which) {
            mClickedDialogEntryIndex = which;
        }
        private String getValue() {
            final ListPreference preference = getCustomizablePreference();
            if (mClickedDialogEntryIndex >= 0 && preference.getEntryValues() != null) {
                return preference.getEntryValues()[mClickedDialogEntryIndex].toString();
            } else {
                return null;
            }
        }

        protected void onItemConfirmed() {
            onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
            getDialog().dismiss();
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            final ListPreference preference = getCustomizablePreference();
            final String value = getValue();
            if (positiveResult && value != null) {
                if (preference.callChangeListener(value)) {
                    preference.setValue(value);
                }
            }
        }
    }
}
