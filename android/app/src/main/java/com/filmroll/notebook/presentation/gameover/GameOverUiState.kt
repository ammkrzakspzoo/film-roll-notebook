package com.filmroll.notebook.presentation.gameover

import com.filmroll.notebook.domain.logic.RollStatistics
import com.filmroll.notebook.domain.model.FilmRoll

data class GameOverUiState(val loading: Boolean = true, val loadError: Boolean = false, val roll: FilmRoll? = null, val statistics: RollStatistics = RollStatistics(null, null, null, 0f), val updating: Boolean = false, val saveError: Boolean = false, val confirmDeveloped: Boolean = false, val developedFeedback: Boolean = false, val canContinue: Boolean = false, val canDevelop: Boolean = false)
