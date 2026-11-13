package com.filmroll.notebook.presentation.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filmroll.notebook.R
import com.filmroll.notebook.core.di.ServiceLocator
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.core.theme.*
import com.filmroll.notebook.core.ui.*

@Composable
fun ExportRoute(route: Screen.Export, onEvent: (UiEvent) -> Unit) {
    val model: ExportViewModel = viewModel(key = route.toString(), factory = ServiceLocator.factory(route))
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri -> model.documentResult(uri?.toString()) }
    UiEffects(model.events) { event ->
        when (event) {
            is UiEvent.CreateDocument -> try { launcher.launch(event.filename) } catch (_: Exception) { model.pickerFailed() }
            is UiEvent.CopyText -> {
                val success = try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(appName, event.text))
                    true
                } catch (_: Exception) { false }
                model.copied(success)
            }
            else -> onEvent(event)
        }
    }
    BackHandler { model.back() }
    ExportScreen(state, model::back, model::menu, model::saveText, model::copy, model::refresh, model::retry)
}

@Composable
fun ExportScreen(state: ExportUiState, onBack: () -> Unit, onMenu: () -> Unit, onSave: () -> Unit, onCopy: () -> Unit, onRefresh: () -> Unit, onRetry: () -> Unit) {
    NotebookPage {
        NotebookTopBar(R.string.export_title, onBack)
        when {
            state.preparing -> LoadingMessage()
            state.loadError -> { StatusMessage(stringResource(R.string.storage_error), true); SecondaryButton(R.string.retry, onRetry) }
            state.missing -> { StatusMessage(stringResource(R.string.missing_roll)); SecondaryButton(R.string.menu, onMenu) }
            else -> {
                Text(state.title, style = MaterialTheme.typography.titleLarge)
                if (state.frameCount == 0) StatusMessage(stringResource(R.string.export_empty))
                Text(stringResource(R.string.export_helper), style = MaterialTheme.typography.bodyMedium)
                val enabled = state.frameCount > 0 && !state.writing && !state.pickerOpen
                PrimaryButton(if (state.status == ExportStatus.WRITE_ERROR) R.string.retry_save else R.string.save_text, onSave, enabled = enabled)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(R.string.copy_text, onCopy, Modifier.weight(1f), enabled)
                    SecondaryButton(R.string.refresh, onRefresh, Modifier.weight(1f), !state.writing && !state.pickerOpen)
                }
                if (state.writing) LoadingMessage()
                val message = when (state.status) {
                    ExportStatus.COPIED -> R.string.text_copied
                    ExportStatus.SAVED -> R.string.text_saved
                    ExportStatus.WRITE_ERROR -> R.string.export_error
                    ExportStatus.COPY_ERROR -> R.string.copy_error
                    ExportStatus.NONE -> null
                }
                message?.let { StatusMessage(stringResource(it), error = state.status == ExportStatus.WRITE_ERROR || state.status == ExportStatus.COPY_ERROR, success = state.status == ExportStatus.COPIED || state.status == ExportStatus.SAVED) }
                NotebookSurface(Modifier.fillMaxWidth()) {
                    SelectionContainer { Text(state.text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium, color = FilmBlack) }
                }
            }
        }
    }
}
