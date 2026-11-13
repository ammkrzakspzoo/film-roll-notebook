package com.filmroll.notebook.presentation.cameras

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.filmroll.notebook.domain.model.CameraBody
import com.filmroll.notebook.presentation.dialog.ConfirmationDialog

@Composable
fun CamerasRoute(route: Screen.Cameras, onEvent: (UiEvent) -> Unit) {
    val model: CamerasViewModel = viewModel(key = route.toString(), factory = ServiceLocator.factory(route))
    val state by model.state.collectAsStateWithLifecycle()
    UiEffects(model.events, onEvent)
    BackHandler { model.back() }
    CamerasScreen(state, model::back, { model.edit() }, model::edit, model::name, model::notes, model::save, model::closeEditor, model::askDelete, model::delete, model::cancelDelete, model::discard, model::keepEditing, model::retry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamerasScreen(state: CamerasUiState, onBack: () -> Unit, onAdd: () -> Unit, onEdit: (CameraBody) -> Unit, onName: (String) -> Unit, onNotes: (String) -> Unit, onSave: () -> Unit, onCloseEditor: () -> Unit, onAskDelete: (CameraBody) -> Unit, onDelete: () -> Unit, onCancelDelete: () -> Unit, onDiscard: () -> Unit, onKeepEditing: () -> Unit, onRetry: () -> Unit) {
    NotebookPage(dark = true) {
        NotebookTopBar(R.string.cameras_title, onBack, dark = true)
        NotebookSurface(Modifier.fillMaxWidth(), dark = true) {
            Image(painterResource(R.drawable.sprite_5), stringResource(R.string.camera_illustration), Modifier.size(120.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
            Text(stringResource(R.string.camera_intro), style = MaterialTheme.typography.titleLarge)
        }
        when {
            state.loading -> LoadingMessage()
            state.loadError -> { StatusMessage(stringResource(R.string.storage_error), true); SecondaryButton(R.string.retry, onRetry) }
            else -> {
                PrimaryButton(R.string.add_camera, onAdd, enabled = !state.saving)
                if (state.saveError) StatusMessage(stringResource(R.string.save_error), true)
                if (state.cameras.isEmpty()) EmptyNotebookState(R.string.empty_cameras, R.string.empty_cameras_helper)
                state.cameras.forEach { camera ->
                    NotebookSurface(Modifier.fillMaxWidth()) {
                        Text(camera.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                        if (camera.isSample) Text(stringResource(R.string.sample), color = PrimaryIndigo, style = MaterialTheme.typography.labelSmall)
                        if (camera.notes.isNotBlank()) Text(camera.notes, style = MaterialTheme.typography.bodyMedium)
                        val editDescription = stringResource(R.string.camera_action, stringResource(R.string.edit), camera.name)
                        val deleteDescription = stringResource(R.string.camera_action, stringResource(R.string.delete), camera.name)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SecondaryButton(R.string.edit, { onEdit(camera) }, Modifier.weight(1f).semantics { contentDescription = editDescription }, enabled = !state.saving)
                            SecondaryButton(R.string.delete, { onAskDelete(camera) }, Modifier.weight(1f).semantics { contentDescription = deleteDescription }, enabled = !state.saving)
                        }
                    }
                }
            }
        }
    }
    if (state.editorOpen) ModalBottomSheet(onDismissRequest = onCloseEditor, containerColor = NotebookBackground) {
        Column(Modifier.fillMaxWidth().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.camera_editor), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(state.name, onName, Modifier.fillMaxWidth(), enabled = !state.saving, label = { Text(stringResource(R.string.camera_name)) }, isError = state.fieldError, supportingText = { if (state.fieldError) Text(stringResource(R.string.camera_name_error)) }, singleLine = true)
            OutlinedTextField(state.notes, onNotes, Modifier.fillMaxWidth(), enabled = !state.saving, label = { Text(stringResource(R.string.camera_notes)) }, maxLines = 4)
            if (state.saveError) StatusMessage(stringResource(R.string.save_error), true)
            PrimaryButton(if (state.saveError) R.string.retry_save else R.string.save_camera, onSave, enabled = !state.saving)
            SecondaryButton(R.string.cancel, onCloseEditor, Modifier.fillMaxWidth(), enabled = !state.saving)
        }
    }
    state.pendingDelete?.let { ConfirmationDialog(R.string.delete_camera_title, stringResource(R.string.delete_camera_message, it.name), R.string.delete, onDelete, onCancelDelete, state.saving) }
    if (state.discardDialog) AlertDialog(onDismissRequest = onKeepEditing, title = { Text(stringResource(R.string.unsaved_title)) }, text = { Text(stringResource(R.string.camera_unsaved)) }, confirmButton = { SecondaryButton(R.string.discard, onDiscard) }, dismissButton = { SecondaryButton(R.string.keep_editing, onKeepEditing) })
}
