package com.lobsterclawe.ui.grocery

import androidx.compose.runtime.mutableStateListOf

object GroceryStore {
    val pendingIngredients = mutableStateListOf<String>()

    fun addAll(items: List<String>) {
        val newItems = items.filter { it !in pendingIngredients }
        pendingIngredients.addAll(newItems)
    }

    fun clear() {
        pendingIngredients.clear()
    }
}
