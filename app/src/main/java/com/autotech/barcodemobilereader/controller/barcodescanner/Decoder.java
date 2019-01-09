package com.autotech.barcodemobilereader.controller.barcodescanner;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.List;

public class Decoder implements ResultPointCallback {
    private Reader reader;
    private List<ResultPoint> possibleResultPoints = new ArrayList<>();

    public Decoder(Reader reader) {
        this.reader = reader;
    }

    protected Reader getReader() {
        return reader;
    }

    public Result decode(LuminanceSource source) {
        return decode(toBitmap(source));
    }

    protected BinaryBitmap toBitmap(LuminanceSource source) {
        return new BinaryBitmap(new HybridBinarizer(source));
    }

    protected Result decode(BinaryBitmap bitmap) {
        possibleResultPoints.clear();
        try {
            if (reader instanceof MultiFormatReader) {
                return ((MultiFormatReader) reader).decodeWithState(bitmap);
            } else {
                return reader.decode(bitmap);
            }
        } catch (Exception e) {
            return null;
        } finally {
            reader.reset();
        }
    }

    public List<ResultPoint> getPossibleResultPoints() {
        return new ArrayList<>(possibleResultPoints);
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }
}
