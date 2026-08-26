package com.weartools.weekdayutccomp.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import androidx.wear.compose.material3.dynamicColorScheme

val appColorScheme = ColorScheme(
    primary = primary,
    primaryDim = primaryDim,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,

    secondary = secondary,
    secondaryDim = secondaryDim,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,

    tertiary = tertiary,
    tertiaryDim = tertiaryDim,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,

    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainer = surfaceContainer,
    surfaceContainerLow = surfaceContainerLow,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,

    outline = outline,
    outlineVariant = outlineVariant,

    background = background,
    onBackground = onBackground,

    error = error,
    errorDim = errorDim,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
)

@Composable
fun ComplicationsSuiteTheme(
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = if (useDynamicColor) dynamicColorScheme(context) ?: appColorScheme else appColorScheme,
        typography = appTypography,
        content = content
    )
}

val appTypography = Typography()