package com.weartools.weekdayutccomp.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.Typography

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
    content: @Composable () -> Unit
) {
    androidx.wear.compose.material3.MaterialTheme(
        colorScheme = appColorScheme,
        typography = appTypography,
        content = content
    )
}

val appTypography = Typography()