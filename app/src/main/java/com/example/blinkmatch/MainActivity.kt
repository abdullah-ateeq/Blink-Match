package com.example.blinkmatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blinkmatch.ui.navigation.BlinkMatchNavHost
import com.example.blinkmatch.ui.theme.BlinkMatchTheme
import com.example.blinkmatch.viewmodel.BlinkMatchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BlinkMatchViewModel = viewModel()
            val preferences by viewModel.preferences.collectAsState()

            BlinkMatchTheme(themeMode = preferences.themeMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BlinkMatchNavHost(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
