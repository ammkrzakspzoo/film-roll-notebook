package com.filmroll.notebook.presentation.export

enum class ExportStatus { NONE, COPIED, SAVED, WRITE_ERROR, COPY_ERROR }
data class ExportUiState(val preparing: Boolean = true, val loadError: Boolean = false, val missing: Boolean = false, val title: String = "", val text: String = "", val filename: String = "", val frameCount: Int = 0, val writing: Boolean = false, val pickerOpen: Boolean = false, val status: ExportStatus = ExportStatus.NONE)
