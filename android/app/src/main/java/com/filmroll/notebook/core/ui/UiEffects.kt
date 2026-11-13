package com.filmroll.notebook.core.ui

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import com.filmroll.notebook.core.navigation.UiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@Composable
fun UiEffects(events: Flow<UiEvent>, onEvent: (UiEvent) -> Unit) {
    val currentHandler by rememberUpdatedState(onEvent)
    val view = LocalView.current
    LaunchedEffect(Unit) {
        events.collect { event ->
            if (event == UiEvent.Haptic) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) else currentHandler(event)
        }
    }
}
