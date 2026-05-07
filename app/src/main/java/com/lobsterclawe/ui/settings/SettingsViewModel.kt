package com.lobsterclawe.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lobsterclawe.data.PrefsRepository
import com.lobsterclawe.network.OpenClawClient
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: PrefsRepository,
    private val openClawClient: OpenClawClient
) : ViewModel() {
    var cuisines by mutableStateOf(prefs.cuisines)
    var dietaryGoal by mutableStateOf(prefs.dietaryGoal)
    var spiceLevel by mutableStateOf(prefs.spiceLevel)
    var householdSize by mutableStateOf(prefs.householdSize)

    var gatewayStatus by mutableStateOf("Not Tested")
    var isTestingGateway by mutableStateOf(false)

    fun save() {
        prefs.cuisines = cuisines
        prefs.dietaryGoal = dietaryGoal
        prefs.spiceLevel = spiceLevel
        prefs.householdSize = householdSize
    }

    fun testConnection() {
        viewModelScope.launch {
            isTestingGateway = true
            gatewayStatus = "Connecting..."
            try {
                val ok = openClawClient.ping()
                gatewayStatus = if (ok) "Connected ✓" else "Failed: gateway returned non-2xx"
            } catch (e: Exception) {
                gatewayStatus = "Failed: ${e.message}"
            } finally {
                isTestingGateway = false
            }
        }
    }
}
