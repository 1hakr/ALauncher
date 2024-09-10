/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package amirz.shade.util;

import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_CONTROLS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_TEXT;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.WindowInsetsCompat;

import com.android.launcher3.R;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.compat.AccessibilityManagerCompat;
import com.android.launcher3.widget.WindowInsetsHelper;

import amirz.helpers.Settings;

/**
 * A toast-like UI at the bottom of the screen with a label, button action, and dismiss action.
 */
public class Snackbar extends LinearLayout {

    private static final long SHOW_DURATION_MS = 180;
    private static final long HIDE_DURATION_MS = 180;
    private static final int TIMEOUT_DURATION_MS = 4000;

    private Runnable mOnDismissed;
    private boolean mIsOpen;

    public Snackbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Snackbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.snackbar, this);
    }

    public static void show(Activity activity, int labelStringResId) {
        Resources res = activity.getResources();
        String labelText = res.getString(labelStringResId);
        show(activity, labelText, null, null, null);
    }

    public static void show(Activity activity, String label) {
        show(activity, label, null, null, null);
    }

    public static void show(Activity activity, String label, String action, Runnable onActionClicked) {
        show(activity, label, action, null, onActionClicked);
    }

    public static void show(Activity activity, int labelStringResId, int actionStringResId,
                            Runnable onDismissed, Runnable onActionClicked) {
        Resources res = activity.getResources();
        String labelText = res.getString(labelStringResId);
        String actionText = res.getString(actionStringResId);
        show(activity, labelText, actionText, onDismissed, onActionClicked);
    }

    public static void show(Activity activity, String labelText, String actionText,
                            Runnable onDismissed, Runnable onActionClicked) {
        closeOpenViews(activity, true);
        Snackbar snackbar = new Snackbar(activity, null);
        // Set some properties here since inflated xml only contains the children.
        snackbar.setOrientation(HORIZONTAL);
        snackbar.setGravity(Gravity.CENTER_VERTICAL);
        Resources res = activity.getResources();
        snackbar.setElevation(res.getDimension(R.dimen.snackbar_elevation));
        int padding = res.getDimensionPixelSize(R.dimen.snackbar_padding);
        snackbar.setPadding(padding, padding, padding, padding);
        snackbar.setBackgroundResource(R.drawable.round_rect_primary);

        snackbar.mIsOpen = true;
        ViewGroup layer = getParentView(activity);
        activity.runOnUiThread(() -> {
            if(null != layer) {
                layer.addView(snackbar);
            }
        });

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbar.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.height = res.getDimensionPixelSize(R.dimen.snackbar_height);
        int maxMarginLeftRight = res.getDimensionPixelSize(R.dimen.snackbar_max_margin_left_right);
        int minMarginLeftRight = res.getDimensionPixelSize(R.dimen.snackbar_min_margin_left_right);
        int marginBottom = res.getDimensionPixelSize(R.dimen.snackbar_margin_bottom);
        Rect insets = new Rect();
        View view = activity.getWindow().getDecorView();
        WindowInsetsCompat mTempRect = WindowInsetsHelper.getRootWindowInsets(view);
        if(null != mTempRect) {
            insets.set(mTempRect.getSystemWindowInsetLeft(), mTempRect.getSystemWindowInsetTop(),
                    mTempRect.getSystemWindowInsetRight(), mTempRect.getSystemWindowInsetBottom());
        }
        int maxWidth = layer.getWidth() - minMarginLeftRight * 2 - insets.left - insets.right;
        int minWidth = layer.getWidth() - maxMarginLeftRight * 2 - insets.left - insets.right;
        params.width = minWidth;
        params.setMargins(0, 0, 0, marginBottom + insets.bottom);

        TextView labelView = snackbar.findViewById(R.id.label);
        TextView actionView = snackbar.findViewById(R.id.action);
        float textWidth = !TextUtils.isEmpty(actionText) ? actionView.getPaint().measureText(actionText) : 0;
        int totalContentWidth = (int) (labelView.getPaint().measureText(labelText)
                + textWidth)
                + labelView.getPaddingRight() + labelView.getPaddingLeft()
                + actionView.getPaddingRight() + actionView.getPaddingLeft()
                + padding * 2;
        if (totalContentWidth > params.width) {
            // The text doesn't fit in our standard width so update width to accommodate.
            if (totalContentWidth <= maxWidth) {
                params.width = totalContentWidth;
            } else {
                // One line will be cut off, fallback to 2 lines and smaller font. (This should only
                // happen in some languages if system display and font size are set to largest.)
                int textHeight = res.getDimensionPixelSize(R.dimen.snackbar_content_height);
                float textSizePx = res.getDimension(R.dimen.snackbar_min_text_size);
                labelView.setLines(2);
                labelView.getLayoutParams().height = textHeight * 2;
                actionView.getLayoutParams().height = textHeight * 2;
                labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
                actionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
                params.height += textHeight;
                params.width = maxWidth;
            }
        }
        labelView.setText(labelText);
        if (null != onActionClicked){
            actionView.setText(actionText);
            actionView.setOnClickListener(v -> {
                if (onActionClicked != null) {
                    onActionClicked.run();
                }
                snackbar.mOnDismissed = null;
                snackbar.handleClose(true);
            });
        }
        snackbar.mOnDismissed = onDismissed;

        snackbar.setAlpha(0);
        snackbar.setScaleX(0.8f);
        snackbar.setScaleY(0.8f);
        snackbar.animate()
                .alpha(1f)
                .withLayer()
                .scaleX(1)
                .scaleY(1)
                .setDuration(SHOW_DURATION_MS)
                .setInterpolator(Interpolators.ACCEL_DEACCEL)
                .start();
        int timeout = AccessibilityManagerCompat.getRecommendedTimeoutMillis(activity,
                TIMEOUT_DURATION_MS, FLAG_CONTENT_TEXT | FLAG_CONTENT_CONTROLS);
        snackbar.postDelayed(() -> snackbar.handleClose(true), timeout);
    }

    //@Override
    public void handleClose(boolean animate) {
        if (mIsOpen) {
            if (animate) {
                animate().alpha(0f)
                        .withLayer()
                        .setStartDelay(0)
                        .setDuration(HIDE_DURATION_MS)
                        .setInterpolator(Interpolators.ACCEL)
                        .withEndAction(this::onClosed)
                        .start();
            } else {
                animate().cancel();
                onClosed();
            }
            mIsOpen = false;
        }
    }

    private void onClosed() {
        if (mOnDismissed != null) {
            mOnDismissed.run();
        }
    }

    private static ViewGroup getParentView(Activity activity){
        if(!Settings.isActivityAlive(activity)){
            return null;
        }
        ViewGroup layer = activity.findViewById(R.id.content_view);
        if(null == layer) {
            layer = activity.findViewById(android.R.id.content);
        }
        if(null == layer) {
            layer = activity.findViewById(R.id.launcher);
        }
        return layer;
    }

    public static void closeOpenViews(Activity activity, boolean animate) {
        ViewGroup dragLayer = getParentView(activity);
        if(null == dragLayer){
            return;
        }
        // Iterate in reverse order. AbstractFloatingView is added later to the dragLayer,
        // and will be one of the last views.
        for (int i = dragLayer.getChildCount() - 1; i >= 0; i--) {
            View child = dragLayer.getChildAt(i);
            if (child instanceof Snackbar) {
                Snackbar abs = (Snackbar) child;
                abs.handleClose(animate);
            }
        }
    }
}
