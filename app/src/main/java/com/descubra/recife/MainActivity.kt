package com.descubra.recife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.descubra.recife.ui.navigation.DescubraRecifeNavHost
import com.descubra.recife.ui.theme.DescubraRecifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DescubraRecifeApp).container
        setContent {
            val escolhido by container.modoEscuro.collectAsStateWithLifecycle()
            val escuro = escolhido ?: isSystemInDarkTheme()
            DescubraRecifeTheme(darkTheme = escuro) {
                DescubraRecifeNavHost(container = container, isDarkMode = escuro)
            }
        }
    }
}
