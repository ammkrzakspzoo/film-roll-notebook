package com.filmroll.notebook.presentation.game

import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.domain.logic.NotebookValidation
import com.filmroll.notebook.domain.model.*
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.presentation.app.ActionModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(private val repository: NotebookRepository, private val route: Screen.Game) : ActionModel() {
    private val _state = MutableStateFlow(GameUiState(editing = route.editingFrameNumber != null))
    val state = _state.asStateFlow()
    private var initialized = false
    private var baseline = FrameDraft(route.rollId)
    private var active = true
    private var confirmation: Job? = null
    private var pendingSummary = false
    init {
        viewModelScope.launch {
            repository.state.collect { stored ->
                val roll = stored.notebook.rolls.find { it.id == route.rollId }
                if (!stored.loading && !stored.failed && roll != null && !initialized) {
                    val edit = route.editingFrameNumber?.let { number -> roll.frames.find { it.number == number } }
                    val last = edit ?: roll.frames.lastOrNull()
                    val saved = stored.notebook.frameDrafts[roll.id]?.takeIf { it.editingNumber == route.editingFrameNumber }
                    baseline = saved ?: FrameDraft(roll.id, route.editingFrameNumber, last?.aperture ?: stored.notebook.defaults.aperture, last?.shutter ?: stored.notebook.defaults.shutter, edit?.note ?: "")
                    _state.update { it.copy(draft = baseline) }
                    initialized = true
                }
                val currentFrameNumber = route.editingFrameNumber ?: ((roll?.frames?.size ?: 0) + 1).coerceAtMost(roll?.capacity ?: 36)
                _state.update { it.copy(loading = stored.loading, loadError = stored.failed, roll = roll, canSave = roll != null && roll.status == RollStatus.ACTIVE && (route.editingFrameNumber != null || roll.frames.size < roll.capacity), currentFrameNumber = currentFrameNumber) }
            }
        }
    }
    fun resume() { active = true }
    fun pause() {
        active = false
        if (confirmation?.isActive == true) {
            confirmation?.cancel()
            _state.update { it.copy(saving = false) }
        }
    }
    fun picker(name: String?) { if (!_state.value.saving) _state.update { it.copy(picker = name) } }
    fun aperture(value: String) { if (value in ExposureOptions.apertures) edit { it.copy(aperture = value) } }
    fun shutter(value: String) { if (value in ExposureOptions.shutters) edit { it.copy(shutter = value) } }
    fun note(value: String) { edit { it.copy(note = value.take(500)) } }
    private fun edit(transform: (FrameDraft) -> FrameDraft) {
        if (_state.value.saving || !_state.value.canSave) return
        _state.update { val draft = transform(it.draft); it.copy(draft = draft, dirty = draft != baseline, savedFrame = null, saveError = false, picker = null) }
    }
    fun step(isAperture: Boolean, delta: Int) {
        val choices = if (isAperture) ExposureOptions.apertures else ExposureOptions.shutters
        val value = if (isAperture) _state.value.draft.aperture else _state.value.draft.shutter
        val index = (choices.indexOf(value) + delta).coerceIn(0, choices.lastIndex)
        if (isAperture) aperture(choices[index]) else shutter(choices[index])
    }
    fun save() {
        val current = _state.value
        val roll = current.roll ?: return
        if (current.saving || !current.canSave || !NotebookValidation.validExposure(current.draft)) return
        _state.update { it.copy(saving = true, saveError = false) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            val frame = repository.saveFrame(current.draft, roll.frames.size)
            baseline = current.draft.copy(note = if (current.editing) current.draft.note else "")
            _state.update { it.copy(saving = active, dirty = false, savedFrame = frame.number, draft = baseline) }
            if (repository.state.value.notebook.defaults.haptics && active) emit(UiEvent.Haptic)
            if (active) {
                confirmation = viewModelScope.launch {
                    delay(180)
                    _state.update { it.copy(saving = false) }
                    if (active && (current.editing || frame.number == roll.capacity)) emit(if (current.editing) UiEvent.Back else UiEvent.Navigate(Screen.GameOver(roll.id)))
                }
            }
        }
    }
    fun summary() { leave(true) }
    fun back() { leave(false) }
    private fun leave(summary: Boolean) {
        if (_state.value.saving) return
        if (_state.value.picker != null) { picker(null); return }
        if (_state.value.unsavedDialog) { keepEditing(); return }
        pendingSummary = summary
        if (_state.value.dirty) _state.update { it.copy(unsavedDialog = true) } else finishLeave()
    }
    private fun finishLeave() { send(if (pendingSummary) UiEvent.Navigate(Screen.GameOver(route.rollId)) else UiEvent.Back) }
    fun keepEditing() { _state.update { it.copy(unsavedDialog = false) } }
    fun saveDraft() {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true) }
        perform({ _state.update { it.copy(saving = false, saveError = true) } }) {
            repository.saveFrameDraft(_state.value.draft)
            baseline = _state.value.draft
            _state.update { it.copy(saving = false, dirty = false, unsavedDialog = false) }
            finishLeave()
        }
    }
    fun discard() {
        _state.update { it.copy(draft = baseline, dirty = false, unsavedDialog = false) }
        finishLeave()
    }
    fun retry() { perform({ _state.update { it.copy(loadError = true) } }) { repository.load() } }
}
