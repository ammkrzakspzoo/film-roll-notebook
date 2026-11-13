package com.filmroll.notebook.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.filmroll.notebook.domain.model.ExposureOptions
import com.filmroll.notebook.presentation.dialog.*

@Composable
fun SettingsRoute(onEvent: (UiEvent) -> Unit) {
    val model: SettingsViewModel = viewModel(factory = ServiceLocator.factory(Screen.Settings))
    val state by model.state.collectAsStateWithLifecycle()
    UiEffects(model.events, onEvent)
    BackHandler { model.back() }
    SettingsScreen(state, model::back, model::customFilm, model::capacity, model::picker, model::choose, model::haptics, { model.save() }, { model.save(true) }, model::restoreDialog, model::restore, model::discard, model::keepEditing, model::retry)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onCustomFilm: (String) -> Unit, onCapacity: (Int) -> Unit, onPicker: (String?) -> Unit, onChoose: (String) -> Unit, onHaptics: (Boolean) -> Unit, onSave: () -> Unit, onSaveAndBack: () -> Unit, onRestoreDialog: (Boolean) -> Unit, onRestore: () -> Unit, onDiscard: () -> Unit, onKeepEditing: () -> Unit, onRetry: () -> Unit) {
    NotebookPage {
        NotebookTopBar(R.string.defaults_title, onBack)
        Text(stringResource(R.string.defaults_helper), style = MaterialTheme.typography.bodyLarge, color = PrimaryIndigo)
        when {
            state.loading -> LoadingMessage()
            state.loadError -> { StatusMessage(stringResource(R.string.storage_error), true); SecondaryButton(R.string.retry, onRetry) }
            else -> {
                NotebookSurface(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.roll_defaults), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
                    RollFormFields(state.draft, state.cameras, state.fieldError, !state.saving, onCustomFilm, onCapacity, { onPicker(it) })
                }
                NotebookSurface(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.frame_defaults), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
                    ChoiceField(R.string.aperture, apertureText(state.aperture), { onPicker("aperture") }, !state.saving)
                    ChoiceField(R.string.shutter, shutterText(state.shutter), { onPicker("shutter") }, !state.saving)
                    Text(stringResource(R.string.feedback), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
                    val hapticState = stringResource(if (state.haptics) R.string.enabled else R.string.disabled)
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(value = state.haptics, enabled = !state.saving, role = Role.Switch, onValueChange = onHaptics).semantics { stateDescription = hapticState }, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.haptic_feedback), Modifier.weight(1f))
                        Switch(state.haptics, onCheckedChange = null, enabled = !state.saving)
                    }
                }
                if (state.saved) StatusMessage(stringResource(R.string.defaults_saved), success = true)
                if (state.saveError) StatusMessage(stringResource(R.string.save_error), true)
                if (state.saving) LoadingMessage()
                PrimaryButton(if (state.saveError) R.string.retry_save else R.string.save_defaults, onSave, enabled = !state.saving)
                SecondaryButton(R.string.restore_defaults, { onRestoreDialog(true) }, Modifier.fillMaxWidth(), enabled = !state.saving)
            }
        }
    }
    RollFormPicker(state.picker, state.draft, state.cameras, onChoose, { onPicker(null) })
    when (state.picker) {
        "aperture" -> ValuePickerSheet(R.string.aperture, ExposureOptions.apertures.map { PickerItem(it, apertureText(it)) }, state.aperture, onChoose, { onPicker(null) })
        "shutter" -> ValuePickerSheet(R.string.shutter, ExposureOptions.shutters.map { PickerItem(it, shutterText(it)) }, state.shutter, onChoose, { onPicker(null) })
    }
    if (state.restoreDialog) ConfirmationDialog(R.string.restore_defaults_title, stringResource(R.string.restore_defaults_message), R.string.restore_defaults, onRestore, { onRestoreDialog(false) }, state.saving)
    if (state.unsavedDialog) UnsavedChangesDialog(onSaveAndBack, onDiscard, onKeepEditing, state.saving, settings = true)
}
