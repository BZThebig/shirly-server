package com.example.shirly

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class ShirlyChatRequest(
    val sessionId: String,
    val message: String,
    val language: String? = "he"
)

data class ShirlyAction(
    val type: String,
    val appId: String? = null,
    val url: String? = null,
    val title: String? = null,
    val body: String? = null
)

data class ShirlyReply(
    val replyText: String,
    val actions: List<ShirlyAction>,
    val mood: String
)

data class ShirlyChatResponse(
    val success: Boolean,
    val reply: ShirlyReply?
)

interface ShirlyApiService {
    @POST("api/v1/shirly/chat")
    fun chat(@Body body: ShirlyChatRequest): Call<ShirlyChatResponse>
}

object ShirlyApiClient {

    // *** חשוב: לשנות לכתובת השרת שלך ***
    private const val BASE_URL = "http://10.0.2.2:4000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ShirlyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ShirlyApiService::class.java)
    }
}