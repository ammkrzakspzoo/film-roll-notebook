package com.filmroll.notebook.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.Screen
import com.filmroll.notebook.core.navigation.UiEvent
import com.filmroll.notebook.domain.repository.NotebookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(repository: NotebookRepository) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state = _state.asStateFlow()
    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = _screen.asStateFlow()
    init { viewModelScope.launch { repository.load() } }
    private fun apply(next: AppUiState) { _state.update { next }; _screen.value = next.screen }
    fun finishSplash() { if (_screen.value == Screen.Splash) apply(AppUiState(Screen.Menu)) }
    fun navigate(target: Screen, replace: Boolean = false) {
        val current = _state.value
        if (target == current.screen) return
        val stack = if (replace || current.screen == Screen.Splash) current.backStack else current.backStack + current.screen
        apply(current.copy(screen = target, backStack = stack))
    }
    fun back() {
        val current = _state.value
        val previous = current.backStack.lastOrNull() ?: Screen.Menu
        apply(current.copy(screen = previous, backStack = current.backStack.dropLast(1)))
    }
    fun consumeCamera() { _state.update { it.copy(returnedCameraId = null) } }
    fun handle(origin: Screen, event: UiEvent) {
        if (origin != _screen.value) return
        when (event) {
            is UiEvent.Navigate -> navigate(event.screen, event.replace)
            UiEvent.Back -> back()
            UiEvent.Menu -> apply(_state.value.copy(screen = Screen.Menu, backStack = emptyList()))
            is UiEvent.CameraCreated -> {
                back()
                _state.update { it.copy(returnedCameraId = event.id) }
            }
            else -> Unit
        }
    }
}
