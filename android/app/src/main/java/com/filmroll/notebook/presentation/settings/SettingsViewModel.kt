package com.filmroll.notebook.presentation.settings

import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.UiEvent
import com.filmroll.notebook.domain.logic.NotebookValidation
import com.filmroll.notebook.domain.model.*
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.presentation.app.ActionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: NotebookRepository) : ActionModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()
    private var initialized = false
    private var baseline = NotebookDefaults()
    init {
        viewModelScope.launch {
            repository.state.collect { stored ->
                if (!stored.loading && !stored.failed && !initialized) {
                    baseline = stored.notebook.defaults
                    applyDefaults(baseline)
                    initialized = true
                }
                val removed = _state.value.draft.cameraId?.takeIf { id -> stored.notebook.cameras.none { it.id == id } }
                _state.update { it.copy(loading = stored.loading, loadError = stored.failed, cameras = stored.notebook.cameras, draft = if (removed != null) it.draft.copy(cameraId = null) else it.draft) }
            }
        }
    }
    private fun applyDefaults(defaults: NotebookDefaults) {
        val index = ExposureOptions.films.indexOf(defaults.film).takeIf { it >= 0 } ?: 4
        _state.update { it.copy(draft = NewRollDraft(filmIndex = index, customFilm = if (index == 4) defaults.film else "", iso = defaults.iso, cameraId = defaults.cameraId, capacity = defaults.capacity), aperture = defaults.aperture, shutter = defaults.shutter, haptics = defaults.haptics, dirty = false) }
    }
    private fun edit(transform: (SettingsUiState) -> SettingsUiState) {
        if (_state.value.saving) return
        _state.update { transform(it).copy(dirty = true, saved = false, saveError = false, fieldError = false) }
    }
    fun customFilm(value: String) { edit { it.copy(draft = it.draft.copy(customFilm = value.take(80))) } }
    fun capacity(value: Int) { if (value in listOf(24, 36)) edit { it.copy(draft = it.draft.copy(capacity = value)) } }
    fun haptics(value: Boolean) { edit { it.copy(haptics = value) } }
    fun picker(value: String?) { _state.update { it.copy(picker = value) } }
    fun choose(value: String) {
        when (_state.value.picker) {
            "film" -> value.toIntOrNull()?.takeIf { it in 0..4 }?.let { index -> edit { it.copy(draft = it.draft.copy(filmIndex = index, iso = if (!it.draft.isoChanged && index < 4) ExposureOptions.nominalIso[index] else it.draft.iso)) } }
            "iso" -> value.toIntOrNull()?.takeIf { it in ExposureOptions.isoValues }?.let { iso -> edit { it.copy(draft = it.draft.copy(iso = iso, isoChanged = true)) } }
            "camera" -> edit { it.copy(draft = it.draft.copy(cameraId = value.takeIf { it.isNotEmpty() })) }
            "aperture" -> if (value in ExposureOptions.apertures) edit { it.copy(aperture = value) }
            "shutter" -> if (value in ExposureOptions.shutters) edit { it.copy(shutter = value) }
        }
        picker(null)
    }
    fun save(andBack: Boolean = false) {
        val current = _state.value
        if (current.saving || current.loading) return
        if (!NotebookValidation.validDraft(current.draft)) { _state.update { it.copy(fieldError = true) }; return }
        val film = if (current.draft.filmIndex == 4) current.draft.customFilm.trim() else ExposureOptions.films[current.draft.filmIndex]
        val defaults = NotebookDefaults(film, current.draft.iso, current.draft.cameraId, current.draft.capacity, current.aperture, current.shutter, current.haptics)
        commit(defaults, andBack)
    }
    private fun commit(defaults: NotebookDefaults, andBack: Boolean) {
        _state.update { it.copy(saving = true, saveError = false) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            repository.saveDefaults(defaults)
            baseline = repository.state.value.notebook.defaults
            applyDefaults(baseline)
            _state.update { it.copy(saving = false, saved = true, unsavedDialog = false, restoreDialog = false) }
            if (andBack) emit(UiEvent.Back)
        }
    }
    fun restoreDialog(value: Boolean) { _state.update { it.copy(restoreDialog = value) } }
    fun restore() { if (!_state.value.saving) commit(NotebookDefaults(), false) }
    fun back() {
        if (_state.value.saving) return
        when {
            _state.value.picker != null -> picker(null)
            _state.value.restoreDialog -> restoreDialog(false)
            _state.value.unsavedDialog -> keepEditing()
            _state.value.dirty -> _state.update { it.copy(unsavedDialog = true) }
            else -> send(UiEvent.Back)
        }
    }
    fun keepEditing() { _state.update { it.copy(unsavedDialog = false) } }
    fun discard() { applyDefaults(baseline); keepEditing(); send(UiEvent.Back) }
    fun retry() { perform({ _state.update { it.copy(loadError = true) } }) { repository.load() } }
}
