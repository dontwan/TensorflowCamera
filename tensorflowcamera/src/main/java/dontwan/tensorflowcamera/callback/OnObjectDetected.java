package dontwan.tensorflowcamera.callback;

import android.graphics.Bitmap;

import java.util.List;

import dontwan.tensorflowcamera.tensorflow.Classifier;

public interface OnObjectDetected {
    public void onDetected(Bitmap frame, List<Classifier.Recognition> objects);
}
