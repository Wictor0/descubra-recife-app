package com.descubra.recife.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta tirada da tela de FAQ (laranja de destaque + fundos claros/escuros).
val Laranja = Color(0xFFD97736)
val BegeClaro = Color(0xFFF1E6DF)
val TextoPrimarioClaro = Color(0xFF1E293B)
val TextoSecundarioClaro = Color(0xFF64748B)

private val Claro = lightColorScheme(
    primary = Laranja,
    onPrimary = Color.White,
    secondaryContainer = BegeClaro,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    surfaceVariant = Color(0xFFFAF8F5),
    onBackground = TextoPrimarioClaro,
    onSurface = TextoPrimarioClaro,
    onSurfaceVariant = TextoSecundarioClaro
)

private val Escuro = darkColorScheme(
    primary = Laranja,
    onPrimary = Color.White,
    secondaryContainer = Color(0xFF2C2C2C),
    background = Color(0xFF121212),
    surface = Color(0xFF181818),
    surfaceVariant = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFA0A0A0)
)

@Composable
fun DescubraRecifeTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Escuro else Claro, content = content)
}
