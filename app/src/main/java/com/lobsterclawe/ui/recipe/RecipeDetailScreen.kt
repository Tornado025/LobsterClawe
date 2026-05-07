package com.lobsterclawe.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lobsterclawe.ui.grocery.GroceryStore
import com.lobsterclawe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    savedViewModel: com.lobsterclawe.ui.saved.SavedViewModel,
    onBack: () -> Unit,
    onGetIngredients: () -> Unit
) {
    val recipe = viewModel.recipe ?: return
    val isSaved by savedViewModel.isSaved(recipe.id).collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isSaved) {
                            savedViewModel.delete(recipe.id)
                        } else {
                            savedViewModel.save(recipe)
                        }
                    }) {
                        Icon(
                            if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Teal else Gray500
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = White,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        GroceryStore.addAll(recipe.ingredients)
                        onGetIngredients()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("Get Ingredients")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Brush.linearGradient(listOf(Teal, TealLight)))
                ) {
                    AsyncImage(
                        model = null,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = recipe.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("${recipe.cookTimeMinutes} mins", color = Gray500)
                        Text("${recipe.servings} servings", color = Gray500)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NutritionChip("Cal", "${recipe.nutrition.calories}")
                        NutritionChip("Prot", "${recipe.nutrition.proteinG}g")
                        NutritionChip("Carb", "${recipe.nutrition.carbsG}g")
                        NutritionChip("Fat", "${recipe.nutrition.fatG}g")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Customise", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        recipe.tags.forEach { tag ->
                            var selected by remember { mutableStateOf(false) }
                            FilterChip(
                                selected = selected,
                                onClick = { 
                                    selected = !selected
                                    viewModel.customise(tag)
                                },
                                label = { Text(tag) }
                            )
                        }
                    }
                    
                    if (viewModel.isCustomising) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Teal)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Ingredients", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(recipe.ingredients) { ingredient ->
                Text(
                    text = "• $ingredient",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    color = Gray900
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Steps",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recipe.steps.indices.toList()) { index ->
                Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text("${index + 1}.", fontWeight = FontWeight.Bold, color = Teal)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(recipe.steps[index], color = Gray900)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun NutritionChip(label: String, value: String) {
    Surface(
        color = Gray50,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Gray500)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray900)
        }
    }
}
