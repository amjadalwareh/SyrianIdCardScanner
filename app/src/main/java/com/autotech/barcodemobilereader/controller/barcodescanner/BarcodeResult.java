package com.autotech.barcodemobilereader.controller.barcodescanner;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

import java.util.Map;

public class BarcodeResult {
    private static final float PREVIEW_LINE_WIDTH = 4.0f;
    private static final float PREVIEW_DOT_WIDTH = 10.0f;
    private final int mScaleFactor = 2;
    protected Result mResult;
    protected SourceData sourceData;
    Context context;

    public BarcodeResult(Result result, SourceData sourceData) {
        this.mResult = result;
        this.sourceData = sourceData;
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, int scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(a.getX() / scaleFactor,
                    a.getY() / scaleFactor,
                    b.getX() / scaleFactor,
                    b.getY() / scaleFactor,
                    paint);
        }
    }

    public Result getResult() {
        return mResult;
    }

    public Bitmap getBitmap() {
        return sourceData.getBitmap(mScaleFactor);
    }

    public Bitmap getBitmapWithResultPoints(int color) {
        Bitmap bitmap = getBitmap();
        Bitmap barcode = bitmap;
        ResultPoint[] points = mResult.getResultPoints();

        if (points != null && points.length > 0 && bitmap != null) {
            barcode = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(barcode);
            canvas.drawBitmap(bitmap, 0, 0, null);
            Paint paint = new Paint();
            paint.setColor(color);
            if (points.length == 2) {
                paint.setStrokeWidth(PREVIEW_LINE_WIDTH);
                drawLine(canvas, paint, points[0], points[1], mScaleFactor);
            } else if (points.length == 4 &&
                    (mResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            mResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                drawLine(canvas, paint, points[0], points[1], mScaleFactor);
                drawLine(canvas, paint, points[2], points[3], mScaleFactor);
            } else {
                paint.setStrokeWidth(PREVIEW_DOT_WIDTH);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(point.getX() / mScaleFactor, point.getY() / mScaleFactor, paint);
                    }
                }
            }
        }
        return barcode;
    }

    public int getBitmapScaleFactor() {
        return mScaleFactor;
    }

    public String getText() {
        return mResult.getText();
    }

    public byte[] getRawBytes() {
        return mResult.getRawBytes();
    }

    public ResultPoint[] getResultPoints() {
        return mResult.getResultPoints();
    }

    public BarcodeFormat getBarcodeFormat() {
        return mResult.getBarcodeFormat();
    }

    public Map<ResultMetadataType, Object> getResultMetadata() {
        return mResult.getResultMetadata();
    }

    public long getTimestamp() {
        return mResult.getTimestamp();
    }

    @Override
    public String toString() {
        return mResult.getText();
    }
}
