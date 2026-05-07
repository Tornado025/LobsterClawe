package com.lobsterclawe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lobsterclawe.ui.navigation.AppNavigation
import com.lobsterclawe.ui.theme.LobsterClawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LobsterClawTheme {
                AppNavigation()
            }
        }
    }
}
