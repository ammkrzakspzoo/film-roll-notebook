package com.filmroll.notebook.data.repository

import com.filmroll.notebook.data.local.NotebookJsonCodec
import com.filmroll.notebook.data.local.SharedPreferencesStore
import com.filmroll.notebook.data.sample.SampleData
import com.filmroll.notebook.domain.logic.NotebookValidation
import com.filmroll.notebook.domain.model.*
import com.filmroll.notebook.domain.repository.NotebookRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NotebookRepositoryImpl(private val store: SharedPreferencesStore, private val generatedTitle: (String, Int) -> String) : NotebookRepository {
    private val codec = NotebookJsonCodec()
    private val mutex = Mutex()
    private val _state = MutableStateFlow(NotebookState())
    override val state = _state.asStateFlow()
    private var encoded: Map<String, String> = emptyMap()

    override suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            _state.update { it.copy(loading = true, failed = false) }
            try {
                val stored = store.read()
                val notebook = if (stored.isEmpty()) SampleData.notebook().also { store.commit(emptyMap(), codec.encode(it)) } else codec.decode(stored)
                encoded = codec.encode(notebook)
                _state.update { it.copy(loading = false, failed = false, notebook = notebook) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(loading = false, failed = true) }
            }
        }
    }

    private suspend fun <T> change(transform: (Notebook) -> Pair<Notebook, T>): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            check(!_state.value.loading && !_state.value.failed)
            val (next, result) = transform(_state.value.notebook)
            NotebookValidation.validate(next)
            val serialized = codec.encode(next)
            store.commit(encoded, serialized)
            encoded = serialized
            _state.update { it.copy(notebook = next) }
            result
        }
    }

    override suspend fun reset() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val empty = Notebook(menu = MenuSelection(rollId = null), nextSequence = 1)
            val serialized = codec.encode(empty)
            store.reset(serialized)
            encoded = serialized
            _state.update { it.copy(loading = false, failed = false, notebook = empty) }
        }
    }
    override suspend fun selectMenu(developed: Boolean, rollId: String?) = change { it.copy(menu = MenuSelection(developed, rollId)) to Unit }
    override suspend fun createRoll(draft: NewRollDraft): FilmRoll = change { notebook ->
        require(NotebookValidation.validDraft(draft))
        val film = if (draft.filmIndex == 4) draft.customFilm.trim() else ExposureOptions.films[draft.filmIndex]
        val suffixTitle = generatedTitle(film.take(35), notebook.nextSequence)
        val camera = notebook.cameras.find { it.id == draft.cameraId }
        val roll = FilmRoll(UUID.randomUUID().toString(), notebook.nextSequence, draft.title.trim().ifEmpty { suffixTitle }.take(60), film, draft.iso, draft.capacity, camera?.id, camera?.name, System.currentTimeMillis())
        notebook.copy(rolls = notebook.rolls + roll, nextSequence = notebook.nextSequence + 1, newRollDraft = null, menu = notebook.menu.copy(rollId = roll.id)) to roll
    }
    override suspend fun deleteRoll(id: String) = change { notebook -> notebook.copy(rolls = notebook.rolls.filterNot { it.id == id }, frameDrafts = notebook.frameDrafts - id, menu = notebook.menu.copy(rollId = notebook.menu.rollId.takeUnless { it == id })) to Unit }
    override suspend fun saveFrame(draft: FrameDraft, expectedCount: Int): ExposureFrame = change { notebook ->
        require(NotebookValidation.validExposure(draft))
        val roll = requireNotNull(notebook.rolls.find { it.id == draft.rollId })
        check(roll.status == RollStatus.ACTIVE && roll.frames.size == expectedCount)
        val now = System.currentTimeMillis()
        val previous = draft.editingNumber?.let { number -> requireNotNull(roll.frames.find { it.number == number }) }
        check(previous != null || roll.frames.size < roll.capacity)
        val frame = ExposureFrame(previous?.id ?: UUID.randomUUID().toString(), previous?.number ?: roll.frames.size + 1, draft.aperture, draft.shutter, draft.note, previous?.createdAt ?: now, now)
        val frames = if (previous == null) roll.frames + frame else roll.frames.map { if (it.id == previous.id) frame else it }
        notebook.copy(rolls = notebook.rolls.map { if (it.id == roll.id) it.copy(frames = frames) else it }, frameDrafts = notebook.frameDrafts - roll.id) to frame
    }
    override suspend fun saveFrameDraft(draft: FrameDraft?) = change { notebook -> notebook.copy(frameDrafts = if (draft == null) notebook.frameDrafts else notebook.frameDrafts + (draft.rollId to draft)) to Unit }
    override suspend fun removeFrameDraft(rollId: String) = change { it.copy(frameDrafts = it.frameDrafts - rollId) to Unit }
    override suspend fun saveNewRollDraft(draft: NewRollDraft?) = change { it.copy(newRollDraft = draft) to Unit }
    override suspend fun markDeveloped(id: String) = change { notebook ->
        val roll = requireNotNull(notebook.rolls.find { it.id == id })
        check(roll.status == RollStatus.ACTIVE && roll.frames.isNotEmpty())
        notebook.copy(rolls = notebook.rolls.map { if (it.id == id) it.copy(status = RollStatus.DEVELOPED, developedAt = System.currentTimeMillis()) else it }, frameDrafts = notebook.frameDrafts - id) to Unit
    }
    override suspend fun saveCamera(id: String?, name: String, notes: String): CameraBody = change { notebook ->
        val clean = name.trim()
        require(clean.isNotEmpty() && clean.length <= 60 && notes.length <= 160)
        require(notebook.cameras.none { it.id != id && it.name.equals(clean, true) })
        val old = notebook.cameras.find { it.id == id }
        val camera = CameraBody(old?.id ?: UUID.randomUUID().toString(), clean, notes, old?.createdAt ?: System.currentTimeMillis(), old?.isSample ?: false)
        notebook.copy(cameras = notebook.cameras.filterNot { it.id == camera.id } + camera) to camera
    }
    override suspend fun deleteCamera(id: String) = change { notebook ->
        notebook.copy(cameras = notebook.cameras.filterNot { it.id == id }, defaults = notebook.defaults.copy(cameraId = notebook.defaults.cameraId.takeUnless { it == id }), newRollDraft = notebook.newRollDraft?.let { it.copy(cameraId = it.cameraId.takeUnless { it == id }) }) to Unit
    }
    override suspend fun saveDefaults(defaults: NotebookDefaults) = change { notebook ->
        require(NotebookValidation.validDefaults(defaults))
        notebook.copy(defaults = defaults.copy(cameraId = defaults.cameraId?.takeIf { id -> notebook.cameras.any { it.id == id } })) to Unit
    }
}
