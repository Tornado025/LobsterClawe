package com.lobsterclawe.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lobsterclawe.ui.theme.*

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Gray50,
        bottomBar = {
            ChatInput(
                value = viewModel.currentInput,
                onValueChange = { viewModel.currentInput = it },
                onSend = { viewModel.sendMessage(it) },
                isStreaming = viewModel.isStreaming
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (viewModel.messages.isEmpty()) {
                SuggestedPrompts(onPromptClick = { viewModel.sendMessage(it) })
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.messages) { message ->
                        ChatBubble(message.role, message.content)
                    }
                    if (viewModel.isStreaming && viewModel.messages.last().content.isEmpty()) {
                        item {
                            Text("...", color = Gray500, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(role: String, content: String) {
    val isUser = role == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) Teal else White,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 12.dp
            ),
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, Gray200),
            tonalElevation = if (isUser) 0.dp else 2.dp
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) White else Gray900,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
    isStreaming: Boolean
) {
    Surface(
        color = White,
        tonalElevation = 8.dp,
        modifier = Modifier.imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Gray50,
                    unfocusedContainerColor = Gray50,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onSend(value) },
                enabled = value.isNotBlank() && !isStreaming,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Teal)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestedPrompts(onPromptClick: (String) -> Unit) {
    val prompts = listOf(
        "What can I cook with paneer?",
        "Suggest a 20-min breakfast",
        "Low-carb dinner ideas",
        "Substitute for heavy cream?"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Try asking...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.forEach { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { onPromptClick(prompt) },
                    label = { Text(prompt) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = White),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Gray200, enabled = true, selected = false)
                )
            }
        }
    }
}
