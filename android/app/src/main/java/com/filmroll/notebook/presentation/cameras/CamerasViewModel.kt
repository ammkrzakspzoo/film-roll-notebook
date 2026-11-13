package com.filmroll.notebook.presentation.cameras

import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.domain.model.CameraBody
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.presentation.app.ActionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CamerasViewModel(private val repository: NotebookRepository, private val route: Screen.Cameras) : ActionModel() {
    private val _state = MutableStateFlow(CamerasUiState())
    val state = _state.asStateFlow()
    private var baselineName = ""
    private var baselineNotes = ""
    init { viewModelScope.launch { repository.state.collect { stored -> _state.update { it.copy(loading = stored.loading, loadError = stored.failed, cameras = stored.notebook.cameras) } } } }
    fun edit(camera: CameraBody? = null) {
        baselineName = camera?.name ?: ""
        baselineNotes = camera?.notes ?: ""
        _state.update { it.copy(editorOpen = true, editingId = camera?.id, name = baselineName, notes = baselineNotes, dirty = false, fieldError = false, saveError = false) }
    }
    fun name(value: String) { _state.update { it.copy(name = value.take(60), dirty = value != baselineName || it.notes != baselineNotes, fieldError = false) } }
    fun notes(value: String) { _state.update { it.copy(notes = value.take(160), dirty = it.name != baselineName || value != baselineNotes) } }
    fun closeEditor() {
        if (_state.value.saving) return
        if (_state.value.dirty) _state.update { it.copy(discardDialog = true) } else _state.update { it.copy(editorOpen = false) }
    }
    fun keepEditing() { _state.update { it.copy(discardDialog = false) } }
    fun discard() { _state.update { it.copy(editorOpen = false, discardDialog = false, dirty = false) } }
    fun back() {
        when {
            _state.value.saving -> return
            _state.value.discardDialog -> keepEditing()
            _state.value.pendingDelete != null -> cancelDelete()
            _state.value.editorOpen -> closeEditor()
            else -> send(UiEvent.Back)
        }
    }
    fun save() {
        val current = _state.value
        if (current.saving) return
        if (current.name.isBlank() || current.cameras.any { it.id != current.editingId && it.name.equals(current.name.trim(), true) }) {
            _state.update { it.copy(fieldError = true) }; return
        }
        _state.update { it.copy(saving = true, saveError = false) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            val camera = repository.saveCamera(current.editingId, current.name, current.notes)
            _state.update { it.copy(saving = false, editorOpen = false, dirty = false) }
            if (route.returnToNewRoll && current.editingId == null) emit(UiEvent.CameraCreated(camera.id))
        }
    }
    fun askDelete(camera: CameraBody) { _state.update { it.copy(pendingDelete = camera) } }
    fun cancelDelete() { _state.update { it.copy(pendingDelete = null) } }
    fun delete() {
        val camera = _state.value.pendingDelete ?: return
        if (_state.value.saving) return
        _state.update { it.copy(saving = true) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            repository.deleteCamera(camera.id)
            _state.update { it.copy(saving = false, pendingDelete = null, saveError = false) }
        }
    }
    fun retry() { perform({ _state.update { it.copy(loadError = true) } }) { repository.load() } }
}
