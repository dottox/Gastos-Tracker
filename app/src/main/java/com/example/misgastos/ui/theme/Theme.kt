package com.example.misgastos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.misgastos.data.local.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PineDark,
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005143),
    onPrimaryContainer = Color(0xFFB6F1DF),
    secondary = CoralDark,
    onSecondary = Color(0xFF5F1110),
    secondaryContainer = Color(0xFF8E302D),
    onSecondaryContainer = Color(0xFFFFDAD5),
    tertiary = SkyDark,
    onTertiary = Color(0xFF00344D),
    tertiaryContainer = Color(0xFF1D506F),
    onTertiaryContainer = Color(0xFFC8E6FF),
    background = CanvasDark,
    onBackground = InkDark,
    surface = CanvasDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceMutedDark,
    onSurfaceVariant = Color(0xFFBECAC4),
    outline = Color(0xFF88948E)
)

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB4F0DF),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD5),
    onSecondaryContainer = Color(0xFF410005),
    tertiary = Sky,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8E6FF),
    onTertiaryContainer = Color(0xFF001E30),
    background = Canvas,
    onBackground = Ink,
    surface = Canvas,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = Color(0xFF3E4944),
    outline = Color(0xFF6F7974)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun MisGastosTheme(
    themeMode: ThemeMode = ThemeMode.SISTEMA,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
        ThemeMode.CLARO -> false
        ThemeMode.OSCURO -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
