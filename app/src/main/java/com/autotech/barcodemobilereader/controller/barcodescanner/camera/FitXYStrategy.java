package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.graphics.Rect;
import com.autotech.barcodemobilereader.controller.barcodescanner.Size;

public class FitXYStrategy extends PreviewScalingStrategy {
    private static final String TAG = FitXYStrategy.class.getSimpleName();


    private static float absRatio(float ratio) {
        if (ratio < 1.0f)
            return 1.0f / ratio;
        else
            return ratio;
    }

    @Override
    protected float getScore(Size size, Size desired) {
        if (size.width <= 0 || size.height <= 0) {
            return 0f;
        }
        float scaleX = absRatio(size.width * 1.0f / desired.width);
        float scaleY = absRatio(size.height * 1.0f / desired.height);

        float scaleScore = 1.0f / scaleX / scaleY;

        float distortion = absRatio((1.0f * size.width / size.height) / (1.0f * desired.width / desired.height));

        float distortionScore = 1.0f / distortion / distortion / distortion;

        return scaleScore * distortionScore;
    }

    public Rect scalePreview(Size previewSize, Size viewfinderSize) {
        return new Rect(0, 0, viewfinderSize.width, viewfinderSize.height);
    }
}
