package com.filmroll.notebook.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.filmroll.notebook.R
import com.filmroll.notebook.core.theme.*

@Composable
fun ScreenBackdrop(dark: Boolean = false, splash: Boolean = false, result: Boolean = false) {
    Image(painterResource(R.drawable.bg_game), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    val colors = when {
        splash -> listOf(FilmBlack.copy(alpha = 0.98f), FilmBlack.copy(alpha = 0.86f), PrimaryIndigo.copy(alpha = 0.94f))
        result -> listOf(PrimaryIndigo.copy(alpha = 0.96f), FilmBlack.copy(alpha = 0.92f), FilmBlack.copy(alpha = 0.99f))
        dark -> listOf(FilmBlack.copy(alpha = 0.98f), PrimaryIndigo.copy(alpha = 0.93f), FilmBlack.copy(alpha = 0.98f))
        else -> listOf(NotebookBackground.copy(alpha = 0.98f), NotebookBackground.copy(alpha = 0.96f), SecondaryLavender.copy(alpha = 0.94f))
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
}

@Composable
fun NotebookPage(dark: Boolean = false, scroll: Boolean = true, result: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        ScreenBackdrop(dark = dark, result = result)
        Column(
            Modifier.widthIn(max = 720.dp).fillMaxSize().safeDrawingPadding().imePadding().padding(horizontal = 20.dp).align(Alignment.TopCenter)
                .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = content
        )
    }
}
