package com.filmroll.notebook.presentation.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.filmroll.notebook.domain.model.*
import com.filmroll.notebook.presentation.dialog.UnsavedChangesDialog

@Composable
fun GameRoute(route: Screen.Game, onEvent: (UiEvent) -> Unit) {
    val model: GameViewModel = viewModel(key = route.toString(), factory = ServiceLocator.factory(route))
    val state by model.state.collectAsStateWithLifecycle()
    UiEffects(model.events, onEvent)
    BackHandler { model.back() }
    DisposableEffect(Unit) { model.resume(); onDispose { model.pause() } }
    GameScreen(state, model::back, model::summary, model::save, model::note, model::picker, model::aperture, model::shutter, model::step, model::saveDraft, model::discard, model::keepEditing, model::retry)
}

@Composable
fun GameScreen(state: GameUiState, onBack: () -> Unit, onSummary: () -> Unit, onSave: () -> Unit, onNote: (String) -> Unit, onPicker: (String?) -> Unit, onAperture: (String) -> Unit, onShutter: (String) -> Unit, onStep: (Boolean, Int) -> Unit, onSaveDraft: () -> Unit, onDiscard: () -> Unit, onKeepEditing: () -> Unit, onRetry: () -> Unit) {
    NotebookPage(dark = true) {
        NotebookTopBar(R.string.frame_log, onBack, dark = true, pause = true)
        when {
            state.loading -> LoadingMessage()
            state.loadError -> { StatusMessage(stringResource(R.string.storage_error), true); SecondaryButton(R.string.retry, onRetry) }
            state.roll == null -> { StatusMessage(stringResource(R.string.missing_roll)); SecondaryButton(R.string.menu, onBack) }
            else -> {
                val roll = state.roll
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(roll.title, color = NotebookBackground, style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.film_metadata, roll.film, roll.iso), color = SecondaryLavender, style = MaterialTheme.typography.bodyMedium)
                        Text(roll.cameraName ?: stringResource(R.string.no_camera), color = SecondaryLavender, style = MaterialTheme.typography.bodyMedium)
                    }
                    Image(painterResource(R.drawable.sprite_3), null, Modifier.size(64.dp), contentScale = ContentScale.Fit)
                }
                SecondaryButton(R.string.view_summary, onSummary, Modifier.fillMaxWidth(), enabled = !state.saving, dark = true)
                if (state.savedFrame != null) StatusMessage(stringResource(R.string.frame_saved, state.savedFrame), success = true)
                if (state.saveError) StatusMessage(stringResource(R.string.save_error), true)
                if (!state.canSave) {
                    StatusMessage(stringResource(if (roll.status == RollStatus.DEVELOPED) R.string.read_only else R.string.full_roll, roll.capacity))
                } else {
                    NotebookSurface(Modifier.fillMaxWidth()) {
                        AnimatedContent(targetState = state.currentFrameNumber, label = "frameNumber") { number ->
                            Text(stringResource(if (state.editing) R.string.editing_frame else R.string.frame_position, number, roll.capacity), style = MaterialTheme.typography.headlineSmall, color = PrimaryIndigo, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        }
                        ExposureStepper(R.string.aperture, apertureText(state.draft.aperture), state.draft.aperture != ExposureOptions.apertures.first(), state.draft.aperture != ExposureOptions.apertures.last(), { onStep(true, -1) }, { onStep(true, 1) }, { onPicker("aperture") }, !state.saving)
                        HorizontalDivider(color = SecondaryLavender.copy(alpha = 0.5f))
                        ExposureStepper(R.string.shutter, shutterText(state.draft.shutter), state.draft.shutter != ExposureOptions.shutters.first(), state.draft.shutter != ExposureOptions.shutters.last(), { onStep(false, -1) }, { onStep(false, 1) }, { onPicker("shutter") }, !state.saving)
                        if (state.draft.shutter == "BULB") Text(stringResource(R.string.bulb_helper), style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(state.draft.note, onNote, Modifier.fillMaxWidth().heightIn(min = 104.dp), enabled = !state.saving, label = { Text(stringResource(R.string.frame_note)) }, placeholder = { Text(stringResource(R.string.note_placeholder)) }, maxLines = 5, supportingText = { Text(stringResource(R.string.note_remaining, 500 - state.draft.note.length)) })
                        Text(stringResource(R.string.saved_frames, roll.frames.size), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(if (state.editing) R.string.save_edit_helper else R.string.save_frame_helper), style = MaterialTheme.typography.bodyMedium)
                        if (state.saving) { CircularProgressIndicator(Modifier.size(24.dp)); Text(stringResource(R.string.saving)) }
                        PrimaryButton(if (state.saveError) R.string.retry_save else if (state.editing) R.string.save_changes else R.string.save_frame, onSave, enabled = !state.saving)
                        if (state.editing) SecondaryButton(R.string.cancel_edit, onBack, Modifier.fillMaxWidth(), enabled = !state.saving)
                    }
                }
            }
        }
    }
    when (state.picker) {
        "aperture" -> ValuePickerSheet(R.string.aperture, ExposureOptions.apertures.map { PickerItem(it, apertureText(it)) }, state.draft.aperture, onAperture, { onPicker(null) })
        "shutter" -> ValuePickerSheet(R.string.shutter, ExposureOptions.shutters.map { PickerItem(it, shutterText(it)) }, state.draft.shutter, onShutter, { onPicker(null) })
    }
    if (state.unsavedDialog) UnsavedChangesDialog(onSaveDraft, onDiscard, onKeepEditing, state.saving)
}
