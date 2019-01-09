package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collection;

public final class AutoFocusManager {
    private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private final boolean useAutoFocus;
    private final Camera camera;
    private boolean stopped;
    private boolean focusing;
    private Handler handler;
    private int MESSAGE_FOCUS = 1;
    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera theCamera) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    focusing = false;
                    autoFocusAgainLater();
                }
            });
        }
    };
    private final Handler.Callback focusHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MESSAGE_FOCUS) {
                focus();
                return true;
            }
            return false;
        }
    };

    public AutoFocusManager(Camera camera, CameraSettings settings) {
        this.handler = new Handler(focusHandlerCallback);
        this.camera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        useAutoFocus = settings.isAutoFocusEnabled() && FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        start();
    }


    private synchronized void autoFocusAgainLater() {
        if (!stopped && !handler.hasMessages(MESSAGE_FOCUS)) {
            handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_FOCUS), AUTO_FOCUS_INTERVAL_MS);
        }
    }

    public void start() {
        stopped = false;
        focus();
    }

    private void focus() {
        if (useAutoFocus) {
            if (!stopped && !focusing) {
                try {
                    camera.autoFocus(autoFocusCallback);
                    focusing = true;
                } catch (RuntimeException re) {
                    autoFocusAgainLater();
                }
            }
        }
    }

    private void cancelOutstandingTask() {
        handler.removeMessages(MESSAGE_FOCUS);
    }

    public void stop() {
        stopped = true;
        focusing = false;
        cancelOutstandingTask();
        if (useAutoFocus) {
            // Doesn't hurt to call this even if not focusing
            try {
                camera.cancelAutoFocus();
            } catch (RuntimeException ignored) {
            }
        }
    }

}
