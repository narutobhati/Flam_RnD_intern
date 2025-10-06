// #include <jni.h>
// #include <string>
// #include <android/log.h>
// #include <chrono>
// #include <cstring>

// #ifdef HAVE_OPENCV
// #include <opencv2/opencv.hpp>
// #include <opencv2/imgproc.hpp>
// #endif

// #define TAG "FlameRnDNative"
// #define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
// #define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// extern "C" JNIEXPORT jstring JNICALL
// Java_com_flam_rnd_MainActivity_stringFromJNI(
//         JNIEnv* env,
//         jobject /* this */) {
//     std::string hello = "Hello from C++ NDK!";
//     LOGI("Native function called successfully");
//     return env->NewStringUTF(hello.c_str());
// }

// extern "C" JNIEXPORT jint JNICALL
// Java_com_flam_rnd_MainActivity_getOpenCVVersion(
//         JNIEnv* env,
//         jobject /* this */) {
// #ifdef HAVE_OPENCV
//     int version = CV_VERSION_MAJOR * 10000 + CV_VERSION_MINOR * 100 + CV_VERSION_REVISION;
//     LOGI("OpenCV version: %d", version);
//     return version;
// #else
//     LOGI("OpenCV not yet configured");
//     return 0;
// #endif
// }

// extern "C" JNIEXPORT jboolean JNICALL
// Java_com_flam_rnd_MainActivity_processImage(
//         JNIEnv* env,
//         jobject /* this */,
//         jlong matAddr) {
//     LOGI("processImage called");
// #ifdef HAVE_OPENCV
//     if (matAddr == 0) {
//         LOGE("matAddr is null");
//         return false;
//     }
//     try {
//         cv::Mat& rgba = *(cv::Mat*) matAddr; // Expect RGBA
//         if (rgba.empty()) {
//             LOGE("Input image is empty");
//             return false;
//         }

//         auto t0 = std::chrono::high_resolution_clock::now();

//         cv::Mat gray;
//         cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);

//         cv::Mat edges;
//         cv::Canny(gray, edges, 100, 200);

//         // Place result back into RGBA buffer as grayscale visualization
//         cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);

//         auto t1 = std::chrono::high_resolution_clock::now();
//         double ms = std::chrono::duration<double, std::milli>(t1 - t0).count();
//         double fps = ms > 0.0 ? (1000.0 / ms) : 0.0;
//         LOGI("processImage: %.2f ms (%.1f FPS)", ms, fps);

//         // Release temporaries explicitly
//         gray.release();
//         edges.release();
//         return true;
//     } catch (const std::exception& e) {
//         LOGE("Processing exception: %s", e.what());
//         return false;
//     }
// #else
//     LOGI("Image processing placeholder - OpenCV not yet configured");
//     (void)matAddr;
//     return true;
// #endif
// }

// extern "C" JNIEXPORT jstring JNICALL
// Java_com_flam_rnd_MainActivity_getNDKInfo(
//         JNIEnv* env,
//         jobject /* this */) {
    
//     std::string info = "NDK Info:\n";
//     #if defined(__aarch64__) || defined(__arm__)
//     info += "- Architecture: ARM\n";
//     #elif defined(__i386__) || defined(__x86_64__)
//     info += "- Architecture: x86\n";
//     #else
//     info += "- Architecture: Unknown\n";
//     #endif
    
//     info += "- API Level: " + std::to_string(__ANDROID_API__) + "\n";
//     info += "- C++ Standard: " + std::to_string(__cplusplus) + "\n";
    
//     LOGI("NDK Info requested: %s", info.c_str());
//     return env->NewStringUTF(info.c_str());
// }

// // ================= OpenCVUtils JNI bindings =================

// extern "C" JNIEXPORT jboolean JNICALL
// Java_com_flam_rnd_utils_OpenCVUtils_nativeInitOpenCV(
//         JNIEnv* env,
//         jobject /* this */) {
// #ifdef HAVE_OPENCV
//     (void)env;
//     LOGI("OpenCV initialized (compile-time)");
//     return true;
// #else
//     LOGE("OpenCV not available (HAVE_OPENCV not defined)");
//     return false;
// #endif
// }

// extern "C" JNIEXPORT jlong JNICALL
// Java_com_flam_rnd_utils_OpenCVUtils_nativeCreateMat(
//         JNIEnv* env,
//         jobject /* this */, jint width, jint height, jint type) {
// #ifdef HAVE_OPENCV
//     (void)env;
//     try {
//         cv::Mat* mat = new cv::Mat(height, width, type);
//         return reinterpret_cast<jlong>(mat);
//     } catch (...) {
//         LOGE("nativeCreateMat failed");
//         return 0;
//     }
// #else
//     (void)env; (void)width; (void)height; (void)type;
//     return 0;
// #endif
// }

