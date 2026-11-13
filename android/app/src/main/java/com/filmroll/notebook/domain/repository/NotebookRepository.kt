package com.filmroll.notebook.domain.repository

import com.filmroll.notebook.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface NotebookRepository {
    val state: StateFlow<NotebookState>
    suspend fun load()
    suspend fun reset()
    suspend fun selectMenu(developed: Boolean, rollId: String?)
    suspend fun createRoll(draft: NewRollDraft): FilmRoll
    suspend fun deleteRoll(id: String)
    suspend fun saveFrame(draft: FrameDraft, expectedCount: Int): ExposureFrame
    suspend fun saveFrameDraft(draft: FrameDraft?)
    suspend fun removeFrameDraft(rollId: String)
    suspend fun saveNewRollDraft(draft: NewRollDraft?)
    suspend fun markDeveloped(id: String)
    suspend fun saveCamera(id: String?, name: String, notes: String): CameraBody
    suspend fun deleteCamera(id: String)
    suspend fun saveDefaults(defaults: NotebookDefaults)
}

interface TextDocumentWriter {
    suspend fun write(uri: String, text: String)
}
