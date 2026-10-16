package com.descubra.recife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.descubra.recife.ui.navigation.DescubraRecifeNavHost
import com.descubra.recife.ui.theme.DescubraRecifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DescubraRecifeApp).container
        setContent {
            val escuro = isSystemInDarkTheme()
            DescubraRecifeTheme(darkTheme = escuro) {
                DescubraRecifeNavHost(container = container, isDarkMode = escuro)
            }
        }
    }
}
