package com.lobsterclawe.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lobsterclawe.BuildConfig
import com.lobsterclawe.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Gray50,
        bottomBar = {
            BottomAppBar(containerColor = White) {
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("Save Profile")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gray900)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Taste Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Teal)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Cuisines", fontWeight = FontWeight.Medium)
            val allCuisines = listOf("South Indian", "North Indian", "Bengali", "Punjabi", "Street Food", "Continental")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allCuisines.forEach { cuisine ->
                    FilterChip(
                        selected = viewModel.cuisines.contains(cuisine),
                        onClick = {
                            viewModel.cuisines = if (viewModel.cuisines.contains(cuisine)) {
                                viewModel.cuisines - cuisine
                            } else {
                                viewModel.cuisines + cuisine
                            }
                        },
                        label = { Text(cuisine) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Dietary Goal", fontWeight = FontWeight.Medium)
            val goals = listOf("Eat Healthy", "Save Money", "Cook Faster", "Explore New Dishes")
            Column {
                goals.forEach { goal ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = viewModel.dietaryGoal == goal,
                            onClick = { viewModel.dietaryGoal = goal },
                            colors = RadioButtonDefaults.colors(selectedColor = Teal)
                        )
                        Text(goal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Household Size: ${viewModel.householdSize}", fontWeight = FontWeight.Medium)
            Slider(
                value = viewModel.householdSize.toFloat(),
                onValueChange = { viewModel.householdSize = it.toInt() },
                valueRange = 1f..8f,
                steps = 6,
                colors = SliderDefaults.colors(thumbColor = Teal, activeTrackColor = Teal)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Gateway", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Teal)
            Spacer(modifier = Modifier.height(8.dp))
            Text("URL: ${BuildConfig.OPENCLAW_GATEWAY_URL}", color = Gray500, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { viewModel.testConnection() },
                    colors = ButtonDefaults.buttonColors(containerColor = Gray200),
                    enabled = !viewModel.isTestingGateway
                ) {
                    Text("Test Connection", color = Gray900)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = viewModel.gatewayStatus,
                    color = if (viewModel.gatewayStatus == "Connected") TealText else if (viewModel.gatewayStatus.startsWith("Failed")) Color.Red else Gray500
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("App Info", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Teal)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Version: 1.0", color = Gray500)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
