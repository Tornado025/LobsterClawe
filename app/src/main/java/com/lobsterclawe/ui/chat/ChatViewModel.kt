package com.lobsterclawe.ui.chat

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lobsterclawe.network.ChatMessage
import com.lobsterclawe.network.Message
import com.lobsterclawe.network.OpenRouterClient

class ChatViewModel(private val client: OpenRouterClient) : ViewModel() {
    val messages = mutableStateListOf<ChatMessage>()
    var isStreaming by mutableStateOf(false)
    var currentInput by mutableStateOf("")

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        messages.add(ChatMessage("user", text))
        currentInput = ""
        isStreaming = true
        
        val apiMessages = messages.map { Message(it.role, it.content) }
        var assistantResponse = ""
        
        messages.add(ChatMessage("assistant", ""))
        val assistantIndex = messages.size - 1

        client.streamChat(
            messages = apiMessages,
            onToken = { token ->
                Handler(Looper.getMainLooper()).post {
                    assistantResponse += token
                    messages[assistantIndex] = ChatMessage("assistant", assistantResponse)
                }
            },
            onDone = {
                Handler(Looper.getMainLooper()).post {
                    isStreaming = false
                }
            }
        )
    }
}
