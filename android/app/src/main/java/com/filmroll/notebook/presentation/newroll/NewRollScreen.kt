package com.filmroll.notebook.presentation.newroll

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filmroll.notebook.R
import com.filmroll.notebook.core.di.ServiceLocator
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.core.theme.*
import com.filmroll.notebook.core.ui.*
import com.filmroll.notebook.presentation.dialog.UnsavedChangesDialog

@Composable
fun NewRollRoute(route: Screen.NewRoll, returnedCameraId: String?, onCameraConsumed: () -> Unit, onEvent: (UiEvent) -> Unit) {
    val model: NewRollViewModel = viewModel(key = route.toString(), factory = ServiceLocator.factory(route))
    val state by model.state.collectAsStateWithLifecycle()
    UiEffects(model.events, onEvent)
    BackHandler { model.back() }
    LaunchedEffect(returnedCameraId) { returnedCameraId?.let { model.cameraReturned(it); onCameraConsumed() } }
    NewRollScreen(state, model::back, model::title, model::customFilm, model::capacity, model::picker, model::choose, model::manageCameras, model::create, model::restoreDraft, model::startFresh, model::saveDraft, model::discard, model::keepEditing, model::retry)
}

@Composable
fun NewRollScreen(state: NewRollUiState, onBack: () -> Unit, onTitle: (String) -> Unit, onCustomFilm: (String) -> Unit, onCapacity: (Int) -> Unit, onPicker: (String?) -> Unit, onChoose: (String) -> Unit, onManageCameras: () -> Unit, onCreate: () -> Unit, onRestore: () -> Unit, onFresh: () -> Unit, onSaveDraft: () -> Unit, onDiscard: () -> Unit, onKeepEditing: () -> Unit, onRetry: () -> Unit) {
    NotebookPage {
        NotebookTopBar(R.string.new_roll_title, onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.new_roll_intro), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = PrimaryIndigo)
            Image(painterResource(R.drawable.sprite_1), null, Modifier.size(88.dp), contentScale = ContentScale.Fit)
        }
        when {
            state.loading -> LoadingMessage()
            state.loadError -> { StatusMessage(stringResource(R.string.storage_error), true); SecondaryButton(R.string.retry, onRetry) }
            else -> {
                if (state.saveError) StatusMessage(stringResource(R.string.save_error), true)
                NotebookSurface(Modifier.fillMaxWidth()) {
                    OutlinedTextField(state.draft.title, onTitle, Modifier.fillMaxWidth(), enabled = !state.saving, label = { Text(stringResource(R.string.roll_title)) }, singleLine = true)
                    RollFormFields(state.draft, state.cameras, state.fieldError, !state.saving, onCustomFilm, onCapacity, { onPicker(it) })
                    if (state.cameraMissing) Text(stringResource(R.string.camera_missing), style = MaterialTheme.typography.bodyMedium)
                    SecondaryButton(R.string.manage_cameras, onManageCameras, Modifier.fillMaxWidth(), enabled = !state.saving)
                }
                Text(stringResource(R.string.local_only), style = MaterialTheme.typography.bodyMedium)
                if (state.saving) LoadingMessage()
                PrimaryButton(if (state.saveError) R.string.retry_create else R.string.create_roll, onCreate, enabled = !state.saving && !state.draftAvailable)
            }
        }
    }
    RollFormPicker(state.picker, state.draft, state.cameras, onChoose, { onPicker(null) })
    if (state.draftAvailable) AlertDialog(onDismissRequest = onBack, title = { Text(stringResource(R.string.draft_available)) }, text = { Text(stringResource(R.string.draft_available_helper)) }, confirmButton = { SecondaryButton(R.string.restore_draft, onRestore, enabled = !state.saving) }, dismissButton = { SecondaryButton(R.string.start_fresh, onFresh, enabled = !state.saving) })
    if (state.unsavedDialog) UnsavedChangesDialog(onSaveDraft, onDiscard, onKeepEditing, state.saving)
}
