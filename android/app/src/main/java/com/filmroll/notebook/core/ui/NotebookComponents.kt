package com.filmroll.notebook.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.filmroll.notebook.R
import com.filmroll.notebook.core.theme.*
import com.filmroll.notebook.domain.model.ExposureFrame
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotebookSurface(modifier: Modifier = Modifier, dark: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = if (dark) PrimaryIndigo.copy(alpha = 0.90f) else NotebookBackground.copy(alpha = 0.97f), contentColor = if (dark) NotebookBackground else FilmBlack, border = BorderStroke(1.dp, SecondaryLavender.copy(alpha = if (dark) 0.35f else 0.75f)), shadowElevation = 4.dp) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun NotebookTopBar(title: Int, onBack: () -> Unit, dark: Boolean = false, pause: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(R.string.back, onBack, dark = dark, icon = if (pause) painterResource(R.drawable.icon_pause) else null)
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, color = if (dark) NotebookBackground else FilmBlack, modifier = Modifier.weight(1f).semantics { heading() })
    }
}

@Composable
fun StatCard(label: Int, value: String, modifier: Modifier = Modifier) {
    NotebookSurface(modifier) {
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
        Text(value, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun StatusMessage(message: String, error: Boolean = false, success: Boolean = false) {
    Surface(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, shape = RoundedCornerShape(12.dp), color = if (error) SecondaryLavender else NotebookBackground, border = BorderStroke(if (error) 2.dp else 1.dp, PrimaryIndigo)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedContent(targetState = success, transitionSpec = { (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.85f)) togetherWith fadeOut(tween(100)) }, label = "statusMark") { completed ->
                Canvas(Modifier.size(20.dp)) {
                    if (completed) {
                        drawLine(PrimaryIndigo, Offset(size.width * 0.12f, size.height * 0.52f), Offset(size.width * 0.4f, size.height * 0.8f), 2.dp.toPx(), StrokeCap.Round)
                        drawLine(PrimaryIndigo, Offset(size.width * 0.4f, size.height * 0.8f), Offset(size.width * 0.92f, size.height * 0.18f), 2.dp.toPx(), StrokeCap.Round)
                    } else {
                        drawCircle(PrimaryIndigo.copy(alpha = 0.15f))
                        drawLine(FilmBlack, Offset(size.width * 0.5f, size.height * 0.25f), Offset(size.width * 0.5f, size.height * 0.55f), 2.dp.toPx(), StrokeCap.Round)
                        drawCircle(FilmBlack, 1.2.dp.toPx(), Offset(size.width * 0.5f, size.height * 0.75f))
                    }
                }
            }
            Text(message, Modifier.weight(1f), color = FilmBlack, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EmptyNotebookState(title: Int, helper: Int) {
    NotebookSurface(Modifier.fillMaxWidth()) {
        Image(painterResource(R.drawable.sprite_5), null, modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(helper), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LoadingMessage() {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CircularProgressIndicator(Modifier.size(28.dp), color = PrimaryIndigo, strokeWidth = 3.dp)
        Text(stringResource(R.string.opening_notebook), color = PrimaryIndigo)
    }
}

@Composable
fun apertureText(value: String): String = stringResource(R.string.aperture_value, value)

@Composable
fun shutterText(value: String): String = if (value == "BULB") stringResource(R.string.bulb) else stringResource(R.string.shutter_value, value)

fun displayDate(timestamp: Long): String = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(timestamp))

@Composable
fun FrameRow(frame: ExposureFrame, onEdit: (() -> Unit)? = null) {
    NotebookSurface(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.frame_number, frame.number), style = MaterialTheme.typography.labelLarge, color = PrimaryIndigo, modifier = Modifier.weight(1f))
            onEdit?.let { SecondaryButton(R.string.edit, it) }
        }
        Text(stringResource(R.string.exposure_pair, apertureText(frame.aperture), shutterText(frame.shutter)), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleLarge)
        Text(frame.note.takeIf { it.isNotBlank() } ?: stringResource(R.string.no_note), style = MaterialTheme.typography.bodyLarge)
    }
}
