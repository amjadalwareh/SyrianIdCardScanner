package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.graphics.Rect;
import com.autotech.barcodemobilereader.controller.barcodescanner.Size;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LegacyPreviewScalingStrategy extends PreviewScalingStrategy {
    private static final String TAG = LegacyPreviewScalingStrategy.class.getSimpleName();

    public static Size scale(Size from, Size to) {
        Size current = from;

        if (!to.fitsIn(current)) {
            while (true) {
                Size scaled150 = current.scale(3, 2);
                Size scaled200 = current.scale(2, 1);
                if (to.fitsIn(scaled150)) {
                    return scaled150;
                } else if (to.fitsIn(scaled200)) {
                    return scaled200;
                } else {
                    current = scaled200;
                }
            }
        } else {
            while (true) {
                Size scaled66 = current.scale(2, 3);
                Size scaled50 = current.scale(1, 2);

                if (!to.fitsIn(scaled50)) {
                    if (to.fitsIn(scaled66)) {
                        // Scale by 2/3
                        return scaled66;
                    } else {
                        return current;
                    }
                } else {
                    current = scaled50;
                }
            }
        }
    }

    public Size getBestPreviewSize(List<Size> sizes, final Size desired) {

        if (desired == null) {
            return sizes.get(0);
        }

        Collections.sort(sizes, new Comparator<Size>() {
            @Override
            public int compare(Size a, Size b) {
                Size ascaled = scale(a, desired);
                int aScale = ascaled.width - a.width;
                Size bscaled = scale(b, desired);
                int bScale = bscaled.width - b.width;

                if (aScale == 0 && bScale == 0) {
                    return a.compareTo(b);
                } else if (aScale == 0) {
                    return -1;
                } else if (bScale == 0) {
                    return 1;
                } else if (aScale < 0 && bScale < 0) {
                    return a.compareTo(b);
                } else if (aScale > 0 && bScale > 0) {
                    return -a.compareTo(b);
                } else if (aScale < 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return sizes.get(0);
    }

    public Rect scalePreview(Size previewSize, Size viewfinderSize) {
        Size scaledPreview = scale(previewSize, viewfinderSize);

        int dx = (scaledPreview.width - viewfinderSize.width) / 2;
        int dy = (scaledPreview.height - viewfinderSize.height) / 2;

        return new Rect(-dx, -dy, scaledPreview.width - dx, scaledPreview.height - dy);
    }
}