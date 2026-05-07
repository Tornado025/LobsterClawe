package com.lobsterclawe.network

import com.google.gson.Gson
import com.lobsterclawe.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class OpenRouterClient {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://openrouter.ai/api/v1/chat/completions"
    private val key = BuildConfig.OPENROUTER_API_KEY

    suspend fun chat(messages: List<Message>, maxTokens: Int = 1500): String = withContext(Dispatchers.IO) {
        val requestBody = mapOf(
            "model" to "openrouter/auto",
            "messages" to messages,
            "max_tokens" to maxTokens,
            "stream" to false
        )

        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("X-Title", "LobsterClawe")
            .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body?.string() ?: throw IOException("Empty body")
            val json = gson.fromJson(body, OpenRouterResponse::class.java)
            json.choices.firstOrNull()?.message?.content ?: throw IOException("No content")
        }
    }

    fun streamChat(messages: List<Message>, onToken: (String) -> Unit, onDone: () -> Unit) {
        val requestBody = mapOf(
            "model" to "openrouter/auto",
            "messages" to messages,
            "stream" to true
        )

        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("X-Title", "LobsterClawe")
            .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onDone()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.use { body ->
                    val source = body.source()
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6)
                            if (data == "[DONE]") {
                                break
                            }
                            try {
                                val chunk = gson.fromJson(data, OpenRouterStreamResponse::class.java)
                                chunk.choices.firstOrNull()?.delta?.content?.let {
                                    if (it.isNotEmpty()) onToken(it)
                                }
                            } catch (e: Exception) {
                                // Ignore parse errors for partial chunks
                            }
                        }
                    }
                }
                onDone()
            }
        })
    }

    private data class OpenRouterResponse(val choices: List<Choice>)
    private data class Choice(val message: Message)
    private data class OpenRouterStreamResponse(val choices: List<StreamChoice>)
    private data class StreamChoice(val delta: Delta)
    private data class Delta(val content: String?)
}
