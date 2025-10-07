# Flam_RnD_intern (IN PROGRESS)

Android application with NDK + CMake support and OpenCV integration for computer vision and image processing.

## Project Structure

```
Flam_RnD_intern/
├── app/
│   ├── build.gradle          # Android app configuration with NDK support
│   └── src/main/
│       ├── AndroidManifest.xml # App manifest with camera permissions
│       ├── java/com/flam/rnd/  # Java/Kotlin source files
│       └── res/                # Android resources
├── jni/
│   ├── CMakeLists.txt         # CMake build configuration
│   └── native_lib.cpp         # Native C++ source code
├── settings.gradle            # Gradle project settings
├── .gitignore                # Git ignore file for Android/NDK
└── README.md                 # This file
```

## Prerequisites

### Required Software
- **Android Studio** (latest version)
- **Android SDK** (API level 24+)
- **Android NDK** (r21e or later)
- **CMake** (3.22.1 or later)
- **OpenCV for Android** (4.8.0 or later)

### Android Studio Setup
1. Install Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Open SDK Manager and install:
   - Android SDK Platform (API 24+)
   - Android NDK
   - CMake
   - LLDB (for debugging)

## OpenCV Setup

### Option 1: OpenCV Android SDK (Recommended)

1. Download OpenCV Android SDK from [opencv.org](https://opencv.org/releases/)
2. Extract to a local directory (e.g., `~/opencv-android-sdk`)
3. In `jni/CMakeLists.txt`, uncomment and update:
   ```cmake
   set(OpenCV_DIR "/path/to/opencv-android-sdk/sdk/native/jni")
   find_package(OpenCV REQUIRED)
   ```
4. In `app/build.gradle`, uncomment OpenCV dependency configuration

### Option 2: Maven Dependency

1. In `app/build.gradle`, uncomment:
   ```gradle
   implementation 'org.opencv:opencv-android:4.8.0'
   ```

### Option 3: Local OpenCV Module

1. Import OpenCV module into your project
2. In `app/build.gradle`, uncomment:
   ```gradle
   implementation project(':opencv')
   ```

## Build Instructions

### 1. Clone and Open Project
```bash
git clone https://github.com/narutobhati/Flam_RnD_intern.git
cd Flam_RnD_intern
```

### 2. Open in Android Studio
- Open Android Studio
- Choose "Open an existing Android Studio project"
- Navigate to the `Flam_RnD_intern` folder
- Click "OK"

### 3. Configure OpenCV
- Follow one of the OpenCV setup options above
- Update paths in `CMakeLists.txt` and `build.gradle`

### 4. Build Project
- Click "Build" > "Make Project" or press Ctrl+F9
- For clean build: "Build" > "Clean Project" then "Build" > "Rebuild Project"

### 5. Run on Device
- Connect Android device (API 24+) with USB debugging enabled
- Click "Run" > "Run 'app'" or press Shift+F10

## NDK Development

### Adding Native Functions

1. Add C++ function in `jni/native_lib.cpp`:
```cpp
extern "C" JNIEXPORT jstring JNICALL
Java_com_flam_rnd_MainActivity_yourFunction(
    JNIEnv* env,
    jobject /* this */) {
    // Your native code here
    return env->NewStringUTF("Result");
}
```

2. Declare in Java/Kotlin:
```java
public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("flam_rnd_native");
    }
    
    public native String yourFunction();
}
```

### OpenCV Integration

Once OpenCV is configured, uncomment the OpenCV code in `native_lib.cpp`:

```cpp
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>

// Use OpenCV functions
cv::Mat image;
cv::cvtColor(image, image, cv::COLOR_BGR2GRAY);
```

## Debugging

### Native Code Debugging
1. Set breakpoints in C++ code
2. Select "Debug 'app'" instead of "Run 'app'"
3. Use LLDB debugger for native code

### Logging
Use Android logging in native code:
```cpp
#include <android/log.h>
#define TAG "YourTag"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
LOGI("Debug message: %s", message);
```

## Troubleshooting

### Common Issues

1. **CMake not found**
   - Install CMake via SDK Manager
   - Check CMake version in `build.gradle`

2. **NDK build fails**
   - Verify NDK installation
   - Check ABI filters in `build.gradle`
   - Clean and rebuild project

3. **OpenCV linking errors**
   - Verify OpenCV path in `CMakeLists.txt`
   - Check OpenCV ABI compatibility
   - Ensure OpenCV version matches

4. **Permission denied (Camera)**
   - Grant camera permission in app settings
   - Check runtime permissions in code

### Build Configuration

- **Debug builds**: Enable debugging symbols and logging
- **Release builds**: Optimize for performance, disable logging
- **ABI variants**: arm64-v8a (64-bit), armeabi-v7a (32-bit)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on multiple devices/architectures
5. Submit a pull request

## License

This project is open source. Please check the repository for license details.

