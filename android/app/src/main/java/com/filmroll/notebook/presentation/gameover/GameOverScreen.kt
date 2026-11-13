package com.filmroll.notebook.presentation.gameover

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filmroll.notebook.R
import com.filmroll.notebook.core.di.ServiceLocator
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.core.theme.*
import com.filmroll.notebook.core.ui.*
import com.filmroll.notebook.domain.model.RollStatus
import com.filmroll.notebook.presentation.dialog.ConfirmationDialog

@Composable
fun GameOverRoute(route: Screen.GameOver, onEvent: (UiEvent) -> Unit) {
    val model: GameOverViewModel = viewModel(key = route.toString(), factory = ServiceLocator.factory(route))
    val state by model.state.collectAsStateWithLifecycle()
    UiEffects(model.events, onEvent)
    BackHandler { model.back() }
    GameOverScreen(state, model::back, model::menu, model::replay, model::export, model::continueLogging, model::edit, model::confirm, model::markDeveloped, model::retry)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onBack: () -> Unit, onMenu: () -> Unit, onReplay: () -> Unit, onExport: () -> Unit, onContinue: () -> Unit, onEdit: (Int) -> Unit, onConfirm: (Boolean) -> Unit, onDevelop: () -> Unit, onRetry: () -> Unit) {
    val progress by animateFloatAsState(state.statistics.progress, tween(180), label = "summaryProgress")
    NotebookPage(dark = true, result = true) {
        NotebookTopBar(R.string.summary_title, onBack, dark = true)
        when {
            state.loading -> LoadingMessage()
            state.loadError -> { StatusMessage(stringResource(R.string.storage_error), true); SecondaryButton(R.string.retry, onRetry) }
            state.roll == null -> { StatusMessage(stringResource(R.string.missing_roll)); SecondaryButton(R.string.menu, onMenu) }
            else -> {
                val roll = state.roll
                val status = rollStatus(roll)
                NotebookSurface(Modifier.fillMaxWidth().semantics { stateDescription = status; liveRegion = LiveRegionMode.Polite }, dark = true) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(roll.title, style = MaterialTheme.typography.headlineMedium)
                            Text(stringResource(R.string.film_metadata, roll.film, roll.iso), style = MaterialTheme.typography.bodyMedium, color = SecondaryLavender)
                            Text(roll.cameraName ?: stringResource(R.string.no_camera), style = MaterialTheme.typography.bodyMedium, color = SecondaryLavender)
                        }
                        Image(painterResource(R.drawable.sprite_4), null, Modifier.size(80.dp), contentScale = ContentScale.Fit)
                    }
                    if (roll.isSample) Text(stringResource(R.string.sample), style = MaterialTheme.typography.labelSmall, color = SafelightAccent)
                    Text(stringResource(R.string.frame_progress, roll.frames.size, roll.capacity), style = MaterialTheme.typography.bodyLarge, color = SafelightAccent)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = SafelightAccent, trackColor = SecondaryLavender.copy(alpha = 0.2f))
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.created_date, displayDate(roll.createdAt)), style = MaterialTheme.typography.labelSmall, color = SecondaryLavender)
                }
                if (roll.frames.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(R.string.recorded, stringResource(R.string.number_value, roll.frames.size), Modifier.weight(1f))
                        val minimum = state.statistics.minimumAperture.orEmpty()
                        val maximum = state.statistics.maximumAperture.orEmpty()
                        StatCard(R.string.aperture, if (minimum == maximum) apertureText(minimum) else stringResource(R.string.aperture_range, minimum, maximum), Modifier.weight(1f))
                    }
                    state.statistics.mostUsedShutter?.let { Text(stringResource(R.string.most_used, shutterText(it)), color = SecondaryLavender, style = MaterialTheme.typography.bodyMedium) }
                } else EmptyNotebookState(R.string.no_frames, R.string.no_frames_helper)
                if (state.saveError) { StatusMessage(stringResource(R.string.save_error), true); SecondaryButton(R.string.retry_save, onDevelop, dark = true, enabled = !state.updating) }
                if (state.developedFeedback) StatusMessage(stringResource(R.string.developed_success), success = true)
                if (state.updating) LoadingMessage()
                if (roll.frames.isEmpty()) Text(stringResource(R.string.export_empty), color = SecondaryLavender, style = MaterialTheme.typography.bodyMedium)
                PrimaryButton(R.string.export, onExport, enabled = roll.frames.isNotEmpty() && !state.updating)
                if (roll.status == RollStatus.ACTIVE) {
                    if (state.canContinue) SecondaryButton(R.string.continue_logging, onContinue, Modifier.fillMaxWidth(), enabled = !state.updating, dark = true)
                    SecondaryButton(R.string.mark_developed, { onConfirm(true) }, Modifier.fillMaxWidth(), enabled = state.canDevelop && !state.updating, dark = true)
                }
                SecondaryButton(R.string.play_again, onReplay, Modifier.fillMaxWidth(), enabled = !state.updating, dark = true)
                SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), enabled = !state.updating, dark = true, icon = painterResource(R.drawable.icon_home))
                roll.frames.forEach { frame -> FrameRow(frame, if (roll.status == RollStatus.ACTIVE && !state.updating) ({ onEdit(frame.number) }) else null) }
            }
        }
    }
    if (state.confirmDeveloped) state.roll?.let { ConfirmationDialog(R.string.develop_title, stringResource(R.string.develop_message, it.frames.size, it.capacity), R.string.mark_developed, onDevelop, { onConfirm(false) }, state.updating) }
}
