package dontwan.example;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import dontwan.tensorflowcamera.TensorFlowCamera;
import dontwan.tensorflowcamera.callback.OnObjectDetected;
import dontwan.tensorflowcamera.tensorflow.Classifier;

public class MainActivity extends AppCompatActivity {

    private TextView tvResult;
    private Switch sLive;
    private SeekBar sbAccuracy;
    private Button btnTakePicture;
    private TensorFlowCamera tfCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inflating
        tvResult = (TextView) findViewById(R.id.tvResult);
        sLive = (Switch) findViewById(R.id.sLive);
        sbAccuracy = (SeekBar) findViewById(R.id.sbAccuracy);
        btnTakePicture = (Button) findViewById(R.id.btnTakePicture);
        tfCamera = (TensorFlowCamera) findViewById(R.id.cameraView);

        // Setup Tensorflow Camera
        tfCamera.setLabelsFile("imagenet_comp_graph_label_strings.txt"); // Label file in assets folder
        tfCamera.setModelFile("file:///android_asset/tensorflow_inception_graph.pb"); // Pb file in assets folder
        tfCamera.start(); // Start the camera
        tfCamera.setOnObjectDetected(new OnObjectDetected() {
            @Override
            public void onDetected(Bitmap frame, List<Classifier.Recognition> objects) {
                tvResult.setText(objects.toString());
            }
        });

        sLive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                tvResult.setText(null);
                if (b){
                    tfCamera.setLiveDetection(true);
                    btnTakePicture.setVisibility(View.GONE);
                }else{
                    tfCamera.setLiveDetection(false);
                    btnTakePicture.setVisibility(View.VISIBLE);
                }
            }
        });

        sbAccuracy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0){i = 1;}
                float newAccuracy = i / 100f;
                tfCamera.setResultConfidenceThreshold(newAccuracy);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = tfCamera.getImage();
                List<Classifier.Recognition> objects = tfCamera.getObjects(bitmap);
                tvResult.setText(objects.toString());
            }
        });
    }
}