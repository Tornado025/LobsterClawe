package com.lobsterclawe.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lobsterclawe.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, onFinish: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Gray50) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (viewModel.currentStep) {
                0 -> CuisineStep(viewModel)
                1 -> GoalStep(viewModel)
                2 -> SpiceStep(viewModel)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.nextStep(onFinish) },
                modifier = Modifier.fillMaxWidth(),
                enabled = when (viewModel.currentStep) {
                    0 -> viewModel.selectedCuisines.isNotEmpty()
                    1 -> viewModel.selectedGoal.isNotEmpty()
                    2 -> true
                    else -> false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(if (viewModel.currentStep == 2) "Finish" else "Next")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CuisineStep(viewModel: OnboardingViewModel) {
    val cuisines = listOf("South Indian", "North Indian", "Bengali", "Punjabi", "Street Food", "Continental")
    Column {
        Text("What cuisines do you love?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cuisines.forEach { cuisine ->
                FilterChip(
                    selected = viewModel.selectedCuisines.contains(cuisine),
                    onClick = {
                        viewModel.selectedCuisines = if (viewModel.selectedCuisines.contains(cuisine)) {
                            viewModel.selectedCuisines - cuisine
                        } else {
                            viewModel.selectedCuisines + cuisine
                        }
                    },
                    label = { Text(cuisine) }
                )
            }
        }
    }
}

@Composable
fun GoalStep(viewModel: OnboardingViewModel) {
    val goals = listOf("Eat Healthy", "Save Money", "Cook Faster", "Explore New Dishes")
    Column {
        Text("What is your dietary goal?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { goal ->
                FilterChip(
                    selected = viewModel.selectedGoal == goal,
                    onClick = { viewModel.selectedGoal = goal },
                    label = { Text(goal) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SpiceStep(viewModel: OnboardingViewModel) {
    val spiceLevels = listOf("Mild", "Medium", "Hot", "Very Hot")
    Column {
        Text("Preferred spice level?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            spiceLevels.forEach { level ->
                FilterChip(
                    selected = viewModel.selectedSpice == level,
                    onClick = { viewModel.selectedSpice = level },
                    label = { Text(level) }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Household size", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = viewModel.householdSize.toFloat(),
            onValueChange = { viewModel.householdSize = it.toInt() },
            valueRange = 1f..8f,
            steps = 6,
            colors = SliderDefaults.colors(thumbColor = Teal, activeTrackColor = Teal)
        )
        Text("${viewModel.householdSize} persons", modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
