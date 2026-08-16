package com.example.goodpet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.goodpet.ui.PetPraiseScreen
import com.example.goodpet.ui.components.LoadingScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isTtsReady && viewModel.initializationError == null
            }
        }
        super.onCreate(savedInstanceState)
        
        viewModel.initTts(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!viewModel.isTtsReady) {
                        LoadingScreen(
                            initializationError = viewModel.initializationError,
                            onReset = { viewModel.resetAppData() }
                        )
                    } else {
                        PetPraiseScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
