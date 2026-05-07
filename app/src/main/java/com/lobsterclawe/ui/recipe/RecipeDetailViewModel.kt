package com.lobsterclawe.ui.recipe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lobsterclawe.network.*
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val openRouterClient: OpenRouterClient
) : ViewModel() {
    var recipe by mutableStateOf<Recipe?>(null)
    var isCustomising by mutableStateOf(false)

    fun setInitialRecipe(initialRecipe: Recipe) {
        if (recipe == null) {
            recipe = initialRecipe
        }
    }

    fun customise(modifier: String) {
        val currentRecipe = recipe ?: return
        viewModelScope.launch {
            isCustomising = true
            try {
                val prompt = "Modify this recipe to be $modifier. Keep the same JSON structure. Return ONLY valid JSON, no prose:\n${Gson().toJson(currentRecipe)}"
                val response = openRouterClient.chat(listOf(Message("user", prompt)))
                val cleanedResponse = extractJson(response)
                val newRecipe = Gson().fromJson(cleanedResponse, Recipe::class.java)
                recipe = newRecipe.copy(id = currentRecipe.id)
            } catch (e: Exception) {
                // Error handling
            } finally {
                isCustomising = false
            }
        }
    }

    private fun extractJson(raw: String): String {
        return Regex("\\{[\\s\\S]*\\}").find(raw)?.value ?: raw
    }
}
