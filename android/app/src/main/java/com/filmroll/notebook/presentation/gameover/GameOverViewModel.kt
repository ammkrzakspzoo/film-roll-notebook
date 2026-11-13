package com.filmroll.notebook.presentation.gameover

import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.domain.logic.summarize
import com.filmroll.notebook.domain.model.RollStatus
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.presentation.app.ActionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameOverViewModel(private val repository: NotebookRepository, private val route: Screen.GameOver) : ActionModel() {
    private val _state = MutableStateFlow(GameOverUiState())
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            repository.state.collect { stored ->
                val roll = stored.notebook.rolls.find { it.id == route.rollId }
                val canContinue = roll != null && roll.status == RollStatus.ACTIVE && roll.frames.size < roll.capacity
                val canDevelop = roll != null && roll.status == RollStatus.ACTIVE && roll.frames.isNotEmpty()
                _state.update { it.copy(loading = stored.loading, loadError = stored.failed, roll = roll, statistics = roll?.let(::summarize) ?: it.statistics, canContinue = canContinue, canDevelop = canDevelop) }
            }
        }
    }
    fun back() { if (!_state.value.updating) { if (_state.value.confirmDeveloped) confirm(false) else send(UiEvent.Back) } }
    fun menu() { send(UiEvent.Menu) }
    fun replay() { send(UiEvent.Navigate(Screen.NewRoll(route.rollId))) }
    fun export() { send(UiEvent.Navigate(Screen.Export(route.rollId))) }
    fun continueLogging() { send(UiEvent.Navigate(Screen.Game(route.rollId))) }
    fun edit(number: Int) { send(UiEvent.Navigate(Screen.Game(route.rollId, number))) }
    fun confirm(value: Boolean) { _state.update { it.copy(confirmDeveloped = value) } }
    fun markDeveloped() {
        val current = _state.value
        val roll = current.roll ?: return
        if (current.updating || roll.status != RollStatus.ACTIVE || roll.frames.isEmpty()) return
        _state.update { it.copy(updating = true, saveError = false) }
        perform({ _state.update { it.copy(updating = false, saveError = true) } }) {
            repository.markDeveloped(route.rollId)
            _state.update { it.copy(updating = false, confirmDeveloped = false, developedFeedback = true) }
            if (repository.state.value.notebook.defaults.haptics) emit(UiEvent.Haptic)
        }
    }
    fun retry() { perform({ _state.update { it.copy(loadError = true) } }) { repository.load() } }
}
