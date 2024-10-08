package amirz.unread;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class UnreadEvent {
    private final List<String> mText = new ArrayList<>();
    private Drawable mIcon;
    private View.OnClickListener mOnClick;
    private View.OnLongClickListener mOnLongClick;

    // Prevent instantiation outside package.
    UnreadEvent() {
    }

    public List<String> getText() {
        return mText;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClick;
    }

    public View.OnLongClickListener getOnLongClickListener() {
        return mOnLongClick;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClick = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mOnLongClick = onLongClickListener;
    }
}
