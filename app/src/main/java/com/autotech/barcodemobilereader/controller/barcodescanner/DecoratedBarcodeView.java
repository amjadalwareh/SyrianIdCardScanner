package com.autotech.barcodemobilereader.controller.barcodescanner;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.autotech.barcodemobilereader.R;
import com.autotech.barcodemobilereader.controller.zxing.client.android.DecodeFormatManager;
import com.autotech.barcodemobilereader.controller.zxing.client.android.DecodeHintManager;
import com.autotech.barcodemobilereader.controller.zxing.client.android.Intents;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ResultPoint;
import com.autotech.barcodemobilereader.controller.barcodescanner.camera.CameraSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DecoratedBarcodeView extends FrameLayout {
    public BarcodeView barcodeView;
    private ViewfinderView viewFinder;
    private TextView statusView;

    private TorchListener torchListener;

    public DecoratedBarcodeView(Context context) {
        super(context);
        initialize();
    }

    public DecoratedBarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public DecoratedBarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_view);

        int scannerLayout = attributes.getResourceId(
                R.styleable.zxing_view_zxing_scanner_layout, R.layout.zxing_barcode_scanner);

        attributes.recycle();

        inflate(getContext(), scannerLayout, this);

        barcodeView = (BarcodeView) findViewById(R.id.zxing_barcode_surface);

        if (barcodeView == null) {
            throw new IllegalArgumentException(
                    "There is no a com.journeyapps.barcodescanner.BarcodeView on provided layout " +
                            "with the id \"zxing_barcode_surface\".");
        }

        barcodeView.initializeAttributes(attrs);

        viewFinder = (ViewfinderView) findViewById(R.id.zxing_viewfinder_view);

        if (viewFinder == null) {
            throw new IllegalArgumentException("ERROR");
        }

        viewFinder.setCameraPreview(barcodeView);

        statusView = (TextView) findViewById(R.id.zxing_status_view);
    }

    private void initialize() {
        initialize(null);
    }

    public void initializeFromIntent(Intent intent) {
        Set<BarcodeFormat> decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
        Map<DecodeHintType, Object> decodeHints = DecodeHintManager.parseDecodeHints(intent);

        CameraSettings settings = new CameraSettings();

        if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
            int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
            if (cameraId >= 0) {
                settings.setRequestedCameraId(cameraId);
            }
        }

        String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
        if (customPromptMessage != null) {
            setStatusText(customPromptMessage);
        }

        String characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

        MultiFormatReader reader = new MultiFormatReader();
        reader.setHints(decodeHints);

        barcodeView.setCameraSettings(settings);
        barcodeView.setDecoderFactory(new DefaultDecoderFactory(decodeFormats, decodeHints, characterSet));
    }

    public void setStatusText(String text) {
        if (statusView != null) {
            statusView.setText(text);
        }
    }

    public void pause() {
        barcodeView.pause();
    }

    public void resume() {
        barcodeView.resume();
    }

    public BarcodeView getBarcodeView() {
        return (BarcodeView) findViewById(R.id.zxing_barcode_surface);
    }

    public ViewfinderView getViewFinder() {
        return viewFinder;
    }

    public TextView getStatusView() {
        return statusView;
    }

    public void decodeSingle(BarcodeCallback callback) {
        barcodeView.decodeSingle(new WrappedCallback(callback));
    }

    public void decodeContinuous(BarcodeCallback callback) {
        barcodeView.decodeContinuous(new WrappedCallback(callback));
    }

    public void setTorchOn() {
        barcodeView.setTorch(true);

        if (torchListener != null) {
            torchListener.onTorchOn();
        }
    }

    public void setTorchOff() {
        barcodeView.setTorch(false);

        if (torchListener != null) {
            torchListener.onTorchOff();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                setTorchOff();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                setTorchOn();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setTorchListener(TorchListener listener) {
        this.torchListener = listener;
    }

    public interface TorchListener {

        void onTorchOn();

        void onTorchOff();
    }

    private class WrappedCallback implements BarcodeCallback {
        private BarcodeCallback delegate;

        public WrappedCallback(BarcodeCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        public void barcodeResult(BarcodeResult result) {
            delegate.barcodeResult(result);
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            for (ResultPoint point : resultPoints) {
                viewFinder.addPossibleResultPoint(point);
            }
            delegate.possibleResultPoints(resultPoints);
        }
    }
}
