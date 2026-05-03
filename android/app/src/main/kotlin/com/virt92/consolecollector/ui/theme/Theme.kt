package com.virt92.consolecollector.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF1F2937),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFD1D5DB),
    error = Color(0xFFEF4444),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    secondary = Color(0xFFEAB308),
    onSecondary = Color(0xFF1F2937),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    error = Color(0xFFDC2626),
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Black),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun ConsoleCollectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
