package com.filmroll.notebook.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val rollId: String, val editingFrameNumber: Int? = null) : Screen
    data class GameOver(val rollId: String) : Screen
    data class NewRoll(val sourceRollId: String? = null, val instance: Long = System.nanoTime()) : Screen
    data class Export(val rollId: String) : Screen
    data class Cameras(val returnToNewRoll: Boolean = false) : Screen
    data object Settings : Screen
}

sealed interface UiEvent {
    data class Navigate(val screen: Screen, val replace: Boolean = false) : UiEvent
    data object Back : UiEvent
    data object Menu : UiEvent
    data object Haptic : UiEvent
    data class CameraCreated(val id: String) : UiEvent
    data class CreateDocument(val filename: String) : UiEvent
    data class CopyText(val text: String) : UiEvent
}
