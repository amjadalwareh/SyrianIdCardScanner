package com.autotech.barcodemobilereader.controller.barcodescanner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import com.autotech.barcodemobilereader.R;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarcodeView extends CameraPreview {

    private DecodeMode decodeMode = DecodeMode.NONE;
    private BarcodeCallback callback = null;
    private DecoderThread decoderThread;
    private final Handler.Callback resultCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == R.id.zxing_decode_succeeded) {
                BarcodeResult result = (BarcodeResult) message.obj;

                if (result != null) {
                    if (callback != null && decodeMode != DecodeMode.NONE) {
                        callback.barcodeResult(result);
                        if (decodeMode == DecodeMode.SINGLE) {
                            stopDecoding();
                        }
                    }
                }
                return true;
            } else if (message.what == R.id.zxing_decode_failed) {
                return true;
            } else if (message.what == R.id.zxing_possible_result_points) {
                List<ResultPoint> resultPoints = (List<ResultPoint>) message.obj;
                if (callback != null && decodeMode != DecodeMode.NONE) {
                    callback.possibleResultPoints(resultPoints);
                }
                return true;
            }
            return false;
        }
    };
    private DecoderFactory decoderFactory;


    private Handler resultHandler;

    public BarcodeView(Context context) {
        super(context);
        initialize(context, null);
    }


    public BarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public BarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        decoderFactory = new DefaultDecoderFactory();
        resultHandler = new Handler(resultCallback);
    }

    private Decoder createDecoder() {
        if (decoderFactory == null) {
            decoderFactory = createDefaultDecoderFactory();
        }
        DecoderResultPointCallback callback = new DecoderResultPointCallback();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, callback);
        Decoder decoder = this.decoderFactory.createDecoder(hints);
        callback.setDecoder(decoder);
        return decoder;
    }

    public DecoderFactory getDecoderFactory() {
        return decoderFactory;
    }

    public void setDecoderFactory(DecoderFactory decoderFactory) {
        Util.validateMainThread();

        this.decoderFactory = decoderFactory;
        if (this.decoderThread != null) {
            this.decoderThread.setDecoder(createDecoder());
        }
    }

    public void decodeSingle(BarcodeCallback callback) {
        this.decodeMode = DecodeMode.SINGLE;
        this.callback = callback;
        startDecoderThread();
    }

    public void decodeContinuous(BarcodeCallback callback) {
        this.decodeMode = DecodeMode.CONTINUOUS;
        this.callback = callback;
        startDecoderThread();
    }

    public void stopDecoding() {
        this.decodeMode = DecodeMode.NONE;
        this.callback = null;
        stopDecoderThread();
    }

    protected DecoderFactory createDefaultDecoderFactory() {
        return new DefaultDecoderFactory();
    }

    private void startDecoderThread() {
        stopDecoderThread();

        if (decodeMode != DecodeMode.NONE && isPreviewActive()) {
            decoderThread = new DecoderThread(getCameraInstance(), createDecoder(), resultHandler);
            decoderThread.setCropRect(getPreviewFramingRect());
            decoderThread.start();
        }
    }

    @Override
    protected void previewStarted() {
        super.previewStarted();

        startDecoderThread();
    }

    private void stopDecoderThread() {
        if (decoderThread != null) {
            decoderThread.stop();
            decoderThread = null;
        }
    }

    @Override
    public void pause() {
        stopDecoderThread();

        super.pause();
    }

    private enum DecodeMode {
        NONE,
        SINGLE,
        CONTINUOUS
    }
}
