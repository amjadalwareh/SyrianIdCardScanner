package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.graphics.Rect;
import com.autotech.barcodemobilereader.controller.barcodescanner.Size;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class PreviewScalingStrategy {

    public Size getBestPreviewSize(List<Size> sizes, final Size desired) {
        List<Size> ordered = getBestPreviewOrder(sizes, desired);
        return ordered.get(0);
    }

    public List<Size> getBestPreviewOrder(List<Size> sizes, final Size desired) {
        if (desired == null) {
            return sizes;
        }
        Collections.sort(sizes, new Comparator<Size>() {
            @Override
            public int compare(Size a, Size b) {
                float aScore = getScore(a, desired);
                float bScore = getScore(b, desired);
                return Float.compare(bScore, aScore);
            }
        });
        return sizes;
    }

    protected float getScore(Size size, Size desired) {
        return 0.5f;
    }

    public abstract Rect scalePreview(Size previewSize, Size viewfinderSize);
}
