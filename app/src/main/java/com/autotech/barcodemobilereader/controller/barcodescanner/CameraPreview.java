package com.autotech.barcodemobilereader.controller.barcodescanner;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.*;
import android.util.AttributeSet;
import android.view.*;
import com.autotech.barcodemobilereader.R;
import com.autotech.barcodemobilereader.controller.barcodescanner.camera.*;

import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends ViewGroup {
    private static final int ROTATION_LISTENER_DELAY_MS = 250;
    private CameraInstance cameraInstance;

    private WindowManager windowManager;

    private Handler stateHandler;

    private boolean useTextureView = false;

    private SurfaceView surfaceView;
    private TextureView textureView;

    private boolean previewActive = false;

    private RotationListener rotationListener;
    private int openedOrientation = -1;
    private List<StateListener> stateListeners = new ArrayList<>();
    private final StateListener fireState = new StateListener() {
        @Override
        public void previewSized() {
            for (StateListener listener : stateListeners) {
                listener.previewSized();
            }
        }

        @Override
        public void previewStarted() {
            for (StateListener listener : stateListeners) {
                listener.previewStarted();
            }

        }

        @Override
        public void previewStopped() {
            for (StateListener listener : stateListeners) {
                listener.previewStopped();
            }
        }

        @Override
        public void cameraError(Exception error) {
            for (StateListener listener : stateListeners) {
                listener.cameraError(error);
            }
        }
    };
    private DisplayConfiguration displayConfiguration;
    private CameraSettings cameraSettings = new CameraSettings();

    private Size containerSize;
    private Size previewSize;
    private Rect surfaceRect;
    private Size currentSurfaceSize;
    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            currentSurfaceSize = null;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder == null) {
                return;
            }
            currentSurfaceSize = new Size(width, height);
            startPreviewIfReady();
        }
    };
    private Rect framingRect = null;
    private Rect previewFramingRect = null;
    private Size framingRectSize = null;
    private double marginFraction = 0.1d;
    private final Handler.Callback stateCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == R.id.zxing_prewiew_size_ready) {
                previewSized((Size) message.obj);
                return true;
            } else if (message.what == R.id.zxing_camera_error) {
                Exception error = (Exception) message.obj;

                if (isActive()) {
                    pause();
                    fireState.cameraError(error);
                }
            }
            return false;
        }
    };
    private PreviewScalingStrategy previewScalingStrategy = null;
    private boolean torchOn = false;
    private RotationCallback rotationCallback = new RotationCallback() {
        @Override
        public void onRotationChanged(int rotation) {
            stateHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rotationChanged();
                }
            }, ROTATION_LISTENER_DELAY_MS);
        }
    };

    public CameraPreview(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(14)
    private TextureView.SurfaceTextureListener surfaceTextureListener() {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                onSurfaceTextureSizeChanged(surface, width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                currentSurfaceSize = new Size(width, height);
                startPreviewIfReady();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (getBackground() == null) {
            setBackgroundColor(Color.BLACK);
        }

        initializeAttributes(attrs);

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        stateHandler = new Handler(stateCallback);

        rotationListener = new RotationListener();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setupSurfaceView();
    }

    protected void initializeAttributes(AttributeSet attrs) {
        TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_camera_preview);

        int framingRectWidth = (int) styledAttributes.getDimension(R.styleable.zxing_camera_preview_zxing_framing_rect_width, -1);
        int framingRectHeight = (int) styledAttributes.getDimension(R.styleable.zxing_camera_preview_zxing_framing_rect_height, -1);

        if (framingRectWidth > 0 && framingRectHeight > 0) {
            this.framingRectSize = new Size(framingRectWidth, framingRectHeight);
        }

        this.useTextureView = styledAttributes.getBoolean(R.styleable.zxing_camera_preview_zxing_use_texture_view, true);

        int scalingStrategyNumber = styledAttributes.getInteger(R.styleable.zxing_camera_preview_zxing_preview_scaling_strategy, -1);
        if (scalingStrategyNumber == 1) {
            previewScalingStrategy = new CenterCropStrategy();
        } else if (scalingStrategyNumber == 2) {
            previewScalingStrategy = new FitCenterStrategy();
        } else if (scalingStrategyNumber == 3) {
            previewScalingStrategy = new FitXYStrategy();
        }

        styledAttributes.recycle();
    }

    private void rotationChanged() {
        if (isActive() && getDisplayRotation() != openedOrientation) {
            pause();
            resume();
        }
    }

    private void setupSurfaceView() {
        if (useTextureView && Build.VERSION.SDK_INT >= 14) {
            textureView = new TextureView(getContext());
            textureView.setSurfaceTextureListener(surfaceTextureListener());
            addView(textureView);
        } else {
            surfaceView = new SurfaceView(getContext());
            if (Build.VERSION.SDK_INT < 11) {
                surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
            surfaceView.getHolder().addCallback(surfaceCallback);
            addView(surfaceView);
        }
    }

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    private void calculateFrames() {
        if (containerSize == null || previewSize == null || displayConfiguration == null) {
            previewFramingRect = null;
            framingRect = null;
            surfaceRect = null;
            throw new IllegalStateException("containerSize or previewSize is not set yet");
        }

        int previewWidth = previewSize.width;
        int previewHeight = previewSize.height;

        int width = containerSize.width;
        int height = containerSize.height;

        surfaceRect = displayConfiguration.scalePreview(previewSize);

        Rect container = new Rect(0, 0, width, height);
        framingRect = calculateFramingRect(container, surfaceRect);
        Rect frameInPreview = new Rect(framingRect);
        frameInPreview.offset(-surfaceRect.left, -surfaceRect.top);

        previewFramingRect = new Rect(frameInPreview.left * previewWidth / surfaceRect.width(),
                frameInPreview.top * previewHeight / surfaceRect.height(),
                frameInPreview.right * previewWidth / surfaceRect.width(),
                frameInPreview.bottom * previewHeight / surfaceRect.height());

        if (previewFramingRect.width() <= 0 || previewFramingRect.height() <= 0) {
            previewFramingRect = null;
            framingRect = null;
        } else {
            fireState.previewSized();
        }
    }

    public void setTorch(boolean on) {
        torchOn = on;
        if (cameraInstance != null) {
            cameraInstance.setTorch(on);
        }
    }

    private void containerSized(Size containerSize) {
        this.containerSize = containerSize;
        if (cameraInstance != null) {
            if (cameraInstance.getDisplayConfiguration() == null) {
                displayConfiguration = new DisplayConfiguration(getDisplayRotation(), containerSize);
                displayConfiguration.setPreviewScalingStrategy(getPreviewScalingStrategy());
                cameraInstance.setDisplayConfiguration(displayConfiguration);
                cameraInstance.configureCamera();
                if (torchOn) {
                    cameraInstance.setTorch(torchOn);
                }
            }
        }
    }

    public PreviewScalingStrategy getPreviewScalingStrategy() {
        if (previewScalingStrategy != null) {
            return previewScalingStrategy;
        }

        if (textureView != null) {
            return new CenterCropStrategy();
        } else {
            return new FitCenterStrategy();
        }

    }

    public void setPreviewScalingStrategy(PreviewScalingStrategy previewScalingStrategy) {
        this.previewScalingStrategy = previewScalingStrategy;
    }

    private void previewSized(Size size) {
        this.previewSize = size;
        if (containerSize != null) {
            calculateFrames();
            requestLayout();
            startPreviewIfReady();
        }
    }

    protected Matrix calculateTextureTransform(Size textureSize, Size previewSize) {
        float ratioTexture = (float) textureSize.width / (float) textureSize.height;
        float ratioPreview = (float) previewSize.width / (float) previewSize.height;

        float scaleX;
        float scaleY;

        if (ratioTexture < ratioPreview) {
            scaleX = ratioPreview / ratioTexture;
            scaleY = 1;
        } else {
            scaleX = 1;
            scaleY = ratioTexture / ratioPreview;
        }

        Matrix matrix = new Matrix();

        matrix.setScale(scaleX, scaleY);

        float scaledWidth = textureSize.width * scaleX;
        float scaledHeight = textureSize.height * scaleY;
        float dx = (textureSize.width - scaledWidth) / 2;
        float dy = (textureSize.height - scaledHeight) / 2;

        matrix.postTranslate(dx, dy);

        return matrix;
    }

    private void startPreviewIfReady() {
        if (currentSurfaceSize != null && previewSize != null && surfaceRect != null) {
            if (surfaceView != null && currentSurfaceSize.equals(new Size(surfaceRect.width(), surfaceRect.height()))) {
                startCameraPreview(new CameraSurface(surfaceView.getHolder()));
            } else if (textureView != null && Build.VERSION.SDK_INT >= 14 && textureView.getSurfaceTexture() != null) {
                if (previewSize != null) {
                    Matrix transform = calculateTextureTransform(new Size(textureView.getWidth(), textureView.getHeight()), previewSize);
                    textureView.setTransform(transform);
                }

                startCameraPreview(new CameraSurface(textureView.getSurfaceTexture()));
            }
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        containerSized(new Size(r - l, b - t));

        if (surfaceView != null) {
            if (surfaceRect == null) {
                surfaceView.layout(0, 0, getWidth(), getHeight());
            } else {
                surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            }
        } else if (textureView != null && Build.VERSION.SDK_INT >= 14) {
            textureView.layout(0, 0, getWidth(), getHeight());
        }
    }

    public Rect getFramingRect() {
        return framingRect;
    }

    public Rect getPreviewFramingRect() {
        return previewFramingRect;
    }

    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public void setCameraSettings(CameraSettings cameraSettings) {
        this.cameraSettings = cameraSettings;
    }

    public void resume() {
        Util.validateMainThread();

        initCamera();

        if (currentSurfaceSize != null) {
            startPreviewIfReady();
        } else if (surfaceView != null) {

            surfaceView.getHolder().addCallback(surfaceCallback);
        } else if (textureView != null && Build.VERSION.SDK_INT >= 14) {
            textureView.setSurfaceTextureListener(surfaceTextureListener());
        }
        requestLayout();
        rotationListener.listen(getContext(), rotationCallback);
    }

    public void pause() {
        Util.validateMainThread();

        openedOrientation = -1;
        if (cameraInstance != null) {
            cameraInstance.close();
            cameraInstance = null;
            previewActive = false;
        }
        if (currentSurfaceSize == null && surfaceView != null) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(surfaceCallback);
        }
        if (currentSurfaceSize == null && textureView != null && Build.VERSION.SDK_INT >= 14) {
            textureView.setSurfaceTextureListener(null);
        }

        this.containerSize = null;
        this.previewSize = null;
        this.previewFramingRect = null;
        rotationListener.stop();

        fireState.previewStopped();
    }

    public Size getFramingRectSize() {
        return framingRectSize;
    }

    public void setFramingRectSize(Size framingRectSize) {
        this.framingRectSize = framingRectSize;
    }

    public double getMarginFraction() {
        return marginFraction;
    }

    public void setMarginFraction(double marginFraction) {
        if (marginFraction >= 0.5d) {
            throw new IllegalArgumentException("The margin fraction must be less than 0.5");
        }
        this.marginFraction = marginFraction;
    }

    public boolean isUseTextureView() {
        return useTextureView;
    }

    public void setUseTextureView(boolean useTextureView) {
        this.useTextureView = useTextureView;
    }

    protected boolean isActive() {
        return cameraInstance != null;
    }

    private int getDisplayRotation() {
        return windowManager.getDefaultDisplay().getRotation();
    }

    private void initCamera() {
        if (cameraInstance != null) {
            return;
        }

        cameraInstance = createCameraInstance();
        cameraInstance.setReadyHandler(stateHandler);
        cameraInstance.open();
        openedOrientation = getDisplayRotation();
    }

    protected CameraInstance createCameraInstance() {
        CameraInstance cameraInstance = new CameraInstance(getContext());
        cameraInstance.setCameraSettings(cameraSettings);
        return cameraInstance;
    }

    private void startCameraPreview(CameraSurface surface) {
        if (!previewActive && cameraInstance != null) {
            cameraInstance.setSurface(surface);
            cameraInstance.startPreview();
            previewActive = true;

            previewStarted();
            fireState.previewStarted();
        }
    }

    protected void previewStarted() {

    }

    public CameraInstance getCameraInstance() {
        return cameraInstance;
    }

    public boolean isPreviewActive() {
        return previewActive;
    }

    protected Rect calculateFramingRect(Rect container, Rect surface) {
        Rect intersection = new Rect(container);
        boolean intersects = intersection.intersect(surface);

        if (framingRectSize != null) {
            int horizontalMargin = Math.max(0, (intersection.width() - framingRectSize.width) / 2);
            int verticalMargin = Math.max(0, (intersection.height() - framingRectSize.height) / 2);
            intersection.inset(horizontalMargin, verticalMargin);
            return intersection;
        }
        int margin = (int) Math.min(intersection.width() * marginFraction, intersection.height() * marginFraction);
        intersection.inset(margin, margin);
        if (intersection.height() > intersection.width()) {
            intersection.inset(0, (intersection.height() - intersection.width()) / 2);
        }
        return intersection;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle myState = new Bundle();
        myState.putParcelable("super", superState);
        myState.putBoolean("torch", torchOn);
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof Bundle)) {
            super.onRestoreInstanceState(state);
            return;
        }
        Bundle myState = (Bundle) state;
        Parcelable superState = myState.getParcelable("super");
        super.onRestoreInstanceState(superState);
        boolean torch = myState.getBoolean("torch");
        setTorch(torch);
    }

    public interface StateListener {
        void previewSized();

        void previewStarted();

        void previewStopped();

        void cameraError(Exception error);
    }
}
