package com.filmroll.notebook.presentation.menu

import com.filmroll.notebook.domain.model.FilmRoll

data class MenuUiState(val loading: Boolean = true, val storageError: Boolean = false, val writeError: Boolean = false, val rolls: List<FilmRoll> = emptyList(), val developed: Boolean = false, val selectedRoll: FilmRoll? = null, val pendingDeletion: FilmRoll? = null, val resetConfirmation: Boolean = false, val busy: Boolean = false)
