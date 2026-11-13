package com.filmroll.notebook.presentation.newroll

import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.domain.logic.NotebookValidation
import com.filmroll.notebook.domain.model.*
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.presentation.app.ActionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewRollViewModel(private val repository: NotebookRepository, private val route: Screen.NewRoll) : ActionModel() {
    private val _state = MutableStateFlow(NewRollUiState())
    val state = _state.asStateFlow()
    private var initialized = false
    private var baseline = NewRollDraft()
    private var savedDraft: NewRollDraft? = null
    init {
        viewModelScope.launch {
            repository.state.collect { stored ->
                val notebook = stored.notebook
                if (!stored.loading && !stored.failed && !initialized) {
                    val source = notebook.rolls.find { it.id == route.sourceRollId }
                    val film = source?.film ?: notebook.defaults.film
                    val filmIndex = ExposureOptions.films.indexOf(film).takeIf { it >= 0 } ?: 4
                    val camera = if (source != null) source.cameraId else notebook.defaults.cameraId
                    baseline = NewRollDraft(filmIndex = filmIndex, customFilm = if (filmIndex == 4) film else "", iso = source?.iso ?: notebook.defaults.iso, cameraId = camera?.takeIf { id -> notebook.cameras.any { it.id == id } }, capacity = source?.capacity ?: notebook.defaults.capacity)
                    savedDraft = if (source == null) notebook.newRollDraft else null
                    _state.update { it.copy(draft = baseline, draftAvailable = savedDraft != null, cameraMissing = camera != null && baseline.cameraId == null) }
                    initialized = true
                }
                val removed = _state.value.draft.cameraId?.takeIf { id -> notebook.cameras.none { it.id == id } }
                _state.update { it.copy(loading = stored.loading, loadError = stored.failed, cameras = notebook.cameras, draft = if (removed != null) it.draft.copy(cameraId = null) else it.draft, cameraMissing = it.cameraMissing || removed != null) }
            }
        }
    }
    private fun edit(transform: (NewRollDraft) -> NewRollDraft) {
        if (_state.value.saving) return
        _state.update { val next = transform(it.draft); it.copy(draft = next, dirty = next != baseline, fieldError = false, saveError = false) }
    }
    fun title(value: String) { edit { it.copy(title = value.take(60)) } }
    fun customFilm(value: String) { edit { it.copy(customFilm = value.take(80)) } }
    fun capacity(value: Int) { if (value in listOf(24, 36)) edit { it.copy(capacity = value) } }
    fun picker(value: String?) { _state.update { it.copy(picker = value) } }
    fun choose(value: String) {
        when (_state.value.picker) {
            "film" -> value.toIntOrNull()?.takeIf { it in 0..4 }?.let { index -> edit { it.copy(filmIndex = index, iso = if (!it.isoChanged && index < 4) ExposureOptions.nominalIso[index] else it.iso) } }
            "iso" -> value.toIntOrNull()?.takeIf { it in ExposureOptions.isoValues }?.let { iso -> edit { it.copy(iso = iso, isoChanged = true) } }
            "camera" -> edit { it.copy(cameraId = value.takeIf { it.isNotEmpty() }) }
        }
        picker(null)
    }
    fun cameraReturned(id: String) { edit { it.copy(cameraId = id) }; _state.update { it.copy(cameraMissing = false) } }
    fun manageCameras() { send(UiEvent.Navigate(Screen.Cameras(true))) }
    fun restoreDraft() {
        savedDraft?.let { saved ->
            val camera = saved.cameraId?.takeIf { id -> _state.value.cameras.any { it.id == id } }
            baseline = saved.copy(cameraId = camera)
            _state.update { it.copy(draft = baseline, dirty = false, draftAvailable = false, cameraMissing = saved.cameraId != null && camera == null) }
        }
    }
    fun startFresh() {
        _state.update { it.copy(saving = true) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            repository.saveNewRollDraft(null)
            savedDraft = null
            _state.update { it.copy(saving = false, draftAvailable = false) }
        }
    }
    fun create() {
        val current = _state.value
        if (current.saving || current.loading) return
        if (!NotebookValidation.validDraft(current.draft)) { _state.update { it.copy(fieldError = true) }; return }
        _state.update { it.copy(saving = true, saveError = false) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            val roll = repository.createRoll(current.draft)
            _state.update { it.copy(saving = false, dirty = false) }
            emit(UiEvent.Navigate(Screen.Game(roll.id), replace = true))
        }
    }
    fun back() {
        if (_state.value.saving) return
        when {
            _state.value.picker != null -> picker(null)
            _state.value.unsavedDialog -> keepEditing()
            _state.value.dirty -> _state.update { it.copy(unsavedDialog = true) }
            else -> send(UiEvent.Back)
        }
    }
    fun keepEditing() { _state.update { it.copy(unsavedDialog = false) } }
    fun discard() { _state.update { it.copy(draft = baseline, dirty = false, unsavedDialog = false) }; send(UiEvent.Back) }
    fun saveDraft() {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            repository.saveNewRollDraft(_state.value.draft)
            baseline = _state.value.draft
            _state.update { it.copy(saving = false, dirty = false, unsavedDialog = false) }
            emit(UiEvent.Back)
        }
    }
    fun retry() { perform({ _state.update { it.copy(loadError = true) } }) { repository.load() } }
}
