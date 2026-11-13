package com.filmroll.notebook.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.UiEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class ActionModel : ViewModel() {
    private val channel = Channel<UiEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    protected fun send(event: UiEvent) { viewModelScope.launch { channel.send(event) } }
    protected suspend fun emit(event: UiEvent) { channel.send(event) }
    protected fun perform(onError: () -> Unit, action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action() } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { onError() }
        }
    }
}
