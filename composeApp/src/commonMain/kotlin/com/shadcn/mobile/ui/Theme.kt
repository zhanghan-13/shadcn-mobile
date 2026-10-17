package com.shadcn.mobile.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Semantic tokens shared by every platform. Inspired by shadcn's zinc palette. */
@Immutable
data class ShadcnColors(
    val background: Color,
    val foreground: Color,
    val muted: Color,
    val mutedForeground: Color,
    val border: Color,
    val input: Color,
    val primary: Color,
    val onPrimary: Color,
    val error: Color,
    val surface: Color,
)

private val LightColors = ShadcnColors(
    background = Color(0xFFFAFAFA),
    foreground = Color(0xFF18181B),
    muted = Color(0xFFF4F4F5),
    mutedForeground = Color(0xFF71717A),
    border = Color(0xFFE4E4E7),
    input = Color(0xFFD4D4D8),
    primary = Color(0xFF18181B),
    onPrimary = Color(0xFFFAFAFA),
    error = Color(0xFFB91C1C),
    surface = Color.White,
)

private val DarkColors = ShadcnColors(
    background = Color(0xFF09090B),
    foreground = Color(0xFFFAFAFA),
    muted = Color(0xFF27272A),
    mutedForeground = Color(0xFFA1A1AA),
    border = Color(0xFF3F3F46),
    input = Color(0xFF52525B),
    primary = Color(0xFFFAFAFA),
    onPrimary = Color(0xFF18181B),
    error = Color(0xFFFCA5A5),
    surface = Color(0xFF18181B),
)

val LocalShadcnColors = staticCompositionLocalOf { LightColors }

object ShadcnTheme {
    val colors: ShadcnColors
        @Composable
        @ReadOnlyComposable
        get() = LocalShadcnColors.current
}

private val ShadcnTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 41.sp, letterSpacing = (-1.3).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.7).sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 27.sp, letterSpacing = (-0.4).sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun ShadcnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val materialColors = if (darkTheme) {
        darkColorScheme(primary = colors.primary, onPrimary = colors.onPrimary, background = colors.background, onBackground = colors.foreground, surface = colors.surface, onSurface = colors.foreground, surfaceVariant = colors.muted, onSurfaceVariant = colors.mutedForeground, outline = colors.border, error = colors.error)
    } else {
        lightColorScheme(primary = colors.primary, onPrimary = colors.onPrimary, background = colors.background, onBackground = colors.foreground, surface = colors.surface, onSurface = colors.foreground, surfaceVariant = colors.muted, onSurfaceVariant = colors.mutedForeground, outline = colors.border, error = colors.error)
    }
    CompositionLocalProvider(LocalShadcnColors provides colors, LocalContentColor provides colors.foreground) {
        MaterialTheme(colorScheme = materialColors, typography = ShadcnTypography, content = content)
    }
}
