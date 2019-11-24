package com.example.nativeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    CameraBridgeViewBase cameraView;
    BaseLoaderCallback baseLoaderCallback;
//    Mat frame;
    Switch algorithmSwitch, thresholdSwitch;
    MSER featuresDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        algorithmSwitch = findViewById(R.id.switch1);
        thresholdSwitch = findViewById(R.id.switch2);
        cameraView = (JavaCameraView) findViewById(R.id.CameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.enableFpsMeter();
        cameraView.setMaxFrameSize(800, 700);
        cameraView.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch(status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
//        frame = new Mat(width, height, CvType.CV_8UC4);
        featuresDetector = MSER.create(3, 2000, 5000);
    }

    @Override
    public void onCameraViewStopped() {
//        frame.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba();

        // ----- Red Color Normalization

        List<Mat> channels = new ArrayList<>(3);
        Core.split(frame, channels);
        Mat red = channels.get(0);
        Mat green = channels.get(1);
        Mat blue = channels.get(2);
        Mat sum = new Mat();
        Mat result = new Mat();

        Core.addWeighted(red, 0.333, red, 0, 0, red);
        Core.addWeighted(blue, 0.333, red, 0.333, 0, sum);
        Core.addWeighted(sum, 0.666, green, 0.333, 0, sum);
        Core.divide(red, sum, result, 1.0, CvType.CV_32F);
        Core.multiply(result, new Scalar(150), result);
        result.convertTo(result, CvType.CV_8U);
        channels.set(0, result);
        channels.set(1, result);
        channels.set(2, result);
        Core.merge(channels, frame);

        red.release(); green.release(); blue.release(); sum.release(); result.release();

        // ------ Hough Circles

        if (algorithmSwitch.isChecked()){
            if (thresholdSwitch.isChecked()){
                Imgproc.threshold(frame, frame, 120, 255, Imgproc.THRESH_BINARY);
            }

            Mat circles = new Mat();
            convertToGray(frame.getNativeObjAddr());

            Imgproc.HoughCircles(frame, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 90, 20, 100);

            if (circles.cols() > 0) {
                for (int x=0; x < Math.min(circles.cols(), 5); x++ ) {
                    double circleVec[] = circles.get(0, x);

                    if (circleVec == null) {
                        break;
                    }

                    Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                    int radius = (int) circleVec[2];
                    Imgproc.circle(frame, center, radius, new Scalar(255, 0, 0), 2);
                }
            }

            circles.release();
        }

        // --------- MSER

        else {
            List<int[]> roiData = new ArrayList<>();
            LinkedList<MatOfPoint> regions = new LinkedList<>();
            MatOfRect bboxes = new MatOfRect();
            convertToGray(frame.getNativeObjAddr());

            featuresDetector.detectRegions(frame, regions, bboxes);
            List<Rect> rects = bboxes.toList();
            Collections.sort(rects, new Comparator<Rect>() {
                @Override
                public int compare(Rect r1, Rect r2) {
                    return Integer.compare(r2.width, r1.width);
                }
            });
            for (Rect r : rects){
                Point p1 = new Point(r.x, r.y);
                Point p2 = new Point(r.x + r.width, r.y + r.height);
                int[] region = {r.x, r.y, r.width, r.height};
                boolean overlaps = false;
                boolean isSquare = CvHelpers.isSquare(r);

                for (int[] roiDatum : roiData){
                    overlaps = CvHelpers.overlaps(region, roiDatum);
                    if (overlaps) break;
                }
                if (!overlaps && isSquare) {
                    roiData.add(region);
                    Imgproc.rectangle(frame, p1, p2, new Scalar(255, 0, 0), 2);
                }
            }

            bboxes.release();
        }

        // ------- return

        return frame;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native void convertToGray(long addr);
}