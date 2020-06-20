package com.example.nativeapp;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
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
import org.opencv.core.Size;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

public class MainActivity extends AppUtilities implements CameraBridgeViewBase.CvCameraViewListener2, AsyncTaskResultListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    // map-related fields
    private Button secondActivityBtn, cameraViewActivityBtn;
    private TextView lat, lon;
    private ImageView img;
    private LocationManager locationManager;
    private LocationListener listener;

    MapView map;
    MapController mc;

    // camera-related fields
    CameraBridgeViewBase cameraView;
    BaseLoaderCallback baseLoaderCallback;
    Switch algorithmSwitch, thresholdSwitch;
    MSER featuresDetector;

    // currently displayed resource's id
    int imgDisplayed;

    // requests-related fields
    OSM_Node nearestNode;
    private int currentChangeset;

    // fixed list of circles on the screen
    CircleCounter c1 = new CircleCounter();
    CircleCounter c2 = new CircleCounter();
    CircleCounter c3 = new CircleCounter();
    CircleCounter c4 = new CircleCounter();
    CircleCounter[] onScreen = {c1, c2, c3, c4};
    List<Integer> stableCircles = new ArrayList<>();
    List<CircleCounter> circlesToUpdate = new ArrayList<>();

    // Activity context (useful for AsyncTasks)
    final Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //map configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().setOsmdroidBasePath(getStorage());
        Configuration.getInstance().setOsmdroidTileCache(getStorage());

        //inflate and create the map
        setContentView(R.layout.activity_main);

        //create map view
        map = (MapView) findViewById(R.id.mapView);
        map.getTileProvider().getTileCache().getProtectedTileComputers().clear();
        map.getTileProvider().getTileCache().setAutoEnsureCapacity(false);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        map.setMaxZoomLevel(20.);
        map.setMinZoomLevel(18.);

        //create overlay
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), map);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.setDrawAccuracyEnabled(false);
        map.getOverlays().add(mLocationOverlay);

        mc = (MapController) map.getController();
        mc.setZoom(20);

        //init text fields and switches
        lat = (TextView) findViewById(R.id.latTextView);
        lon = (TextView) findViewById(R.id.lonTextView);
        img = (ImageView) findViewById(R.id.imageView);

        algorithmSwitch = findViewById(R.id.switch1);
        thresholdSwitch = findViewById(R.id.switch2);

        //camera view
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

        //gps location configuration (manager and location listener)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat.setText(""  + location.getLatitude());
                lon.setText("" + location.getLongitude());
                GeoPoint currentCenter = new GeoPoint(location.getLatitude(), location.getLongitude());
                mc.animateTo(currentCenter);

                //download current bounding box
                BoundingBoxAsyncTask boundingBoxAsyncTask = new BoundingBoxAsyncTask(thisActivity, encodeAuth());
                boundingBoxAsyncTask.execute(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                //go to settings
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        checkForPermissions();
    }

    /***
     * Location permission methods for GPS features
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 10:
                checkForPermissions();
                break;
            default:
                break;
        }
    }

    void checkForPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.INTERNET,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}
                        ,10);
            }
            return;
        }

        // this code won't execute if permissions are not granted
        locationManager.requestLocationUpdates("gps", 0, 1, listener);
        locationManager.requestLocationUpdates("network", 0, 1, listener);
    }

    /***
     * Override AsyncTaskResultListener interface methods
     */
    @Override
    public void giveResult(int imgClass) {
        // imgClass is integer corresponding to R.drawable
        img.setImageResource(imgClass);
        imgDisplayed = imgClass;
    }

    @Override
    public void giveImgClass(int imgClass) {
        // create new node if no sign nearby
        if (imgDisplayed == R.drawable.koniec) {
            imgDisplayed = decodeAnsToDrawable(imgClass);
            Map<String, String> tags = decodeAnsToTags(imgClass);
            CreateNodeAsyncTask createNodeAsyncTask = new CreateNodeAsyncTask(encodeAuth(),
                                                Double.parseDouble(lat.getText().toString()),
                                                Double.parseDouble(lon.getText().toString()),
                                                tags, 100, thisActivity);
            createNodeAsyncTask.execute();
        } // edit node if it differs from class obtained from server
        else if (imgDisplayed != imgClass) {
            imgDisplayed = decodeAnsToDrawable(imgClass);
            Map<String, String> tags = decodeAnsToTags(imgClass);
            EditNodeAsyncTask editNodeAsyncTask = new EditNodeAsyncTask(encodeAuth(), nearestNode, tags,100, thisActivity);
            editNodeAsyncTask.execute();
        }
    }

    @Override
    public void giveNearestNode(OSM_Node node) {
        nearestNode = node;
    }

    /***
     * Override CameraView methods
     */
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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){

        Mat frame = inputFrame.rgba();

        // ----- Red Color Normalization

        List<Mat> channels = new ArrayList<>(3);
        Core.split(frame, channels);
        Mat red = channels.get(0);
        Mat green = channels.get(1);
        Mat blue = channels.get(2);
        Mat sum = new Mat();
        Mat result = new Mat();
        Mat colorFrame = frame.clone();

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

        if (!algorithmSwitch.isChecked()){
            if (thresholdSwitch.isChecked()){
                Imgproc.threshold(frame, frame, 210, 255, Imgproc.THRESH_BINARY);
            }

            Mat circles = new Mat();
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2GRAY);

            convexContours(frame.getNativeObjAddr());

//            houghCircles(frame.getNativeObjAddr(), colorFrame.getNativeObjAddr(), circlesOnScreen);

            Mat morphKernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(5,5));
            Imgproc.dilate(frame, frame, morphKernel);
            Imgproc.erode(frame, frame, morphKernel);

            Imgproc.HoughCircles(frame, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 200, 300, 90, 20, 60);

            stableCircles = new ArrayList<>();
            circlesToUpdate = new ArrayList<>();
            for (CircleCounter c : onScreen){
                int res = c.intersects(circles);
                if (res == 10 & c.r > 15){         // code 10 means blob is stable and can be identified
                    Rect blob = new Rect((int)(c.x - c.r), (int)(c.y - c.r), (int)(c.r*2), (int)(c.r*2));
                    Mat roi = new Mat(colorFrame, blob);

                    Imgproc.cvtColor(roi, roi, Imgproc.COLOR_RGBA2BGR);
                    Imgproc.resize(roi, roi, new Size(64,64));
                    ServerConnectAsyncTask asyncTask = null;
                    try {
                        asyncTask = new ServerConnectAsyncTask(roi, thisActivity);
                        asyncTask.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (res >= 0) {
                    stableCircles.add(res);
                } else {
                    circlesToUpdate.add(c);
                }
                Log.i("COUNTER INFO", String.valueOf(onScreen[0].cnt));
            }
            int i = 0;
            for (CircleCounter c : circlesToUpdate){
                while(stableCircles.contains(i)){
                    i++;
                }
                if (circles.cols() > i) {
                    c.updateCircle(circles.get(0, i)[0], circles.get(0, i)[1], circles.get(0, i)[2]);
                    i++;
                } else {
                    c.updateCircle(0d, 0d, 0d);             // when less than 4 circles found
                }
            }
            for (CircleCounter c : onScreen){
                Imgproc.circle(colorFrame, new Point((int) c.x, (int) c.y), (int) c.r, new Scalar(255, 0, 0), 2);
            }

//            if (circles.cols() > 0) {
//                for (int x = 0; x < Math.min(circles.cols(), 5); x++) {
//                    double[] circleVec = circles.get(0, x);
//
//                    if (circleVec == null) {
//                        break;
//                    }
//
//                    double oldX = circlesOnScreen[x][0];
//                    double oldY = circlesOnScreen[x][1];
//                    double oldRadius = circlesOnScreen[x][2];
//                    double newX = circleVec[0];
//                    double newY = circleVec[1];
//                    double newRadius = circleVec[2];
//
//                    if ((newX > oldX + 7 || newX < oldX - 7) &&
//                            (newY > oldY + 7 || newY < oldX - 7)){
//                        circlesOnScreen[x][0] = newX;
//                        circlesOnScreen[x][1] = newY;
//                        circlesOnScreen[x][2] = newRadius;
//                        // send extracted blob to identification
//                    }
//
////                    Point center = new Point((int) circleVec[0], (int) circleVec[1]);
////                    int radius = (int) circleVec[2];
//                    Point center = new Point((int) circlesOnScreen[x][0], (int) circlesOnScreen[x][1]);
//                    int radius = (int) circlesOnScreen[x][2];
//                    Imgproc.circle(frame, center, radius, new Scalar(255, 0, 0), 2);
//                }
//            }
//
            circles.release();
        }

        // --------- MSER

        else {
            List<int[]> roiData = new ArrayList<>();
            LinkedList<MatOfPoint> regions = new LinkedList<>();
            MatOfRect bboxes = new MatOfRect();
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2GRAY);

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
                    Imgproc.rectangle(colorFrame, p1, p2, new Scalar(255, 0, 0), 2);
                }
            }

            bboxes.release();
        }

        // ------- return

//        frame.release();
        return colorFrame;
    }

    /***
     * Override other App methods
     */
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
    public native void convexContours(long addr);
    public native void houghCircles(long matAddr, long colorAddr, double[][] oldCircles);
}
