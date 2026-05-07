package com.lobsterclawe.network

import com.google.gson.Gson
import com.lobsterclawe.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenClawClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val baseUrl = BuildConfig.OPENCLAW_GATEWAY_URL
    private val token = BuildConfig.OPENCLAW_GATEWAY_TOKEN

    suspend fun message(prompt: String): String = withContext(Dispatchers.IO) {
        val requestBody = mapOf("message" to prompt, "stream" to false)
        val request = Request.Builder()
            .url("$baseUrl/api/agents/default/message")
            .addHeader("Authorization", "Bearer $token")
            .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw IOException("Empty response body")
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: $body")
            }
            gson.fromJson(body, OpenClawResponse::class.java).response
        }
    }

    suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()
            client.newCall(request).execute().use { response -> response.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    private data class OpenClawResponse(val response: String)
}
