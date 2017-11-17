Welcome to StackEdit!
===================


Hey! This is my first library for TensorFlow, I hope you like it.
This library is based on Camera, TextureView and Tensorflow.

I would like to thank [@shawngit](https://github.com/androidthings/sample-tensorflow-imageclassifier) for his code where this library is based on.

----------
Example images
-------------
<img src="https://i.imgur.com/HsTjkfY.jpg" width="25%"><img src="https://i.imgur.com/5oX3Kop.jpg" width="25%"><img src="https://i.imgur.com/P3Oe7Fc.jpg" width="25%">


Usage
-------------

Add the following code to your activity XML file:

    <dontwan.tensorflowcamera.TensorFlowCamera
        android:id="@+id/cameraView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

And the following code in the Activity class

    TensorFlowCamera tfCamera = (TensorFlowCamera) findViewById(R.id.cameraView);
	// Setup Tensorflow Camera
    tfCamera.setLabelsFile("imagenet_comp_graph_label_strings.txt"); // Label file in assets folder
    tfCamera.setModelFile("file:///android_asset/tensorflow_inception_graph.pb"); // Pb file in assets folder
    tfCamera.start(); // Start the camera

----------
Assets files
-------------
Download the following [file](https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip)
Extract the zip and place the two files in the: **app/main/assets**
This are the model and label file.

 - imagenet_comp_graph_label_strings.txt
 - tensorflow_inception_graph.pb

*If you gonna use other modal or label file please add the following code in the activity class:

    tfCamera.setLabelsFile("imagenet_comp_graph_label_strings.txt"); // Label file in assets folder
    tfCamera.setModelFile("file:///android_asset/tensorflow_inception_graph.pb"); // Pb file in assets folder

----------

Callbacks
----------
**OnCameraStarted**

    tfCamera.setOnCameraStarted(new OnCameraStarted() {
            @Override
            public void onStarted() {
               // Do something 
            }
        });

**OnNoPermissions**

    tfCamera.setOnNoPermissions(new OnNoPermissions() {
            @Override
            public void onNoPermissions() {
                // Do something like asking the camera permissions
            }
        });

**OnObjectDetected**

    tfCamera.setOnObjectDetected(new OnObjectDetected() {
            @Override
            public void onDetected(Bitmap frame, List<Classifier.Recognition> objects) {
                // Do something
            }
        });

----------

Method
---------- 

Method     | Description
-------- | ---
.setLabelFile | Set the label file of the models
.setModelFile    | Set the model file
.setLiveDectection     | Enable or disable live recognition. 
.getImage | Returns the current frame of the camera.
.getObjects    | Returns results from a bitmap or drawable int.
.setResultConfidenceThreshold     | Set a minimum value to match the object.
.setMaxBestResults | Set the max number of returning results.
.setCameraOrientation     | Set the camera vertical or horizontal.
.setWarningToasts     | Show warnings in a toast when they are available, perfect for debug.
.setCameraFlash     | Enable or disable the flash, **FLASH_MODE_TORCH**, **FLASH_MODE_AUTO**, **FLASH_MODE_OFF**
.setCameraFocus     | Set focus of the camera, **FOCUS_MODE_AUTO**, **FOCUS_MODE_INFINITY**, **FOCUS_MODE_MACRO**, **FOCUS_MODE_CONTINUOUS_PICTURE**
.start     | Start the camera.
.stop     | Stop the camera.