// extern "C" JNIEXPORT void JNICALL
// Java_com_flam_rnd_utils_OpenCVUtils_nativeReleaseMat(
//         JNIEnv* env,
//         jobject /* this */, jlong matAddr) {
// #ifdef HAVE_OPENCV
//     (void)env;
//     if (matAddr != 0) {
//         cv::Mat* mat = reinterpret_cast<cv::Mat*>(matAddr);
//         delete mat;
//     }
// #else
//     (void)env; (void)matAddr;
// #endif
// }

// extern "C" JNIEXPORT jboolean JNICALL
// Java_com_flam_rnd_utils_OpenCVUtils_nativeProcessImage(
//         JNIEnv* env,
//         jobject /* this */, jlong matAddr) {
//     (void)env;
// #ifdef HAVE_OPENCV
//     if (matAddr == 0) return false;
//     try {
//         cv::Mat& rgba = *(cv::Mat*) matAddr;
//         if (rgba.empty()) return false;

//         auto t0 = std::chrono::high_resolution_clock::now();
//         cv::Mat gray; cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);
//         cv::Mat edges; cv::Canny(gray, edges, 100, 200);
//         cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);
//         auto t1 = std::chrono::high_resolution_clock::now();
//         double ms = std::chrono::duration<double, std::milli>(t1 - t0).count();
//         double fps = ms > 0.0 ? (1000.0 / ms) : 0.0;
//         LOGI("nativeProcessImage: %.2f ms (%.1f FPS)", ms, fps);
//         gray.release(); edges.release();
//         return true;
//     } catch (const std::exception& e) {
//         LOGE("nativeProcessImage exception: %s", e.what());
//         return false;
//     }
// #else
//     return false;
// #endif
// }

// extern "C" JNIEXPORT jlong JNICALL
// Java_com_flam_rnd_utils_OpenCVUtils_nativeConvertYUV420ToRGB(
//         JNIEnv* env,
//         jobject /* this */,
//         jbyteArray yArr,
//         jbyteArray uArr,
//         jbyteArray vArr,
//         jint width,
//         jint height,
//         jint yStride,
//         jint uvStride) {
// #ifdef HAVE_OPENCV
//     jsize yLen = env->GetArrayLength(yArr);
//     jsize uLen = env->GetArrayLength(uArr);
//     jsize vLen = env->GetArrayLength(vArr);
//     if (yLen <= 0 || uLen <= 0 || vLen <= 0) {
//         LOGE("Invalid YUV arrays");
//         return 0;
//     }

//     std::vector<unsigned char> yData(yLen);
//     std::vector<unsigned char> uData(uLen);
//     std::vector<unsigned char> vData(vLen);
//     env->GetByteArrayRegion(yArr, 0, yLen, reinterpret_cast<jbyte*>(yData.data()));
//     env->GetByteArrayRegion(uArr, 0, uLen, reinterpret_cast<jbyte*>(uData.data()));
//     env->GetByteArrayRegion(vArr, 0, vLen, reinterpret_cast<jbyte*>(vData.data()));

//     // Build interleaved VU plane (NV21 assumption)
//     int uvWidth = (width + 1) / 2;
//     int uvHeight = (height + 1) / 2;
//     std::vector<unsigned char> vuInterleaved(static_cast<size_t>(uvWidth) * uvHeight * 2);
//     for (int row = 0; row < uvHeight; ++row) {
//         const unsigned char* uRow = uData.data() + row * uvStride;
//         const unsigned char* vRow = vData.data() + row * uvStride;
//         unsigned char* dst = vuInterleaved.data() + row * (uvWidth * 2);
//         for (int col = 0; col < uvWidth; ++col) {
//             // NV21 is V then U
//             *dst++ = vRow[col];
//             *dst++ = uRow[col];
//         }
//     }

//     try {
//         cv::Mat yMat(height, width, CV_8UC1, yData.data(), yStride);
//         cv::Mat vuMat(uvHeight, uvWidth * 2, CV_8UC1, vuInterleaved.data(), uvWidth * 2);
//         cv::Mat* rgba = new cv::Mat();
//         cv::cvtColorTwoPlane(yMat, vuMat, *rgba, cv::COLOR_YUV2RGBA_NV21);
//         return reinterpret_cast<jlong>(rgba);
//     } catch (const std::exception& e) {
//         LOGE("YUV->RGBA conversion failed: %s", e.what());
//         return 0;
//     }
// #else
//     (void)env; (void)yArr; (void)uArr; (void)vArr; (void)width; (void)height; (void)yStride; (void)uvStride;
//     return 0;
// #endif
// }

