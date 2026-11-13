package com.filmroll.notebook.presentation.cameras

import com.filmroll.notebook.domain.model.CameraBody

data class CamerasUiState(val loading: Boolean = true, val loadError: Boolean = false, val cameras: List<CameraBody> = emptyList(), val editorOpen: Boolean = false, val editingId: String? = null, val name: String = "", val notes: String = "", val dirty: Boolean = false, val fieldError: Boolean = false, val saveError: Boolean = false, val saving: Boolean = false, val pendingDelete: CameraBody? = null, val discardDialog: Boolean = false)
