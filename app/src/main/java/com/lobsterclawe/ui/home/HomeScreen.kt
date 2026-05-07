package com.lobsterclawe.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lobsterclawe.network.UiState
import com.lobsterclawe.ui.components.MoodChip
import com.lobsterclawe.ui.components.RecipeCard
import com.lobsterclawe.ui.components.RecipeSkeleton
import com.lobsterclawe.ui.theme.*
import java.util.*

@Composable
fun HomeScreen(viewModel: HomeViewModel, onRecipeClick: (String) -> Unit) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }

    LaunchedEffect(Unit) {
        if (viewModel.uiState is UiState.Loading) {
            viewModel.fetchRecipes()
        }
    }

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
            Text(
                text = "What's cooking, $greeting?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            MoodSelector(
                selectedMood = viewModel.selectedMood,
                onMoodChange = {
                    viewModel.selectedMood = it
                    viewModel.fetchRecipes()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = viewModel.uiState) {
                is UiState.Loading -> RecipeSkeleton()
                is UiState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(state.data) { recipe ->
                            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                        }
                    }
                }
                is UiState.Error -> {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.fetchRecipes() }, colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodSelector(selectedMood: String, onMoodChange: (String) -> Unit) {
    val moods = listOf("Quick Cook", "Weekend Mode", "Budget Friendly", "Healthy")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(moods) { mood ->
            MoodChip(
                text = mood,
                isSelected = selectedMood == mood,
                onClick = { onMoodChange(mood) }
            )
        }
    }
}
