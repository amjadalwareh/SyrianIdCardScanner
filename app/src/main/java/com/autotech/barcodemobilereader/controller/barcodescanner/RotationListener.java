package com.autotech.barcodemobilereader.controller.barcodescanner;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import android.view.WindowManager;

public class RotationListener {
    private int lastRotation;

    private WindowManager windowManager;
    private OrientationEventListener orientationEventListener;
    private RotationCallback callback;

    public RotationListener() {
    }


    public void listen(Context context, RotationCallback callback) {
        stop();

        context = context.getApplicationContext();

        this.callback = callback;

        this.windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        this.orientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                WindowManager localWindowManager = windowManager;
                RotationCallback localCallback = RotationListener.this.callback;
                if (windowManager != null && localCallback != null) {
                    int newRotation = localWindowManager.getDefaultDisplay().getRotation();
                    if (newRotation != lastRotation) {
                        lastRotation = newRotation;
                        localCallback.onRotationChanged(newRotation);
                    }
                }
            }
        };
        this.orientationEventListener.enable();

        lastRotation = windowManager.getDefaultDisplay().getRotation();
    }

    public void stop() {
        if (this.orientationEventListener != null) {
            this.orientationEventListener.disable();
        }
        this.orientationEventListener = null;
        this.windowManager = null;
        this.callback = null;
    }
}
