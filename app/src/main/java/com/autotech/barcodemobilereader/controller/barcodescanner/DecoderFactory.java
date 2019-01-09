package com.autotech.barcodemobilereader.controller.barcodescanner;

import com.google.zxing.DecodeHintType;

import java.util.Map;

public interface DecoderFactory {
    Decoder createDecoder(Map<DecodeHintType, ?> baseHints);
}
