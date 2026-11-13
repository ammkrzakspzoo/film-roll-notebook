package com.filmroll.notebook.presentation.settings

import com.filmroll.notebook.domain.model.*

data class SettingsUiState(val loading: Boolean = true, val loadError: Boolean = false, val draft: NewRollDraft = NewRollDraft(), val cameras: List<CameraBody> = emptyList(), val aperture: String = "5.6", val shutter: String = "1/125", val haptics: Boolean = true, val picker: String? = null, val dirty: Boolean = false, val fieldError: Boolean = false, val saving: Boolean = false, val saveError: Boolean = false, val saved: Boolean = false, val unsavedDialog: Boolean = false, val restoreDialog: Boolean = false)
