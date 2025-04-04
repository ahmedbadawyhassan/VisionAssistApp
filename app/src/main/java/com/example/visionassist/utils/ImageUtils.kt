package com.example.visionassist.utils

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object ImageUtils {
    // Convert Bitmap to ByteBuffer for TFLite
    fun bitmapToByteBuffer(bitmap: Bitmap, buffer: ByteBuffer) {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        buffer.rewind()
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                buffer.putFloat((pixel shr 16 and 0xFF) / 255.0f) // Red
                buffer.putFloat((pixel shr 8 and 0xFF) / 255.0f)  // Green
                buffer.putFloat((pixel and 0xFF) / 255.0f)        // Blue
            }
        }
    }

    // Convert Bitmap to Base64 for ChatGPT API
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }
}