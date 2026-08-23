package com.pocketrulebooks.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

val Paper = Color(0xFFF3EAD9)
val Cream = Color(0xFFFFFDF8)
val Ink = Color(0xFF241910)
val Muted = Color(0xFF7A6B5C)
val Line = Color(0xFFE6D7C3)
val Burgundy = Color(0xFF9C3B2D)
val BurgundySoft = Color(0xFFF6E0D8)
val Sage = Color(0xFF2F5D4A)

private val Colors = lightColorScheme(
    primary = Burgundy,
    onPrimary = Cream,
    secondary = Sage,
    onSecondary = Cream,
    background = Paper,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFE4D2),
    onSurfaceVariant = Muted,
    outline = Line,
    error = Burgundy,
)

private val Type = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        color = Ink,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = Ink,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = Ink,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        color = Ink,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = Ink,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Ink,
    ),
)

@Composable
fun PocketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        typography = Type,
        content = content,
    )
}
