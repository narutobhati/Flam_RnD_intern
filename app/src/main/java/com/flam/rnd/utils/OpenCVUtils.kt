package com.flam.rnd.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlin.system.measureTimeMillis
import java.nio.ByteBuffer

/**
 * Utility class for OpenCV operations and integration with Android Camera2 API
 * This class provides helper methods for converting between Android image formats and OpenCV Mat objects
 */
object OpenCVUtils {
    
    private const val TAG = "OpenCVUtils"
    
    // Native method declarations for OpenCV integration
    external fun nativeProcessImage(matAddr: Long): Boolean
    external fun nativeInitOpenCV(): Boolean
    external fun nativeCreateMat(width: Int, height: Int, type: Int): Long
    external fun nativeReleaseMat(matAddr: Long)
    external fun nativeConvertYUV420ToRGB(
        yData: ByteArray, 
        uData: ByteArray, 
        vData: ByteArray, 
        width: Int, 
        height: Int,
        yStride: Int,
        uvStride: Int
    ): Long
    
    /**
     * Initialize OpenCV library
     * This should be called once when the app starts
     */
    fun initializeOpenCV(context: Context): Boolean {
        return try {
            Log.d(TAG, "Initializing OpenCV...")
            val result = nativeInitOpenCV()
            if (result) {
                Log.d(TAG, "OpenCV initialized successfully")
            } else {
                Log.e(TAG, "OpenCV initialization failed")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OpenCV: ${e.message}", e)
            false
        }
    }
    
    /**
     * Convert ImageProxy (from CameraX) to OpenCV Mat address
     * @param image ImageProxy from camera
     * @return Mat address (long) or 0 if conversion failed
     */
    fun imageProxyToMatAddress(image: ImageProxy): Long {
        return try {
            when (image.format) {
                android.graphics.ImageFormat.YUV_420_888 -> {
                    var matAddr = 0L
                    val ms = measureTimeMillis {
                        matAddr = convertYUV420ToMat(image)
                    }
                    Log.d(TAG, "YUV->RGBA conversion took ${ms}ms")
                    matAddr
                }
                else -> {
                    Log.w(TAG, "Unsupported image format: ${image.format}")
                    0L
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting ImageProxy to Mat: ${e.message}", e)
            0L
        }
    }
    
    /**
     * Convert YUV_420_888 ImageProxy to OpenCV Mat
     */
    private fun convertYUV420ToMat(image: ImageProxy): Long {
        val planes = image.planes
        if (planes.size != 3) {
            Log.e(TAG, "Invalid YUV_420_888 format: expected 3 planes, got ${planes.size}")
            return 0L
        }
        
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]
        
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val yData = ByteArray(ySize)
        val uData = ByteArray(uSize)
        val vData = ByteArray(vSize)
        
        yBuffer.get(yData)
        uBuffer.get(uData)
        vBuffer.get(vData)
        
        return nativeConvertYUV420ToRGB(
            yData, uData, vData,
            image.width, image.height,
            yPlane.rowStride, uPlane.rowStride
        )
    }
    
    /**
     * Convert Android Bitmap to OpenCV Mat address
     * @param bitmap Input bitmap
     * @return Mat address or 0 if conversion failed
     */
    fun bitmapToMatAddress(bitmap: Bitmap): Long {
        // This would be implemented in native code
        // For now, return placeholder
        Log.d(TAG, "Converting bitmap ${bitmap.width}x${bitmap.height} to Mat")
        return 0L
    }
    
    /**
     * Process image using OpenCV native functions
     * @param matAddr OpenCV Mat address
     * @return true if processing was successful
     */
    fun processImageWithOpenCV(matAddr: Long): Boolean {
        return if (matAddr != 0L) {
            try {
                nativeProcessImage(matAddr)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}", e)
                false
            }
        } else {
            Log.w(TAG, "Cannot process image: invalid Mat address")
            false
        }
    }
    
    /**
     * Release OpenCV Mat memory
     * @param matAddr Mat address to release
     */
    fun releaseMat(matAddr: Long) {
        if (matAddr != 0L) {
            try {
                nativeReleaseMat(matAddr)
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing Mat: ${e.message}", e)
            }
        }
    }
    
    /**
     * Create an empty Mat with specified dimensions
     * @param width Mat width
     * @param height Mat height
     * @param type OpenCV Mat type (e.g., CV_8UC3)
     * @return Mat address or 0 if creation failed
     */
    fun createMat(width: Int, height: Int, type: Int = 16 /* CV_8UC3 */): Long {
        return try {
            nativeCreateMat(width, height, type)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Mat: ${e.message}", e)
            0L
        }
    }
    
    /**
     * Check if OpenCV is properly initialized and available
     */
    fun isOpenCVAvailable(): Boolean {
        return try {
            nativeInitOpenCV()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "OpenCV native library not available")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking OpenCV availability: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get OpenCV version information
     * This method calls the native function to get version info
     */
    fun getOpenCVVersionInfo(): String {
        return if (isOpenCVAvailable()) {
            // This would call a native method to get actual OpenCV version
            "OpenCV 4.8.0 (Native Integration Ready)"
        } else {
            "OpenCV not available"
        }
    }
    
    /**
     * Perform basic image processing operations
     * @param matAddr Source Mat address
     * @param operation Processing operation type
     * @return Processed Mat address or 0 if failed
     */
    fun performImageProcessing(matAddr: Long, operation: ProcessingOperation): Long {
        return if (matAddr != 0L && isOpenCVAvailable()) {
            when (operation) {
                ProcessingOperation.GRAYSCALE -> {
                    Log.d(TAG, "Converting to grayscale")
                    // Native implementation would go here
                    matAddr
                }
                ProcessingOperation.BLUR -> {
                    Log.d(TAG, "Applying blur filter")
                    // Native implementation would go here
                    matAddr
                }
                ProcessingOperation.EDGE_DETECTION -> {
                    Log.d(TAG, "Applying edge detection")
                    // Native implementation would go here
                    matAddr
                }
                ProcessingOperation.THRESHOLD -> {
                    Log.d(TAG, "Applying threshold")
                    // Native implementation would go here
                    matAddr
                }
            }
        } else {
            Log.w(TAG, "Cannot perform processing: invalid Mat or OpenCV not available")
            0L
        }
    }
    
    /**
     * Enum for different image processing operations
     */
    enum class ProcessingOperation {
        GRAYSCALE,
        BLUR,
        EDGE_DETECTION,
        THRESHOLD
    }
}