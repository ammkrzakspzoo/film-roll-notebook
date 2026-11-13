package com.filmroll.notebook.data.sample

import com.filmroll.notebook.domain.model.*
import java.time.Instant

object SampleData {
    fun notebook(): Notebook {
        val cityDate = Instant.parse("2026-11-08T12:00:00Z").toEpochMilli()
        val riverDate = Instant.parse("2026-11-07T12:00:00Z").toEpochMilli()
        val portraitDate = Instant.parse("2026-11-10T12:00:00Z").toEpochMilli()
        val cameras = listOf(
            CameraBody("nikon", "Nikon FM2", "Manual 35 mm body, 50 mm lens", cityDate, true),
            CameraBody("pentax", "Pentax K1000", "Mechanical 35 mm body, 50 mm lens", riverDate, true),
            CameraBody("olympus", "Olympus OM-1", "Compact 35 mm body, 35 mm lens", portraitDate, true)
        )
        fun frames(prefix: String, date: Long, notes: List<String>): List<ExposureFrame> {
            val apertures = listOf("8", "5.6", "4", "11", "2.8")
            val shutters = listOf("1/250", "1/125", "1/60", "1/500", "1/125")
            return notes.mapIndexed { index, note ->
                ExposureFrame("$prefix-${index + 1}", index + 1, apertures[index], shutters[index], note, date + (index + 1) * 60000L, date + (index + 1) * 60000L)
            }
        }
        return Notebook(rolls = listOf(
            FilmRoll("city-light", 1, "City light studies", "Kodak Gold 200", 200, 36, "nikon", "Nikon FM2", cityDate, frames = frames("city", cityDate, listOf("Morning tram stop, light from the left.", "Cafe window reflection, expose for the faces.", "Bookshop doorway in open shade.", "Bright crossing, deep shadows on the pavement.", "Last light on a bicycle by the wall.")), isSample = true),
            FilmRoll("riverside", 2, "Riverside monochrome", "Ilford HP5 Plus 400", 800, 24, "pentax", "Pentax K1000", riverDate, Instant.parse("2026-11-09T12:00:00Z").toEpochMilli(), RollStatus.DEVELOPED, frames("river", riverDate, listOf("River path after rain, bright sky.", "Bridge supports reflected in the water.", "Portrait under the stone arch.", "Sun breaking through over the footbridge.")), true),
            FilmRoll("portraits", 3, "Weekend portraits", "Kodak Portra 400", 400, 24, "olympus", "Olympus OM-1", portraitDate, isSample = true)
        ), cameras = cameras)
    }
}
