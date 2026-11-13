package com.filmroll.notebook.presentation.app

import com.filmroll.notebook.core.navigation.Screen

data class AppUiState(val screen: Screen = Screen.Splash, val backStack: List<Screen> = emptyList(), val returnedCameraId: String? = null)
