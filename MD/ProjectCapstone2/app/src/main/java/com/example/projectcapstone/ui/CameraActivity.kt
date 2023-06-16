package com.example.projectcapstone.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.projectcapstone.createFile
import com.example.projectcapstone.databinding.ActivityCameraBinding
import com.example.projectcapstone.ui.tryon.TryonFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraActivity : AppCompatActivity() {

    private var _binding: ActivityCameraBinding? = null
    private val binding get() = _binding
    private var imageCapture : ImageCapture? = null
    private var cameraSelector : CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var rotation : Int = Surface.ROTATION_0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.captureImage?.setOnClickListener {
            takePicture(this)
        }

        binding?.switchCamera?.setOnClickListener {
            cameraSelector = if(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA){
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            else{
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            launchCamera()
        }

    }

    public override fun onResume() {
        super.onResume()
        launchCamera()
    }

    private fun takePicture(context: Context) {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        @Suppress("DEPRECATION")
        rotation = windowManager.defaultDisplay.rotation

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val intent = Intent().apply {
                        putExtra("picture", photoFile)
                        putExtra("isBackCamera", cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        putExtra("rotation", rotation)
                    }
                    setResult(TryonFragment.CAMERA_X_RESULT, intent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Gagal mengambil gambar.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun launchCamera() = lifecycleScope.launch {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@CameraActivity)
        val cameraProvider : ProcessCameraProvider =
            withContext(Dispatchers.IO) {
                cameraProviderFuture.get()
            }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder().build()

        try{
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this@CameraActivity,
                cameraSelector,
                preview,
                imageCapture
            )
        }
        catch (e : Exception){
            Toast.makeText(
                this@CameraActivity,
                "Gagal memunculkan kamera.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}