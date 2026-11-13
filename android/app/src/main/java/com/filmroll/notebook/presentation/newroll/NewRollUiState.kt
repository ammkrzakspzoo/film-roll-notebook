package com.filmroll.notebook.presentation.newroll

import com.filmroll.notebook.domain.model.*

data class NewRollUiState(val loading: Boolean = true, val loadError: Boolean = false, val draft: NewRollDraft = NewRollDraft(), val cameras: List<CameraBody> = emptyList(), val picker: String? = null, val fieldError: Boolean = false, val saveError: Boolean = false, val saving: Boolean = false, val dirty: Boolean = false, val unsavedDialog: Boolean = false, val draftAvailable: Boolean = false, val cameraMissing: Boolean = false)
