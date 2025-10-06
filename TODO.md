# Task: Change Text Color in CameraActivity to Black for Visibility

## Completed Tasks
- [x] Changed text color of FPS counter (tv_fps) from white to black
- [x] Changed text color of processing status (tv_processing_status) from white to black
- [x] Changed text color of resolution info (tv_resolution) from white to black
- [x] Fixed build.gradle plugin versions to resolve build error
- [x] Removed explicit ndkVersion to use bundled NDK and resolve configuration issue

## Summary
Modified app/src/main/res/layout/activity_camera.xml to set android:textColor="@android:color/black" for the three TextViews displaying FPS, processing status, and resolution in the camera controls layout. This should improve text visibility on the camera interface. Also fixed the Gradle build configuration by adding plugin versions.
