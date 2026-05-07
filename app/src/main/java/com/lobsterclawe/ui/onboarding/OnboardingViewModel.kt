package com.lobsterclawe.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lobsterclawe.data.PrefsRepository

class OnboardingViewModel(private val prefs: PrefsRepository) : ViewModel() {
    var currentStep by mutableStateOf(0)
    var selectedCuisines by mutableStateOf(setOf<String>())
    var selectedGoal by mutableStateOf("")
    var selectedSpice by mutableStateOf("Medium")
    var householdSize by mutableStateOf(2)

    fun nextStep(onComplete: () -> Unit) {
        if (currentStep < 2) {
            currentStep++
        } else {
            saveAndFinish()
            onComplete()
        }
    }

    private fun saveAndFinish() {
        prefs.cuisines = selectedCuisines.toList()
        prefs.dietaryGoal = selectedGoal
        prefs.spiceLevel = selectedSpice
        prefs.householdSize = householdSize
        prefs.onboardingDone = true
    }
}
