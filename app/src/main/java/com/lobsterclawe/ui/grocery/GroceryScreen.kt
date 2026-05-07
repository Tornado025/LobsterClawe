package com.lobsterclawe.ui.grocery

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lobsterclawe.network.UiState
import com.lobsterclawe.ui.components.PriceRow
import com.lobsterclawe.ui.theme.*

@Composable
fun GroceryScreen(viewModel: GroceryViewModel) {
    val context = LocalContext.current
    val ingredients = GroceryStore.pendingIngredients

    Scaffold(
        containerColor = Gray50,
        bottomBar = {
            if (ingredients.isNotEmpty()) {
                BottomAppBar(containerColor = White) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.fetchPrices(ingredients) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal)
                        ) {
                            Text("Fetch Prices")
                        }
                        OutlinedButton(
                            onClick = { GroceryStore.clear() },
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)
                        ) {
                            Text("Clear List", color = Gray500)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Grocery List", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(modifier = Modifier.height(24.dp))

            if (ingredients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Add ingredients from a recipe", color = Gray500)
                }
            } else {
                when (val state = viewModel.uiState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Teal)
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn {
                            if (state.data.isNotEmpty()) {
                                items(state.data) { priceResult ->
                                    PriceRow(
                                        priceResult = priceResult,
                                        onStoreClick = { url ->
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        }
                                    )
                                    HorizontalDivider(color = Gray200)
                                }
                            } else {
                                items(ingredients) { ingredient ->
                                    Text(
                                        text = ingredient,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = Gray900
                                    )
                                    HorizontalDivider(color = Gray200)
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
