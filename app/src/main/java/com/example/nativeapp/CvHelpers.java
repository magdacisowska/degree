package com.example.nativeapp;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CvHelpers {

    public static Mat equalizeIntensity(Mat baseImg){
        Mat img = new Mat();

        Imgproc.cvtColor(baseImg, img, Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2YCrCb);

        List<Mat> channels = new ArrayList<>();
        Mat element =  new Mat();
        Core.split(img, channels);
        Imgproc.equalizeHist(channels.get(0), element);
        channels.set(0, element);
        Core.merge(channels, img);

        Imgproc.cvtColor(img, img, Imgproc.COLOR_YCrCb2RGB);

        return img;
    }

    /// Check whether two rectangles over-lapse (rects are in form [x, y, width, height]).
    /// Each of 4 overlap conditions must be satisfied.
    public static boolean overlaps(int[] rect1, int[] rect2){
        boolean condX1 = rect2[0] < rect1[0] + rect1[2];        // x2 < x1 + width1
        boolean condX2 = rect1[0] < rect2[0] + rect2[2];        // x1 < x2 + width2
        boolean condY1 = rect2[1] < rect1[1] + rect1[3];        // y2 < y1 + height1
        boolean condY2 = rect1[1] < rect2[1] + rect2[3];        // y1 < y2 + height2
        return condX1 && condX2 && condY1 && condY2;
    }

    /// Check if ROI is square (more or less at least)
    public static boolean isSquare(Rect r){
        return Math.abs(r.height - r.width) < 10;
    }
}
