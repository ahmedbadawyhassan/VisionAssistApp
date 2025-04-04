package com.example.visionassist

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatGPTApi {
    @POST("v1/chat/completions")
    suspend fun analyzeImage(
        @Header("Authorization") apiKey: String,
        @Body request: ChatGPTRequest
    ): ChatGPTResponse

    data class ChatGPTRequest(
        val model: String = "gpt-4-vision-preview",
        val messages: List<Message>,
        val max_tokens: Int = 300
    )

    data class Message(
        val role: String = "user",
        val content: List<Content>
    )

    data class Content(
        val type: String,
        val text: String? = null,
        val image_url: ImageUrl? = null
    )

    data class ImageUrl(val url: String)
    data class ChatGPTResponse(val choices: List<Choice>)
    data class Choice(val message: Message)
}