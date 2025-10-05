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

# OpenCV configuration (optional)
# Provide OpenCV_DIR via Gradle/CMake configure to enable OpenCV
if(DEFINED OpenCV_DIR)
    message(STATUS "Using OpenCV from: ${OpenCV_DIR}")
    find_package(OpenCV REQUIRED)
    if(OpenCV_FOUND)
        add_definitions(-DHAVE_OPENCV)
        include_directories(${OpenCV_INCLUDE_DIRS})
        message(STATUS "OpenCV include dirs: ${OpenCV_INCLUDE_DIRS}")
        message(STATUS "OpenCV libs: ${OpenCV_LIBS}")
    endif()
else()
    message(WARNING "OpenCV_DIR not set. Building without OpenCV. Native processing will be a no-op.")
endif()

# Include directories
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

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
        
        # OpenCV libraries (linked when available)
        ${OpenCV_LIBS}
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