package com.example.agendaparaprofessores.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ============ PALETA ROXA ORIGINAL (intacta) ============ */
val Roxo         = Color(0xFF6C3FD1)
val RoxoMedio    = Color(0xFF8B5CF6)
val RoxoEscuro   = Color(0xFF2B0A70)
val RoxoProfundo = Color(0xFF4B1FA8)
val RoxoClaro    = Color(0xFFE9DDFF)
val RoxoSuave    = Color(0xFFF7F3FF)

/* Cores de apoio (chips de dificuldade e status dos planos) */
val VerdeFundo     = Color(0xFFDCF5E3); val VerdeTexto     = Color(0xFF16663A)
val AmbarFundo     = Color(0xFFFFF0CF); val AmbarTexto     = Color(0xFF7A5310)
val VermelhoFundo  = Color(0xFFFFE0DE); val VermelhoTexto  = Color(0xFF93231C)

/* ============ GRADIENTES (reutilizáveis) ============ */
val GradienteVioleta     = Brush.linearGradient(listOf(VioletaProfundo, Violeta, VioletaClaro))
val GradienteFundo       = Brush.verticalGradient(listOf(FundoLavandaTopo, FundoLavandaBase))
val GradienteFundoEscuro = Brush.verticalGradient(listOf(Color(0xFF150E22), Color(0xFF0B0714)))

private val CoresClaro = lightColorScheme(
    primary = Violeta,                onPrimary = Color.White,
    primaryContainer = VioletaWash,   onPrimaryContainer = VioletaProfundo,
    secondary = VioletaClaro,         onSecondary = Color.White,
    secondaryContainer = VioletaWash, onSecondaryContainer = VioletaProfundo,
    tertiary = Magenta,               onTertiary = Color.White,
    background = FundoLavanda,        onBackground = TextoPrincipal,
    surface = SuperficieBranca,       onSurface = TextoPrincipal,
    surfaceVariant = SuperficieBaixa, onSurfaceVariant = TextoSecundario,
    outline = TextoSuave,             outlineVariant = BordaSuave,
    error = Color(0xFFB3261E),        onError = Color.White,
)

private val CoresEscuro = darkColorScheme(
    primary = VioletaClaro,             onPrimary = VioletaProfundo,
    primaryContainer = Color(0xFF3B1D6E), onPrimaryContainer = VioletaWash,
    secondary = Color(0xFFCBB4FF),      onSecondary = Color(0xFF2A1069),
    secondaryContainer = Color(0xFF3E2A73), onSecondaryContainer = VioletaWash,
    tertiary = Color(0xFFE879F9),       onTertiary = Color(0xFF3B0764),
    background = Color(0xFF120E1B),     onBackground = Color(0xFFEDE7F6),
    surface = Color(0xFF1B1526),        onSurface = Color(0xFFEDE7F6),
    surfaceVariant = Color(0xFF2A2435), onSurfaceVariant = Color(0xFFC8C1D6),
    outline = Color(0xFF7A7387),        outlineVariant = Color(0xFF3A3346),
    error = Color(0xFFF2B8B5),          onError = Color(0xFF601410),
)

private val FormasApp = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val TipografiaApp = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)

@Composable
fun AgendaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CoresEscuro else CoresClaro,
        typography = TipografiaApp,
        shapes = FormasApp,
        content = content
    )
}
