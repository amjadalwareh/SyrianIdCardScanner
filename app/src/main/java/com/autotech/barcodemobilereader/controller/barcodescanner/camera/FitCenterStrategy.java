package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.graphics.Rect;
import com.autotech.barcodemobilereader.controller.barcodescanner.Size;

public class FitCenterStrategy extends PreviewScalingStrategy {
    private static final String TAG = FitCenterStrategy.class.getSimpleName();


    @Override
    protected float getScore(Size size, Size desired) {
        if (size.width <= 0 || size.height <= 0) {
            return 0f;
        }
        Size scaled = size.scaleFit(desired);
        float scaleRatio = scaled.width * 1.0f / size.width;

        float scaleScore;
        if (scaleRatio > 1.0f) {
            scaleScore = (float) Math.pow(1.0f / scaleRatio, 1.1);
        } else {
            scaleScore = scaleRatio;
        }

        float cropRatio = (desired.width * 1.0f / scaled.width) *
                (desired.height * 1.0f / scaled.height);
        float cropScore = 1.0f / cropRatio / cropRatio / cropRatio;

        return scaleScore * cropScore;
    }

    public Rect scalePreview(Size previewSize, Size viewfinderSize) {
        Size scaledPreview = previewSize.scaleFit(viewfinderSize);
        int dx = (scaledPreview.width - viewfinderSize.width) / 2;
        int dy = (scaledPreview.height - viewfinderSize.height) / 2;

        return new Rect(-dx, -dy, scaledPreview.width - dx, scaledPreview.height - dy);
    }

}
