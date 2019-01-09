package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;
import com.autotech.barcodemobilereader.R;
import com.autotech.barcodemobilereader.controller.barcodescanner.Size;
import com.autotech.barcodemobilereader.controller.barcodescanner.Util;

public class CameraInstance {
    private static final String TAG = CameraInstance.class.getSimpleName();

    private CameraThread cameraThread;
    private CameraSurface surface;

    private CameraManager cameraManager;
    private Handler readyHandler;
    private DisplayConfiguration displayConfiguration;
    private boolean open = false;
    private CameraSettings cameraSettings = new CameraSettings();
    private Runnable opener = new Runnable() {
        @Override
        public void run() {
            try {
                cameraManager.open();
            } catch (Exception e) {
                notifyError(e);
            }
        }
    };
    private Runnable configure = new Runnable() {
        @Override
        public void run() {
            try {
                cameraManager.configure();
                if (readyHandler != null) {
                    readyHandler.obtainMessage(R.id.zxing_prewiew_size_ready, getPreviewSize()).sendToTarget();
                }
            } catch (Exception e) {
                notifyError(e);
            }
        }
    };
    private Runnable previewStarter = new Runnable() {
        @Override
        public void run() {
            try {
                cameraManager.setPreviewDisplay(surface);
                cameraManager.startPreview();
            } catch (Exception e) {
                notifyError(e);
            }
        }
    };
    private Runnable closer = new Runnable() {
        @Override
        public void run() {
            try {
                cameraManager.stopPreview();
                cameraManager.close();
            } catch (Exception ignored) {
            }

            cameraThread.decrementInstances();
        }
    };

    public CameraInstance(Context context) {
        Util.validateMainThread();

        this.cameraThread = CameraThread.getInstance();
        this.cameraManager = new CameraManager(context);
        this.cameraManager.setCameraSettings(cameraSettings);
    }

    public CameraInstance(CameraManager cameraManager) {
        Util.validateMainThread();

        this.cameraManager = cameraManager;
    }

    public DisplayConfiguration getDisplayConfiguration() {
        return displayConfiguration;
    }

    public void setDisplayConfiguration(DisplayConfiguration configuration) {
        this.displayConfiguration = configuration;
        cameraManager.setDisplayConfiguration(configuration);
    }

    public void setReadyHandler(Handler readyHandler) {
        this.readyHandler = readyHandler;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        setSurface(new CameraSurface(surfaceHolder));
    }

    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public void setCameraSettings(CameraSettings cameraSettings) {
        if (!open) {
            this.cameraSettings = cameraSettings;
            this.cameraManager.setCameraSettings(cameraSettings);
        }
    }

    private Size getPreviewSize() {
        return cameraManager.getPreviewSize();
    }

    public int getCameraRotation() {
        return cameraManager.getCameraRotation();
    }

    public void open() {
        Util.validateMainThread();

        open = true;

        cameraThread.incrementAndEnqueue(opener);
    }

    public void configureCamera() {
        Util.validateMainThread();
        validateOpen();

        cameraThread.enqueue(configure);
    }

    public void startPreview() {
        Util.validateMainThread();
        validateOpen();

        cameraThread.enqueue(previewStarter);
    }

    public void setTorch(final boolean on) {
        Util.validateMainThread();

        if (open) {
            cameraThread.enqueue(new Runnable() {
                @Override
                public void run() {
                    cameraManager.setTorch(on);
                }
            });
        }
    }

    public void close() {
        Util.validateMainThread();

        if (open) {
            cameraThread.enqueue(closer);
        }

        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void requestPreview(final PreviewCallback callback) {
        validateOpen();

        cameraThread.enqueue(new Runnable() {
            @Override
            public void run() {
                cameraManager.requestPreviewFrame(callback);
            }
        });
    }

    private void validateOpen() {
        if (!open) {
            throw new IllegalStateException("CameraInstance is not open");
        }
    }

    private void notifyError(Exception error) {
        if (readyHandler != null) {
            readyHandler.obtainMessage(R.id.zxing_camera_error, error).sendToTarget();
        }
    }

    protected CameraManager getCameraManager() {
        return cameraManager;
    }

    protected CameraThread getCameraThread() {
        return cameraThread;
    }

    protected CameraSurface getSurface() {
        return surface;
    }

    public void setSurface(CameraSurface surface) {
        this.surface = surface;
    }
}
