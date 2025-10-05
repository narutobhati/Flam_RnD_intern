package com.flam.rnd.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Utility class for camera operations and configurations
 * Provides helper methods for CameraX setup and camera capability detection
 */
object CameraUtils {
    
    private const val TAG = "CameraUtils"
    
    /**
     * Camera configuration data class
     */
    data class CameraConfig(
        val targetResolution: Size = Size(1920, 1080),
        val enableImageAnalysis: Boolean = true,
        val enableImageCapture: Boolean = true,
        val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    )
    
    /**
     * Camera information data class
     */
    data class CameraInfo(
        val cameraId: String,
        val lensFacing: Int,
        val supportedResolutions: List<Size>,
        val hasFlash: Boolean,
        val hasAutoFocus: Boolean,
        val maxDigitalZoom: Float
    )
    
    /**
     * Get available cameras and their capabilities
     */
    fun getAvailableCameras(context: Context): List<CameraInfo> {
        val cameraInfoList = mutableListOf<CameraInfo>()
        
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList
            
            for (cameraId in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING) ?: -1
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val hasAutoFocus = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)?.contains(
                    CameraCharacteristics.CONTROL_AF_MODE_AUTO
                ) == true
                val maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1.0f
                
                val configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val supportedResolutions = configMap?.getOutputSizes(android.graphics.ImageFormat.YUV_420_888)?.toList() ?: emptyList()
                
                cameraInfoList.add(
                    CameraInfo(
                        cameraId = cameraId,
                        lensFacing = lensFacing,
                        supportedResolutions = supportedResolutions,
                        hasFlash = hasFlash,
                        hasAutoFocus = hasAutoFocus,
                        maxDigitalZoom = maxZoom
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting camera information: ${e.message}", e)
        }
        
        return cameraInfoList
    }
    
    /**
     * Get optimal resolution for image processing
     */
    fun getOptimalResolution(availableResolutions: List<Size>, targetResolution: Size = Size(1920, 1080)): Size {
        if (availableResolutions.isEmpty()) {
            return targetResolution
        }
        
        // Find the resolution closest to target while maintaining aspect ratio
        val targetAspectRatio = targetResolution.width.toFloat() / targetResolution.height
        
        return availableResolutions.minByOrNull { size ->
            val aspectRatio = size.width.toFloat() / size.height
            val aspectRatioDiff = Math.abs(aspectRatio - targetAspectRatio)
            val resolutionDiff = Math.abs((size.width * size.height) - (targetResolution.width * targetResolution.height))
            aspectRatioDiff * 10000 + resolutionDiff / 1000000f
        } ?: targetResolution
    }
    
    /**
     * Create Preview use case with optimal configuration
     */
    fun createPreviewUseCase(config: CameraConfig): Preview {
        return Preview.Builder()
            .setTargetResolution(config.targetResolution)
            .build()
    }
    
    /**
     * Create ImageCapture use case with optimal configuration
     */
    fun createImageCaptureUseCase(config: CameraConfig): ImageCapture {
        return ImageCapture.Builder()
            .setTargetResolution(config.targetResolution)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    
    /**
     * Create ImageAnalysis use case for real-time processing
     */
    fun createImageAnalysisUseCase(
        config: CameraConfig,
        analyzer: ImageAnalysis.Analyzer,
        executor: ExecutorService = Executors.newSingleThreadExecutor()
    ): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setTargetResolution(config.targetResolution)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(executor, analyzer)
            }
    }
    
    /**
     * Setup camera with configuration
     */
    fun setupCamera(
        context: Context,
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        config: CameraConfig,
        preview: Preview,
        imageCapture: ImageCapture? = null,
        imageAnalysis: ImageAnalysis? = null,
        onSuccess: (Camera) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Unbind previous use cases
                cameraProvider.unbindAll()
                
                // Prepare use cases list
                val useCases = mutableListOf<UseCase>(preview)
                imageCapture?.let { useCases.add(it) }
                imageAnalysis?.let { useCases.add(it) }
                
                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    config.cameraSelector,
                    *useCases.toTypedArray()
                )
                
                onSuccess(camera)
                Log.d(TAG, "Camera setup successful with ${useCases.size} use cases")
                
            } catch (e: Exception) {
                Log.e(TAG, "Camera setup failed: ${e.message}", e)
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * Check if device has front camera
     */
    fun hasFrontCamera(context: Context): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager.cameraIdList.any { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for front camera: ${e.message}", e)
            false
        }
    }
    
    /**
     * Check if device has back camera
     */
    fun hasBackCamera(context: Context): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager.cameraIdList.any { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for back camera: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get camera selector based on preference
     */
    fun getCameraSelector(preferFront: Boolean = false): CameraSelector {
        return if (preferFront) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
    
    /**
     * FPS counter utility class
     */
    class FPSCounter {
        private var frameCount = 0
        private var startTime = System.currentTimeMillis()
        private var lastFPS = 0f
        
        fun update(): Float {
            frameCount++
            val currentTime = System.currentTimeMillis()
            val timeElapsed = currentTime - startTime
            
            if (timeElapsed >= 1000) { // Update every second
                lastFPS = (frameCount * 1000f) / timeElapsed
                frameCount = 0
                startTime = currentTime
            }
            
            return lastFPS
        }
        
        fun getCurrentFPS(): Float = lastFPS
    }
    
    /**
     * Camera state enum
     */
    enum class CameraState {
        UNINITIALIZED,
        INITIALIZING,
        READY,
        CAPTURING,
        PROCESSING,
        ERROR
    }
    
    /**
     * Camera error types
     */
    enum class CameraError {
        PERMISSION_DENIED,
        HARDWARE_UNAVAILABLE,
        INITIALIZATION_FAILED,
        CAPTURE_FAILED,
        PROCESSING_FAILED
    }
    
    /**
     * Image format utilities
     */
    object ImageFormatUtils {
        
        /**
         * Get human-readable format name
         */
        fun getFormatName(format: Int): String {
            return when (format) {
                android.graphics.ImageFormat.YUV_420_888 -> "YUV_420_888"
                android.graphics.ImageFormat.NV21 -> "NV21"
                android.graphics.ImageFormat.NV16 -> "NV16"
                android.graphics.ImageFormat.RGB_565 -> "RGB_565"
                android.graphics.ImageFormat.JPEG -> "JPEG"
                else -> "Unknown ($format)"
            }
        }
        
        /**
         * Check if format is supported for processing
         */
        fun isSupportedForProcessing(format: Int): Boolean {
            return when (format) {
                android.graphics.ImageFormat.YUV_420_888,
                android.graphics.ImageFormat.NV21 -> true
                else -> false
            }
        }
    }
}