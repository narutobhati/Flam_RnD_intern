package com.flam.rnd

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.flam.rnd.utils.OpenCVUtils
import kotlin.system.measureTimeMillis

class CameraActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CameraActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

    // Native method declarations
    external fun processImage(matAddr: Long): Boolean
    external fun stringFromJNI(): String

    private lateinit var viewFinder: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var btnProcessing: Button
    private lateinit var btnBack: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvFps: TextView
    private lateinit var tvResolution: TextView
    private lateinit var tvProcessingStatus: TextView
    private lateinit var ivProcessed: ImageView
    
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    
    private var isProcessingEnabled = false
    private var frameCount = 0
    private var fpsStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initializeViews()
        setupClickListeners()
        
        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check permissions and start camera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            finish() // Go back if no camera permission
        }
    }

    private fun initializeViews() {
        viewFinder = findViewById(R.id.viewFinder)
        btnCapture = findViewById(R.id.btn_capture)
        btnProcessing = findViewById(R.id.btn_toggle_processing)
        btnBack = findViewById(R.id.btn_back)
        tvStatus = findViewById(R.id.tv_status)
        tvFps = findViewById(R.id.tv_fps)
        tvResolution = findViewById(R.id.tv_resolution)
        tvProcessingStatus = findViewById(R.id.tv_processing_status)
        ivProcessed = findViewById(R.id.iv_processed)
    }

    private fun setupClickListeners() {
        btnCapture.setOnClickListener {
            captureImage()
        }

        btnProcessing.setOnClickListener {
            toggleImageProcessing()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Image capture
            imageCapture = ImageCapture.Builder()
                .build()

            // Image analysis
            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer())
                }

            // Select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, 
                    cameraSelector, 
                    preview, 
                    imageCapture,
                    imageAnalyzer
                )

                updateStatus("Camera initialized successfully")

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                updateStatus("Camera initialization failed: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create output file options (for now, just capture without saving)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            createTempFile("captured_image", ".jpg", cacheDir)
        ).build()

        // Set up image capture listener
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    updateStatus("Photo capture failed")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo captured successfully"
                    updateStatus(msg)
                    Log.d(TAG, msg)
                    
                    // Here you could process the captured image with native code
                    processImageWithNative()
                }
            }
        )
    }

    private fun toggleImageProcessing() {
        isProcessingEnabled = !isProcessingEnabled
        val statusText = if (isProcessingEnabled) {
            "Real-time processing: ON"
        } else {
            "Real-time processing: OFF"
        }
        updateStatus(statusText)
        tvProcessingStatus.text = if (isProcessingEnabled) "Processing: ON" else "Processing: OFF"
        
        val buttonText = if (isProcessingEnabled) {
            "Stop Processing"
        } else {
            "Start Processing"
        }
        btnProcessing.text = buttonText
        if (isProcessingEnabled) {
            frameCount = 0
            fpsStartTime = System.currentTimeMillis()
        }
    }

    private fun processImageWithNative() {
        try {
            // This is a placeholder - in a real implementation you would:
            // 1. Convert the captured image to OpenCV Mat
            // 2. Get the Mat address
            // 3. Pass it to the native function
            val result = processImage(0L) // Placeholder address
            
            val statusMsg = if (result) {
                "Image processed successfully with native code"
            } else {
                "Native image processing failed"
            }
            updateStatus(statusMsg)
            
        } catch (e: Exception) {
            Log.e(TAG, "Native processing error", e)
            updateStatus("Native processing error: ${e.message}")
        }
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            tvStatus.text = message
            Log.d(TAG, message)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // Image analyzer class for real-time processing
    private inner class ImageAnalyzer : ImageAnalysis.Analyzer {
        
        override fun analyze(image: ImageProxy) {
            // Only process if real-time processing is enabled
            if (isProcessingEnabled) {
                try {
                    val w = image.width
                    val h = image.height
                    var matAddr = 0L
                    val convertMs = measureTimeMillis {
                        matAddr = OpenCVUtils.imageProxyToMatAddress(image)
                    }
                    Log.d(TAG, "Analyzing frame: ${w}x${h}, convert ${convertMs}ms")

                    var processed = false
                    if (matAddr != 0L) {
                        val processMs = measureTimeMillis {
                            processed = OpenCVUtils.processImageWithOpenCV(matAddr)
                        }
                        Log.d(TAG, "Native processed in ${processMs}ms")
                        if (processed) {
                            val bmp = OpenCVUtils.matToBitmap(matAddr, w, h)
                            if (bmp != null) {
                                runOnUiThread {
                                    ivProcessed.setImageBitmap(bmp)
                                }
                            }
                        }
                        OpenCVUtils.releaseMat(matAddr)
                    }

                    if (processed) {
                        frameCount += 1
                        val now = System.currentTimeMillis()
                        if (fpsStartTime == 0L) fpsStartTime = now
                        val elapsed = now - fpsStartTime
                        if (elapsed >= 1000L) {
                            val fps = (frameCount * 1000f) / elapsed
                            Log.d(TAG, "AVG FPS: ${"%.1f".format(fps)} over ${elapsed}ms")
                            runOnUiThread {
                                tvFps.text = "FPS: ${"%.1f".format(fps)}"
                            }
                            frameCount = 0
                            fpsStartTime = now
                        }
                        runOnUiThread {
                            tvResolution.text = "${w}x${h}"
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Analysis error", e)
                } finally {
                    // Always close the image to avoid blocking the pipeline
                    image.close()
                }
            } else {
                // Close the image without processing
                image.close()
            }
        }
    }
}