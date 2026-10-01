package com.yunfei.log.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF6750A4)
val PurpleContainer = Color(0xFFEADDFF)
val PurpleSoft = Color(0xFFE8DEF8)
val SurfaceLow = Color(0xFFF7F2FA)
val SurfaceMid = Color(0xFFF3EDF7)
val SurfaceHigh = Color(0xFFECE6F0)
val OutlineVariantColor = Color(0xFFCAC4D0)
val TextMuted = Color(0xFF49454F)

val OkGreen = Color(0xFF146C2E)
val OkGreenBg = Color(0xFFC4EED0)
val WarnAmber = Color(0xFF7A5900)
val WarnAmberBg = Color(0xFFFFDF9E)
val OtRed = Color(0xFFB3261E)
val OtRedBg = Color(0xFFFFDAD6)
val HolidayOrange = Color(0xFF8F4C00)
val HolidayOrangeBg = Color(0xFFFFE0C2)
val MakeupBlueBg = Color(0xFFD7E3FF)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = Color(0xFF21005D),
    secondaryContainer = PurpleSoft,
    onSecondaryContainer = Color(0xFF1D192B),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = SurfaceMid,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF79747E),
    outlineVariant = OutlineVariantColor,
    error = OtRed
)

@Composable
fun YunfeiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
