package com.filmroll.notebook.domain.logic

import com.filmroll.notebook.domain.model.FilmRoll

data class RollStatistics(val minimumAperture: String?, val maximumAperture: String?, val mostUsedShutter: String?, val progress: Float)

fun summarize(roll: FilmRoll): RollStatistics {
    val values = roll.frames.map { it.aperture }.distinct().sortedBy { it.toDouble() }
    val counts = roll.frames.groupingBy { it.shutter }.eachCount()
    val most = roll.frames.map { it.shutter }.distinct().maxByOrNull { counts[it] ?: 0 }
    return RollStatistics(values.firstOrNull(), values.lastOrNull(), most, roll.frames.size.toFloat() / roll.capacity)
}
