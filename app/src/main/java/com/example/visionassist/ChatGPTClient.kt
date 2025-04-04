package com.example.visionassist

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatGPTClient {
    private const val BASE_URL = "https://api.openai.com/"
    val instance: ChatGPTApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatGPTApi::class.java)
    }
}