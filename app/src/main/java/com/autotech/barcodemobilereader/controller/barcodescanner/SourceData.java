package com.autotech.barcodemobilereader.controller.barcodescanner;

import android.graphics.*;
import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.ByteArrayOutputStream;

public class SourceData {
    private byte[] data;

    private int dataWidth;

    private int dataHeight;

    private int imageFormat;

    private int rotation;

    private Rect cropRect;

    public SourceData(byte[] data, int dataWidth, int dataHeight, int imageFormat, int rotation) {
        this.data = data;
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.rotation = rotation;
        this.imageFormat = imageFormat;
    }

    public static byte[] rotateCameraPreview(int cameraRotation, byte[] data, int imageWidth, int imageHeight) {
        switch (cameraRotation) {
            case 0:
                return data;
            case 90:
                return rotateCW(data, imageWidth, imageHeight);
            case 180:
                return rotate180(data, imageWidth, imageHeight);
            case 270:
                return rotateCCW(data, imageWidth, imageHeight);
            default:
                return data;
        }
    }

    public static byte[] rotateCW(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight];
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        return yuv;
    }

    public static byte[] rotate180(byte[] data, int imageWidth, int imageHeight) {
        int n = imageWidth * imageHeight;
        byte[] yuv = new byte[n];

        int i = n - 1;
        for (int j = 0; j < n; j++) {
            yuv[i] = data[j];
            i--;
        }
        return yuv;
    }

    public static byte[] rotateCCW(byte[] data, int imageWidth, int imageHeight) {
        int n = imageWidth * imageHeight;
        byte[] yuv = new byte[n];
        int i = n - 1;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i--;
            }
        }
        return yuv;
    }

    public Rect getCropRect() {
        return cropRect;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
    }

    public byte[] getData() {
        return data;
    }

    public int getDataWidth() {
        return dataWidth;
    }

    public int getDataHeight() {
        return dataHeight;
    }

    public boolean isRotated() {
        return rotation % 180 != 0;
    }

    public int getImageFormat() {
        return imageFormat;
    }

    public PlanarYUVLuminanceSource createSource() {
        byte[] rotated = rotateCameraPreview(rotation, data, dataWidth, dataHeight);
        if (isRotated()) {
            return new PlanarYUVLuminanceSource(rotated, dataHeight, dataWidth, cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), false);
        } else {
            return new PlanarYUVLuminanceSource(rotated, dataWidth, dataHeight, cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), false);
        }
    }

    public Bitmap getBitmap() {
        return getBitmap(1);
    }

    public Bitmap getBitmap(int scaleFactor) {
        return getBitmap(cropRect, scaleFactor);
    }

    private Bitmap getBitmap(Rect cropRect, int scaleFactor) {
        if (isRotated()) {
            cropRect = new Rect(cropRect.top, cropRect.left, cropRect.bottom, cropRect.right);
        }

        YuvImage img = new YuvImage(data, imageFormat, dataWidth, dataHeight, null);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        img.compressToJpeg(cropRect, 90, buffer);
        byte[] jpegData = buffer.toByteArray();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);

        if (rotation != 0) {
            Matrix imageMatrix = new Matrix();
            imageMatrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), imageMatrix, false);
        }

        return bitmap;
    }
}