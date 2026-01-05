package com.bloodmoon.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BloodMoonColorScheme = darkColorScheme(
    primary = ElectricPink,
    onPrimary = DeepBlack,
    primaryContainer = VampyPurple,
    onPrimaryContainer = PaleLilac,

    secondary = VampyPurple,
    onSecondary = PaleLilac,
    secondaryContainer = DarkMagenta,
    onSecondaryContainer = PaleLilac,

    tertiary = FertileBlue,
    onTertiary = DeepBlack,

    background = DeepBlack,
    onBackground = PaleLilac,

    surface = HellCrimsonShadow,
    onSurface = PaleLilac,
    surfaceVariant = DarkMagenta,
    onSurfaceVariant = PaleLilac,

    error = PeriodRed,
    onError = DeepBlack,

    outline = VampyPurple,
    outlineVariant = DarkMagenta
)

private val MetalModeColorScheme = darkColorScheme(
    primary = MetalHellPink,
    onPrimary = DeepBlack,
    primaryContainer = MetalCrimson,
    onPrimaryContainer = PaleLilac,

    secondary = MetalCrimson,
    onSecondary = PaleLilac,
    secondaryContainer = DarkMagenta,
    onSecondaryContainer = PaleLilac,

    tertiary = MetalHellPink,
    onTertiary = DeepBlack,

    background = DeepBlack,
    onBackground = PaleLilac,

    surface = HellCrimsonShadow,
    onSurface = PaleLilac,
    surfaceVariant = DarkMagenta,
    onSurfaceVariant = PaleLilac,

    error = MetalHellPink,
    onError = DeepBlack,

    outline = MetalCrimson,
    outlineVariant = DarkMagenta
)

@Composable
fun BloodMoonTheme(
    metalMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (metalMode) MetalModeColorScheme else BloodMoonColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepBlack.toArgb()
            window.navigationBarColor = DeepBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
