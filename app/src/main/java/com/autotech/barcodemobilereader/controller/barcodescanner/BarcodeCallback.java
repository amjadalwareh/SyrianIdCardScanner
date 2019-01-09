package com.autotech.barcodemobilereader.controller.barcodescanner;

import com.google.zxing.ResultPoint;

import java.util.List;

public interface BarcodeCallback {
    void barcodeResult(BarcodeResult result);

    void possibleResultPoints(List<ResultPoint> resultPoints);
}
