package com.anna.gamingcafe.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    background = BackBg,
    surface = SurfaceCard,
    surfaceVariant = SurfaceElevated,
    primary = NeonCyan,
    secondary = NeonPink,
    tertiary = StarGold,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = BackBg,
    error = ErrorRed,
    outline = EdgeBorder
)

private val AnnaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val AnnaTypography = Typography(
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
    labelSmall = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
)

@Composable
fun AnnaGamingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes = AnnaShapes,
        typography = AnnaTypography,
        content = content
    )
}
