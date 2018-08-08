/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.launcher3.allapps.search;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.launcher3.discovery.AppDiscoveryItem;
import com.android.launcher3.discovery.AppDiscoveryUpdateState;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.PackageManagerHelper;

import java.util.ArrayList;

/**
 * An interface to a search box that AllApps can command.
 */
public class AllAppsSearchBarController
        implements TextWatcher, OnEditorActionListener, ExtendedEditText.OnBackKeyListener {

    protected Launcher mLauncher;
    protected Callbacks mCb;
    protected ExtendedEditText mInput;
    protected String mQuery;

    protected SearchAlgorithm mSearchAlgorithm;
    protected InputMethodManager mInputMethodManager;

    public void setVisibility(int visibility) {
        mInput.setVisibility(visibility);
    }
    /**
     * Sets the references to the apps model and the search result callback.
     */
    public final void initialize(
            SearchAlgorithm searchAlgorithm, ExtendedEditText input,
            Launcher launcher, Callbacks cb) {
        mCb = cb;
        mLauncher = launcher;

        mInput = input;
        mInput.addTextChangedListener(this);
        mInput.setOnEditorActionListener(this);
        mInput.setOnBackKeyListener(this);

        mInputMethodManager = (InputMethodManager)
                mInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        mSearchAlgorithm = searchAlgorithm;
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
    public void afterTextChanged(final Editable s) {
        mQuery = s.toString();
        if (mQuery.isEmpty()) {
            mSearchAlgorithm.cancel(true);
            mCb.clearSearchResult();
        } else {
            mSearchAlgorithm.cancel(false);
            mSearchAlgorithm.doSearch(mQuery, mCb);
        }
    }

    public void refreshSearchResult() {
        if (TextUtils.isEmpty(mQuery)) {
            return;
        }
        // If play store continues auto updating an app, we want to show partial result.
        mSearchAlgorithm.cancel(false);
        mSearchAlgorithm.doSearch(mQuery, mCb);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Skip if it's not the right action
        if (actionId != EditorInfo.IME_ACTION_SEARCH) {
            return false;
        }

        // Skip if the query is empty
        String query = v.getText().toString();
        if (query.isEmpty()) {
            ((InputMethodManager) mLauncher.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(v.getWindowToken(), 0);
            return false;
        }
        return mLauncher.startActivitySafely(v,
                PackageManagerHelper.getMarketSearchIntent(mLauncher, query), null);
    }

    @Override
    public boolean onBackKey() {
        // Only hide the search field if there is no query
        String query = Utilities.trim(mInput.getEditableText().toString());
        if (query.isEmpty()) {
            reset();
            return true;
        }
        return false;
    }

    /**
     * Resets the search bar state.
     */
    public void reset() {
        unfocusSearchField();
        mCb.clearSearchResult();
        mInput.setText("");
        mQuery = null;
        hideKeyboard();
    }

    protected void hideKeyboard() {
        mInputMethodManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    protected void unfocusSearchField() {
        View nextFocus = mInput.focusSearch(View.FOCUS_DOWN);
        if (nextFocus != null) {
            nextFocus.requestFocus();
        }
    }

    /**
     * Focuses the search field to handle key events.
     */
    public void focusSearchField() {
        mInput.showKeyboard();
    }

    /**
     * Returns whether the search field is focused.
     */
    public boolean isSearchFieldFocused() {
        return mInput.isFocused();
    }

    /**
     * Callback for getting search results.
     */
    public interface Callbacks {

        /**
         * Called when the search is complete.
         *
         * @param apps sorted list of matching components or null if in case of failure.
         */
        void onSearchResult(String query, ArrayList<ComponentKey> apps);

        /**
         * Called when the search results should be cleared.
         */
        void clearSearchResult();

        /**
         * Called when the app discovery is providing an update of search, which can either be
         * START for starting a new discovery,
         * UPDATE for providing a new search result, can be called multiple times,
         * END for indicating the end of results.
         *
         * @param app result item if UPDATE, else null
         * @param app the update state, START, UPDATE or END
         */
        void onAppDiscoverySearchUpdate(@Nullable AppDiscoveryItem app,
                @NonNull AppDiscoveryUpdateState state);
    }

}