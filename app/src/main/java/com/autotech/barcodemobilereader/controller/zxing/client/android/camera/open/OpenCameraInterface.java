package com.autotech.barcodemobilereader.controller.zxing.client.android.camera.open;

import android.hardware.Camera;

public final class OpenCameraInterface {

    public static final int NO_REQUESTED_CAMERA = -1;
    private static final String TAG = OpenCameraInterface.class.getName();

    private OpenCameraInterface() {
    }

    public static int getCameraId(int requestedId) {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            return -1;
        }

        int cameraId = requestedId;

        boolean explicitRequest = cameraId >= 0;

        if (!explicitRequest) {
            int index = 0;
            while (index < numCameras) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(index, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    break;
                }
                index++;
            }

            cameraId = index;
        }

        if (cameraId < numCameras) {
            return cameraId;
        } else {
            if (explicitRequest) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static Camera open(int requestedId) {
        int cameraId = getCameraId(requestedId);
        if (cameraId == -1) {
            return null;
        } else {
            return Camera.open(cameraId);
        }
    }

}
