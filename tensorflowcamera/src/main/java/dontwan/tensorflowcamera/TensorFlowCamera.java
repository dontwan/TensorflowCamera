package dontwan.tensorflowcamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dontwan.tensorflowcamera.callback.OnCameraStarted;
import dontwan.tensorflowcamera.callback.OnNoPermissions;
import dontwan.tensorflowcamera.callback.OnObjectDetected;
import dontwan.tensorflowcamera.exception.TensorFlowCameraException;
import dontwan.tensorflowcamera.tensorflow.Classifier;
import dontwan.tensorflowcamera.tensorflow.TensorFlowImageClassifier;

public class TensorFlowCamera extends TextureView implements TextureView.SurfaceTextureListener {
    private TextureView mTextureView;
    private Context mContext;

    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    public static String FOCUS_MODE_CONTINUOUS_PICTURE = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
    public static String FOCUS_MODE_AUTO = Camera.Parameters.FOCUS_MODE_AUTO;
    public static String FOCUS_MODE_INFINITY = Camera.Parameters.FOCUS_MODE_INFINITY;
    public static String FOCUS_MODE_MACRO = Camera.Parameters.FOCUS_MODE_MACRO;
    public static String FLASH_MODE_TORCH = Camera.Parameters.FLASH_MODE_TORCH;
    public static String FLASH_MODE_OFF = Camera.Parameters.FLASH_MODE_OFF;
    public static String FLASH_MODE_AUTO = Camera.Parameters.FLASH_MODE_AUTO;

    private TensorFlowImageClassifier mTensorFlowClassifier;
    private static String LABELS_FILE = "imagenet_comp_graph_label_strings.txt";
    private static String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static int MAX_BEST_RESULTS = 10;
    private static float RESULT_CONFIDENCE_THRESHOLD = 0.1f; // Minimal score for the list of recognition

    private OnCameraStarted onCameraStarted;
    private OnObjectDetected onObjectDetected;
    private OnNoPermissions onNoPermissions;

    private final Lock lock = new ReentrantLock();
    private int frameCounter;

    private boolean showWarningToasts = true;
    private boolean liveDetection = true;

    public TensorFlowCamera(Context context) {
        super(context);
        init(context);
    }

    public TensorFlowCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TensorFlowCamera(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TensorFlowCamera(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    // Set interfaces
    public void setOnCameraStarted(OnCameraStarted onCameraStarted) {
        this.onCameraStarted = onCameraStarted;
    }

    // Set interfaces
    public void setOnObjectDetected(OnObjectDetected onObjectDetected) {
        this.onObjectDetected = onObjectDetected;
    }

    // Set interfaces
    public void setOnNoPermissions(OnNoPermissions onNoPermissions) {
        this.onNoPermissions = onNoPermissions;
    }

    // Set camera orientation
    public void setCameraOrientation(int degrees) {
        if (mCamera != null) mCamera.setDisplayOrientation(degrees);
    }

    // Set camera focus
    public void setCameraFocus(String focus) {
        mCameraParameters.setFocusMode(focus);
        mCamera.setParameters(mCameraParameters);
    }

    // Set camera flash
    public void setCameraFlash(String flash) {
        mCameraParameters.setFlashMode(flash);
        mCamera.setParameters(mCameraParameters);
    }

    // Set warning toasts
    public void setWarningToasts(boolean showWarningToasts) {
        this.showWarningToasts = showWarningToasts;
    }

    public void setLabelsFile(String labelsFile) {
        LABELS_FILE = labelsFile;
    }

    public void setModelFile(String modelFile) {
        MODEL_FILE = modelFile;
    }

    public static int getMaxBestResults() {
        return MAX_BEST_RESULTS;
    }

    public void setMaxBestResults(int maxBestResults) {
        MAX_BEST_RESULTS = maxBestResults;
    }

    public static float getResultConfidenceThreshold() {
        return RESULT_CONFIDENCE_THRESHOLD;
    }

    public void setResultConfidenceThreshold(float resultConfidenceThreshold) {
        RESULT_CONFIDENCE_THRESHOLD = resultConfidenceThreshold;
    }

    public static String getLabelsFile() {
        return LABELS_FILE;
    }

    public static String getModelFile() {
        return MODEL_FILE;
    }

    public boolean isShowWarningToasts() {
        return showWarningToasts;
    }

    public boolean isLiveDetection() {
        return liveDetection;
    }

    public void setLiveDetection(boolean liveDetection) {
        this.liveDetection = liveDetection;
    }

    private void init(Context context) {
        this.mContext = context;
        this.mTextureView = this;
        this.mTextureView.setSurfaceTextureListener(this);
        this.mTensorFlowClassifier = new TensorFlowImageClassifier(getContext());

        try {
            if (checkPermission((Manifest.permission.CAMERA))) {
                mCamera = Camera.open();
                mCameraParameters = mCamera.getParameters();
                mCamera.setDisplayOrientation(90);
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (TensorFlowCameraException re) {
            if (onNoPermissions != null) {
                onNoPermissions.onNoPermissions();
            }
            showToast("No camera permissions");
            re.printStackTrace();
        }
    }

    private void showToast(String msg) {
        if (showWarningToasts) Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private boolean checkPermission(String permission) throws TensorFlowCameraException {
        int res = getContext().checkCallingOrSelfPermission(permission);
        if (res == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            throw new TensorFlowCameraException("No permissions: " + permission);
        }
    }

    public void start() {
        try {
            if (checkPermission((Manifest.permission.CAMERA))) {
                mCamera.setParameters(mCameraParameters);
                mCamera.startPreview();

                if (onCameraStarted != null) {
                    onCameraStarted.onStarted();
                }
            }
        } catch (TensorFlowCameraException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            mCamera = android.hardware.Camera.open();
        } catch (RuntimeException e) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            if (checkPermission((Manifest.permission.CAMERA))) {
                mCamera.setPreviewTexture(surface);
            }
        } catch (IOException | TensorFlowCameraException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, the Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stop();
        surface.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        try {
            if (liveDetection) {
                frameCounter++;
                if (frameCounter == mCamera.getParameters().getPreviewFrameRate() / 2) {
                    lock.lock();
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                Bitmap frame = mTextureView.getBitmap();
                                List<Classifier.Recognition> results = mTensorFlowClassifier.doRecognize(frame);
                                if (onObjectDetected != null) {
                                    onObjectDetected.onDetected(frame, results);
                                }
                            }
                        }).start();
                    } finally {
                        lock.unlock();
                        frameCounter = 0;
                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getImage() {
        if (mTextureView != null) {
            return mTextureView.getBitmap();
        }
        return null;
    }

    public List<Classifier.Recognition> getObjects(final Bitmap bitmap) {
        return mTensorFlowClassifier.doRecognize(bitmap);
    }

    public List<Classifier.Recognition> getObjects(int drawable) {
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), drawable);
        return mTensorFlowClassifier.doRecognize(bitmap);
    }
}

