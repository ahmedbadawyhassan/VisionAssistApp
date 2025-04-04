package com.example.visionassist

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ObjectDetector(context: Context) {
    private val interpreter: Interpreter
    private val labels: List<String>

    init {
        // Load TFLite model
        val model = context.assets.open("mobilenet_v3.tflite").use { input ->
            ByteBuffer.allocateDirect(input.available()).apply {
                order(ByteOrder.nativeOrder())
                input.read(array())
            }
        }
        interpreter = Interpreter(model)

        // Load labels
        labels = context.assets.open("labelmap.txt").bufferedReader().readLines()
    }

    fun detect(bitmap: Bitmap): List<String> {
        val inputBuffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
            ImageUtils.bitmapToByteBuffer(bitmap, this)
        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)

        return output[0]
            .mapIndexed { idx, confidence -> Pair(labels[idx], confidence) }
            .filter { it.second > 0.5 } // Confidence threshold
            .sortedByDescending { it.second }
            .map { it.first }
    }
}