package com.filmroll.notebook.presentation.app

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filmroll.notebook.core.di.ServiceLocator
import com.filmroll.notebook.core.navigation.Screen
import com.filmroll.notebook.presentation.splash.SplashRoute
import com.filmroll.notebook.presentation.menu.MenuRoute
import com.filmroll.notebook.presentation.game.GameRoute
import com.filmroll.notebook.presentation.gameover.GameOverRoute
import com.filmroll.notebook.presentation.newroll.NewRollRoute
import com.filmroll.notebook.presentation.cameras.CamerasRoute
import com.filmroll.notebook.presentation.settings.SettingsRoute
import com.filmroll.notebook.presentation.export.ExportRoute

@Composable
fun AppRoot() {
    val model: AppViewModel = viewModel(factory = ServiceLocator.factory())
    val screen by model.screen.collectAsStateWithLifecycle()
    val state by model.state.collectAsStateWithLifecycle()
    val view = LocalView.current
    val dark = screen is Screen.Game || screen is Screen.GameOver || screen is Screen.Cameras || screen == Screen.Splash
    SideEffect {
        (view.context as? Activity)?.let { activity ->
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    BackHandler(enabled = screen != Screen.Menu && screen != Screen.Splash) { model.back() }
    AnimatedContent(targetState = screen, modifier = Modifier.fillMaxSize(), transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(220)) }, label = "notebookNavigation") { route ->
        when (route) {
            Screen.Splash -> SplashRoute(model::finishSplash)
            Screen.Menu -> MenuRoute { model.handle(route, it) }
            is Screen.Game -> GameRoute(route) { model.handle(route, it) }
            is Screen.GameOver -> GameOverRoute(route) { model.handle(route, it) }
            is Screen.NewRoll -> NewRollRoute(route, state.returnedCameraId, model::consumeCamera) { model.handle(route, it) }
            is Screen.Export -> ExportRoute(route) { model.handle(route, it) }
            is Screen.Cameras -> CamerasRoute(route) { model.handle(route, it) }
            Screen.Settings -> SettingsRoute { model.handle(route, it) }
        }
    }
}
