package com.filmroll.notebook.domain.model

enum class RollStatus { ACTIVE, DEVELOPED }

object ExposureOptions {
    val apertures = listOf("1.4", "1.8", "2", "2.8", "4", "5.6", "8", "11", "16", "22")
    val shutters = listOf("1", "1/2", "1/4", "1/8", "1/15", "1/30", "1/60", "1/125", "1/250", "1/500", "1/1000", "1/2000", "1/4000", "BULB")
    val isoValues = listOf(25, 50, 64, 100, 125, 160, 200, 250, 320, 400, 640, 800, 1000, 1600, 3200, 6400)
    val films = listOf("Kodak Gold 200", "Kodak Portra 400", "Ilford HP5 Plus 400", "Fujifilm Fujicolor 200")
    val nominalIso = listOf(200, 400, 400, 200)
}

data class ExposureFrame(val id: String, val number: Int, val aperture: String, val shutter: String, val note: String, val createdAt: Long, val updatedAt: Long)
data class FilmRoll(val id: String, val sequence: Int, val title: String, val film: String, val iso: Int, val capacity: Int, val cameraId: String?, val cameraName: String?, val createdAt: Long, val developedAt: Long? = null, val status: RollStatus = RollStatus.ACTIVE, val frames: List<ExposureFrame> = emptyList(), val isSample: Boolean = false)
data class CameraBody(val id: String, val name: String, val notes: String, val createdAt: Long, val isSample: Boolean = false)
data class NotebookDefaults(val film: String = ExposureOptions.films.first(), val iso: Int = 200, val cameraId: String? = null, val capacity: Int = 36, val aperture: String = "5.6", val shutter: String = "1/125", val haptics: Boolean = true)
data class NewRollDraft(val title: String = "", val filmIndex: Int = 0, val customFilm: String = "", val iso: Int = 200, val cameraId: String? = null, val capacity: Int = 36, val isoChanged: Boolean = false)
data class FrameDraft(val rollId: String, val editingNumber: Int? = null, val aperture: String = "5.6", val shutter: String = "1/125", val note: String = "")
data class MenuSelection(val developed: Boolean = false, val rollId: String? = "city-light")
data class Notebook(val rolls: List<FilmRoll> = emptyList(), val cameras: List<CameraBody> = emptyList(), val defaults: NotebookDefaults = NotebookDefaults(), val menu: MenuSelection = MenuSelection(), val newRollDraft: NewRollDraft? = null, val frameDrafts: Map<String, FrameDraft> = emptyMap(), val nextSequence: Int = 4)
data class NotebookState(val loading: Boolean = true, val failed: Boolean = false, val notebook: Notebook = Notebook())
