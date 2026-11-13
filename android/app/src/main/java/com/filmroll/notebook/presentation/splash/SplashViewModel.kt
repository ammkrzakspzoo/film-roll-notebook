package com.filmroll.notebook.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _state = MutableStateFlow(SplashUiState())
    val state = _state.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    init {
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _state.update { it.copy(finished = true) }
            _finished.value = true
        }
    }
    fun consumeTransition(): Boolean {
        if (!_state.value.finished || _state.value.transitionConsumed) return false
        _state.update { it.copy(transitionConsumed = true) }
        return true
    }
}
