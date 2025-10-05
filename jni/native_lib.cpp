#include <jni.h>
#include <string>
#include <android/log.h>

// Include OpenCV headers (uncomment when OpenCV is configured)
// #include <opencv2/opencv.hpp>
// #include <opencv2/imgproc.hpp>

#define TAG "FlameRnDNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_flam_rnd_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ NDK!";
    LOGI("Native function called successfully");
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jint JNICALL
Java_com_flam_rnd_MainActivity_getOpenCVVersion(
        JNIEnv* env,
        jobject /* this */) {
    
    // Example OpenCV version check (uncomment when OpenCV is configured)
    // int version = CV_VERSION_MAJOR * 10000 + CV_VERSION_MINOR * 100 + CV_VERSION_REVISION;
    // LOGI("OpenCV version: %d", version);
    // return version;
    
    // Placeholder return value
    LOGI("OpenCV not yet configured");
    return 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flam_rnd_MainActivity_processImage(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr) {
    
    LOGI("processImage called");
    
    // Example OpenCV image processing (uncomment when OpenCV is configured)
    /*
    try {
        cv::Mat& mat = *(cv::Mat*) matAddr;
        
        if (mat.empty()) {
            LOGE("Input image is empty");
            return false;
        }
        
        // Example: Convert to grayscale
        cv::Mat gray;
        cv::cvtColor(mat, gray, cv::COLOR_BGR2GRAY);
        
        // Example: Apply Gaussian blur
        cv::Mat blurred;
        cv::GaussianBlur(gray, blurred, cv::Size(15, 15), 0);
        
        // Copy result back to original mat
        cv::cvtColor(blurred, mat, cv::COLOR_GRAY2BGR);
        
        LOGI("Image processed successfully");
        return true;
        
    } catch (const cv::Exception& e) {
        LOGE("OpenCV exception: %s", e.what());
        return false;
    }
    */
    
    // Placeholder implementation
    LOGI("Image processing placeholder - OpenCV not yet configured");
    return true;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_flam_rnd_MainActivity_getNDKInfo(
        JNIEnv* env,
        jobject /* this */) {
    
    std::string info = "NDK Info:\n";
    info += "- Architecture: " + std::string(ANDROID_CPU_FAMILY_ARM ? "ARM" : "Unknown") + "\n";
    info += "- API Level: " + std::to_string(__ANDROID_API__) + "\n";
    info += "- C++ Standard: " + std::to_string(__cplusplus) + "\n";
    
    LOGI("NDK Info requested: %s", info.c_str());
    return env->NewStringUTF(info.c_str());
}