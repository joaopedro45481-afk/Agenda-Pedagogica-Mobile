package com.example.agendaparaprofessores.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ============ PALETA ROXA ============ */
val Roxo         = Color(0xFF6C3FD1)   // cor principal
val RoxoMedio    = Color(0xFF8B5CF6)   // gradiente
val RoxoEscuro   = Color(0xFF2B0A70)   // topo do gradiente / textos sobre roxo claro
val RoxoProfundo = Color(0xFF4B1FA8)
val RoxoClaro    = Color(0xFFE9DDFF)   // fundos de destaque
val RoxoSuave    = Color(0xFFF7F3FF)   // fundo geral

/* Cores de apoio (só para os chips de dificuldade) */
val VerdeFundo     = Color(0xFFDCF5E3); val VerdeTexto     = Color(0xFF16663A)
val AmbarFundo     = Color(0xFFFFF0CF); val AmbarTexto     = Color(0xFF7A5310)
val VermelhoFundo  = Color(0xFFFFE0DE); val VermelhoTexto  = Color(0xFF93231C)

private val CoresClaro = lightColorScheme(
    primary = Roxo,               onPrimary = Color.White,
    primaryContainer = RoxoClaro, onPrimaryContainer = RoxoEscuro,
    secondary = RoxoMedio,        onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE3FF), onSecondaryContainer = RoxoEscuro,
    tertiary = Color(0xFFB4579C), onTertiary = Color.White,
    background = RoxoSuave,       onBackground = Color(0xFF1B1526),
    surface = Color.White,        onSurface = Color(0xFF1B1526),
    surfaceVariant = Color(0xFFEFE9F7), onSurfaceVariant = Color(0xFF585166),
    outline = Color(0xFF9A93A8),  outlineVariant = Color(0xFFDDD6E8),
    error = Color(0xFFB3261E),    onError = Color.White,
)

private val CoresEscuro = darkColorScheme(
    primary = Color(0xFFCDB6FF),  onPrimary = Color(0xFF38199B),
    primaryContainer = Color(0xFF4B2AA8), onPrimaryContainer = RoxoClaro,
    secondary = Color(0xFFC9B4FF), onSecondary = Color(0xFF2A1069),
    secondaryContainer = Color(0xFF3E2A73), onSecondaryContainer = RoxoClaro,
    background = Color(0xFF130F1C), onBackground = Color(0xFFE9E3F3),
    surface = Color(0xFF1B1626),   onSurface = Color(0xFFE9E3F3),
    surfaceVariant = Color(0xFF2A2435), onSurfaceVariant = Color(0xFFC8C1D6),
    outline = Color(0xFF7A7387),   outlineVariant = Color(0xFF3A3346),
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
