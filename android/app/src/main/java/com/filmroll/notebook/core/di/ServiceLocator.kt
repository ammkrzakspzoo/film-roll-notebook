package com.filmroll.notebook.core.di

import android.content.Context
import com.filmroll.notebook.R
import com.filmroll.notebook.core.navigation.Screen
import com.filmroll.notebook.data.local.LocalTextDocumentWriter
import com.filmroll.notebook.data.local.SharedPreferencesStore
import com.filmroll.notebook.data.repository.NotebookRepositoryImpl
import com.filmroll.notebook.domain.logic.ExportLabels
import com.filmroll.notebook.domain.logic.TextExportFormatter
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.domain.repository.TextDocumentWriter
import com.filmroll.notebook.presentation.app.AppViewModel
import com.filmroll.notebook.presentation.splash.SplashViewModel
import com.filmroll.notebook.presentation.menu.MenuViewModel
import com.filmroll.notebook.presentation.game.GameViewModel
import com.filmroll.notebook.presentation.gameover.GameOverViewModel
import com.filmroll.notebook.presentation.newroll.NewRollViewModel
import com.filmroll.notebook.presentation.cameras.CamerasViewModel
import com.filmroll.notebook.presentation.settings.SettingsViewModel
import com.filmroll.notebook.presentation.export.ExportViewModel

object ServiceLocator {
    private data class Dependencies(val notebook: NotebookRepository, val writer: TextDocumentWriter, val formatter: TextExportFormatter)
    private var dependencies: Dependencies? = null
    fun initialize(context: Context) {
        if (dependencies != null) return
        val applicationContext = context.applicationContext
        fun text(id: Int) = applicationContext.getString(id)
        val labels = ExportLabels(text(R.string.app_name), text(R.string.export_sample_label), text(R.string.export_yes), text(R.string.export_no), text(R.string.film_stock), text(R.string.rated_iso), text(R.string.camera), text(R.string.no_camera), text(R.string.export_created), text(R.string.export_status), text(R.string.export_active), text(R.string.export_developed), text(R.string.export_recorded), text(R.string.export_of), text(R.string.export_frames), text(R.string.export_frame), text(R.string.export_note), text(R.string.no_note), text(R.string.export_aperture_prefix), text(R.string.export_seconds), text(R.string.bulb))
        dependencies = Dependencies(NotebookRepositoryImpl(SharedPreferencesStore(applicationContext)) { film, sequence -> applicationContext.getString(R.string.generated_title, film, sequence) }, LocalTextDocumentWriter(applicationContext), TextExportFormatter(labels))
    }
    fun factory(screen: Screen? = null): AppViewModelFactory {
        val services = checkNotNull(dependencies)
        return AppViewModelFactory {
            when (screen) {
                null -> AppViewModel(services.notebook)
                Screen.Splash -> SplashViewModel()
                Screen.Menu -> MenuViewModel(services.notebook)
                is Screen.Game -> GameViewModel(services.notebook, screen)
                is Screen.GameOver -> GameOverViewModel(services.notebook, screen)
                is Screen.NewRoll -> NewRollViewModel(services.notebook, screen)
                is Screen.Export -> ExportViewModel(services.notebook, services.writer, services.formatter, screen)
                is Screen.Cameras -> CamerasViewModel(services.notebook, screen)
                Screen.Settings -> SettingsViewModel(services.notebook)
            }
        }
    }
}