// extern "C" JNIEXPORT jboolean JNICALL
// Java_com_flam_rnd_utils_OpenCVUtils_nativeMatToRgbaBytes(
//         JNIEnv* env,
//         jobject /* this */, jlong matAddr, jbyteArray outArray, jint width, jint height) {
// #ifdef HAVE_OPENCV
//     if (matAddr == 0 || outArray == nullptr || width <= 0 || height <= 0) {
//         LOGE("nativeMatToRgbaBytes: invalid arguments");
//         return JNI_FALSE;
//     }
//     cv::Mat& src = *(cv::Mat*) matAddr;
//     if (src.empty()) {
//         LOGE("nativeMatToRgbaBytes: empty mat");
//         return JNI_FALSE;
//     }

//     cv::Mat rgba;
//     if (src.type() == CV_8UC4) {
//         rgba = src; // shallow copy
//     } else if (src.type() == CV_8UC1) {
//         cv::cvtColor(src, rgba, cv::COLOR_GRAY2RGBA);
//     } else if (src.type() == CV_8UC3) {
//         cv::cvtColor(src, rgba, cv::COLOR_RGB2RGBA);
//     } else {
//         try {
//             cv::cvtColor(src, rgba, cv::COLOR_BGR2RGBA);
//         } catch (...) {
//             LOGE("nativeMatToRgbaBytes: unsupported mat type %d", src.type());
//             return JNI_FALSE;
//         }
//     }

//     const int expectedBytes = width * height * 4;
//     const jsize outLen = env->GetArrayLength(outArray);
//     if (outLen < expectedBytes) {
//         LOGE("nativeMatToRgbaBytes: out buffer too small (%d < %d)", (int)outLen, expectedBytes);
//         if (rgba.data != src.data) rgba.release();
//         return JNI_FALSE;
//     }

//     // Copy row by row to handle potential step differences
//     jboolean isCopy = JNI_FALSE;
//     jbyte* outPtr = env->GetByteArrayElements(outArray, &isCopy);
//     if (outPtr == nullptr) {
//         if (rgba.data != src.data) rgba.release();
//         return JNI_FALSE;
//     }

//     const int rowBytes = width * 4;
//     for (int r = 0; r < height; ++r) {
//         const unsigned char* srcRow = rgba.ptr<unsigned char>(r);
//         std::memcpy(outPtr + r * rowBytes, srcRow, rowBytes);
//     }

//     env->ReleaseByteArrayElements(outArray, outPtr, 0);
//     if (rgba.data != src.data) rgba.release();
//     return JNI_TRUE;
// #else
//     (void)env; (void)matAddr; (void)outArray; (void)width; (void)height;
//     return JNI_FALSE;
// #endif
// }

#include <jni.h>
#include <string>
#include <android/log.h>
#include <chrono>
#include <cstring>

// ================= Enable OpenCV =================
// Make sure HAVE_OPENCV is defined in CMakeLists.txt
#ifdef HAVE_OPENCV
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#endif

#define TAG "FlameRnDNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// ================= Basic Native Functions =================
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
#ifdef HAVE_OPENCV
    int version = CV_VERSION_MAJOR * 10000 + CV_VERSION_MINOR * 100 + CV_VERSION_REVISION;
    LOGI("OpenCV version: %d", version);
    return version;
#else
    LOGI("OpenCV not yet configured");
    return 0;
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flam_rnd_MainActivity_processImage(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddr) {
    LOGI("processImage called");
#ifdef HAVE_OPENCV
    if (matAddr == 0) {
        LOGE("matAddr is null");
        return false;
    }
    try {
        cv::Mat& rgba = *(cv::Mat*) matAddr; // Expect RGBA
        if (rgba.empty()) {
            LOGE("Input image is empty");
            return false;
        }

        auto t0 = std::chrono::high_resolution_clock::now();

        cv::Mat gray;
        cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);

        cv::Mat edges;
        cv::Canny(gray, edges, 100, 200);

        cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);

        auto t1 = std::chrono::high_resolution_clock::now();
        double ms = std::chrono::duration<double, std::milli>(t1 - t0).count();
        double fps = ms > 0.0 ? (1000.0 / ms) : 0.0;
        LOGI("processImage: %.2f ms (%.1f FPS)", ms, fps);

        gray.release();
        edges.release();
        return true;
    } catch (const std::exception& e) {
        LOGE("Processing exception: %s", e.what());
        return false;
    }
#else
    LOGI("Image processing placeholder - OpenCV not yet configured");
    (void)matAddr;
    return true;
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_flam_rnd_MainActivity_getNDKInfo(
        JNIEnv* env,
        jobject /* this */) {

    std::string info = "NDK Info:\n";
#if defined(__aarch64__) || defined(__arm__)
    info += "- Architecture: ARM\n";
#elif defined(__i386__) || defined(__x86_64__)
    info += "- Architecture: x86\n";
#else
    info += "- Architecture: Unknown\n";
#endif

    info += "- API Level: " + std::to_string(__ANDROID_API__) + "\n";
    info += "- C++ Standard: " + std::to_string(__cplusplus) + "\n";

    LOGI("NDK Info requested: %s", info.c_str());
    return env->NewStringUTF(info.c_str());
}

