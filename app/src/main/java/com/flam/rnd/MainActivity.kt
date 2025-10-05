package com.flam.rnd

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        // Load the native library
        init {
            System.loadLibrary("flam_rnd_native")
        }

        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    // Native method declarations
    external fun stringFromJNI(): String
    external fun getOpenCVVersion(): Int
    external fun getNDKInfo(): String
    external fun processImage(matAddr: Long): Boolean

    private lateinit var tvWelcome: TextView
    private lateinit var tvNativeInfo: TextView
    private lateinit var tvOpenCVInfo: TextView
    private lateinit var btnCamera: Button
    private lateinit var btnTestNative: Button

    // Camera permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCameraActivity()
        } else {
            Toast.makeText(
                this,
                "Camera permission is required for this feature",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
        loadNativeInfo()
    }

    private fun initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome)
        tvNativeInfo = findViewById(R.id.tv_native_info)
        tvOpenCVInfo = findViewById(R.id.tv_opencv_info)
        btnCamera = findViewById(R.id.btn_camera)
        btnTestNative = findViewById(R.id.btn_test_native)
    }

    private fun setupClickListeners() {
        btnCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        btnTestNative.setOnClickListener {
            testNativeFunctions()
        }
    }

    private fun loadNativeInfo() {
        try {
            // Display native library info
            val welcomeMessage = stringFromJNI()
            tvWelcome.text = welcomeMessage

            // Display NDK information
            val ndkInfo = getNDKInfo()
            tvNativeInfo.text = ndkInfo

            // Display OpenCV information
            val openCvVersion = getOpenCVVersion()
            val openCvInfo = if (openCvVersion > 0) {
                "OpenCV Version: ${formatOpenCVVersion(openCvVersion)}"
            } else {
                "OpenCV not configured yet"
            }
            tvOpenCVInfo.text = openCvInfo

        } catch (e: UnsatisfiedLinkError) {
            tvWelcome.text = "Error loading native library: ${e.message}"
            tvNativeInfo.text = "NDK integration failed"
            tvOpenCVInfo.text = "OpenCV integration failed"
        }
    }

    private fun formatOpenCVVersion(version: Int): String {
        val major = version / 10000
        val minor = (version % 10000) / 100
        val revision = version % 100
        return "$major.$minor.$revision"
    }

    private fun testNativeFunctions() {
        try {
            // Test native function call
            val result = stringFromJNI()
            
            // Test OpenCV version
            val openCvVersion = getOpenCVVersion()
            
            // Test image processing (with dummy data)
            val processingResult = processImage(0L) // Passing 0 as placeholder
            
            val testResults = """
                Native Test Results:
                ├─ String from JNI: $result
                ├─ OpenCV Version: ${if (openCvVersion > 0) formatOpenCVVersion(openCvVersion) else "Not configured"}
                └─ Image Processing Test: ${if (processingResult) "SUCCESS" else "FAILED"}
            """.trimIndent()
            
            Toast.makeText(this, "Native functions test completed!", Toast.LENGTH_SHORT).show()
            
            // Update UI with test results
            tvNativeInfo.text = testResults
            
        } catch (e: Exception) {
            Toast.makeText(this, "Native test failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                openCameraActivity()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show rationale and request permission
                Toast.makeText(
                    this,
                    "Camera permission is needed to capture and process images",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // Request permission directly
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Refresh native info when returning to activity
        loadNativeInfo()
    }
}