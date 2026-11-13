package com.filmroll.notebook.presentation.menu

import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.domain.model.*
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.presentation.app.ActionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: NotebookRepository) : ActionModel() {
    private val _state = MutableStateFlow(MenuUiState())
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            repository.state.collect { stored ->
                val notebook = stored.notebook
                val selected = notebook.rolls.find { it.id == notebook.menu.rollId && it.status == RollStatus.ACTIVE }
                    ?: notebook.rolls.filter { it.status == RollStatus.ACTIVE }.maxByOrNull { it.createdAt }
                _state.update { it.copy(loading = stored.loading, storageError = stored.failed, rolls = notebook.rolls, developed = notebook.menu.developed, selectedRoll = selected) }
            }
        }
    }
    fun tab(developed: Boolean) {
        if (_state.value.busy) return
        _state.update { it.copy(developed = developed) }
        perform({ _state.update { it.copy(writeError = true) } }) { repository.selectMenu(developed, _state.value.selectedRoll?.id) }
    }
    fun open(roll: FilmRoll) {
        if (roll.status == RollStatus.ACTIVE) {
            perform({ _state.update { it.copy(writeError = true) } }) { repository.selectMenu(_state.value.developed, roll.id) }
        }
        send(UiEvent.Navigate(if (roll.status == RollStatus.DEVELOPED || roll.frames.size == roll.capacity) Screen.GameOver(roll.id) else Screen.Game(roll.id)))
    }
    fun play() { _state.value.selectedRoll?.let(::open) ?: send(UiEvent.Navigate(Screen.NewRoll())) }
    fun navigate(screen: Screen) { send(UiEvent.Navigate(screen)) }
    fun askDelete(roll: FilmRoll) { _state.update { it.copy(pendingDeletion = roll) } }
    fun cancelDialog() { _state.update { it.copy(pendingDeletion = null, resetConfirmation = false) } }
    fun askReset() { _state.update { it.copy(resetConfirmation = true) } }
    fun delete() {
        if (_state.value.busy) return
        val roll = _state.value.pendingDeletion ?: return
        _state.update { it.copy(busy = true) }
        perform({ _state.update { it.copy(busy = false, writeError = true) } }) {
            repository.deleteRoll(roll.id)
            _state.update { it.copy(busy = false, pendingDeletion = null, writeError = false) }
        }
    }
    fun reset() {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true) }
        perform({ _state.update { it.copy(busy = false, writeError = true) } }) {
            repository.reset()
            _state.update { it.copy(busy = false, resetConfirmation = false, writeError = false) }
        }
    }
    fun retry() { perform({ _state.update { it.copy(writeError = true) } }) { repository.load() } }
}
