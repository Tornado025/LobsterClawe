package com.lobsterclawe.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.lobsterclawe.network.Recipe
import com.lobsterclawe.ui.components.RecipeCard
import com.lobsterclawe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(viewModel: SavedViewModel, onRecipeClick: (Recipe) -> Unit) {
    val savedRecipes by viewModel.savedRecipes.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Gray50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Saved Recipes", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(modifier = Modifier.height(24.dp))

            if (savedRecipes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No saved recipes yet", color = Gray500)
                }
            } else {
                LazyColumn {
                    items(savedRecipes, key = { it.id }) { saved ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            LaunchedEffect(saved.id) {
                                viewModel.delete(saved.id)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        ) {
                            val recipe = Gson().fromJson(saved.fullJson, Recipe::class.java)
                            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
                        }
                    }
                }
            }
        }
    }
}
