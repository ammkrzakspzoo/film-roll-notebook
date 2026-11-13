package com.filmroll.notebook.presentation.export

import androidx.lifecycle.viewModelScope
import com.filmroll.notebook.core.navigation.*
import com.filmroll.notebook.domain.logic.TextExportFormatter
import com.filmroll.notebook.domain.repository.NotebookRepository
import com.filmroll.notebook.domain.repository.TextDocumentWriter
import com.filmroll.notebook.presentation.app.ActionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExportViewModel(private val repository: NotebookRepository, private val writer: TextDocumentWriter, private val formatter: TextExportFormatter, private val route: Screen.Export) : ActionModel() {
    private val _state = MutableStateFlow(ExportUiState())
    val state = _state.asStateFlow()
    private var prepared = false
    init {
        viewModelScope.launch {
            repository.state.collect { stored ->
                if (!stored.loading && !prepared) prepare()
            }
        }
    }
    private fun prepare() {
        val stored = repository.state.value
        if (stored.failed) { _state.update { it.copy(preparing = false, loadError = true) }; return }
        val roll = stored.notebook.rolls.find { it.id == route.rollId }
        if (roll == null) { _state.update { it.copy(preparing = false, missing = true) }; return }
        val exported = formatter.format(roll)
        _state.update { it.copy(preparing = false, loadError = false, missing = false, title = roll.title, text = exported.text, filename = exported.filename, frameCount = roll.frames.size, status = ExportStatus.NONE) }
        prepared = true
    }
    fun refresh() { if (!_state.value.writing) { _state.update { it.copy(preparing = true) }; prepare() } }
    fun saveText() {
        if (_state.value.writing || _state.value.pickerOpen || _state.value.frameCount == 0) return
        _state.update { it.copy(pickerOpen = true, status = ExportStatus.NONE) }
        send(UiEvent.CreateDocument(_state.value.filename))
    }
    fun documentResult(uri: String?) {
        _state.update { it.copy(pickerOpen = false) }
        if (uri == null) return
        val text = _state.value.text
        _state.update { it.copy(writing = true, status = ExportStatus.NONE) }
        perform({ _state.update { it.copy(writing = false, status = ExportStatus.WRITE_ERROR) } }) {
            writer.write(uri, text)
            _state.update { it.copy(writing = false, status = ExportStatus.SAVED) }
        }
    }
    fun pickerFailed() { _state.update { it.copy(pickerOpen = false, status = ExportStatus.WRITE_ERROR) } }
    fun copy() { if (_state.value.frameCount > 0) send(UiEvent.CopyText(_state.value.text)) }
    fun copied(success: Boolean) { _state.update { it.copy(status = if (success) ExportStatus.COPIED else ExportStatus.COPY_ERROR) } }
    fun back() { if (!_state.value.writing) send(UiEvent.Back) }
    fun menu() { send(UiEvent.Menu) }
    fun retry() { perform({ _state.update { it.copy(loadError = true) } }) { repository.load(); prepare() } }
}
