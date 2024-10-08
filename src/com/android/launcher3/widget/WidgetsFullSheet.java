/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.launcher3.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.ColorUtils;

import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetHost.ProviderChangedListener;
import com.android.launcher3.R;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.RecyclerViewFastScroller;
import com.android.launcher3.views.TopRoundedCornerView;

import amirz.shade.search.AllAppsSearchBackground;
import amirz.shade.search.EditText;

/**
 * Popup for showing the full list of available widgets
 */
public class WidgetsFullSheet extends BaseWidgetSheet implements Insettable, TextWatcher,
        ProviderChangedListener, View.OnFocusChangeListener {

    private static final long DEFAULT_OPEN_DURATION = 267;
    private static final long FADE_IN_DURATION = 150;
    private static final float VERTICAL_START_POSITION = 0.3f;

    private final Rect mInsets = new Rect();

    private final WidgetsListAdapter mAdapter;

    private WidgetsRecyclerView mRecyclerView;
    private AllAppsSearchBackground mFallbackSearchView;
    private EditText mFallbackSearchViewText;
    private String mQuery;

    public WidgetsFullSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LauncherAppState apps = LauncherAppState.getInstance(context);
        mAdapter = new WidgetsListAdapter(context,
                LayoutInflater.from(context), apps.getWidgetCache(), apps.getIconCache(),
                this, this);

    }

    public WidgetsFullSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContent = findViewById(R.id.container);

        mRecyclerView = findViewById(R.id.widgets_list_view);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setApplyBitmapDeferred(true, mRecyclerView);

        mFallbackSearchView = findViewById(R.id.fallback_search_view);
        mFallbackSearchViewText = findViewById(R.id.fallback_search_view_text);
        if(Themes.isTrickyMode(getContext())) {
            mFallbackSearchViewText.setTextColor(Color.GRAY);
        }
        mFallbackSearchViewText.addTextChangedListener(this);
        mFallbackSearchViewText.setOnFocusChangeListener(this);
        mFallbackSearchViewText.setHintTextColor(Themes.getShadeColorAccent(getContext()));

        int bgColor = Themes.getAttrColor(getContext(), R.attr.shadeColorSearchBar);
        int overlay = Themes.getAttrColor(getContext(), R.attr.shadeColorAllAppsOverlay);

        if (ColorUtils.setAlphaComponent(overlay, 0) != overlay) {
            // Alpha is not zero, so update it to the right value.
            overlay = ColorUtils.setAlphaComponent(overlay,
                    getContext().getResources().getInteger(R.integer.shade_qsb_color_alpha));

            bgColor = ColorUtils.compositeColors(overlay, bgColor);
        }

        mFallbackSearchView.setColor(bgColor);

        TopRoundedCornerView springLayout = (TopRoundedCornerView) mContent;
        springLayout.addSpringView(R.id.widgets_list_view);
        mRecyclerView.setEdgeEffectFactory(springLayout.createEdgeEffectFactory());
        onWidgetsBound();
    }

    @Override
    protected Pair<View, String> getAccessibilityTarget() {
        return Pair.create(mRecyclerView, getContext().getString(
                mIsOpen ? R.string.widgets_list : R.string.widgets_list_closed));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLauncher.getAppWidgetHost().addProviderChangeListener(this);
        notifyWidgetProvidersChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLauncher.getAppWidgetHost().removeProviderChangeListener(this);
    }

    @Override
    public void setInsets(Rect insets) {
        mInsets.set(insets);

        mRecyclerView.setPadding(
                mRecyclerView.getPaddingLeft(), mRecyclerView.getPaddingTop(),
                mRecyclerView.getPaddingRight(), insets.bottom);
        if (insets.bottom > 0) {
            setupNavBarColor();
        } else {
            clearNavBarColor();
        }

        ((TopRoundedCornerView) mContent).setNavBarScrimHeight(mInsets.bottom);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthUsed;
        if (mInsets.bottom > 0) {
            widthUsed = 0;
        } else {
            Rect padding = mLauncher.getDeviceProfile().workspacePadding;
            widthUsed = Math.max(padding.left + padding.right,
                    2 * (mInsets.left + mInsets.right));
        }

        int heightUsed = mInsets.top + mLauncher.getDeviceProfile().edgeMarginPx;
        measureChildWithMargins(mContent, widthMeasureSpec,
                widthUsed, heightMeasureSpec, heightUsed);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        // Content is laid out as center bottom aligned
        int contentWidth = mContent.getMeasuredWidth();
        int contentLeft = (width - contentWidth) / 2;
        mContent.layout(contentLeft, height - mContent.getMeasuredHeight(),
                contentLeft + contentWidth, height);

        setTranslationShift(mTranslationShift);
    }

    @Override
    public void notifyWidgetProvidersChanged() {
        mLauncher.refreshAndBindWidgetsForPackageUser(null);
    }

    @Override
    public void onWidgetsBound() {
        mAdapter.setWidgets(mLauncher.getPopupDataProvider().getAllWidgets());
    }

    private void open(boolean animate) {
        if (animate) {
            if (getPopupContainer().getInsets().bottom > 0) {
                mContent.setAlpha(0);
                setTranslationShift(VERTICAL_START_POSITION);
            }
            mOpenCloseAnimator.setValues(
                    PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_OPENED));
            mOpenCloseAnimator
                    .setDuration(DEFAULT_OPEN_DURATION)
                    .setInterpolator(AnimationUtils.loadInterpolator(
                            getContext(), android.R.interpolator.linear_out_slow_in));
            mOpenCloseAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRecyclerView.suppressLayout(false);
                    mAdapter.setApplyBitmapDeferred(false, mRecyclerView);
                    mOpenCloseAnimator.removeListener(this);
                }
            });
            post(() -> {
                mRecyclerView.suppressLayout(true);
                mOpenCloseAnimator.start();
                mContent.animate().alpha(1).setDuration(FADE_IN_DURATION);
            });
        } else {
            setTranslationShift(TRANSLATION_SHIFT_OPENED);
            mAdapter.setApplyBitmapDeferred(false, mRecyclerView);
            post(this::announceAccessibilityChanges);
        }
    }

    @Override
    protected void handleClose(boolean animate) {
        handleClose(animate, DEFAULT_OPEN_DURATION);
    }

    @Override
    protected boolean isOfType(int type) {
        return (type & TYPE_WIDGETS_FULL_SHEET) != 0;
    }

    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        // Disable swipe down when recycler view is scrolling
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mNoIntercept = false;
            RecyclerViewFastScroller scroller = mRecyclerView.getScrollbar();
            if (scroller.getThumbOffsetY() >= 0 &&
                    getPopupContainer().isEventOverView(scroller, ev)) {
                mNoIntercept = true;
            } else if (getPopupContainer().isEventOverView(mContent, ev)) {
                mNoIntercept = !mRecyclerView.shouldContainerScroll(ev, getPopupContainer());
            }
        }
        return super.onControllerInterceptTouchEvent(ev);
    }

    public static WidgetsFullSheet show(Launcher launcher, boolean animate) {
        WidgetsFullSheet sheet = (WidgetsFullSheet) launcher.getLayoutInflater()
                .inflate(R.layout.widgets_full_sheet, launcher.getDragLayer(), false);
        sheet.attachToContainer();
        sheet.mIsOpen = true;
        sheet.open(animate);
        return sheet;
    }

    @VisibleForTesting
    public static WidgetsRecyclerView getWidgetsView(Launcher launcher) {
        return launcher.findViewById(R.id.widgets_list_view);
    }

    @Override
    protected int getElementsRowCount() {
        return mAdapter.getItemCount();
    }

    @Nullable
    @Override
    public Animator createHintCloseAnim(float distanceToMove) {
        AnimatorSet anim = new AnimatorSet();
        anim.play(ObjectAnimator.ofFloat(mRecyclerView, TRANSLATION_Y, -distanceToMove));
        anim.play(ObjectAnimator.ofFloat(mRecyclerView, ALPHA, 0.5f));
        return anim;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        mQuery = s.toString();
        mAdapter.getFilter().filter(mQuery);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            mFallbackSearchViewText.hideKeyboard();
        }
    }
}
