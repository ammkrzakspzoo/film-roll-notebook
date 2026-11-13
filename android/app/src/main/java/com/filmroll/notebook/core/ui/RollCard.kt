package com.filmroll.notebook.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.filmroll.notebook.R
import com.filmroll.notebook.core.theme.*
import com.filmroll.notebook.domain.model.*

@Composable
fun rollStatus(roll: FilmRoll): String = when {
    roll.developedAt != null -> stringResource(R.string.developed_date, displayDate(roll.developedAt))
    roll.frames.size == roll.capacity -> stringResource(R.string.ready_to_develop)
    else -> stringResource(R.string.in_progress)
}

@Composable
fun RollCard(roll: FilmRoll, selected: Boolean, onOpen: () -> Unit, onDelete: () -> Unit) {
    val progress by animateFloatAsState(roll.frames.size.toFloat() / roll.capacity, tween(180), label = "recordedProgress")
    val status = rollStatus(roll)
    NotebookSurface(Modifier.fillMaxWidth().semantics { this.selected = selected; stateDescription = status }) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(roll.title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            if (roll.isSample) Text(stringResource(R.string.sample), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
        }
        if (selected) Text(stringResource(R.string.selected_roll), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
        Text(stringResource(R.string.film_metadata, roll.film, roll.iso), style = MaterialTheme.typography.bodyMedium)
        Text(roll.cameraName ?: stringResource(R.string.no_camera), style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = PrimaryIndigo, trackColor = SecondaryLavender.copy(alpha = 0.4f))
        Text(stringResource(R.string.frame_progress, roll.frames.size, roll.capacity), style = MaterialTheme.typography.labelSmall)
        Text(status, style = MaterialTheme.typography.bodyMedium, color = PrimaryIndigo)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(R.string.open, onOpen, Modifier.weight(1f))
            SecondaryButton(R.string.delete, onDelete, Modifier.weight(1f))
        }
    }
}
