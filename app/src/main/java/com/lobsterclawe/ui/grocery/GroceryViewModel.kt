package com.lobsterclawe.ui.grocery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lobsterclawe.network.OpenClawClient
import com.lobsterclawe.network.PriceResult
import com.lobsterclawe.network.UiState
import kotlinx.coroutines.launch

class GroceryViewModel(private val client: OpenClawClient) : ViewModel() {
    var uiState by mutableStateOf<UiState<List<PriceResult>>>(UiState.Success(emptyList()))

    fun fetchPrices(ingredients: List<String>) {
        viewModelScope.launch {
            uiState = UiState.Loading
            val results = mutableListOf<PriceResult>()
            try {
                ingredients.forEach { ingredient ->
                    val prompt = "Use the LobsterClawe grocery skill. Find prices in Blinkit, Zepto, Instamart for: $ingredient. Return only JSON."
                    val response = client.message(prompt)
                    val json = extractJson(response)
                    val priceResult = Gson().fromJson(json, PriceResult::class.java)
                    results.add(priceResult)
                }
                uiState = UiState.Success(results)
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun extractJson(raw: String): String {
        return Regex("\\{[\\s\\S]*\\}").find(raw)?.value ?: raw
    }
}
