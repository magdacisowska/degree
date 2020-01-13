#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <cmath>

using namespace std;
using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativeapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_nativeapp_MainActivity_convexContours(JNIEnv *env, jobject , jlong addr){
    Mat &src = *(Mat*)addr;
    medianBlur(src, src, 5);
    Mat cannyOutput;
    Canny(src, cannyOutput, 100, 200);

    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    findContours(cannyOutput, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE);
    Mat drawing = Mat::zeros(cannyOutput.size(), CV_8UC1);
    vector<vector<Point> >hull(contours.size());

    for(int i = 0; i < contours.size(); i++){
        convexHull(Mat(contours[i]), hull[i]);
    }

    for( size_t i = 0; i< contours.size(); i++ )
    {
        Scalar color = Scalar(255,0,255);
        drawContours( drawing, hull, (int)i, color );
    }

    src = drawing.clone();
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_nativeapp_MainActivity_houghCircles(JNIEnv *env, jobject , jlong matAddr, jlong colorAddr, jobjectArray circlesOnScreen){
    Mat &src = *(Mat*)matAddr;
    Mat &colorFrame = *(Mat*)colorAddr;
    vector<Vec3f> circles;

    HoughCircles(src, circles, HOUGH_GRADIENT, 2, 2000, 300, 90, 20, 60);
    for(size_t i = 0; i < circles.size(); i++){

        Vec3i c = circles[i];

        jdoubleArray oldCircle = (jdoubleArray) (env->GetObjectArrayElement(circlesOnScreen, i));
        jboolean isCopy;
        jdouble *elem = (env->GetDoubleArrayElements(oldCircle, &isCopy));

        double oldX = elem[0];
        double oldY = elem[1];
        double oldRadius = elem[2];
        double newX = c[0];
        double newY = c[1];
        double newRadius = c[2];

        if ((newX > oldX + 5.0 || newX < oldX - 5.0) &&
                (newY > oldY + 5.0 || newY < oldX - 5.0)){
            elem[0] = newX;
            elem[1] = newY;
            elem[2] = newRadius;
            // send extracted blob to identification
        }

//        Point center = Point(c[0], c[1]);
//        int radius = c[2];

        Point center = Point((int) elem[0], (int) elem[1]);
        int radius = (int) elem[2];
        circle(colorFrame, center, radius, Scalar(255,0,255), 3, LINE_AA);
    }
}
