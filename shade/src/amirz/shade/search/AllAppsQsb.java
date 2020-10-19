package amirz.shade.search;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.qsb.QsbWidgetHostView;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.Themes;

import java.util.ArrayList;

import amirz.shade.customization.DockSearch;
import amirz.shade.hidden.HiddenAppsSearchAlgorithm;

import static amirz.shade.customization.DockSearch.KEY_DOCK_SEARCH;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.android.launcher3.LauncherState.ALL_APPS_CONTENT;
import static com.android.launcher3.LauncherState.HOTSEAT_SEARCH_BOX;
import static com.android.launcher3.icons.IconNormalizer.ICON_VISIBLE_AREA_FACTOR;

public class AllAppsQsb extends QsbContainerView
        implements Insettable, SearchUiManager,
        AllAppsSearchBarController.Callbacks, AllAppsStore.OnUpdateListener {
    private final Launcher mLauncher;
    private final AllAppsSearchBarController mSearchBarController;
    private final SpannableStringBuilder mSearchQueryBuilder;

    private AlphabeticalAppsList mApps;
    private AllAppsContainerView mAppsView;

    // This value was used to position the QSB. We store it here for translationY animations.
    private final float mFixedTranslationY;
    private final float mMarginTopAdjusting;
    private final int mMinTopInset;

    // Delegate views.
    private FrameLayout mSearchWrapperView;
    private AllAppsSearchBackground mFallbackSearchView;
    private EditText mFallbackSearchViewText;

    private boolean mSearchRequested;
    private final int[] mCurrentWidgetPadding = new int[2];

    public static class HotseatQsbFragment extends QsbFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onInit(Bundle savedInstanceState) {
            Utilities.getPrefs(getContext()).registerOnSharedPreferenceChangeListener(this);
            super.onInit(savedInstanceState);
        }

        @Override
        public void onDestroy() {
            Utilities.getPrefs(getContext()).unregisterOnSharedPreferenceChangeListener(this);
            super.onDestroy();
        }

        @Override
        public boolean isQsbEnabled() {
            return true;
        }

        @Override
        protected QsbWidgetHost createHost() {
            return new QsbWidgetHost(getContext(), QSB_WIDGET_HOST_ID,
                    (c) -> new QsbWidgetHostView(c));
        }

        @Override
        protected AppWidgetProviderInfo getSearchWidgetProvider() {
            return DockSearch.getWidgetInfo(getContext());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (KEY_DOCK_SEARCH.equals(key)) {
                rebindFragment();
            }
        }
    }

    public AllAppsQsb(Context context) {
        this(context, null);
    }

    public AllAppsQsb(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsQsb(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLauncher = Launcher.getLauncher(context);
        mSearchBarController = new AllAppsSearchBarController();

        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);

        mFixedTranslationY = getTranslationY();
        mMarginTopAdjusting = mFixedTranslationY - getPaddingTop();
        setPadding(0, 0, 0, 0);

        mMinTopInset = context.getResources().getDimensionPixelSize(R.dimen.all_apps_min_top_inset);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLauncher.getAppsView().getAppsStore().addUpdateListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLauncher.getAppsView().getAppsStore().removeUpdateListener(this);
    }

    private boolean shouldHideDockSearch() {
        return DockSearch.getWidgetInfo(mLauncher) == null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSearchWrapperView = findViewById(R.id.search_wrapper_view);
        mSearchWrapperView.setVisibility(shouldHideDockSearch()
                ? View.GONE
                : View.VISIBLE);

        mFallbackSearchView = findViewById(R.id.fallback_search_view);
        mFallbackSearchViewText = findViewById(R.id.fallback_search_view_text);

        mFallbackSearchView.setVisibility(View.INVISIBLE);

        Context context = getContext();

        int bgColor = Themes.getAttrColor(context, R.attr.shadeColorSearchBar);
        int overlay = Themes.getAttrColor(context, R.attr.shadeColorAllAppsOverlay);

        if (ColorUtils.setAlphaComponent(overlay, 0) != overlay) {
            // Alpha is not zero, so update it to the right value.
            overlay = ColorUtils.setAlphaComponent(overlay,
                    context.getResources().getInteger(R.integer.shade_qsb_color_alpha));

            bgColor = ColorUtils.compositeColors(overlay, bgColor);
        }

        mFallbackSearchView.setColor(bgColor);

        if (Utilities.ATLEAST_Q) {
            // The corners should be 2x as curved as the dialog curve.
            float radius = Themes.getDialogCornerRadius(context) * 2f;
            mFallbackSearchView.setRadius(radius);

            InsetDrawable inset = (InsetDrawable) mFallbackSearchView.getBackground();
            RippleDrawable ripple = (RippleDrawable) inset.getDrawable();
            GradientDrawable shape =
                    (GradientDrawable) ripple.findDrawableByLayerId(android.R.id.mask);
            shape.setCornerRadius(radius);
        }

        mFallbackSearchViewText.setSpannedHint(mFallbackSearchViewText.getHint());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Update the width to match the grid padding
        int myRequestedWidth = getSize(widthMeasureSpec);
        int myRequestedHeight = getSize(heightMeasureSpec);

        DeviceProfile dp = mLauncher.getDeviceProfile();

        int rowWidth = myRequestedWidth - mAppsView.getActiveRecyclerView().getPaddingLeft()
                - mAppsView.getActiveRecyclerView().getPaddingRight();

        int cellWidth = DeviceProfile.calculateCellWidth(rowWidth, dp.inv.numHotseatIcons);
        int iconVisibleSize = Math.round(ICON_VISIBLE_AREA_FACTOR * dp.iconSizePx);
        int iconPadding = cellWidth - iconVisibleSize;

        int myWidth = rowWidth - iconPadding + getPaddingLeft() + getPaddingRight();

        Resources res = getResources();
        int widgetPad = res.getDimensionPixelSize(R.dimen.qsb_widget_padding);

        mFallbackSearchView.measure(makeMeasureSpec(myWidth + 2 * widgetPad, EXACTLY),
                makeMeasureSpec(myRequestedHeight + widgetPad, EXACTLY));

        mCurrentWidgetPadding[0] = 0;
        mCurrentWidgetPadding[1] = 0;

        View child = getWidgetChild();
        if (child != null) {
            calcPadding(child);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            int size = res.getDimensionPixelSize(R.dimen.qsb_wrapper_height) - widgetPad;
            if (lp.height > 0 && size > lp.height) {
                lp.height = size;
            }
        }

        mSearchWrapperView.setPadding(Math.max(0, widgetPad - mCurrentWidgetPadding[0]), 0,
                Math.max(0, widgetPad - mCurrentWidgetPadding[1]), 0);

        mSearchWrapperView.measure(makeMeasureSpec(myWidth + 2 * widgetPad, EXACTLY),
                makeMeasureSpec(myRequestedHeight, EXACTLY));
    }

    private View getWidgetChild() {
        if (mSearchWrapperView != null && mSearchWrapperView.getChildCount() == 1) {
            View fragmentView = mSearchWrapperView.getChildAt(0);
            if (fragmentView instanceof QsbWidgetHostView) {
                QsbWidgetHostView hostView = (QsbWidgetHostView) fragmentView;
                if (hostView.getChildCount() == 1) {
                    return hostView.getChildAt(0);
                }
            }
        }
        return null;
    }

    private void calcPadding(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof MarginLayoutParams) {
            MarginLayoutParams mlp = (MarginLayoutParams) params;
            mCurrentWidgetPadding[0] += mlp.leftMargin;
            mCurrentWidgetPadding[1] += mlp.rightMargin;
        }

        mCurrentWidgetPadding[0] += view.getPaddingLeft();
        mCurrentWidgetPadding[1] += view.getPaddingRight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Shift the widget horizontally so that its centered in the parent (b/63428078)
        View parent = (View) getParent();
        int availableWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        int myWidth = right - left;
        int expectedLeft = parent.getPaddingLeft() + (availableWidth - myWidth) / 2;
        int shift = expectedLeft - left;
        setTranslationX(shift);
    }

    @Override
    public void initialize(AllAppsContainerView appsView) {
        mApps = appsView.getApps();
        mAppsView = appsView;
        mSearchBarController.initialize(
                new HiddenAppsSearchAlgorithm(mLauncher, appsView.getAppsStore()),
                mFallbackSearchViewText, mLauncher, this);
    }

    @Override
    public void onAppsUpdated() {
        mSearchBarController.refreshSearchResult();
    }

    @Override
    public void resetSearch() {
        mSearchBarController.reset();
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent event) {
        // Determine if the key event was actual text, if so, focus the search bar and then dispatch
        // the key normally so that it can process this key event
        if (!mSearchBarController.isSearchFieldFocused() &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            final int unicodeChar = event.getUnicodeChar();
            final boolean isKeyNotWhitespace = unicodeChar > 0 &&
                    !Character.isWhitespace(unicodeChar) && !Character.isSpaceChar(unicodeChar);
            if (isKeyNotWhitespace) {
                boolean gotKey = TextKeyListener.getInstance().onKeyDown(this, mSearchQueryBuilder,
                        event.getKeyCode(), event);
                if (gotKey && mSearchQueryBuilder.length() > 0) {
                    mSearchBarController.focusSearchField();
                }
            }
        }
    }

    @Override
    public void onSearchResult(String query, ArrayList<ComponentKey> apps) {
        if (apps != null) {
            mApps.setOrderedFilter(apps);
            notifyResultChanged();
            mAppsView.setLastSearchQuery(query);
        }
    }

    @Override
    public void clearSearchResult() {
        if (mApps.setOrderedFilter(null)) {
            notifyResultChanged();
        }

        // Clear the search query
        mSearchQueryBuilder.clear();
        mSearchQueryBuilder.clearSpans();
        Selection.setSelection(mSearchQueryBuilder, 0);
        mAppsView.onClearSearchResult();
    }

    private void notifyResultChanged() {
        mAppsView.onSearchResultsChanged();
        mAppsView.getFloatingHeaderView().setCollapsed(hasSearchQuery() || mApps.hasNoFilteredResults());
    }

    @Override
    public void setInsets(Rect insets) {
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        int topInset = Math.max(mMinTopInset, insets.top);
        mlp.topMargin = Math.round(Math.max(
                -mFixedTranslationY, topInset - mMarginTopAdjusting));
        requestLayout();
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        if (mLauncher.getDeviceProfile().isVerticalBarLayout() || shouldHideDockSearch()) {
            return 0;
        } else {
            int topInset = Math.max(mMinTopInset, insets.top);
            int topMargin = Math.round(Math.max(
                    -mFixedTranslationY, topInset - mMarginTopAdjusting));

            DeviceProfile dp = mLauncher.getWallpaperDeviceProfile();
            int searchPadding = getLayoutParams().height;
            int hotseatPadding = dp.hotseatBarBottomPaddingPx - searchPadding;

            return insets.bottom + topMargin + mFixedTranslationY + searchPadding
                    + hotseatPadding * 0.65f;
        }
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter,
                                     Interpolator interpolator) {
        boolean showDock = (visibleElements & HOTSEAT_SEARCH_BOX) != 0;
        boolean showAllApps = (visibleElements & ALL_APPS_CONTENT) != 0;
        setter.setViewAlpha(mSearchWrapperView,
                showDock && !shouldHideDockSearch() ? 1f : 0f, interpolator);
        setter.setViewAlpha(mFallbackSearchView,
                showAllApps ? 1f : 0f, interpolator);
    }

    public void requestSearch() {
        mSearchRequested = true;
    }

    public void showKeyboardOnSearchRequest() {
        if (mSearchRequested) {
            mSearchRequested = false;
            mFallbackSearchViewText.showKeyboard();
        }
    }

    public boolean tryClearSearch() {
        if (mFallbackSearchViewText.length() > 0) {
            mAppsView.reset(true);
            mAppsView.requestFocus();
            return true;
        }
        return false;
    }

    public boolean hasSearchQuery() {
        return !TextUtils.isEmpty(mSearchBarController.searchQuery());
    }
}
