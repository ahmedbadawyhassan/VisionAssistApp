package com.example.visionassist

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class LanguageManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("VisionAssist", Context.MODE_PRIVATE)

    fun getCurrentLocale(): Locale {
        return when (prefs.getString("lang", "en")) {
            "es" -> Locale("es", "ES")
            "fr" -> Locale("fr", "FR")
            else -> Locale.ENGLISH
        }
    }

    fun setLanguage(langCode: String) {
        prefs.edit().putString("lang", langCode).apply()
    }
}