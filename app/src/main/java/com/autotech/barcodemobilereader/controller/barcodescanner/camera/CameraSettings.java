package com.autotech.barcodemobilereader.controller.barcodescanner.camera;


import com.autotech.barcodemobilereader.controller.zxing.client.android.camera.open.OpenCameraInterface;

public class CameraSettings {
    private int requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA;
    private boolean scanInverted = false;
    private boolean barcodeSceneModeEnabled = false;
    private boolean meteringEnabled = false;
    private boolean autoFocusEnabled = true;
    private boolean continuousFocusEnabled = false;
    private boolean exposureEnabled = false;
    private boolean autoTorchEnabled = false;
    private FocusMode focusMode = FocusMode.AUTO;

    public int getRequestedCameraId() {
        return requestedCameraId;
    }

    public void setRequestedCameraId(int requestedCameraId) {
        this.requestedCameraId = requestedCameraId;
    }

    public boolean isScanInverted() {
        return scanInverted;
    }

    public void setScanInverted(boolean scanInverted) {
        this.scanInverted = scanInverted;
    }

    public boolean isBarcodeSceneModeEnabled() {
        return barcodeSceneModeEnabled;
    }

    public void setBarcodeSceneModeEnabled(boolean barcodeSceneModeEnabled) {
        this.barcodeSceneModeEnabled = barcodeSceneModeEnabled;
    }

    public boolean isExposureEnabled() {
        return exposureEnabled;
    }

    public void setExposureEnabled(boolean exposureEnabled) {
        this.exposureEnabled = exposureEnabled;
    }

    public boolean isMeteringEnabled() {
        return meteringEnabled;
    }

    public void setMeteringEnabled(boolean meteringEnabled) {
        this.meteringEnabled = meteringEnabled;
    }

    public boolean isAutoFocusEnabled() {
        return autoFocusEnabled;
    }

    public void setAutoFocusEnabled(boolean autoFocusEnabled) {
        this.autoFocusEnabled = autoFocusEnabled;

        if (autoFocusEnabled && continuousFocusEnabled) {
            focusMode = FocusMode.CONTINUOUS;
        } else if (autoFocusEnabled) {
            focusMode = FocusMode.AUTO;
        } else {
            focusMode = null;
        }
    }

    public boolean isContinuousFocusEnabled() {
        return continuousFocusEnabled;
    }

    public void setContinuousFocusEnabled(boolean continuousFocusEnabled) {
        this.continuousFocusEnabled = continuousFocusEnabled;

        if (continuousFocusEnabled) {
            focusMode = FocusMode.CONTINUOUS;
        } else if (autoFocusEnabled) {
            focusMode = FocusMode.AUTO;
        } else {
            focusMode = null;
        }
    }

    public FocusMode getFocusMode() {
        return focusMode;
    }

    public void setFocusMode(FocusMode focusMode) {
        this.focusMode = focusMode;
    }

    public boolean isAutoTorchEnabled() {
        return autoTorchEnabled;
    }

    public void setAutoTorchEnabled(boolean autoTorchEnabled) {
        this.autoTorchEnabled = autoTorchEnabled;
    }

    public enum FocusMode {
        AUTO,
        CONTINUOUS,
        INFINITY,
        MACRO
    }
}
