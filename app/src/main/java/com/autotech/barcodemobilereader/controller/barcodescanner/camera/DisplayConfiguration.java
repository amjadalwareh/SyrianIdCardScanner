package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.graphics.Rect;
import com.autotech.barcodemobilereader.controller.barcodescanner.Size;

import java.util.List;

public class DisplayConfiguration {
    private static final String TAG = DisplayConfiguration.class.getSimpleName();

    private Size viewfinderSize;
    private int rotation;
    private boolean center = false;
    private PreviewScalingStrategy previewScalingStrategy = new FitCenterStrategy();

    public DisplayConfiguration(int rotation) {
        this.rotation = rotation;
    }

    public DisplayConfiguration(int rotation, Size viewfinderSize) {
        this.rotation = rotation;
        this.viewfinderSize = viewfinderSize;
    }

    public int getRotation() {
        return rotation;
    }

    public Size getViewfinderSize() {
        return viewfinderSize;
    }

    public PreviewScalingStrategy getPreviewScalingStrategy() {
        return previewScalingStrategy;
    }

    public void setPreviewScalingStrategy(PreviewScalingStrategy previewScalingStrategy) {
        this.previewScalingStrategy = previewScalingStrategy;
    }

    public Size getDesiredPreviewSize(boolean rotate) {
        if (viewfinderSize == null) {
            return null;
        } else if (rotate) {
            return viewfinderSize.rotate();
        } else {
            return viewfinderSize;
        }
    }

    public Size getBestPreviewSize(List<Size> sizes, boolean isRotated) {
        // Sample of supported preview sizes:
        // http://www.kirill.org/ar/ar.php


        final Size desired = getDesiredPreviewSize(isRotated);

        return previewScalingStrategy.getBestPreviewSize(sizes, desired);
    }

    public Rect scalePreview(Size previewSize) {
        return previewScalingStrategy.scalePreview(previewSize, viewfinderSize);
    }
}
