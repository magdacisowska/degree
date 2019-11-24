#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

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
Java_com_example_nativeapp_MainActivity_convertToGray(JNIEnv *env, jobject , jlong addr){
    Mat &src = *(Mat*)addr;
    cvtColor(src, src, CV_RGBA2GRAY);
}
