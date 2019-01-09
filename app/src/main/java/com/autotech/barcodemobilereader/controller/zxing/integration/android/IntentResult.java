package com.autotech.barcodemobilereader.controller.zxing.integration.android;

public final class IntentResult {

    private final String contents;
    private final String formatName;
    private final byte[] rawBytes;
    private final Integer orientation;
    private final String errorCorrectionLevel;
    private final String barcodeImagePath;

    IntentResult() {
        this(null, null, null, null, null, null);
    }

    IntentResult(String contents,
                 String formatName,
                 byte[] rawBytes,
                 Integer orientation,
                 String errorCorrectionLevel,
                 String barcodeImagePath) {
        this.contents = contents;
        this.formatName = formatName;
        this.rawBytes = rawBytes;
        this.orientation = orientation;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.barcodeImagePath = barcodeImagePath;
    }

    public String getContents() {
        return contents;
    }

    public String getFormatName() {
        return formatName;
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }

    public Integer getOrientation() {
        return orientation;
    }

    public String getErrorCorrectionLevel() {
        return errorCorrectionLevel;
    }

    public String getBarcodeImagePath() {
        return barcodeImagePath;
    }

    @Override
    public String toString() {
        StringBuilder dialogText = new StringBuilder(120);
        dialogText.append("Format: ").append(formatName).append('\n');
        dialogText.append("Contents: ").append(contents).append('\n');
        int rawBytesLength = rawBytes == null ? 0 : rawBytes.length;
        dialogText.append("Raw bytes: (").append(rawBytesLength).append(" bytes)\n");
        dialogText.append("Orientation: ").append(orientation).append('\n');
        dialogText.append("EC level: ").append(errorCorrectionLevel).append('\n');
        dialogText.append("Barcode image: ").append(barcodeImagePath).append('\n');
        return dialogText.toString();
    }
}