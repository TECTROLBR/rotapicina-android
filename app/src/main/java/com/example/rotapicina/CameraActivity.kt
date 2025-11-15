package com.example.rotapicina

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnGravar: ImageButton
    private lateinit var btnConcluir: Button
    private lateinit var contadorVideos: TextView
    private lateinit var chronometer: Chronometer

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private val capturedVideos = ArrayList<String>()
    private var recordAnimation: ObjectAnimator? = null

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.CAMERA] == true && permissions[android.Manifest.permission.RECORD_AUDIO] == true) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissões de câmera e áudio são necessárias.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        btnGravar = findViewById(R.id.btnGravar)
        btnConcluir = findViewById(R.id.btnConcluir)
        contadorVideos = findViewById(R.id.contadorVideos)
        chronometer = findViewById(R.id.chronometer)

        permissionsLauncher.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO))

        btnGravar.setOnClickListener { toggleRecording() }
        btnConcluir.setOnClickListener { finishAndReturnVideos() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, videoCapture
                )
            } catch (exc: Exception) {
                // Handle exception
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleRecording() {
        if (recording != null) {
            stopRecording()
            return
        }
        startRecording()
    }

    private fun startRecording() {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/PiscinaFacil-Videos")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture?.output?.prepareRecording(this, mediaStoreOutputOptions)?.withAudioEnabled()
            ?.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                if (recordEvent is VideoRecordEvent.Finalize) {
                    if (!recordEvent.hasError()) {
                        val uri = recordEvent.outputResults.outputUri.toString()
                        capturedVideos.add(uri)
                        updateUI()
                    } else {
                        stopRecording()
                    }
                }
            }

        chronometer.visibility = View.VISIBLE
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        startRecordAnimation()
    }

    private fun stopRecording() {
        recording?.close()
        recording = null

        chronometer.stop()
        chronometer.visibility = View.GONE
        stopRecordAnimation()
    }

    private fun startRecordAnimation() {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.2f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.2f)
        recordAnimation = ObjectAnimator.ofPropertyValuesHolder(btnGravar, scaleX, scaleY).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    private fun stopRecordAnimation() {
        recordAnimation?.cancel()
        btnGravar.scaleX = 1.0f
        btnGravar.scaleY = 1.0f
    }

    private fun updateUI() {
        contadorVideos.text = capturedVideos.size.toString()
        btnConcluir.visibility = if (capturedVideos.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun finishAndReturnVideos() {
        val resultIntent = Intent()
        resultIntent.putStringArrayListExtra("video_uris", capturedVideos)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}