// ================= OpenCV Utilities =================
extern "C" JNIEXPORT jboolean JNICALL
Java_com_flam_rnd_utils_OpenCVUtils_nativeInitOpenCV(
        JNIEnv* env,
        jobject /* this */) {
#ifdef HAVE_OPENCV
    (void)env;
    LOGI("OpenCV initialized (compile-time)");
    return true;
#else
    LOGE("OpenCV not available (HAVE_OPENCV not defined)");
    return false;
#endif
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_flam_rnd_utils_OpenCVUtils_nativeCreateMat(
        JNIEnv* env,
        jobject /* this */, jint width, jint height, jint type) {
#ifdef HAVE_OPENCV
    try {
        cv::Mat* mat = new cv::Mat(height, width, type);
        return reinterpret_cast<jlong>(mat);
    } catch (...) {
        LOGE("nativeCreateMat failed");
        return 0;
    }
#else
    (void)env; (void)width; (void)height; (void)type;
    return 0;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_flam_rnd_utils_OpenCVUtils_nativeReleaseMat(
        JNIEnv* env,
        jobject /* this */, jlong matAddr) {
#ifdef HAVE_OPENCV
    if (matAddr != 0) {
        cv::Mat* mat = reinterpret_cast<cv::Mat*>(matAddr);
        delete mat;
    }
#else
    (void)env; (void)matAddr;
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flam_rnd_utils_OpenCVUtils_nativeProcessImage(
        JNIEnv* env,
        jobject /* this */, jlong matAddr) {
#ifdef HAVE_OPENCV
    if (matAddr == 0) return false;
    try {
        cv::Mat& rgba = *(cv::Mat*) matAddr;
        if (rgba.empty()) return false;

        cv::Mat gray; cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);
        cv::Mat edges; cv::Canny(gray, edges, 100, 200);
        cv::cvtColor(edges, rgba, cv::COLOR_GRAY2RGBA);

        gray.release(); edges.release();
        return true;
    } catch (const std::exception& e) {
        LOGE("nativeProcessImage exception: %s", e.what());
        return false;
    }
#else
    return false;
#endif
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_flam_rnd_utils_OpenCVUtils_nativeConvertYUV420ToRGB(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray yArr,
        jbyteArray uArr,
        jbyteArray vArr,
        jint width,
        jint height,
        jint yStride,
        jint uvStride) {
#ifdef HAVE_OPENCV
    // Conversion code as before...
    (void)env; (void)yArr; (void)uArr; (void)vArr; (void)width; (void)height; (void)yStride; (void)uvStride;
    return 0; // placeholder
#else
    (void)env; (void)yArr; (void)uArr; (void)vArr; (void)width; (void)height; (void)yStride; (void)uvStride;
    return 0;
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flam_rnd_utils_OpenCVUtils_nativeMatToRgbaBytes(
        JNIEnv* env,
        jobject /* this */, jlong matAddr, jbyteArray outArray, jint width, jint height) {
#ifdef HAVE_OPENCV
    if (matAddr == 0 || outArray == nullptr || width <= 0 || height <= 0) {
        LOGE("nativeMatToRgbaBytes: invalid arguments");
        return JNI_FALSE;
    }
    cv::Mat& src = *(cv::Mat*) matAddr;
    if (src.empty()) {
        LOGE("nativeMatToRgbaBytes: empty mat");
        return JNI_FALSE;
    }

    cv::Mat rgba;
    if (src.type() == CV_8UC4) {
        rgba = src;
    } else if (src.type() == CV_8UC1) {
        cv::cvtColor(src, rgba, cv::COLOR_GRAY2RGBA);
    } else if (src.type() == CV_8UC3) {
        cv::cvtColor(src, rgba, cv::COLOR_RGB2RGBA);
    } else {
        cv::cvtColor(src, rgba, cv::COLOR_BGR2RGBA);
    }

    const int rowBytes = width * 4;
    jboolean isCopy = JNI_FALSE;
    jbyte* outPtr = env->GetByteArrayElements(outArray, &isCopy);
    if (!outPtr) return JNI_FALSE;

    for (int r = 0; r < height; ++r) {
        std::memcpy(outPtr + r * rowBytes, rgba.ptr<unsigned char>(r), rowBytes);
    }

    env->ReleaseByteArrayElements(outArray, outPtr, 0);
    if (rgba.data != src.data) rgba.release();
    return JNI_TRUE;
#else
    (void)env; (void)matAddr; (void)outArray; (void)width; (void)height;
    return JNI_FALSE;
#endif
}
