# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.
project("flam_rnd_native")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# OpenCV configuration
# Option 1: Using OpenCV SDK (uncomment and adjust path)
# set(OpenCV_DIR "/path/to/opencv-android-sdk/sdk/native/jni")
# find_package(OpenCV REQUIRED)

# Option 2: Using prebuilt OpenCV static libraries (uncomment and adjust)
# set(opencv_libs_dir "${CMAKE_CURRENT_SOURCE_DIR}/../app/src/main/jniLibs")
# set(opencv_include_dir "${CMAKE_CURRENT_SOURCE_DIR}/../opencv/include")

# Include directories
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
# include_directories(${opencv_include_dir})

# Add your native source files here
add_library( # Sets the name of the library.
        flam_rnd_native

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native_lib.cpp
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
)

# Android NDK libraries
find_library(android-lib android)
find_library(camera2ndk-lib camera2ndk)
find_library(mediandk-lib mediandk)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        flam_rnd_native

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${android-lib}
        ${camera2ndk-lib}
        ${mediandk-lib}
        
        # OpenCV libraries (uncomment when using OpenCV)
        # ${OpenCV_LIBS}
        
        # Or link specific OpenCV static libraries
        # opencv_core
        # opencv_imgproc
        # opencv_features2d
        # opencv_imgcodecs
)

# Compiler-specific options
target_compile_options(flam_rnd_native PRIVATE
    -Wall
    -Wextra
    -fexceptions
    -frtti
)

# C++ standard
set_property(TARGET flam_rnd_native PROPERTY CXX_STANDARD 17)