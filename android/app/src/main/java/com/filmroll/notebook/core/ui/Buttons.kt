package com.filmroll.notebook.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.filmroll.notebook.core.theme.*

@Composable
fun PrimaryButton(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, spring(dampingRatio = 0.8f, stiffness = 500f), label = "buttonScale")
    val shape = RoundedCornerShape(16.dp)
    Button(
        onClick = onClick, enabled = enabled, interactionSource = interaction,
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).scale(scale).shadow(if (enabled) 6.dp else 0.dp, shape).clip(shape)
            .background(Brush.linearGradient(if (enabled) listOf(SafelightAccent, SecondaryLavender) else listOf(SecondaryLavender.copy(alpha = 0.4f), NotebookBackground.copy(alpha = 0.5f)))),
        shape = shape, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = FilmBlack, disabledContainerColor = Color.Transparent, disabledContentColor = FilmBlack.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) { Text(stringResource(label), style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SecondaryButton(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, dark: Boolean = false, icon: Painter? = null) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (dark) SecondaryLavender.copy(alpha = 0.5f) else PrimaryIndigo.copy(alpha = 0.35f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (dark) PrimaryIndigo.copy(alpha = 0.3f) else NotebookBackground.copy(alpha = 0.7f), contentColor = if (dark) NotebookBackground else PrimaryIndigo, disabledContentColor = FilmBlack.copy(alpha = 0.45f)), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        icon?.let { Image(it, contentDescription = null, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(8.dp)) }
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
    }
}
