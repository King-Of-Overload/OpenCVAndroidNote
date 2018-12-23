//
// Created by Alan Croft on 2018/12/21.
//
#include <jni.h>
#include <opencv2/opencv.hpp>
#include <iostream>
#include <vector>
#include <android/log.h>
#include "zjut_alan_opencvdemo_c7_DisplayModeActivity.h"

#define LOG_TAG "ZJUT_HAAR_DETECTION"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace std;

extern "C"{
 CascadeClassifier face_detector;

JNIEXPORT void JNICALL Java_zjut_alan_opencvdemo_c7_DisplayModeActivity_faceDetection
        (JNIEnv *env, jobject jclazz, jlong addrRgba){
    int flag = 1000;
    Mat &mRgb = *(Mat*)addrRgba;
    Mat gray;
    cvtColor(mRgb, gray, COLOR_BGR2GRAY);
    vector<Rect> faces;
    face_detector.detectMultiScale(gray, faces, 1.1,1,0,Size(50,50),Size(300,300));
    if(faces.empty()){
        return;
    }
    for(int i = 0; i < faces.size(); i++){
        rectangle(mRgb,faces[i],Scalar(255,0,0),2,8,0);
        LOGD("Face Detection: %s", "found face");
    }
}

JNIEXPORT void JNICALL Java_zjut_alan_opencvdemo_c7_DisplayModeActivity_initLoad
        (JNIEnv *env, jobject jclazz, jstring haarfilePath){
    const char *nativeString = (*env).GetStringUTFChars(haarfilePath, 0);
    face_detector.load(nativeString);
    (*env).ReleaseStringUTFChars(haarfilePath, nativeString);
    LOGD("Method Description: %s", "loaded haar files...");
}



}
