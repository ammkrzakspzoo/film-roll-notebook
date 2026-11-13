package com.filmroll.notebook.domain.logic

import com.filmroll.notebook.domain.model.*

object NotebookValidation {
    fun validDefaults(defaults: NotebookDefaults): Boolean = defaults.film.isNotBlank() && defaults.film.length <= 80 && defaults.iso in ExposureOptions.isoValues && defaults.capacity in listOf(24, 36) && defaults.aperture in ExposureOptions.apertures && defaults.shutter in ExposureOptions.shutters
    fun validDraft(draft: NewRollDraft): Boolean = draft.title.length <= 60 && draft.filmIndex in 0..4 && (draft.filmIndex != 4 || draft.customFilm.trim().isNotEmpty() && draft.customFilm.length <= 80) && draft.iso in ExposureOptions.isoValues && draft.capacity in listOf(24, 36)
    fun validDraftShape(draft: NewRollDraft): Boolean = draft.title.length <= 60 && draft.filmIndex in 0..4 && draft.customFilm.length <= 80 && draft.iso in ExposureOptions.isoValues && draft.capacity in listOf(24, 36)
    fun validExposure(draft: FrameDraft): Boolean = draft.aperture in ExposureOptions.apertures && draft.shutter in ExposureOptions.shutters && draft.note.length <= 500
    fun validate(notebook: Notebook) {
        require(validDefaults(notebook.defaults))
        require(notebook.nextSequence > (notebook.rolls.maxOfOrNull { it.sequence } ?: 0))
        require(notebook.rolls.map { it.id }.distinct().size == notebook.rolls.size)
        require(notebook.cameras.map { it.id }.distinct().size == notebook.cameras.size)
        require(notebook.cameras.map { it.name.trim().lowercase() }.distinct().size == notebook.cameras.size)
        notebook.cameras.forEach { require(it.name.isNotBlank() && it.name.length <= 60 && it.notes.length <= 160) }
        notebook.rolls.forEach { roll ->
            require(roll.title.isNotBlank() && roll.title.length <= 60 && roll.film.isNotBlank() && roll.film.length <= 80)
            require(roll.iso in ExposureOptions.isoValues && roll.capacity in listOf(24, 36) && roll.frames.size <= roll.capacity)
            require((roll.status == RollStatus.DEVELOPED) == (roll.developedAt != null))
            require(roll.frames.map { it.id }.distinct().size == roll.frames.size)
            roll.frames.forEachIndexed { index, frame ->
                require(frame.number == index + 1 && frame.aperture in ExposureOptions.apertures && frame.shutter in ExposureOptions.shutters && frame.note.length <= 500)
            }
        }
        notebook.newRollDraft?.let { require(validDraftShape(it)) }
        notebook.frameDrafts.forEach { (key, draft) ->
            require(key == draft.rollId && validExposure(draft))
            val roll = requireNotNull(notebook.rolls.find { it.id == key })
            require(roll.status == RollStatus.ACTIVE)
            draft.editingNumber?.let { number -> require(roll.frames.any { it.number == number }) }
        }
    }
}
