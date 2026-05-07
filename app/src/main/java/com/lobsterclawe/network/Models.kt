package com.lobsterclawe.network

import java.util.UUID

data class Message(val role: String, val content: String)

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val cookTimeMinutes: Int,
    val servings: Int,
    val nutrition: Nutrition,
    val ingredients: List<String>,
    val steps: List<String>,
    val tags: List<String>
)

data class Nutrition(
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
)

data class PriceResult(
    val item: String,
    val prices: List<StorePrice>,
    val searched_at: String
)

data class StorePrice(
    val store: String,
    val price_inr: Int,
    val product_name: String,
    val url: String
)

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
