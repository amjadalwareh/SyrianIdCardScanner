package com.autotech.barcodemobilereader.controller.barcodescanner.camera;


import com.autotech.barcodemobilereader.controller.barcodescanner.SourceData;

public interface PreviewCallback {
    void onPreview(SourceData sourceData);
}
