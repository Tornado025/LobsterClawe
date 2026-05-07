package com.lobsterclawe.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lobsterclawe.data.PrefsRepository
import com.lobsterclawe.network.Message
import com.lobsterclawe.network.OpenRouterClient
import com.lobsterclawe.network.Recipe
import com.lobsterclawe.network.UiState
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
    private val prefs: PrefsRepository,
    private val client: OpenRouterClient
) : ViewModel() {
    var uiState by mutableStateOf<UiState<List<Recipe>>>(UiState.Loading)
    var selectedMood by mutableStateOf("Quick Cook")
    val recipeCache = mutableMapOf<String, Recipe>()

    fun fetchRecipes() {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val cuisines = prefs.cuisines.joinToString(", ")
                val prompt = """
                    Suggest 3 Indian recipes for a household of ${prefs.householdSize}.
                    Mood: $selectedMood (quick_cook = under 25 min, weekend_mode = 60-90 min,
                                 budget_friendly = under ₹150 total, healthy = low calorie).
                    Cuisines they like: $cuisines.
                    Dietary goal: ${prefs.dietaryGoal}.
                    Spice level: ${prefs.spiceLevel}.

                    Return ONLY a valid JSON array with exactly 3 objects. No prose. No markdown fences.
                    Each object:
                    {
                      "title": "...",
                      "summary": "one sentence",
                      "cookTimeMinutes": 25,
                      "servings": ${prefs.householdSize},
                      "nutrition": { "calories": 320, "proteinG": 12, "carbsG": 45, "fatG": 8 },
                      "ingredients": ["500g basmati rice", "2 onions", ...],
                      "steps": ["Step 1 text", "Step 2 text", ...],
                      "tags": ["vegan", "gluten-free", "low-carb"]
                    }
                """.trimIndent()

                val response = client.chat(listOf(Message("user", prompt)))
                var recipes = parseRecipes(response)
                
                if (recipes.isEmpty()) {
                    recipes = parseRecipes("[$response]")
                }

                if (recipes.isNotEmpty()) {
                    recipes.forEach { recipeCache[it.id] = it }
                    uiState = UiState.Success(recipes)
                } else {
                    uiState = UiState.Error("Failed to parse recipes")
                }
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun parseRecipes(json: String): List<Recipe> {
        return try {
            val list = Gson().fromJson(json, Array<Recipe>::class.java).toList()
            list.map { it.copy(id = UUID.randomUUID().toString()) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
