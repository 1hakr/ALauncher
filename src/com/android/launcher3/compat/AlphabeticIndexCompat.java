package com.android.launcher3.compat;

import android.content.Context;
import android.icu.text.AlphabeticIndex;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import com.android.launcher3.Utilities;

import java.lang.reflect.Method;
import java.util.Locale;

public class AlphabeticIndexCompat {

    private static final String MID_DOT = "\u2219";
    private final String mDefaultMiscLabel;

    private AlphabeticIndex.ImmutableIndex mBaseIndex;
    private AlphabeticIndexV16 mIndex;

    public AlphabeticIndexCompat(Context context) {
        this(ConfigurationCompat.getLocales(context.getResources().getConfiguration()));
    }

    public AlphabeticIndexCompat(LocaleListCompat locales) {
        int localeCount = locales.size();

        Locale primaryLocale = localeCount == 0 ? Locale.ENGLISH : locales.get(0);
        if(Utilities.ATLEAST_NOUGAT) {
            AlphabeticIndex indexBuilder = new AlphabeticIndex(primaryLocale);
            for (int i = 1; i < localeCount; i++) {
                indexBuilder.addLabels(locales.get(i));
            }
            indexBuilder.addLabels(Locale.ENGLISH);
            mBaseIndex = indexBuilder.buildImmutableIndex();
        } else {
            try {
                mIndex = new AlphabeticIndexV16(primaryLocale);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (primaryLocale.getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            // Japanese character 他 ("misc")
            mDefaultMiscLabel = "\u4ed6";
            // TODO(winsonc, omakoto): We need to handle Japanese sections better,
            // especially the kanji
        } else {
            // Dot
            mDefaultMiscLabel = MID_DOT;
        }
    }

    public String getSection(String s) {
        if(Utilities.ATLEAST_NOUGAT) {
            return mBaseIndex.getBucket(mBaseIndex.getBucketIndex(s)).getLabel();
        } else {
           return mIndex.getBucketLabel(mIndex.getBucketIndex(s));
        }
    }

    /**
     * Computes the section name for an given string {@param s}.
     */
    public String computeSectionName(@NonNull CharSequence cs) {
        String s = Utilities.trim(cs);
        String sectionName = getSection(cs.toString());
        if (Utilities.trim(sectionName).isEmpty() && s.length() > 0) {
            int c = s.codePointAt(0);
            boolean startsWithDigit = Character.isDigit(c);
            if (startsWithDigit) {
                // Digit section
                return "#";
            } else {
                boolean startsWithLetter = Character.isLetter(c);
                if (startsWithLetter) {
                    return mDefaultMiscLabel;
                } else {
                    // In languages where these differ, this ensures that we differentiate
                    // between the misc section in the native language and a misc section
                    // for everything else.
                    return MID_DOT;
                }
            }
        }
        return sectionName;
    }

    /**
     * Base class to support Alphabetic indexing if not supported by the framework.
     * TODO(winsonc): disable for non-english locales
     */
    private static class BaseIndex {

        private static final String BUCKETS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-";
        private static final int UNKNOWN_BUCKET_INDEX = BUCKETS.length() - 1;

        /**
         * Returns the index of the bucket in which the given string should appear.
         */
        protected int getBucketIndex(String s) {
            if (s.isEmpty()) {
                return UNKNOWN_BUCKET_INDEX;
            }
            int index = BUCKETS.indexOf(s.substring(0, 1).toUpperCase());
            if (index != -1) {
                return index;
            }
            return UNKNOWN_BUCKET_INDEX;
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            return BUCKETS.substring(index, index + 1);
        }
    }

    /**
     * Reflected libcore.icu.AlphabeticIndex implementation, falls back to the base
     * alphabetic index.
     */
    private static class AlphabeticIndexV16 extends BaseIndex {

        private Object mAlphabeticIndex;
        private Method mGetBucketIndexMethod;
        private Method mGetBucketLabelMethod;

        public AlphabeticIndexV16(Locale locale) throws Exception {
            Class clazz = Class.forName("libcore.icu.AlphabeticIndex");
            mGetBucketIndexMethod = clazz.getDeclaredMethod("getBucketIndex", String.class);
            mGetBucketLabelMethod = clazz.getDeclaredMethod("getBucketLabel", int.class);
            mAlphabeticIndex = clazz.getConstructor(Locale.class).newInstance(locale);

            if (!locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                clazz.getDeclaredMethod("addLabels", Locale.class)
                        .invoke(mAlphabeticIndex, Locale.ENGLISH);
            }
        }

        /**
         * Returns the index of the bucket in which {@param s} should appear.
         * Function is synchronized because underlying routine walks an iterator
         * whose state is maintained inside the index object.
         */
        protected int getBucketIndex(String s) {
            try {
                return (Integer) mGetBucketIndexMethod.invoke(mAlphabeticIndex, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketIndex(s);
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            try {
                return (String) mGetBucketLabelMethod.invoke(mAlphabeticIndex, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketLabel(index);
        }
    }
}
