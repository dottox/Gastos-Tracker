package com.example.misgastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.misgastos.ui.MisGastosApp
import com.example.misgastos.ui.AppSplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showSplash by rememberSaveable { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1_000)
                showSplash = false
            }

            if (showSplash) {
                AppSplashScreen()
            } else {
                MisGastosApp(viewModel = viewModel)
            }
        }
    }
}
