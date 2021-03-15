package com.android.launcher3.anim;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ObjectAnimatorCompat {
    private static final int MAX_NUM_POINTS = 100;

    public static ObjectAnimator ofFloat(
            @NonNull Object target,
            @Nullable String xPropertyName,
            @Nullable String yPropertyName,
            @NonNull Path path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ObjectAnimator.ofFloat(target, xPropertyName, yPropertyName, path);
        } else {
            final ObjectAnimator anim = new ObjectAnimator();
            anim.setTarget(target);
            animateAlongPath(anim, xPropertyName, yPropertyName, path);
            return anim;
        }
    }

    private static void animateAlongPath(
            @NonNull ObjectAnimator anim,
            @Nullable String xPropertyName,
            @Nullable String yPropertyName,
            @NonNull Path path) {
        if (xPropertyName == null && yPropertyName == null) {
            return;
        }

        // Measure the total length the whole path.
        final PathMeasure measureForTotalLength = new PathMeasure(path, false);
        float totalLength = 0;
        // The sum of the previous contour plus the current one. Using the sum here
        // because we want to directly subtract from it later.
        final List<Float> contourLengths = new ArrayList<>();
        contourLengths.add(0f);
        do {
            final float pathLength = measureForTotalLength.getLength();
            totalLength += pathLength;
            contourLengths.add(totalLength);
        } while (measureForTotalLength.nextContour());

        // Now determine how many sample points we need, and the step for next sample.
        final PathMeasure pathMeasure = new PathMeasure(path, false);

        final int numPoints = Math.min(MAX_NUM_POINTS, (int) (2 * totalLength) + 1);

        final float[] coordsX = new float[numPoints];
        final float[] coordsY = new float[numPoints];
        final float[] position = new float[2];

        int contourIndex = 0;
        final float step = totalLength / (numPoints - 1);
        float currentDistance = 0;

        // For each sample point, determine whether we need to move on to next contour.
        // After we find the right contour, then sample it using the current distance value minus
        // the previously sampled contours' total length.
        for (int i = 0; i < numPoints; i++) {
            pathMeasure.getPosTan(currentDistance, position, null);
            pathMeasure.getPosTan(currentDistance, position, null);

            coordsX[i] = position[0];
            coordsY[i] = position[1];
            currentDistance += step;
            if ((contourIndex + 1) < contourLengths.size()
                    && currentDistance > contourLengths.get(contourIndex + 1)) {
                currentDistance -= contourLengths.get(contourIndex + 1);
                contourIndex++;
                pathMeasure.nextContour();
            }
        }

        // Given the x and y value of the sample points, setup the ObjectAnimator properly.
        PropertyValuesHolder x = null;
        PropertyValuesHolder y = null;
        if (xPropertyName != null) {
            x = PropertyValuesHolder.ofFloat(xPropertyName, coordsX);
        }
        if (yPropertyName != null) {
            y = PropertyValuesHolder.ofFloat(yPropertyName, coordsY);
        }
        if (x == null) {
            anim.setValues(y);
        } else if (y == null) {
            anim.setValues(x);
        } else {
            anim.setValues(x, y);
        }
    }

    private ObjectAnimatorCompat() {
    }
}