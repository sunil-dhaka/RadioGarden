package com.example.radiogarden

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.radiogarden.ui.RadioViewModel
import com.example.radiogarden.ui.screens.home.HomeScreen
import com.example.radiogarden.ui.theme.RadioGardenTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RadioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadioGardenTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
