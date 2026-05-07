package com.lobsterclawe.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lobsterclawe.data.SavedRecipe
import com.lobsterclawe.data.SavedRecipeDao
import com.lobsterclawe.network.Recipe
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SavedViewModel(private val dao: SavedRecipeDao) : ViewModel() {
    val savedRecipes: Flow<List<SavedRecipe>> = dao.getAll()

    fun save(recipe: Recipe) {
        viewModelScope.launch {
            val savedRecipe = SavedRecipe(
                id = recipe.id,
                title = recipe.title,
                summary = recipe.summary,
                fullJson = Gson().toJson(recipe)
            )
            dao.save(savedRecipe)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun isSaved(id: String): Flow<Boolean> = dao.isSaved(id).map { it > 0 }
}
