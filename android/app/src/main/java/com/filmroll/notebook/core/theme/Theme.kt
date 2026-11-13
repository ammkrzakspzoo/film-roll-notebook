package com.filmroll.notebook.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NotebookColors = lightColorScheme(
    primary = PrimaryIndigo, onPrimary = NotebookBackground,
    secondary = SecondaryLavender, onSecondary = FilmBlack,
    tertiary = SafelightAccent, onTertiary = FilmBlack,
    background = NotebookBackground, onBackground = FilmBlack,
    surface = NotebookBackground, onSurface = FilmBlack,
    surfaceVariant = SecondaryLavender.copy(alpha = 0.28f), onSurfaceVariant = FilmBlack,
    outline = PrimaryIndigo, outlineVariant = SecondaryLavender,
    error = PrimaryIndigo, onError = NotebookBackground,
    errorContainer = SecondaryLavender, onErrorContainer = FilmBlack,
    surfaceTint = PrimaryIndigo, inverseSurface = FilmBlack, inverseOnSurface = NotebookBackground
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NotebookColors, typography = NotebookTypography, content = content)
}
