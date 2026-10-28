package com.descubra.recife.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.descubra.recife.R

// Paleta do protótipo (laranja da barra, pêssego da splash/cadastro, marrom da máscara).
val Laranja = Color(0xFFD97736)
/** Regra do app: todo texto e ícone sobre fundo laranja é branco. */
val SobreLaranja = Color.White
val Pessego = Color(0xFFFFD8B1)
val PessegoClaro = Color(0xFFFFF1E6)
val Marrom = Color(0xFF7A3E00)
val CinzaCampo = Color(0xFFD9D9D9)
val CinzaBotao = Color(0xFFB0B0B0)
val Coral = Color(0xFFFF6B5B)
val BegeClaro = Color(0xFFF1E6DF)
val TextoPrimarioClaro = Color(0xFF1E293B)
val TextoSecundarioClaro = Color(0xFF64748B)

/** Poppins é a fonte usada no protótipo. */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

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

private val Tipografia = Typography().let { base ->
    fun TextStyle.poppins() = copy(fontFamily = Poppins)
    Typography(
        displayLarge = base.displayLarge.poppins(),
        displayMedium = base.displayMedium.poppins(),
        displaySmall = base.displaySmall.poppins(),
        headlineLarge = base.headlineLarge.poppins(),
        headlineMedium = base.headlineMedium.poppins(),
        headlineSmall = base.headlineSmall.poppins(),
        titleLarge = base.titleLarge.poppins(),
        titleMedium = base.titleMedium.poppins(),
        titleSmall = base.titleSmall.poppins(),
        bodyLarge = base.bodyLarge.poppins(),
        bodyMedium = base.bodyMedium.poppins(),
        bodySmall = base.bodySmall.poppins(),
        labelLarge = base.labelLarge.poppins(),
        labelMedium = base.labelMedium.poppins(),
        labelSmall = base.labelSmall.poppins()
    )
}

@Composable
fun DescubraRecifeTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Escuro else Claro, typography = Tipografia, content = content)
}
