package com.example.visionassist

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.visionassist.databinding.ActivityCameraBinding
import com.pedro.library.util.sources.MjpegView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class CameraActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var objectDetector: ObjectDetector
    private lateinit var tts: TextToSpeech
    private lateinit var languageManager: LanguageManager

    // Replace placeholders
    private val esp32CamIP = "http://192.168.43.216/video"
    private val chatGPTApiKey = "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"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCameraStream()
        initializeComponents()
    }

    private fun setupCameraStream() {
        binding.mjpegView.setSource(esp32CamIP)
        binding.mjpegView.startStream()
    }

    private fun initializeComponents() {
        objectDetector = ObjectDetector(this)
        tts = TextToSpeech(this, this)
        languageManager = LanguageManager(this)
        startFrameProcessing()
    }

    private fun startFrameProcessing() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                processFrame()
                kotlinx.coroutines.delay(2000) // Process every 2 seconds
            }
        }
    }

    private fun processFrame() {
        val bitmap = binding.mjpegView.bitmap ?: return
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        
        // Offline Detection
        val objects = objectDetector.detect(resizedBitmap)
        if (objects.isNotEmpty()) speak("Offline: ${objects[0]}")

        // Online Analysis
        CoroutineScope(Dispatchers.IO).launch {
            analyzeWithChatGPT(resizedBitmap)
        }
    }

    private suspend fun analyzeWithChatGPT(bitmap: Bitmap) {
        try {
            val base64Image = ImageUtils.bitmapToBase64(bitmap)
            val response = ChatGPTClient.instance.analyzeImage(
                "Bearer $chatGPTApiKey",
                ChatGPTApi.ChatGPTRequest(
                    messages = listOf(
                        ChatGPTApi.Message(
                            content = listOf(
                                ChatGPTApi.Content(text = "Describe this image for a blind user"),
                                ChatGPTApi.Content(image_url = ChatGPTApi.ImageUrl("data:image/jpeg;base64,$base64Image"))
                            )
                        )
                    )
                )
            )
            val description = response.choices[0].message.content[0].text ?: "No description"
            speak(description)
        } catch (e: Exception) {
            Log.e("ChatGPT", "API Error: ${e.message}")
        }
    }

    private fun speak(text: String) {
        runOnUiThread {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = languageManager.getCurrentLocale()
        }
    }

    override fun onDestroy() {
        binding.mjpegView.stopStream()
        tts.shutdown()
        super.onDestroy()
    }
}