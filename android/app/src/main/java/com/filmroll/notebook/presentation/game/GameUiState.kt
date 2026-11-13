package com.filmroll.notebook.presentation.game

import com.filmroll.notebook.domain.model.*

data class GameUiState(val loading: Boolean = true, val loadError: Boolean = false, val roll: FilmRoll? = null, val draft: FrameDraft = FrameDraft(""), val dirty: Boolean = false, val saving: Boolean = false, val saveError: Boolean = false, val savedFrame: Int? = null, val picker: String? = null, val unsavedDialog: Boolean = false, val editing: Boolean = false, val canSave: Boolean = false, val currentFrameNumber: Int = 1)
